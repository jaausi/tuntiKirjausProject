package com.sirvja.tuntikirjaus.utils;

import com.sirvja.tuntikirjaus.dao.ConfigurationDao;
import com.sirvja.tuntikirjaus.dao.ReportConfigDao;
import com.sirvja.tuntikirjaus.dao.TuntiKirjausDao;
import com.sirvja.tuntikirjaus.migration.Migration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

import static com.sirvja.tuntikirjaus.utils.Constants.DROP_TABLE_ON_START;

public class Initializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Initializer.class);

    public static void initializeApplication(){
        DBUtil.checkOrCreateDatabaseFile();
        System.setProperty("prism.lcdtext", "false");
        assert DBUtil.checkDrivers();
        createDbTablesIfNotExisting();
        initializeTestData();
        runDbMigrations();
        Locale.setDefault(Locale.of("fi", "FI"));
    }

    private static void createDbTablesIfNotExisting(){
        LOGGER.debug("Initializing Tuntikirjaus table...");
        TuntiKirjausDao.initializeTableIfNotExisting();
        LOGGER.debug("Tuntikirjaus table initialized.");
        LOGGER.debug("Initializing ReportConfig table...");
        ReportConfigDao.initializeTableIfNotExisting();
        LOGGER.debug("ReportConfig table initialized.");
        LOGGER.debug("Initializing Configuration table...");
        ConfigurationDao.initializeTableIfNotExisting();
        LOGGER.debug("Configuration table initialized.");
    }

    private static void initializeTestData(){
        if(DROP_TABLE_ON_START){
            TuntiKirjausDao.dropTable();
            ReportConfigDao.dropTable();
            ConfigurationDao.dropTable();

            Initializer.populateTestData();
        }
    }

    private static boolean populateTestData(){

        // TODO: Set currentDate
        //TODO: Add tuntikirjausdata
        return true;
    }

    private static void runDbMigrations() {
        LOGGER.info("Appliying database migrations... Only migrations which changes are not yet applied to the database will be applied, so it's safe to add new migrations and run this method on every application start");

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
    }
}
