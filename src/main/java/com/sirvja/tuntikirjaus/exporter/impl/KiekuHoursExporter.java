package com.sirvja.tuntikirjaus.exporter.impl;

import com.sirvja.tuntikirjaus.exporter.Exporter;
import com.sirvja.tuntikirjaus.service.AlertService;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Fills project specific hours to the Kieku work time allocation page.
 * <p>
 * The page shows one week at a time. Every project has its own row with a title and an hour field for each
 * day. The exporter moves to the week of the items, finds the row of each project by its title and types the
 * hours of each day to the fields of the row. Existing values of the filled fields are replaced, other fields
 * are left as they are.
 * <p>
 * Element ids and selectors come from {@link KiekuHoursConfiguration}.
 */
public class KiekuHoursExporter implements Exporter<KiekuHoursConfiguration, KiekuHoursItem> {

    private static final Logger LOGGER = LoggerFactory.getLogger(KiekuHoursExporter.class);

    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(20);
    private static final int MAX_WEEK_STEPS = 60;
    private static final DateTimeFormatter HEADER_DATE = DateTimeFormatter.ofPattern("dd.MM");

    private final Function<KiekuConfiguration, WebDriver> webDriverFactory;
    private final AlertService alertService;
    private KiekuHoursConfiguration configuration;
    private WebDriver driver;
    private WebDriverWait wait;

    public KiekuHoursExporter() {
        this(KiekuExporter::createWebDriver, new AlertService());
    }

    /**
     * Allows tests to use their own browser (e.g. headless Chrome) and alert handling instead of
     * the browser profile of the user and JavaFX dialogs.
     */
    public KiekuHoursExporter(Function<KiekuConfiguration, WebDriver> webDriverFactory, AlertService alertService) {
        this.webDriverFactory = webDriverFactory;
        this.alertService = alertService;
    }

    @Override
    public void setConfiguration(KiekuHoursConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void prepareExporter() {
        LOGGER.info("Starting browser {} for Kieku hours export", configuration.base().browser());
        driver = webDriverFactory.apply(configuration.base());
        wait = new WebDriverWait(driver, WAIT_TIMEOUT);
        driver.get(configuration.hoursUrl());
    }

    /**
     * Fills the hours one week at a time. Items of the same project and day are summed.
     */
    @Override
    public void exportItems(List<KiekuHoursItem> items) {
        LOGGER.info("Exporting {} project hour item(s) to Kieku", items.size());
        // After logging in the single sign-on may leave the browser to another page
        driver.get(configuration.hoursUrl());
        waitUntilLoaded();

        Map<LocalDate, List<KiekuHoursItem>> byWeek = items.stream()
                .collect(Collectors.groupingBy(
                        item -> item.date().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)),
                        TreeMap::new,
                        Collectors.toList()));

        byWeek.forEach((monday, weekItems) -> {
            LOGGER.info("Filling week starting {} ({} item(s))", monday, weekItems.size());
            navigateToWeek(monday);
            if (fillWeek(sumByProjectAndDay(weekItems))) {
                save(monday);
            } else {
                LOGGER.info("Nothing changed on week {}, not saving", monday);
            }
        });
        LOGGER.info("Exported project hours of {} week(s) to Kieku", byWeek.size());
    }

    @Override
    public void destroyExporter() {
        if (driver != null) {
            LOGGER.debug("Closing browser");
            driver.quit();
            driver = null;
        }
    }

    // ---------- week ----------

    private void navigateToWeek(LocalDate targetMonday) {
        for (int i = 0; i < MAX_WEEK_STEPS; i++) {
            String headerText = mondayHeaderText();
            LocalDate shownMonday = parseMonday(headerText, targetMonday);
            long weeks = ChronoUnit.WEEKS.between(shownMonday, targetMonday);
            if (weeks == 0) {
                return;
            }
            LOGGER.debug("Shown week {} -> target {}, moving {}", shownMonday, targetMonday, weeks > 0 ? "forward" : "back");
            String weekButton = weeks > 0 ? configuration.nextWeekCssSelector() : configuration.previousWeekCssSelector();
            driver.findElement(By.cssSelector(weekButton)).click();
            wait.until(d -> !mondayHeaderText().equals(headerText));
            waitUntilLoaded();
        }
        throw new IllegalStateException("Kiekussa ei päästy viikolle " + targetMonday);
    }

    private String mondayHeaderText() {
        return waitForVisible(By.xpath(configuration.mondayHeaderXpath())).getText().trim();
    }

    /**
     * Parses the date of a week header, e.g. "MA 05.10" -> 2026-10-05. The year is taken from the reference
     * date, taking the turn of the year into account.
     */
    static LocalDate parseMonday(String headerText, LocalDate reference) {
        Matcher matcher = Pattern.compile("(\\d{1,2})\\.(\\d{1,2})").matcher(headerText);
        if (!matcher.find()) {
            throw new IllegalStateException("Viikon päivämäärää ei löytynyt tekstistä: " + headerText);
        }
        MonthDay monthDay = MonthDay.parse(
                String.format("%02d.%02d", Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2))),
                HEADER_DATE);
        LocalDate candidate = monthDay.atYear(reference.getYear());
        if (candidate.isAfter(reference.plusMonths(6))) {
            candidate = monthDay.atYear(reference.getYear() - 1);
        } else if (candidate.isBefore(reference.minusMonths(6))) {
            candidate = monthDay.atYear(reference.getYear() + 1);
        }
        return candidate;
    }

    // ---------- rows and fields ----------

    private Map<String, Map<DayOfWeek, Duration>> sumByProjectAndDay(List<KiekuHoursItem> weekItems) {
        int dayCount = configuration.dayIdPrefixList().size();
        Map<String, Map<DayOfWeek, Duration>> result = new LinkedHashMap<>();
        for (KiekuHoursItem item : weekItems) {
            DayOfWeek day = item.date().getDayOfWeek();
            if (day.ordinal() >= dayCount) {
                LOGGER.warn("Skipping item of {}: Kieku has no field for {}", item.date(), day);
                continue;
            }
            result.computeIfAbsent(item.project(), _ -> new TreeMap<>()).merge(day, item.duration(), Duration::plus);
        }
        return result;
    }

    /**
     * @return true if the value of any field changed
     */
    private boolean fillWeek(Map<String, Map<DayOfWeek, Duration>> hours) {
        Map<String, String> rows = readRows();
        LOGGER.debug("Found {} row(s) from Kieku", rows.size());

        boolean changed = false;
        for (Map.Entry<String, Map<DayOfWeek, Duration>> projectHours : hours.entrySet()) {
            String project = projectHours.getKey();
            String kiekuRow = configuration.kiekuRowFor(project);
            String rowNumber = findRow(rows, kiekuRow);
            if (rowNumber == null) {
                LOGGER.warn("No unambiguous Kieku row found for a project");
                boolean skip = alertService.showConfirmationAlert(
                        "Kieku-projektia ei löytynyt",
                        String.format("Projektille '%s' ei löytynyt yksiselitteistä Kieku-projektia (haettiin '%s').%n"
                                        + "Kieku-projektit: %s%n%n"
                                        + "Voit määrittää projektin Kieku-projektin asetuksista. Ohitetaanko projekti ja jatketaan?",
                                project, kiekuRow, String.join(", ", rows.keySet())));
                if (!skip) {
                    throw new IllegalStateException("Kieku-projektia ei löytynyt projektille " + project);
                }
                continue;
            }
            for (Map.Entry<DayOfWeek, Duration> dayHours : projectHours.getValue().entrySet()) {
                String fieldId = configuration.dayFieldId(dayHours.getKey().ordinal(), rowNumber);
                changed |= fillField(fieldId, toKiekuTime(dayHours.getValue()));
            }
            LOGGER.info("Filled {} day(s) to Kieku row {}", projectHours.getValue().size(), rowNumber);
        }
        return changed;
    }

    /** Row title -> row number. */
    private Map<String, String> readRows() {
        By rowTitles = By.cssSelector(configuration.rowTitleCssSelector());
        Pattern rowNumberPattern = Pattern.compile(configuration.rowNumberRegex());
        wait.until(ExpectedConditions.presenceOfElementLocated(rowTitles));

        Map<String, String> rows = new LinkedHashMap<>();
        for (WebElement title : driver.findElements(rowTitles)) {
            String id = title.getDomAttribute("id");
            String text = title.getDomProperty("textContent");
            Matcher matcher = rowNumberPattern.matcher(id == null ? "" : id);
            if (matcher.find() && text != null && !text.isBlank()) {
                rows.put(text.trim(), matcher.group(1));
            }
        }
        return rows;
    }

    /**
     * Finds the row whose title equals the wanted one, then a row whose title starts with it and finally one
     * whose title contains it (case insensitive). Returns null if there is not exactly one match.
     */
    static String findRow(Map<String, String> rows, String wanted) {
        String w = wanted.toLowerCase(Locale.ROOT).trim();
        List<Predicate<String>> matchers = List.of(
                t -> t.equals(w),
                t -> t.startsWith(w),
                t -> t.contains(w));
        for (Predicate<String> matcher : matchers) {
            List<String> hits = new ArrayList<>();
            rows.forEach((title, number) -> {
                if (matcher.test(title.toLowerCase(Locale.ROOT).trim())) hits.add(number);
            });
            if (hits.size() == 1) return hits.getFirst();
            // Ambiguous, the user has to fix the mapping
            if (hits.size() > 1) return null;
        }
        return null;
    }

    /**
     * @return true if the value of the field changed
     */
    private boolean fillField(String inputId, String value) {
        WebElement input = waitForVisible(By.id(inputId));
        String oldValue = input.getDomProperty("value");
        if (toMinutes(oldValue) == toMinutes(value)) {
            return false;
        }
        js().executeScript("arguments[0].scrollIntoView({block:'center'});", input);
        input.click();
        // select() + typing replaces the old value, works on every OS unlike Ctrl+A / Cmd+A
        js().executeScript("arguments[0].select();", input);
        input.sendKeys(value);
        // TAB fires the change event, without it the value is not saved
        input.sendKeys(Keys.TAB);
        String shown = input.getDomProperty("value");
        if (toMinutes(shown) != toMinutes(value)) {
            LOGGER.warn("Field {} shows '{}' after typing '{}'", inputId, shown, value);
        }
        return true;
    }

    /** "1:30" / "01:30" -> 90, empty -> 0, otherwise -1. */
    static long toMinutes(String time) {
        if (time == null || time.isBlank()) return 0;
        if (!time.trim().matches("\\d{1,2}:\\d{2}")) return -1;
        String[] p = time.trim().split(":");
        return Long.parseLong(p[0]) * 60 + Long.parseLong(p[1]);
    }

    static String toKiekuTime(Duration duration) {
        long minutes = duration.toMinutes();
        return String.format("%02d:%02d", minutes / 60, minutes % 60);
    }

    // ---------- saving ----------

    private void save(LocalDate monday) {
        boolean doSave = configuration.saveAutomatically() || alertService.showConfirmationAlert(
                "Tallennetaanko?",
                "Tunnit viikolle " + monday + " on syötetty selaimeen. Tarkista ne ja valitse Kyllä tallentaaksesi.");
        if (!doSave) {
            LOGGER.info("User chose not to save week {}", monday);
            return;
        }
        By saveButtonSelector = By.xpath(configuration.saveButtonXpath());
        WebElement saveButton = wait.until(d -> {
            WebElement button = d.findElement(saveButtonSelector);
            return isEnabled(button) ? button : null;
        });
        saveButton.click();
        waitUntilLoaded();
        LOGGER.info("Saved week {}", monday);
    }

    private boolean isEnabled(WebElement button) {
        String disabledClass = configuration.disabledButtonCssClass();
        if (disabledClass == null || disabledClass.isBlank()) {
            return button.isEnabled();
        }
        String classes = button.getDomAttribute("class");
        return classes == null || Arrays.stream(classes.split("\\s+")).noneMatch(disabledClass.trim()::equals);
    }

    // ---------- helpers ----------

    /**
     * Waits until an element matching the selector is visible. Selenium's own visibility checks (e.g.
     * {@link ExpectedConditions#visibilityOfElementLocated(By)}) can't be used, because their JavaScript is not
     * found when the application runs as a module.
     */
    private WebElement waitForVisible(By by) {
        return wait.until(d -> d.findElements(by).stream()
                .filter(element -> (Boolean) js().executeScript("return arguments[0].offsetParent !== null;", element))
                .findFirst()
                .orElse(null));
    }

    private void waitUntilLoaded() {
        String busyIndicator = configuration.busyIndicatorCssSelector();
        wait.until(d -> (Boolean) ((JavascriptExecutor) d).executeScript(
                "return document.readyState === 'complete' && (!arguments[0] || "
                        + "!Array.from(document.querySelectorAll(arguments[0])).some(e => e.offsetParent !== null));",
                busyIndicator == null || busyIndicator.isBlank() ? "" : busyIndicator));
    }

    private JavascriptExecutor js() {
        return (JavascriptExecutor) driver;
    }
}
