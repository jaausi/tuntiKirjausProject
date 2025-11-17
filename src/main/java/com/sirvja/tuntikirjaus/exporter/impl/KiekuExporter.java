package com.sirvja.tuntikirjaus.exporter.impl;

import com.sirvja.tuntikirjaus.exporter.Exporter;
import com.sirvja.tuntikirjaus.service.AlertService;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.safari.SafariDriver;

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
            case FIREFOX -> driver = new FirefoxDriver();
            case CHROME -> driver = new ChromeDriver();
            case EDGE -> driver = new EdgeDriver();
        }
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
        WebElement dropdown = driver.findElement(By.id(dropdownId));
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
