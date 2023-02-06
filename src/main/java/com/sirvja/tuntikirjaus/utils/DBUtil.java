package com.sirvja.tuntikirjaus.utils;

import com.sirvja.tuntikirjaus.TuntikirjausApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Optional;


public class DBUtil {

    private static String location;
    private static final Logger LOGGER = LoggerFactory.getLogger(DBUtil.class);

    public static void checkOrCreateDatabaseFile(){
        Optional<String> optionalLocation = Optional.ofNullable(TuntikirjausApplication.class.getResource("database/tuntikirjaus.db").toExternalForm());

        // Don't use database in resource folder, if running from jar file
        if(optionalLocation.isPresent() && !optionalLocation.get().contains(".jar") && false){
            location = optionalLocation.get();
            System.out.println(String.format("Current dir: %s", location));
        } else {
            Path rootPath = Paths.get(System.getProperty("user.home")+"/tuntikirjaus/database");

            System.out.println(String.format("Current dir: %s", rootPath));
            LOGGER.debug("Creating database (database/tuntikirjaus.db) to current directory: {}", rootPath);

            try {
                Files.createDirectories(rootPath);

                File directoryFile = new File(System.getProperty("user.home")+"/tuntikirjaus/database");
                File databaseFile = new File(System.getProperty("user.home")+"/tuntikirjaus/database/tuntikirjaus.db");

                assert directoryFile.exists() || directoryFile.mkdir();
                assert databaseFile.exists() || databaseFile.createNewFile();

                location = databaseFile.getPath();
            } catch (IOException e){
                LOGGER.error("Couldn't create database file: {}", e.getMessage());
            }
        }
        LOGGER.debug("Using database in location: {}", location);
    }

    public static Connection connect() {
        String dbPrefix = "jdbc:sqlite:";
        Connection connection;
        try {
            LOGGER.debug("Connecting to database with address: {}", dbPrefix+location);
            connection = DriverManager.getConnection(dbPrefix + location);
        } catch (SQLException exception) {
            LOGGER.error("Could not connect to SQLite DB at: {}", location);
            return null;
        }
        return connection;
    }

    public static boolean checkDrivers() {
        try {
            Class.forName("org.sqlite.JDBC");
            DriverManager.registerDriver(new org.sqlite.JDBC());
            return true;
        } catch (ClassNotFoundException | SQLException e) {
            LOGGER.error("Could not start SQLite Drivers");
            return false;
        }
    }

    //DB Execute Query Operation
    public static ResultSet dbExecuteQuery(String queryStmt) throws SQLException, ClassNotFoundException {
        //Declare statement, resultSet and CachedResultSet as null
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        CachedRowSet crs = RowSetProvider.newFactory().createCachedRowSet();
        try {
            //Connect to DB (Establish Oracle Connection)
            connection = connect();
            LOGGER.debug("Select statement: {}", queryStmt);
            //Create statement
            assert connection != null;
            statement = connection.createStatement();
            //Execute select (query) operation
            resultSet = statement.executeQuery(queryStmt);
            crs.populate(resultSet);
        } catch (Exception e) {
            LOGGER.error("Problem occurred at executeUpdate operation : {}", e.getMessage());
            throw e;
        } finally {
            closeQuietly(connection);
            closeQuietly(statement);
            closeQuietly(resultSet);
        }
        //Return CachedRowSet
        return crs;
    }

    //DB Execute Update (For Update/Insert/Delete) Operation
    public static void dbExecuteUpdate(String updateStmt) throws SQLException, ClassNotFoundException {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = connect();
            assert connection != null;
            statement = connection.createStatement();
            statement.executeUpdate(updateStmt);
        } catch (Exception e) {
            LOGGER.error("Problem occurred at executeUpdate operation : {}" + e.getMessage());
            throw e;
        } finally {
            closeQuietly(connection);
            closeQuietly(statement);
        }
    }

    private static void closeQuietly(AutoCloseable object){
        try{
            object.close();
        } catch (Exception e){/* Ignored */}
    }
}
