package com.sirvja.tuntikirjaus.utils;

import com.sirvja.tuntikirjaus.domain.Paiva;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;

import static com.sirvja.tuntikirjaus.service.MainViewService.setCurrentDate;
import static com.sirvja.tuntikirjaus.utils.Constants.DROP_TABLE_ON_START;

public class Initializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Initializer.class);

    public static void initializeApplication(){
        DBUtil.checkOrCreateDatabaseFile();
        System.setProperty("prism.lcdtext", "false");
        assert DBUtil.checkDrivers();
        createDbTablesIfNotExisting();
        initializeTestData();
    }

    private static void createDbTablesIfNotExisting(){
        LOGGER.debug("Initializing Tuntikirjaus table...");
        TuntiKirjausDao.initializeTableIfNotExisting();
        LOGGER.debug("Tuntikirjaus table initialized.");
        LOGGER.debug("Initializing ReportConfig table...");
        ReportConfigDao.initializeTableIfNotExisting();
        LOGGER.debug("ReportConfig table initialized.");
    }

    private static void initializeTestData(){
        if(DROP_TABLE_ON_START){
            TuntiKirjausDao.dropTable();
            ReportConfigDao.dropTable();

            Initializer.populateTestData();
        }
    }

    private static boolean populateTestData(){

        setCurrentDate(new Paiva(LocalDate.now().minusDays(2)));
        //TODO: Add tuntikirjausdata
        return true;
    }
}
