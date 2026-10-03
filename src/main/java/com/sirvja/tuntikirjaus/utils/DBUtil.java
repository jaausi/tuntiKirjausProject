package com.sirvja.tuntikirjaus.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.List;


public class DBUtil {

    private static String location;
    private static final Logger LOGGER = LoggerFactory.getLogger(DBUtil.class);

    public static void checkOrCreateDatabaseFile(){
        Path rootPath = Paths.get(System.getProperty("user.home"), "tuntikirjaus", "database");
        try {
            Files.createDirectories(rootPath);
        } catch (IOException e){
            throw new IllegalStateException("Couldn't create database directory: " + rootPath, e);
        }
        // SQLite creates the database file itself on first connection
        location = rootPath.resolve("tuntikirjaus.db").toString();
        LOGGER.debug("Using database in location: {}", location);
    }

    public static Connection connect() throws SQLException {
        LOGGER.debug("Connecting to database with address: jdbc:sqlite:{}", location);
        return DriverManager.getConnection("jdbc:sqlite:" + location);
    }

    /**
     * Executes a select (or INSERT ... RETURNING) statement. Values in params are bound to the '?' placeholders.
     */
    public static ResultSet dbExecuteQuery(String queryStmt, Object... params) throws SQLException {
        LOGGER.debug("Select statement: {}", queryStmt);
        try (Connection connection = connect();
             PreparedStatement statement = prepare(connection, queryStmt, params);
             ResultSet resultSet = statement.executeQuery()) {
            CachedRowSet crs = RowSetProvider.newFactory().createCachedRowSet();
            crs.populate(resultSet);
            return crs;
        } catch (SQLException e) {
            LOGGER.error("Problem occurred at executeQuery operation: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Executes an update/insert/delete/DDL statement. Values in params are bound to the '?' placeholders.
     */
    public static void dbExecuteUpdate(String updateStmt, Object... params) throws SQLException {
        LOGGER.debug("Update statement: {}", updateStmt);
        try (Connection connection = connect();
             PreparedStatement statement = prepare(connection, updateStmt, params)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Problem occurred at executeUpdate operation: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Executes all statements in a single transaction. If any of them fails, none of them is applied.
     */
    public static void dbExecuteInTransaction(List<String> statements) throws SQLException {
        try (Connection connection = connect()) {
            connection.setAutoCommit(false);
            try (Statement statement = connection.createStatement()) {
                for (String sql : statements) {
                    LOGGER.debug("Transaction statement: {}", sql);
                    statement.executeUpdate(sql);
                }
                connection.commit();
            } catch (SQLException e) {
                LOGGER.error("Problem occurred in transaction, rolling back: {}", e.getMessage());
                connection.rollback();
                throw e;
            }
        }
    }

    private static PreparedStatement prepare(Connection connection, String sql, Object... params) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
        return statement;
    }
}
