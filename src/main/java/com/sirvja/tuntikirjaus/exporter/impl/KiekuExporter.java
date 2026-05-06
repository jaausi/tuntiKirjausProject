package com.sirvja.tuntikirjaus.exporter.impl;

import com.sirvja.tuntikirjaus.exporter.Exporter;
import com.sirvja.tuntikirjaus.service.AlertService;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.safari.SafariDriver;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.List;

public class KiekuExporter implements Exporter<KiekuConfiguration, KiekuItem> {

    private static final Dimension WINDOW_DIMENSION = new Dimension(1440, 1267);
    private static final DateTimeFormatter KIEKU_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter KIEKU_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private KiekuConfiguration configuration;
    private WebDriver driver;
    private AlertService alertService;

    public KiekuExporter() {
        this.alertService = new AlertService();
    }

    @Override
    public void setConfiguration(KiekuConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void prepareExporter() {
        setWebDriver();
        loginToPortal();
    }

    @Override
    public void exportItems(List<KiekuItem> items) {
        navigateToPageAndSetSize();
        items.forEach(this::fillInItem);
    }

    @Override
    public void destroyExporter() {
        driver.quit();
    }

    private void setWebDriver() {
        switch (configuration.browser()) {
            case SAFARI -> driver = new SafariDriver();
            case FIREFOX -> {
                FirefoxOptions options = new FirefoxOptions();
                // Käytä Firefoxin olemassa olevaa oletusprofiilia (evästeet, sessiot säilyvät)
                String firefoxProfilePath = findFirefoxDefaultProfile();
                if (firefoxProfilePath != null) {
                    options.addArguments("-profile", firefoxProfilePath);
                }
                driver = new FirefoxDriver(options);
            }
            case CHROME -> {
                ChromeOptions options = new ChromeOptions();
                // Käytä Chromen olemassa olevaa käyttäjäprofiilia (evästeet, sessiot säilyvät)
                String userHome = System.getProperty("user.home");
                String chromeProfile = userHome + "/Library/Application Support/Google/Chrome";
                options.addArguments("--user-data-dir=" + chromeProfile);
                options.addArguments("--profile-directory=Default");
                driver = new ChromeDriver(options);
            }
            case EDGE -> {
                EdgeOptions options = new EdgeOptions();
                String userHome = System.getProperty("user.home");
                String edgeProfile = userHome + "/Library/Application Support/Microsoft Edge";
                options.addArguments("--user-data-dir=" + edgeProfile);
                options.addArguments("--profile-directory=Default");
                driver = new EdgeDriver(options);
            }
        }
    }

    /**
     * Etsii Firefoxin oletusprofiilin hakemiston macOS:ssä.
     * Profiili löytyy ~/Library/Application Support/Firefox/Profiles/ alta.
     */
    private String findFirefoxDefaultProfile() {
        String userHome = System.getProperty("user.home");
        File profilesDir = new File(userHome + "/Library/Application Support/Firefox/Profiles");
        if (profilesDir.exists() && profilesDir.isDirectory()) {
            File[] profiles = profilesDir.listFiles();
            if (profiles != null) {
                // Priorisoidaan oletusprofiili (default-release)
                for (File profile : profiles) {
                    if (profile.getName().endsWith(".default-release")) {
                        return profile.getAbsolutePath();
                    }
                }
                // Fallback: ensimmäinen profiili
                for (File profile : profiles) {
                    if (profile.isDirectory()) {
                        return profile.getAbsolutePath();
                    }
                }
            }
        }
        return null;
    }

    private void loginToPortal() {
        driver.get(configuration.loginPortalUrl());
        driver.manage().window().setSize(WINDOW_DIMENSION);
        safelyFindElement(By.cssSelector(configuration.loginButtonCssSelector())).click();
        safelyFindElement(By.cssSelector(configuration.loginToOrganizationOptionCssSelector())).click();
        safelyFindElement(By.name(configuration.loginButtonName())).click();
    }

    private void navigateToPageAndSetSize() {
        driver.get(configuration.kiekuUrl());
        driver.manage().window().setSize(WINDOW_DIMENSION);
    }

    private void fillInItem(KiekuItem kiekuItem) {
        selectWeek(kiekuItem);
        clickAddHours();
        fillInDate(kiekuItem);
        fillInTime(kiekuItem);
        fillInEvent(kiekuItem);
        clickSave();
        clickClose();
    }

    private void selectWeek(KiekuItem kiekuItem) {
        int weekNumber = kiekuItem.time().get(WeekFields.ISO.weekOfWeekBasedYear());
        selectFromDropdown(configuration.weekDropdownId(), By.cssSelector(configuration.weekOptionCssSelectorFunction().apply(weekNumber)));
    }

    private void clickAddHours() {
        safelyFindElement(By.id(configuration.addWorkHoursButtonId())).click();
    }

    private void fillInDate(KiekuItem kiekuItem) {
        String date = kiekuItem.time().format(KIEKU_DATE_FORMATTER);
        safelyFindElement(By.id(configuration.dateFieldId())).click();
        safelyFindElement(By.id(configuration.dateFieldId())).sendKeys(date);
    }

    private void fillInTime(KiekuItem kiekuItem) {
        String time = kiekuItem.time().format(KIEKU_TIME_FORMATTER);
        safelyFindElement(By.id(configuration.timeFieldId())).click();
        safelyFindElement(By.id(configuration.timeFieldId())).sendKeys(time);
    }

    private void fillInEvent(KiekuItem kiekuItem) {
        safelyFindElement(By.id(configuration.eventDropdownId())).click();
        switch (kiekuItem.event()) {
            case IN -> selectFromDropdown(configuration.eventDropdownId(), By.xpath(configuration.toihinTuloOptionXpath()));
            case REMOTE_IN -> {
                selectFromDropdown(configuration.eventDropdownId(), By.xpath(configuration.toihinTuloSyyllaOptionXpath()));
                selectFromDropdown(configuration.reasonCodeDropdownId(), By.xpath(configuration.etatyoOptionXpath()));
            }
            case OUT -> selectFromDropdown(configuration.eventDropdownId(), By.xpath(configuration.toistaLahtoOptionXpath()));
            case REMOTE_OUT -> {
                selectFromDropdown(configuration.eventDropdownId(), By.xpath(configuration.toistaLahtoSyyllaOptionXpath()));
                selectFromDropdown(configuration.reasonCodeDropdownId(), By.xpath(configuration.etatyoOptionXpath()));
            }
        }
    }

    private void selectFromDropdown(String dropdownId, By dropdownItemSelector) {
        safelyFindElement(By.id(dropdownId)).click();
        WebElement dropdown = safelyFindElement(By.id(dropdownId));
        dropdown.findElement(dropdownItemSelector).click();
    }

    private void clickSave() {
        safelyFindElement(By.id(configuration.saveButtonId())).click();
    }

    private void clickClose() {
        safelyFindElement(By.id(configuration.closeButtonId())).click();
    }

    private WebElement safelyFindElement(By by) {
        WebElement element;
        try {
            element = driver.findElement(by);
        } catch (NoSuchElementException e) {
            boolean tryAgain = alertService.showConfirmationAlert(
                    "Elementtiä ei löytynyt",
                    String.format("Elementtiä (%s), jota selenium yritti hakea ei löytynyt. Haluatko yrittää uudestaan?", by.toString())
            );
            if(tryAgain) {
                element = safelyFindElement(by);
            } else {
                throw e;
            }
        }
        return element;
    }
}
