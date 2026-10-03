package com.sirvja.tuntikirjaus.dao;

import com.sirvja.tuntikirjaus.domain.Configuration;
import com.sirvja.tuntikirjaus.domain.ReportConfig;
import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;
import com.sirvja.tuntikirjaus.exception.DataAccessException;
import com.sirvja.tuntikirjaus.utils.DBUtil;
import com.sirvja.tuntikirjaus.utils.Initializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DaoIntegrationTest {

    private Path tempDbPath;
    private String originalDbLocation;

    @BeforeEach
    void setUp() throws Exception {
        tempDbPath = Files.createTempFile("tuntikirjaus-dao-", ".db");
        originalDbLocation = getDbLocation();
        setDbLocation(tempDbPath.toString());
        TuntiKirjausDao.initializeTableIfNotExisting();
        ReportConfigDao.initializeTableIfNotExisting();
        ConfigurationDao.initializeTableIfNotExisting();
    }

    @AfterEach
    void tearDown() throws Exception {
        setDbLocation(originalDbLocation);
        Files.deleteIfExists(tempDbPath);
    }

    @Test
    void givenTopicWithQuotes_whenSaved_thenItIsStoredAsIs() {
        TuntiKirjausDao dao = new TuntiKirjausDao();
        String topic = "IBD-1 Asiakkaan 'X' palaveri; DROP TABLE Tuntikirjaus; --";

        TuntiKirjaus saved = dao.save(new TuntiKirjaus(LocalDateTime.of(2026, 1, 1, 8, 0), null, topic, true));

        assertTrue(saved.getId() > 0);
        TuntiKirjaus fetched = dao.get(saved.getId()).orElseThrow();
        assertEquals(topic, fetched.getTopic());
        assertTrue(fetched.isRemote());
        assertTrue(fetched.isEndTimeNull());
    }

    @Test
    void givenNoEndTime_whenSaved_thenEndTimeIsSqlNull() throws Exception {
        TuntiKirjausDao dao = new TuntiKirjausDao();
        TuntiKirjaus saved = dao.save(new TuntiKirjaus(LocalDateTime.of(2026, 1, 1, 8, 0), null, "OAW", false));

        ResultSet resultSet = DBUtil.dbExecuteQuery("SELECT END_TIME FROM Tuntikirjaus WHERE ROWID=?", saved.getId());
        assertTrue(resultSet.next());
        assertNull(resultSet.getString("END_TIME"));
    }

    @Test
    void givenSavedTuntikirjaus_whenUpdatedAndDeleted_thenChangesArePersisted() {
        TuntiKirjausDao dao = new TuntiKirjausDao();
        TuntiKirjaus saved = dao.save(new TuntiKirjaus(LocalDateTime.of(2026, 1, 1, 8, 0), null, "OAW", false));

        saved.setEndTime(LocalDateTime.of(2026, 1, 1, 9, 30));
        saved.setTopic("It's updated");
        dao.update(saved);

        TuntiKirjaus fetched = dao.get(saved.getId()).orElseThrow();
        assertEquals(Optional.of(LocalDateTime.of(2026, 1, 1, 9, 30)), fetched.getEndTime());
        assertEquals("It's updated", fetched.getTopic());

        dao.delete(saved);
        assertTrue(dao.get(saved.getId()).isEmpty());
    }

    @Test
    void givenEntriesOnDifferentDays_whenFetchedFromDate_thenOnlyLaterEntriesAreReturned() {
        TuntiKirjausDao dao = new TuntiKirjausDao();
        dao.save(new TuntiKirjaus(LocalDateTime.of(2026, 1, 1, 8, 0), null, "old", false));
        dao.save(new TuntiKirjaus(LocalDateTime.of(2026, 1, 5, 8, 0), null, "new", false));

        List<TuntiKirjaus> result = dao.getAllFromToList(LocalDate.of(2026, 1, 3));

        assertEquals(1, result.size());
        assertEquals("new", result.getFirst().getTopic());
    }

    @Test
    void givenReportConfig_whenSavedAndFetchedById_thenItIsReturned() {
        ReportConfigDao dao = new ReportConfigDao();
        ReportConfig saved = dao.save(new ReportConfig(LocalDate.of(2026, 1, 1), null, "IBD-'1'", "Raportti"));

        ReportConfig fetched = dao.get(saved.getId()).orElseThrow();

        assertEquals(Optional.of(LocalDate.of(2026, 1, 1)), fetched.getStartDate());
        assertTrue(fetched.getEndDate().isEmpty());
        assertEquals("IBD-'1'", fetched.getSearchQuery());
        assertEquals(1, dao.getAllToList().size());

        dao.delete(saved);
        assertTrue(dao.getAllToList().isEmpty());
    }

    @Test
    void givenXpathConfigurationWithQuotes_whenSavedAndUpdated_thenValueIsStoredAsIs() {
        ConfigurationDao dao = new ConfigurationDao();
        dao.save(new Configuration("toihinTuloOptionXpath", "//option[text()='Töihin tulo']"));
        dao.update(new Configuration("toihinTuloOptionXpath", "//option[text()='Töihin tulo syyllä']"));

        assertEquals("//option[text()='Töihin tulo syyllä']", dao.get("toihinTuloOptionXpath").orElseThrow().getValue());
    }

    @Test
    void givenDuplicateKey_whenSaved_thenDataAccessExceptionIsThrown() {
        ConfigurationDao dao = new ConfigurationDao();
        dao.save(new Configuration("key", "value"));

        assertThrows(DataAccessException.class, () -> dao.save(new Configuration("key", "other")));
    }

    @Test
    void givenLegacyNullStrings_whenMigrationsRun_thenTheyAreConvertedToSqlNull() throws Exception {
        DBUtil.dbExecuteUpdate("INSERT INTO Tuntikirjaus(START_TIME, END_TIME, TOPIC) VALUES ('2026-01-01T08:00:00', 'null', 'OAW')");
        DBUtil.dbExecuteUpdate("INSERT INTO ReportConfig(START_DATE, END_DATE, SEARCH_QUERY, REPORT_NAME) VALUES ('null', 'null', 'q', 'r')");

        Initializer.runDbMigrations();

        assertTrue(new TuntiKirjausDao().getAllToList().getFirst().isEndTimeNull());
        ReportConfig reportConfig = new ReportConfigDao().getAllToList().getFirst();
        assertTrue(reportConfig.getStartDate().isEmpty());
        assertTrue(reportConfig.getEndDate().isEmpty());
    }

    @Test
    void givenFailingStatement_whenExecutedInTransaction_thenEarlierStatementsAreRolledBack() throws Exception {
        assertThrows(Exception.class, () -> DBUtil.dbExecuteInTransaction(List.of(
                "INSERT INTO Tuntikirjaus(START_TIME, TOPIC) VALUES ('2026-01-01T08:00:00', 'OAW')",
                "INSERT INTO NonExistingTable VALUES (1)"
        )));

        assertTrue(new TuntiKirjausDao().getAllToList().isEmpty());
    }

    private static String getDbLocation() throws Exception {
        Field locationField = DBUtil.class.getDeclaredField("location");
        locationField.setAccessible(true);
        return (String) locationField.get(null);
    }

    private static void setDbLocation(String dbLocation) throws Exception {
        Field locationField = DBUtil.class.getDeclaredField("location");
        locationField.setAccessible(true);
        locationField.set(null, dbLocation);
    }
}
