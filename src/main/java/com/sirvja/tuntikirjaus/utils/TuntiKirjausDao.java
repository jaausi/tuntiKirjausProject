package com.sirvja.tuntikirjaus.utils;

import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class TuntiKirjausDao implements Dao<TuntiKirjaus> {

    public static final boolean DROP_TABLE_ON_START = true;

    private static final Logger LOGGER = LoggerFactory.getLogger(TuntiKirjausDao.class);
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Override
    public Optional<ObservableList<TuntiKirjaus>> getAll() {
        String query = "SELECT * FROM Tuntikirjaus ORDER BY START_TIME ASC";

        ObservableList<TuntiKirjaus> returnObject = FXCollections.observableArrayList();
        try {
            ResultSet resultSet = DBUtil.dbExecuteQuery(query);

            while (resultSet.next()){
                LOGGER.debug(String.format("%s, %s, %s, %s, %b",resultSet.getInt("ROWID"), resultSet.getString("START_TIME"),resultSet.getString("END_TIME"), resultSet.getString("TOPIC"), resultSet.getString("DURATION_ENABLED")));
                String endTimeString = resultSet.getString("END_TIME");
                Optional<String> endTime = Optional.ofNullable(endTimeString.isEmpty() || endTimeString.equals("null") ? null : endTimeString);
                LocalDateTime localEndDateTime = endTime.map(s -> LocalDateTime.parse(s, dateTimeFormatter)).orElse(null);
                returnObject.add(
                        new TuntiKirjaus(
                                resultSet.getInt("ROWID"),
                                LocalDateTime.parse(resultSet.getString("START_TIME"), dateTimeFormatter),
                                localEndDateTime,
                                resultSet.getString("TOPIC"),
                                Boolean.parseBoolean(resultSet.getString("DURATION_ENABLED"))
                        )
                );
            }
        } catch (SQLException | ClassNotFoundException e) {
            LOGGER.error("Couldn't get all Tuntikirjaus' from database: {}", e.getMessage());
            return Optional.empty();
        }

        return Optional.of(returnObject);
    }

    @Override
    public TuntiKirjaus save(TuntiKirjaus tuntiKirjaus) {
        String query = String.format("INSERT INTO Tuntikirjaus(START_TIME, END_TIME, TOPIC, DURATION_ENABLED) " +
                "VALUES ('%s', '%s', '%s', '%b') " +
                "RETURNING ROWID", tuntiKirjaus.getStartTime().format(dateTimeFormatter), tuntiKirjaus.getEndTime().map(localDateTime -> localDateTime.format(dateTimeFormatter)).orElse(null), tuntiKirjaus.getTopic(), tuntiKirjaus.isDurationEnabled());
        LOGGER.debug("Inserting Tuntikirjaus with sql query: {}", query);

        try{
            ResultSet resultSet = DBUtil.dbExecuteQuery(query);

            while (resultSet.next()){
                LOGGER.debug(String.format("%s",resultSet.getInt("ROWID")));
                tuntiKirjaus.setId(resultSet.getInt("ROWID"));
            }
        } catch (SQLException | ClassNotFoundException e){
            LOGGER.error("Couldn't save Tuntikirjaus to database: {}", e.getMessage());
        }

        return tuntiKirjaus;
    }

    @Override
    public void update(TuntiKirjaus tuntiKirjaus) {
        String query = String.format("UPDATE Tuntikirjaus " +
                "SET START_TIME='%s', END_TIME='%s', TOPIC='%s', DURATION_ENABLED='%b' " +
                "WHERE ROWID=%s", tuntiKirjaus.getStartTime().format(dateTimeFormatter), tuntiKirjaus.getEndTime().map(localDateTime -> localDateTime.format(dateTimeFormatter)).orElse(null), tuntiKirjaus.getTopic(), tuntiKirjaus.isDurationEnabled(), tuntiKirjaus.getId());
        LOGGER.debug("Updating Tuntikirjaus with sql query: {}", query);

        try{
            DBUtil.dbExecuteUpdate(query);
        } catch (SQLException | ClassNotFoundException e){
            LOGGER.error("Couldn't save Tuntikirjaus to database: {}", e.getMessage());
        }
    }

    @Override
    public void delete(TuntiKirjaus tuntiKirjaus) {
        String query = String.format("DELETE FROM Tuntikirjaus " +
                "WHERE ROWID='%s' LIMIT 1", tuntiKirjaus.getId());
        LOGGER.debug("Deleting Tuntikirjaus with sql query: {}", query);

        try{
            DBUtil.dbExecuteUpdate(query);
        } catch (SQLException | ClassNotFoundException e){
            LOGGER.error("Couldn't save Tuntikirjaus to database: {}", e.getMessage());
        }
    }

    @Override
    public Optional<TuntiKirjaus> get(int id) {
        String query = String.format("SELECT * FROM Tuntikirjaus WHERE ROWID=%s LIMIT 1", id);
        LOGGER.debug("Trying to find tuntikirjaus with sql query: {}", query);

        TuntiKirjaus tuntiKirjaus = null;
        try {
            ResultSet resultSet = DBUtil.dbExecuteQuery(query);

            while (resultSet.next()){
                LOGGER.debug(String.format("%s, %s, %s, %s, %b",resultSet.getInt("ROWID"), resultSet.getDate("START_TIME"),resultSet.getDate("END_TIME"), resultSet.getString("TOPIC"), resultSet.getBoolean("DURATION_ENABLED")));
                tuntiKirjaus = new TuntiKirjaus(
                        resultSet.getInt("ROWID"),
                        LocalDateTime.parse(resultSet.getString("START_TIME"), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")),
                        LocalDateTime.parse(resultSet.getString("END_TIME"), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")),
                        resultSet.getString("TOPIC"),
                        Boolean.parseBoolean(resultSet.getString("DURATION_ENABLED"))
                );
            }
        } catch (SQLException | ClassNotFoundException e) {
            LOGGER.error("Couldn't get all Tuntikirjaus' from database: {}", e.getMessage());
        }

        return Optional.ofNullable(tuntiKirjaus);
    }

    public static void initializeTable() {
        String sqlQuery = "CREATE TABLE IF NOT EXISTS Tuntikirjaus(" +
                "ROWID              INTEGER                     PRIMARY KEY," +
                "START_TIME         TEXT                        NOT NULL," +
                "END_TIME           TEXT                                ," +
                "TOPIC              TEXT                        NOT NULL," +
                "DURATION_ENABLED   INTEGER                     NOT NULL)";
        LOGGER.debug("Initializing table with sql query: {}", sqlQuery);

        try {
            DBUtil.dbExecuteUpdate(sqlQuery);
        } catch (SQLException | ClassNotFoundException e) {
            LOGGER.error("Couldn't initialize table: {}", e.getMessage());
        }
    }

    public static boolean dropTable() {
        String sqlQuery = "DROP TABLE IF EXISTS Tuntikirjaus";
        LOGGER.debug("Dropping table with sql query: {}", sqlQuery);

        try {
            DBUtil.dbExecuteUpdate(sqlQuery);
            return true;
        } catch (SQLException | ClassNotFoundException e) {
            LOGGER.error("Couldn't drop table: {}", e.getMessage());
            return false;
        }
    }
}
