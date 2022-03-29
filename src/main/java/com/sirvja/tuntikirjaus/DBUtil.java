package com.sirvja.tuntikirjaus;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBUtil {
    private static final String location = Objects.requireNonNull(TuntikirjausApplication.class.getResource("database/database.sqlite")).toExternalForm();

    public static void initializeTable(){
        Connection connection = null;
        Statement statement = null;
        try {
            connection = connect();
            assert connection != null;
            statement = connection.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS Tuntikirjaus(" +
                    "KELLONAIKA         DATETIME                    NOT NULL," +
                    "TOPIC              TEXT                        NOT NULL," +
                    "DURATION_ENABLED   BIT)";
            statement.executeUpdate(sql);
        } catch (Exception exception){
            Logger.getAnonymousLogger().log(Level.SEVERE,
                    LocalDateTime.now() + ": " +
                            exception);
        } finally {
            closeQuietly(connection);
            closeQuietly(statement);
        }
    }

    public static void dropTable(){
        Connection connection = null;
        Statement statement = null;
        try {
            connection = connect();
            assert connection != null;
            statement = connection.createStatement();
            String sql = "DROP TABLE IF EXISTS Tuntikirjaus";
            statement.executeUpdate(sql);
        } catch (Exception exception){
            Logger.getAnonymousLogger().log(Level.SEVERE,
                    LocalDateTime.now() + ": " +
                            exception);
        } finally {
            closeQuietly(connection);
            closeQuietly(statement);
        }
    }

    public static Connection connect() {
        String dbPrefix = "jdbc:sqlite:";
        Connection connection;
        try {
            connection = DriverManager.getConnection(dbPrefix + location);
        } catch (SQLException exception) {
            Logger.getAnonymousLogger().log(Level.SEVERE,
                    LocalDateTime.now() + ": Could not connect to SQLite DB at " +
                            location);
            return null;
        }
        return connection;
    }

    public static boolean checkDrivers() {
        try {
            Class.forName("org.sqlite.JDBC");
            DriverManager.registerDriver(new org.sqlite.JDBC());
            return true;
        } catch (ClassNotFoundException | SQLException classNotFoundException) {
            Logger.getAnonymousLogger().log(Level.SEVERE, LocalDateTime.now() + ": Could not start SQLite Drivers");
            return false;
        }
    }

    //DB Execute Query Operation
    public static ObservableList<TuntiKirjaus> getAllTuntikirjaus() {
        String query = "SELECT * FROM Tuntikirjaus";

        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        ObservableList<TuntiKirjaus> returnObject = FXCollections.observableArrayList();
        try {
            connection = connect();
            System.out.println("Select statement: " + query + "\n");
            assert connection != null;
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);

            while (resultSet.next()){
                System.out.println(String.format("%s, %s, %b",resultSet.getString("KELLONAIKA"), resultSet.getString("TOPIC"), resultSet.getString("DURATION_ENABLED")));
                returnObject.add(new TuntiKirjaus(
                        LocalDateTime.parse(resultSet.getString("KELLONAIKA"), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")),
                        resultSet.getString("TOPIC"),
                        Boolean.parseBoolean(resultSet.getString("DURATION_ENABLED"))
                ));
            }

        } catch (Exception e) {
            System.out.println("Problem occurred at executeQuery operation : " + e);
        } finally {
            closeQuietly(connection);
            closeQuietly(statement);
            closeQuietly(resultSet);
        }
        return returnObject;
    }

    //DB Execute Update (For Update/Insert/Delete) Operation
    public static void insertTuntikirjaus(TuntiKirjaus tuntiKirjaus) throws SQLException, ClassNotFoundException {
        String sqlStmt = String.format("INSERT INTO Tuntikirjaus(KELLONAIKA, TOPIC, DURATION_ENABLED) " +
                "VALUES ('%s', '%s', '%s')", tuntiKirjaus.getDateTime().toString(), tuntiKirjaus.getTopic(), tuntiKirjaus.getDurationEnabled());

        System.out.println("SQL statement: "+sqlStmt);

        Connection connection = null;
        Statement statement = null;
        try {
            connection = connect();
            assert connection != null;
            statement = connection.createStatement();
            statement.executeUpdate(sqlStmt);
        } catch (Exception e) {
            System.out.println("Problem occurred at executeUpdate operation : " + e);
            throw e;
        } finally {
            closeQuietly(connection);
            closeQuietly(statement);
        }
    }

    //DB Execute Update (For Update/Insert/Delete) Operation
    public static void dbExecuteUpdate(String sqlStmt) throws SQLException, ClassNotFoundException {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = connect();
            assert connection != null;
            statement = connection.createStatement();
            statement.executeUpdate(sqlStmt);
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
