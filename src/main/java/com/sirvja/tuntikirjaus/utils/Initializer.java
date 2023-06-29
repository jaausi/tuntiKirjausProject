package com.sirvja.tuntikirjaus.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.sirvja.tuntikirjaus.utils.Constants.DROP_TABLE_ON_START;

public class Initializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Initializer.class);

    public static void initializeApplication(){
        DBUtil.checkOrCreateDatabaseFile();
        System.setProperty("prism.lcdtext", "false");
        assert DBUtil.checkDrivers();
        createDbTablesIfNotExisting();
        initializeDb();
    }

    private static void createDbTablesIfNotExisting(){
        LOGGER.debug("Initializing Tuntikirjaus table...");
        TuntiKirjausDao.initializeTableIfNotExisting();
        LOGGER.debug("Tuntikirjaus table initialized.");
        LOGGER.debug("Initializing ReportConfig table...");
        ReportConfigDao.initializeTableIfNotExisting();
        LOGGER.debug("ReportConfig table initialized.");
    }

    private static void initializeDb(){
        if(DROP_TABLE_ON_START){
            TuntiKirjausDao.dropTable();
            ReportConfigDao.dropTable();
        }
    }
}
