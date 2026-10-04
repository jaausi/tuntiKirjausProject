package com.sirvja.tuntikirjaus.exporter.impl;

import com.sirvja.tuntikirjaus.service.AlertService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Runs the real KiekuExporter against a fake Kieku page (src/test/resources/.../fake-kieku) in Chrome,
 * so the whole export flow can be tested without logging in to the real Kieku.
 * <p>
 * Run with: {@code mvn test -Pbrowser-tests -Dtest=KiekuExporterFakePageTest}
 * <br>
 * Add {@code -Dkieku.headless=false} to watch the browser, and {@code -Dkieku.keepOpenMillis=10000}
 * to keep the page open for a while after the export.
 */
@Tag("browser")
class KiekuExporterFakePageTest {

    private WebDriver driver;
    private KiekuExporter exporter;

    @BeforeEach
    void setUp() {
        exporter = new KiekuExporter(configuration -> {
            driver = startChrome();
            return driver;
        }, new AlwaysCancelAlertService());
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        long keepOpenMillis = Long.getLong("kieku.keepOpenMillis", 0L);
        if (driver != null && keepOpenMillis > 0) {
            Thread.sleep(keepOpenMillis);
        }
        exporter.destroyExporter();
    }

    @Test
    @DisplayName("should export all event types to Kieku")
    void shouldExportAllEventTypes() {
        exportToFakeKieku(fakeKiekuConfiguration(), List.of(
                new KiekuItem(LocalDateTime.of(2026, 9, 28, 8, 5), KiekuEvent.IN),
                new KiekuItem(LocalDateTime.of(2026, 9, 28, 11, 30), KiekuEvent.OUT),
                new KiekuItem(LocalDateTime.of(2026, 9, 28, 12, 0), KiekuEvent.REMOTE_IN),
                new KiekuItem(LocalDateTime.of(2026, 9, 28, 16, 15), KiekuEvent.REMOTE_OUT)
        ));

        assertEquals(List.of(), errors());
        assertEquals(List.of(
                List.of("40", "28.09.2026", "08:05", "IN", ""),
                List.of("40", "28.09.2026", "11:30", "OUT", ""),
                List.of("40", "28.09.2026", "12:00", "IN_WITH_REASON", "ETATYO"),
                List.of("40", "28.09.2026", "16:15", "OUT_WITH_REASON", "ETATYO")
        ), savedEntries());
    }

    @Test
    @DisplayName("should select the ISO week of each item")
    void shouldSelectWeekOfEachItem() {
        exportToFakeKieku(fakeKiekuConfiguration(), List.of(
                new KiekuItem(LocalDateTime.of(2026, 9, 27, 9, 0), KiekuEvent.IN), // Sunday, week 39
                new KiekuItem(LocalDateTime.of(2026, 9, 28, 9, 0), KiekuEvent.IN), // Monday, week 40
                new KiekuItem(LocalDateTime.of(2027, 1, 1, 9, 0), KiekuEvent.IN)   // Friday, week 53 of 2026
        ));

        assertEquals(List.of(), errors());
        assertEquals(List.of("39", "40", "53"), savedEntries().stream().map(entry -> entry.get(0)).toList());
    }

    @Test
    @DisplayName("should fail when an element is not found and the user does not want to retry")
    void shouldFailWhenElementIsNotFound() {
        KiekuConfiguration configuration = fakeKiekuConfiguration();
        KiekuConfiguration brokenConfiguration = KiekuConfiguration.mapToConfiguration(
                withValue(KiekuConfiguration.toMap(configuration), "saveButtonId", "non-existing-save-button"));

        exporter.setConfiguration(brokenConfiguration);
        exporter.prepareExporter();

        assertThrows(NoSuchElementException.class, () -> exporter.exportItems(List.of(
                new KiekuItem(LocalDateTime.of(2026, 9, 28, 8, 0), KiekuEvent.IN)
        )));
        assertTrue(savedEntries().isEmpty());
    }

    private void exportToFakeKieku(KiekuConfiguration configuration, List<KiekuItem> items) {
        exporter.setConfiguration(configuration);
        exporter.prepareExporter();
        exporter.exportItems(items);
    }

    private List<List<String>> savedEntries() {
        return findWithoutWaiting(By.cssSelector("#saved-entries tbody tr")).stream()
                .map(row -> row.findElements(By.tagName("td")).stream().map(WebElement::getText).toList())
                .toList();
    }

    private List<String> errors() {
        return findWithoutWaiting(By.cssSelector("#errors li")).stream().map(WebElement::getText).toList();
    }

    // The exporter sets an implicit wait, which would make every empty result wait for the whole timeout
    private List<WebElement> findWithoutWaiting(By by) {
        Duration implicitWait = driver.manage().timeouts().getImplicitWaitTimeout();
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        try {
            return driver.findElements(by);
        } finally {
            driver.manage().timeouts().implicitlyWait(implicitWait);
        }
    }

    /**
     * Configuration matching the element ids of the fake Kieku pages. Compare these to your own Kieku
     * configuration in the application if the export fails against the real Kieku.
     */
    static KiekuConfiguration fakeKiekuConfiguration() {
        return new KiekuConfiguration(
                Browser.CHROME,
                fakePageUrl("login.html"),
                fakePageUrl("kieku.html"),
                "week-dropdown",
                "#week-option-week_index",
                "add-work-hours",
                "date-field",
                "time-field",
                "event-dropdown",
                "reason-dropdown",
                "save-button",
                "close-button",
                ".//li[@data-value='IN']",
                ".//li[@data-value='OUT']",
                ".//li[@data-value='IN_WITH_REASON']",
                ".//li[@data-value='OUT_WITH_REASON']",
                ".//li[@data-value='ETATYO']",
                "#login-button",
                "#org-option-valtio",
                "login-submit"
        );
    }

    private static String fakePageUrl(String page) {
        URL resource = KiekuExporterFakePageTest.class.getResource("fake-kieku/" + page);
        if (resource == null) {
            throw new IllegalStateException("Fake Kieku page not found: " + page);
        }
        try {
            return Path.of(resource.toURI()).toUri().toString();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Map<String, String> withValue(Map<String, String> map, String key, String value) {
        map.put(key, value);
        return map;
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

    /** Answers "no" to the "element not found, try again?" question instead of opening a JavaFX dialog. */
    private static class AlwaysCancelAlertService extends AlertService {
        @Override
        public boolean showConfirmationAlert(String confirmationHeader, String confirmationText) {
            return false;
        }
    }
}
