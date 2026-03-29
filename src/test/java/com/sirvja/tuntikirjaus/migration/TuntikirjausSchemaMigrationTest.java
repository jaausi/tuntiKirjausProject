package com.sirvja.tuntikirjaus.migration;

import com.sirvja.tuntikirjaus.utils.DBUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TuntikirjausSchemaMigrationTest {

    private static final String ADD_IS_REMOTE_MIGRATION_CHECK_SQL =
            "SELECT EXISTS (SELECT 1 FROM pragma_table_info('Tuntikirjaus') WHERE name = 'IS_REMOTE') AS is_run;";
    private static final String ADD_IS_REMOTE_MIGRATION_SQL =
            "ALTER TABLE Tuntikirjaus ADD COLUMN IS_REMOTE INTEGER NOT NULL DEFAULT 0";

    private static final String REMOVE_DURATION_ENABLED_MIGRATION_CHECK_SQL =
            "SELECT NOT EXISTS (SELECT 1 FROM pragma_table_info('Tuntikirjaus') WHERE name = 'DURATION_ENABLED') AS is_run;";
    private static final String REMOVE_DURATION_ENABLED_MIGRATION_SQL =
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
                    "DROP TABLE Tuntikirjaus_old;";

    private Path tempDbPath;
    private String originalDbLocation;

    @BeforeEach
    void setUp() throws Exception {
        tempDbPath = Files.createTempFile("tuntikirjaus-migration-", ".db");
        originalDbLocation = getDbLocation();
        setDbLocation(tempDbPath.toString());
    }

    @AfterEach
    void tearDown() throws Exception {
        setDbLocation(originalDbLocation);
        Files.deleteIfExists(tempDbPath);
    }

    @Test
    void givenLegacyTuntikirjausTable_whenMigrationsRun_thenDurationEnabledIsRemovedAndDataIsPreserved() throws Exception {
        DBUtil.dbExecuteUpdate("CREATE TABLE Tuntikirjaus(" +
                "ROWID INTEGER PRIMARY KEY," +
                "START_TIME TEXT NOT NULL," +
                "END_TIME TEXT," +
                "TOPIC TEXT NOT NULL," +
                "DURATION_ENABLED INTEGER NOT NULL)");

        DBUtil.dbExecuteUpdate("INSERT INTO Tuntikirjaus(ROWID, START_TIME, END_TIME, TOPIC, DURATION_ENABLED) VALUES " +
                "(1, '2026-01-01T08:00:00', '2026-01-01T09:00:00', 'IBD-1 Koodaus', 1)");

        runCurrentTuntikirjausMigrations();

        assertTrue(columnExists("IS_REMOTE"));
        assertFalse(columnExists("DURATION_ENABLED"));

        ResultSet resultSet = DBUtil.dbExecuteQuery("SELECT ROWID, START_TIME, END_TIME, TOPIC, IS_REMOTE FROM Tuntikirjaus");
        assertTrue(resultSet.next());
        assertEquals(1, resultSet.getInt("ROWID"));
        assertEquals("2026-01-01T08:00:00", resultSet.getString("START_TIME"));
        assertEquals("2026-01-01T09:00:00", resultSet.getString("END_TIME"));
        assertEquals("IBD-1 Koodaus", resultSet.getString("TOPIC"));
        assertEquals(0, resultSet.getInt("IS_REMOTE"));
        assertFalse(resultSet.next());
    }

    @Test
    void givenAlreadyMigratedTable_whenMigrationsRunAgain_thenSchemaAndDataStayUnchanged() throws Exception {
        DBUtil.dbExecuteUpdate("CREATE TABLE Tuntikirjaus(" +
                "ROWID INTEGER PRIMARY KEY," +
                "START_TIME TEXT NOT NULL," +
                "END_TIME TEXT," +
                "TOPIC TEXT NOT NULL," +
                "DURATION_ENABLED INTEGER NOT NULL)");

        DBUtil.dbExecuteUpdate("INSERT INTO Tuntikirjaus(ROWID, START_TIME, END_TIME, TOPIC, DURATION_ENABLED) VALUES " +
                "(1, '2026-01-01T08:00:00', '2026-01-01T09:00:00', 'IBD-1 Koodaus', 1)");

        runCurrentTuntikirjausMigrations();
        runCurrentTuntikirjausMigrations();

        assertTrue(columnExists("IS_REMOTE"));
        assertFalse(columnExists("DURATION_ENABLED"));

        ResultSet resultSet = DBUtil.dbExecuteQuery("SELECT COUNT(*) AS row_count FROM Tuntikirjaus");
        assertTrue(resultSet.next());
        assertEquals(1, resultSet.getInt("row_count"));

        resultSet = DBUtil.dbExecuteQuery("SELECT ROWID, START_TIME, END_TIME, TOPIC, IS_REMOTE FROM Tuntikirjaus");
        assertTrue(resultSet.next());
        assertEquals(1, resultSet.getInt("ROWID"));
        assertEquals("2026-01-01T08:00:00", resultSet.getString("START_TIME"));
        assertEquals("2026-01-01T09:00:00", resultSet.getString("END_TIME"));
        assertEquals("IBD-1 Koodaus", resultSet.getString("TOPIC"));
        assertEquals(0, resultSet.getInt("IS_REMOTE"));
        assertFalse(resultSet.next());
    }

    private static void runCurrentTuntikirjausMigrations() {
        new Migration(
                "Add IS_REMOTE column to Tuntikirjaus table",
                ADD_IS_REMOTE_MIGRATION_CHECK_SQL,
                ADD_IS_REMOTE_MIGRATION_SQL
        ).run();

        new Migration(
                "Remove DURATION_ENABLED column from Tuntikirjaus table",
                REMOVE_DURATION_ENABLED_MIGRATION_CHECK_SQL,
                REMOVE_DURATION_ENABLED_MIGRATION_SQL
        ).run();
    }

    private boolean columnExists(String columnName) throws Exception {
        String query = String.format("SELECT EXISTS (SELECT 1 FROM pragma_table_info('Tuntikirjaus') WHERE name = '%s') AS is_run;", columnName);
        ResultSet resultSet = DBUtil.dbExecuteQuery(query);
        assertTrue(resultSet.next());
        return resultSet.getInt("is_run") == 1;
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
