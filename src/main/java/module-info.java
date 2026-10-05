module com.sirvja.tuntikirjaus {
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires java.sql.rowset;
    requires org.slf4j;
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;
    // Used to send log files to a server; jlink leaves the module out unless required here
    requires java.net.http;
    requires javafx.graphics;
    requires javafx.controls;
    requires org.seleniumhq.selenium.safari_driver;
    requires org.seleniumhq.selenium.firefox_driver;
    requires org.seleniumhq.selenium.chrome_driver;
    requires org.seleniumhq.selenium.edge_driver;
    // Selenium declares Guava as a static (optional) dependency, so jlink leaves it out unless required here.
    // Without it ChromeDriver fails at runtime with NoClassDefFoundError: com/google/common/net/MediaType.
    requires com.google.common;
    requires javafx.base;
    requires tuntikirjaus.components.lib;

    opens com.sirvja.tuntikirjaus to javafx.fxml;
    exports com.sirvja.tuntikirjaus;
    exports com.sirvja.tuntikirjaus.domain;
    opens com.sirvja.tuntikirjaus.domain to javafx.fxml;
    exports com.sirvja.tuntikirjaus.utils;
    opens com.sirvja.tuntikirjaus.utils to javafx.fxml;
    exports com.sirvja.tuntikirjaus.controller;
    opens com.sirvja.tuntikirjaus.controller to javafx.fxml;
    exports com.sirvja.tuntikirjaus.dao;
    opens com.sirvja.tuntikirjaus.dao to javafx.fxml;
    // Logback instantiates MaskingPatternLayout from logback.xml with reflection
    exports com.sirvja.tuntikirjaus.logging;
}