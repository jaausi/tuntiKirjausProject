package com.sirvja.tuntikirjaus.utils;

import com.sirvja.tuntikirjaus.TuntikirjausApplication;
import com.sirvja.tuntikirjaus.exception.DatabaseNotInitializedException;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Optional;


@Log4j2
@Service
public class DBUtil {

    @Value("${tuntikirjaus.in-memory.database.location}")
    private String databaseLocation;
    private final String databaseFileName = Constants.DATABASE_FILE_NAME;
    private Optional<Path> optionalDbFile;

    public void checkOrCreateDatabaseFile() throws IOException {
        log.debug("In check or create database file");

        if(!checkIfDatabaseExists()) {
            log.debug("No database file found");
            createDatabaseFile();
        }
        optionalDbFile = Optional.of(Paths.get(databaseLocation, databaseFileName));
    }

    private boolean createDatabaseFile() throws IOException {
        log.debug("Creating database file");
        Path databaseFilePath = Paths.get(databaseLocation, databaseFileName);
        return databaseFilePath.toFile().createNewFile();
    }

    private boolean checkIfDatabaseExists() {
        log.debug("Checking if database file exists in folder: {}", databaseLocation);
        Path databaseFilePath = Paths.get(databaseLocation, databaseFileName);
        return databaseFilePath.toFile().exists();
    }

    public Connection connect() throws DatabaseNotInitializedException {
        Path databaseFilePath = optionalDbFile.orElseThrow(() -> new DatabaseNotInitializedException("You need to initialize the database with dbUtil.checkOrCreateDatabaseFile() before calling connect"));
        String dbPrefix = "jdbc:sqlite:";
        Connection connection;
        try {
            log.debug("Connecting to database with address: {}", dbPrefix + databaseFilePath);
            connection = DriverManager.getConnection(dbPrefix + databaseFilePath);
        } catch (SQLException exception) {
            log.error("Could not connect to SQLite DB at: {}", databaseFilePath);
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
            log.error("Could not start SQLite Drivers");
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
//            connection = connect();
            log.debug("Select statement: {}", queryStmt);
            //Create statement
            assert connection != null;
            statement = connection.createStatement();
            //Execute select (query) operation
            resultSet = statement.executeQuery(queryStmt);
            crs.populate(resultSet);
        } catch (Exception e) {
            log.error("Problem occurred at executeUpdate operation : {}", e.getMessage());
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
//            connection = connect();
            assert connection != null;
            statement = connection.createStatement();
            statement.executeUpdate(updateStmt);
        } catch (Exception e) {
            log.error("Problem occurred at executeUpdate operation : {}" + e.getMessage());
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
