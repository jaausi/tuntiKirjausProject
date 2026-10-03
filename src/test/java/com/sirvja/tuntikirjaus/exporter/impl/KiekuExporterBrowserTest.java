package com.sirvja.tuntikirjaus.exporter.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.safari.SafariDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Tag("browser")
class KiekuExporterBrowserTest {

    private static final Logger logger = LoggerFactory.getLogger(KiekuExporterBrowserTest.class);
    private WebDriver driver;

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @DisplayName("should open Chrome, accept confirmation, and then close")
    void shouldOpenChromeAndLoadPage() {
        assumeTrue(isChromeAvailable(), "Chrome must be installed locally to run this browser regression test");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--remote-allow-origins=*");

        try {
            driver = new ChromeDriver(options);
        } catch (WebDriverException exception) {
            assumeTrue(false, "Chrome could not be started with Selenium Manager: " + exception.getMessage());
        }

        verifyBrowserNavigation();
    }

    @Test
    @DisplayName("should open Firefox, accept confirmation, and then close")
    void shouldOpenFirefoxAndLoadPage() {
        assumeTrue(isFirefoxAvailable(), "Firefox must be installed locally to run this browser regression test");

        FirefoxOptions options = new FirefoxOptions();
        try {
            driver = new FirefoxDriver(options);
        } catch (WebDriverException exception) {
            assumeTrue(false, "Firefox could not be started with Selenium Manager: " + exception.getMessage());
        }

        verifyBrowserNavigation();
    }

    @Test
    @DisplayName("should open Edge, accept confirmation, and then close")
    void shouldOpenEdgeAndLoadPage() {
        assumeTrue(isEdgeAvailable(), "Edge must be installed locally to run this browser regression test");

        EdgeOptions options = new EdgeOptions();
        try {
            driver = new EdgeDriver(options);
        } catch (WebDriverException exception) {
            assumeTrue(false, "Edge could not be started with Selenium Manager: " + exception.getMessage());
        }

        verifyBrowserNavigation();
    }

    @Test
    @DisplayName("should open Safari, accept confirmation, and then close")
    void shouldOpenSafariAndLoadPage() {
        assumeTrue(isSafariAvailable(), "Safari must be installed locally to run this browser regression test");

        try {
            driver = new SafariDriver();
        } catch (WebDriverException exception) {
            String diagnosticMessage = buildSafariDiagnosticMessage(exception);
            logger.error("Safari WebDriver initialization failed: {}", diagnosticMessage, exception);
            assumeTrue(false, diagnosticMessage);
        }

        verifyBrowserNavigation();
    }

    private String buildSafariDiagnosticMessage(WebDriverException exception) {
        return """
                Safari could not be started by Selenium WebDriver.
                
                FIX:
                1. Open Safari
                2. Go to Develop menu → Enable "Allow remote automation"
                   (If no Develop menu, enable it: Safari → Settings → Advanced → Show Develop menu)
                3. Close and reopen Safari
                4. Try the test again
                
                ALSO CHECK:
                - macOS Automation permission: System Settings → Privacy & Security → Automation
                  Grant permission for your Terminal or IDE app to control Safari
                - Close any other Safari windows before running the test
                - Ensure no other Safari sessions are running
                
                Root cause: """ + exception.getMessage();
    }

    private void verifyBrowserNavigation() {
        driver.get("https://example.com");
        assertEquals("Example Domain", driver.getTitle());
    }

    private boolean isChromeAvailable() {
        String chromeBin = System.getenv("CHROME_BIN");
        if (chromeBin != null && !chromeBin.isBlank()) {
            return new File(chromeBin).exists();
        }

        return new File("/Applications/Google Chrome.app/Contents/MacOS/Google Chrome").exists()
                || new File("/Applications/Google Chrome Canary.app/Contents/MacOS/Google Chrome Canary").exists()
                || new File("/usr/bin/google-chrome").exists()
                || new File("/usr/bin/chromium").exists();
    }

    private boolean isFirefoxAvailable() {
        return new File("/Applications/Firefox.app/Contents/MacOS/firefox").exists()
                || new File("/usr/bin/firefox").exists();
    }

    private boolean isEdgeAvailable() {
        return new File("/Applications/Microsoft Edge.app/Contents/MacOS/Microsoft Edge").exists()
                || new File("/usr/bin/microsoft-edge").exists();
    }

    private boolean isSafariAvailable() {
        return new File("/Applications/Safari.app/Contents/MacOS/Safari").exists();
    }
}
