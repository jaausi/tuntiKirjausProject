package com.sirvja.tuntikirjaus.exporter.impl;

import com.sirvja.tuntikirjaus.service.AlertService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Runs the real KiekuHoursExporter against a fake Kieku work time allocation page
 * (src/test/resources/.../fake-kieku/hours.html) in Chrome.
 * <p>
 * Run with: {@code mvn test -Pbrowser-tests -Dtest=KiekuHoursExporterFakePageTest}
 * <br>
 * Add {@code -Dkieku.headless=false} to watch the browser, and {@code -Dkieku.keepOpenMillis=10000}
 * to keep the page open for a while after the export.
 */
@Tag("browser")
class KiekuHoursExporterFakePageTest {

    /** The fake page shows this week first. */
    private static final LocalDate SHOWN_MONDAY = LocalDate.of(2026, 10, 5);

    private WebDriver driver;
    private KiekuHoursExporter exporter;
    private final List<String> alerts = new ArrayList<>();

    @AfterEach
    void tearDown() throws InterruptedException {
        long keepOpenMillis = Long.getLong("kieku.keepOpenMillis", 0L);
        if (driver != null && keepOpenMillis > 0) {
            Thread.sleep(keepOpenMillis);
        }
        if (exporter != null) {
            exporter.destroyExporter();
        }
    }

    @Test
    @DisplayName("should fill the hours of each project to the right row, day and week")
    void shouldFillHoursOfEachProject() {
        export(fakeHoursConfiguration(Map.of("TUNTI", "Kehitys")), true, List.of(
                new KiekuHoursItem("TUNTI", SHOWN_MONDAY, Duration.ofMinutes(90)),
                new KiekuHoursItem("TUNTI", SHOWN_MONDAY.plusDays(4), Duration.ofMinutes(445)),
                new KiekuHoursItem("SUPPORT", SHOWN_MONDAY.plusDays(1), Duration.ofMinutes(30)),
                new KiekuHoursItem("Hallinto", SHOWN_MONDAY.minusDays(5), Duration.ofHours(2)),
                // Saturday, Kieku has no field for it
                new KiekuHoursItem("Hallinto", SHOWN_MONDAY.plusDays(5), Duration.ofHours(1))
        ));

        assertEquals(List.of(), errors());
        assertEquals(List.of(
                List.of("2026-09-28", "Hallinto", "wed", "02:00"),
                List.of("2026-10-05", "Kehitys (jatkuva)", "mon", "01:30"),
                List.of("2026-10-05", "Kehitys (jatkuva)", "fri", "07:25"),
                List.of("2026-10-05", "SUPPORT", "tue", "00:30")
        ), savedHours());
    }

    @Test
    @DisplayName("should move forward to a later week")
    void shouldMoveToLaterWeek() {
        export(fakeHoursConfiguration(Map.of()), true, List.of(
                new KiekuHoursItem("SUPPORT 2", SHOWN_MONDAY.plusWeeks(2).plusDays(3), Duration.ofMinutes(75))
        ));

        assertEquals(List.of(), errors());
        assertEquals(List.of(List.of("2026-10-19", "SUPPORT 2", "thu", "01:15")), savedHours());
    }

    @Test
    @DisplayName("should skip a project without a Kieku row when the user wants to continue")
    void shouldSkipProjectWithoutRow() {
        export(fakeHoursConfiguration(Map.of()), true, List.of(
                new KiekuHoursItem("UNKNOWN", SHOWN_MONDAY, Duration.ofHours(1)),
                new KiekuHoursItem("Hallinto", SHOWN_MONDAY, Duration.ofHours(1))
        ));

        assertEquals(List.of(), errors());
        assertEquals(1, alerts.size());
        assertEquals(List.of(List.of("2026-10-05", "Hallinto", "mon", "01:00")), savedHours());
    }

    @Test
    @DisplayName("should fail when a project has no Kieku row and the user does not want to continue")
    void shouldFailWhenProjectHasNoRow() {
        prepare(fakeHoursConfiguration(Map.of()), false);

        // "SUPP" matches both "SUPPORT" and "SUPPORT 2"
        assertThrows(IllegalStateException.class, () -> exporter.exportItems(List.of(
                new KiekuHoursItem("SUPP", SHOWN_MONDAY, Duration.ofHours(1)))));
        assertEquals(List.of(), savedHours());
    }

    @Test
    @DisplayName("should not save when the user does not confirm saving")
    void shouldNotSaveWithoutConfirmation() {
        KiekuHoursConfiguration configuration = KiekuHoursConfiguration.mapToConfiguration(
                withValue(KiekuHoursConfiguration.SAVE_AUTOMATICALLY_KEY, "false"));

        export(configuration, false, List.of(new KiekuHoursItem("Hallinto", SHOWN_MONDAY, Duration.ofHours(1))));

        assertEquals(List.of("Tallennetaanko?"), alerts);
        assertEquals(List.of(), savedHours());
    }

    private void export(KiekuHoursConfiguration configuration, boolean alertAnswer, List<KiekuHoursItem> items) {
        prepare(configuration, alertAnswer);
        exporter.exportItems(items);
    }

    private void prepare(KiekuHoursConfiguration configuration, boolean alertAnswer) {
        exporter = new KiekuHoursExporter(base -> {
            driver = startChrome();
            return driver;
        }, new AnsweringAlertService(alertAnswer, alerts));
        exporter.setConfiguration(configuration);
        exporter.prepareExporter();
    }

    private List<List<String>> savedHours() {
        return driver.findElements(By.cssSelector("#saved-hours tbody tr")).stream()
                .map(row -> row.findElements(By.tagName("td")).stream().map(WebElement::getText).toList())
                .toList();
    }

    private List<String> errors() {
        return driver.findElements(By.cssSelector("#errors li")).stream().map(WebElement::getText).toList();
    }

    /**
     * Configuration matching the elements of the fake page. Compare these to your own Kieku configuration
     * in the application if the export fails against the real Kieku.
     */
    static KiekuHoursConfiguration fakeHoursConfiguration(Map<String, String> projectMapping) {
        return new KiekuHoursConfiguration(
                KiekuConfiguration.mapToConfiguration(Map.of(KiekuConfiguration.BROWSER_KEY, Browser.CHROME.name())),
                fakePageUrl("hours.html") + "?week=" + SHOWN_MONDAY,
                ".row-title",
                "row-title-(\\d+)",
                "hours-{day}-{row}",
                "mon,tue,wed,thu,fri",
                "//*[@id='week-days']/*[starts-with(normalize-space(text()),'MA ')]",
                "#previous-week",
                "#next-week",
                "//button[@id='save-button']",
                "disabled",
                ".busy",
                true,
                projectMapping
        );
    }

    private static Map<String, String> withValue(String key, String value) {
        KiekuHoursConfiguration configuration = fakeHoursConfiguration(Map.of());
        Map<String, String> map = new HashMap<>(KiekuConfiguration.toMap(configuration.base()));
        map.putAll(KiekuHoursConfiguration.toMap(configuration));
        map.put(key, value);
        return map;
    }

    private static String fakePageUrl(String page) {
        URL resource = KiekuHoursExporterFakePageTest.class.getResource("fake-kieku/" + page);
        if (resource == null) {
            throw new IllegalStateException("Fake Kieku page not found: " + page);
        }
        try {
            return Path.of(resource.toURI()).toUri().toString();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private static WebDriver startChrome() {
        ChromeOptions options = new ChromeOptions();
        if (Boolean.parseBoolean(System.getProperty("kieku.headless", "true"))) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--remote-allow-origins=*");
        String chromeBin = System.getenv("CHROME_BIN");
        if (chromeBin != null && !chromeBin.isBlank()) {
            options.setBinary(chromeBin);
        }

        try {
            return new ChromeDriver(options);
        } catch (WebDriverException exception) {
            assumeTrue(false, "Chrome could not be started with Selenium: " + exception.getMessage());
            return null;
        }
    }

    /** Gives the same answer to every question instead of opening a JavaFX dialog and records the questions. */
    private static class AnsweringAlertService extends AlertService {
        private final boolean answer;
        private final List<String> questions;

        AnsweringAlertService(boolean answer, List<String> questions) {
            this.answer = answer;
            this.questions = questions;
        }

        @Override
        public boolean showConfirmationAlert(String confirmationHeader, String confirmationText) {
            questions.add(confirmationHeader);
            return answer;
        }
    }
}
