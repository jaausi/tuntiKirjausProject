package com.sirvja.tuntikirjaus.utils;

import com.sirvja.tuntikirjaus.TuntikirjausApplication;
import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DBUtil {
    private static final String location = Objects.requireNonNull(TuntikirjausApplication.class.getResource("database/tuntikirjaus.db")).toExternalForm();
    private static final Logger LOGGER = LoggerFactory.getLogger(DBUtil.class);

    public static Connection connect() {
        String dbPrefix = "jdbc:sqlite:";
        Connection connection;
        try {
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
            System.out.println("Select statement: " + queryStmt + "\n");
            //Create statement
            assert connection != null;
            statement = connection.createStatement();
            //Execute select (query) operation
            resultSet = statement.executeQuery(queryStmt);
            crs.populate(resultSet);
        } catch (Exception e) {
            System.out.println("Problem occurred at executeUpdate operation : " + e);
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
            System.out.println("Problem occurred at executeUpdate operation : " + e);
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
