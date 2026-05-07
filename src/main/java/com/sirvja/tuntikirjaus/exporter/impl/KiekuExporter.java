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
import java.util.Locale;

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

    private static final String OS = System.getProperty("os.name").toLowerCase(Locale.ROOT);

    private static boolean isMac() {
        return OS.contains("mac");
    }

    private static boolean isWindows() {
        return OS.contains("win");
    }

    private void setWebDriver() {
        switch (configuration.browser()) {
            case SAFARI -> driver = new SafariDriver();
            case FIREFOX -> {
                FirefoxOptions options = new FirefoxOptions();
                // Aseta Firefoxin binääripolku, jotta selain löytyy app bundle -ympäristössä
                File firefoxBinary;
                if (isMac()) {
                    firefoxBinary = findBrowserBinary("/Applications/Firefox.app/Contents/MacOS/firefox");
                } else if (isWindows()) {
                    firefoxBinary = findBrowserBinary(
                            "C:\\Program Files\\Mozilla Firefox\\firefox.exe",
                            "C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe");
                } else {
                    firefoxBinary = findBrowserBinary("/usr/bin/firefox", "/usr/bin/firefox-esr");
                }
                if (firefoxBinary != null) {
                    options.setBinary(firefoxBinary.getAbsolutePath());
                }
                // Käytä TuntikirjausApp-kohtaista Firefox-profiilia (evästeet, sessiot säilyvät)
                // eikä käyttäjän aktiivista profiilia, jotta vältetään profiilin lukittumisongelma
                String firefoxProfilePath = getOrCreateSeleniumBrowserProfile("SeleniumFirefoxProfile");
                if (firefoxProfilePath != null) {
                    options.addArguments("-profile", firefoxProfilePath);
                }
                driver = new FirefoxDriver(options);
            }
            case CHROME -> {
                ChromeOptions options = new ChromeOptions();
                // Aseta Chromen binääripolku, jotta selain löytyy app bundle -ympäristössä
                File chromeBinary;
                if (isMac()) {
                    chromeBinary = findBrowserBinary("/Applications/Google Chrome.app/Contents/MacOS/Google Chrome");
                } else if (isWindows()) {
                    chromeBinary = findBrowserBinary(
                            "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
                            "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe");
                } else {
                    chromeBinary = findBrowserBinary("/usr/bin/google-chrome", "/usr/bin/chromium-browser", "/usr/bin/chromium");
                }
                if (chromeBinary != null) {
                    options.setBinary(chromeBinary.getAbsolutePath());
                }
                // Käytä TuntikirjausApp-kohtaista Chrome-profiilia (evästeet, sessiot säilyvät)
                // eikä käyttäjän aktiivista profiilia, jotta vältetään profiilin lukittumisongelma
                // kun Chrome on jo auki
                String seleniumChromeProfile = getOrCreateSeleniumBrowserProfile("SeleniumChromeProfile");
                if (seleniumChromeProfile != null) {
                    options.addArguments("--user-data-dir=" + seleniumChromeProfile);
                    options.addArguments("--profile-directory=Default");
                }
                driver = new ChromeDriver(options);
            }
            case EDGE -> {
                EdgeOptions options = new EdgeOptions();
                // Aseta Edgen binääripolku, jotta selain löytyy app bundle -ympäristössä
                File edgeBinary;
                if (isMac()) {
                    edgeBinary = findBrowserBinary("/Applications/Microsoft Edge.app/Contents/MacOS/Microsoft Edge");
                } else if (isWindows()) {
                    edgeBinary = findBrowserBinary(
                            "C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe",
                            "C:\\Program Files\\Microsoft\\Edge\\Application\\msedge.exe");
                } else {
                    edgeBinary = findBrowserBinary("/usr/bin/microsoft-edge", "/usr/bin/microsoft-edge-stable");
                }
                if (edgeBinary != null) {
                    options.setBinary(edgeBinary.getAbsolutePath());
                }
                // Käytä TuntikirjausApp-kohtaista Edge-profiilia (evästeet, sessiot säilyvät)
                // eikä käyttäjän aktiivista profiilia, jotta vältetään profiilin lukittumisongelma
                // kun Edge on jo auki
                String seleniumEdgeProfile = getOrCreateSeleniumBrowserProfile("SeleniumEdgeProfile");
                if (seleniumEdgeProfile != null) {
                    options.addArguments("--user-data-dir=" + seleniumEdgeProfile);
                    options.addArguments("--profile-directory=Default");
                }
                driver = new EdgeDriver(options);
            }
        }
    }

    /**
     * Palauttaa ensimmäisen olemassaolevan tiedoston annetuista poluista,
     * tai null jos yksikään ei löydy.
     */
    private File findBrowserBinary(String... candidates) {
        for (String path : candidates) {
            File f = new File(path);
            if (f.exists()) {
                return f;
            }
        }
        return null;
    }

    /**
     * Palauttaa TuntikirjausApp-kohtaisen selainprofiilin hakemistopolun.
     * Käyttää alustasta riippuvaa hakemistoa:
     *   macOS:   ~/Library/Application Support/TuntikirjausApp/<nimi>
     *   Windows: %APPDATA%\TuntikirjausApp\<nimi>
     *   Linux:   ~/.config/TuntikirjausApp/<nimi>
     * Käyttää erillistä profiilia (ei käyttäjän aktiivista profiilia), jotta
     * vältetään profiilin lukittuminen kun selain on jo auki.
     * Hakemisto luodaan tarvittaessa automaattisesti.
     * Palauttaa null jos hakemiston luonti epäonnistuu.
     */
    private String getOrCreateSeleniumBrowserProfile(String profileDirName) {
        String userHome = System.getProperty("user.home");
        String baseDir;
        if (isMac()) {
            baseDir = userHome + "/Library/Application Support/TuntikirjausApp";
        } else if (isWindows()) {
            String appData = System.getenv("APPDATA");
            baseDir = (appData != null ? appData : userHome) + File.separator + "TuntikirjausApp";
        } else {
            baseDir = userHome + "/.config/TuntikirjausApp";
        }
        File profileDir = new File(baseDir + File.separator + profileDirName);
        if (!profileDir.mkdirs() && !profileDir.exists()) {
            return null;
        }
        return profileDir.getAbsolutePath();
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
