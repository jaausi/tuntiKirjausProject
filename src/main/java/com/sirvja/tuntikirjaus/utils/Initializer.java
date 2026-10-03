package com.sirvja.tuntikirjaus.utils;

import com.sirvja.tuntikirjaus.dao.ConfigurationDao;
import com.sirvja.tuntikirjaus.dao.ReportConfigDao;
import com.sirvja.tuntikirjaus.dao.TuntiKirjausDao;
import com.sirvja.tuntikirjaus.migration.Migration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;

public class Initializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Initializer.class);

    public static void initializeApplication(){
        createLogsDirectory();
        DBUtil.checkOrCreateDatabaseFile();
        System.setProperty("prism.lcdtext", "false");
        createDbTablesIfNotExisting();
        runDbMigrations();
        Locale.setDefault(Locale.of("fi", "FI"));
    }

    private static void createLogsDirectory() {
        java.nio.file.Path logsPath = Paths.get(System.getProperty("user.home"), "tuntikirjaus", "logs");
        try {
            Files.createDirectories(logsPath);
        } catch (IOException e) {
            LOGGER.error("Failed to create logs directory at {}: {}", logsPath, e.getMessage());
        }
    }

    private static void createDbTablesIfNotExisting(){
        LOGGER.debug("Initializing database tables...");
        TuntiKirjausDao.initializeTableIfNotExisting();
        ReportConfigDao.initializeTableIfNotExisting();
        ConfigurationDao.initializeTableIfNotExisting();
        LOGGER.debug("Database tables initialized.");
    }

    public static void runDbMigrations() {
        LOGGER.info("Applying database migrations. Only pending migrations will be applied, so it's safe to add new migrations and run this on every application start.");
        new Migration(
                "Add IS_REMOTE column to Tuntikirjaus table",
                "SELECT EXISTS (SELECT 1 FROM pragma_table_info('Tuntikirjaus') WHERE name = 'IS_REMOTE') AS is_run;",
                "ALTER TABLE Tuntikirjaus ADD COLUMN IS_REMOTE INTEGER NOT NULL DEFAULT 0"
        ).run();

        new Migration(
                "Remove DURATION_ENABLED column from Tuntikirjaus table",
                "SELECT NOT EXISTS (SELECT 1 FROM pragma_table_info('Tuntikirjaus') WHERE name = 'DURATION_ENABLED') AS is_run;",
                "ALTER TABLE Tuntikirjaus RENAME TO Tuntikirjaus_old;" +
                        "CREATE TABLE Tuntikirjaus(" +
                        "ROWID INTEGER PRIMARY KEY," +
                        "START_TIME TEXT NOT NULL," +
                        "END_TIME TEXT," +
                        "TOPIC TEXT NOT NULL," +
                        "IS_REMOTE INTEGER NOT NULL DEFAULT 0" +
                        ");" +
                        "INSERT INTO Tuntikirjaus(ROWID, START_TIME, END_TIME, TOPIC, IS_REMOTE) " +
                        "SELECT ROWID, START_TIME, END_TIME, TOPIC, COALESCE(IS_REMOTE, 0) FROM Tuntikirjaus_old;" +
                        "DROP TABLE Tuntikirjaus_old;"
        ).run();

        // Earlier versions stored missing values as the literal string 'null' instead of SQL NULL
        new Migration(
                "Convert 'null' strings to SQL NULL",
                "SELECT NOT EXISTS (SELECT 1 FROM Tuntikirjaus WHERE END_TIME IN ('null', '')) " +
                        "AND NOT EXISTS (SELECT 1 FROM ReportConfig WHERE START_DATE IN ('null', '') OR END_DATE IN ('null', '')) AS is_run;",
                "UPDATE Tuntikirjaus SET END_TIME = NULL WHERE END_TIME IN ('null', '');" +
                        "UPDATE ReportConfig SET START_DATE = NULL WHERE START_DATE IN ('null', '');" +
                        "UPDATE ReportConfig SET END_DATE = NULL WHERE END_DATE IN ('null', '');"
        ).run();
    }
}
