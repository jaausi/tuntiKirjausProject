package com.sirvja.tuntikirjaus.utils;

import com.sirvja.tuntikirjaus.domain.Paiva;
import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;
import com.sirvja.tuntikirjaus.service.MainViewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.sirvja.tuntikirjaus.service.MainViewService.addTuntikirjaus;
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
        addTuntikirjaus(new TuntiKirjaus(LocalDateTime.now().minusHours(5).minusDays(2), LocalDateTime.now().minusHours(6).minusDays(2), "IBD-1220 Koodaus", true));
        addTuntikirjaus(new TuntiKirjaus(LocalDateTime.now().minusHours(6).minusDays(2), LocalDateTime.now().minusHours(7).minusDays(2), "HAAV-1102 Koodaus", true));
        addTuntikirjaus(new TuntiKirjaus(LocalDateTime.now().minusHours(7).minusDays(2), null, "HAAV-1234 Migraatiot", true));

        setCurrentDate(new Paiva(LocalDate.now().minusDays(1)));
        addTuntikirjaus(new TuntiKirjaus(LocalDateTime.now().minusHours(3).minusDays(1), LocalDateTime.now().minusHours(4).minusDays(1), "LON-1244 Koodaus", true));
        addTuntikirjaus(new TuntiKirjaus(LocalDateTime.now().minusHours(4).minusDays(1), LocalDateTime.now().minusHours(5).minusDays(1), "POL-1334 Katselmointi", true));
        addTuntikirjaus(new TuntiKirjaus(LocalDateTime.now().minusHours(5).minusDays(1), LocalDateTime.now().minusHours(6).minusDays(1), "TYR-1234 Migraatiot", true));
        addTuntikirjaus(new TuntiKirjaus(LocalDateTime.now().minusHours(7).minusDays(1), null, "IBD-1534 Testaus", true));

        setCurrentDate(new Paiva(LocalDate.now()));
        addTuntikirjaus(new TuntiKirjaus(LocalDateTime.now().minusHours(3), LocalDateTime.now().minusHours(4), "IBD-1114 Koodaus", true));
        addTuntikirjaus(new TuntiKirjaus(LocalDateTime.now().minusHours(4), LocalDateTime.now().minusHours(5), "NEF-1334 Lomakemuutokset", true));
        addTuntikirjaus(new TuntiKirjaus(LocalDateTime.now().minusHours(5), LocalDateTime.now().minusHours(6), "UR-1234 Testaus", true));
        addTuntikirjaus(new TuntiKirjaus(LocalDateTime.now().minusHours(6), LocalDateTime.now().minusHours(7), "IBD-1224 Migraatiot", true));
        addTuntikirjaus(new TuntiKirjaus(LocalDateTime.now().minusHours(7), null, "VI-1234 Katselmointi", true));

        return true;
    }
}
