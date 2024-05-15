package com.sirvja.tuntikirjaus.utils;

import com.sirvja.tuntikirjaus.model.HourRecord;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static com.sirvja.tuntikirjaus.utils.Constants.dateFormatter;
import static com.sirvja.tuntikirjaus.utils.Constants.dateTimeFormatter;

public class TuntiKirjausDao implements Dao<HourRecord> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TuntiKirjausDao.class);

    @Override
    public Optional<ObservableList<HourRecord>> getAll() {
        return getAllInternal(Optional.empty());
    }

    @Override
    public Optional<ObservableList<HourRecord>> getAllFrom(LocalDate localDate) {
        return getAllInternal(Optional.of(localDate));
    }

    public Optional<ObservableList<HourRecord>> getAllInternal(Optional<LocalDate> optionalLocalDate) {
        String query = optionalLocalDate
                .map(localDate ->
                        "SELECT * FROM Tuntikirjaus WHERE CAST(strftime('%s', START_TIME)  AS  integer) > CAST(strftime('%s', '" +
                                localDate.format(dateFormatter) + "')  AS  integer) ORDER BY START_TIME ASC;")
                .orElse("SELECT * FROM Tuntikirjaus ORDER BY START_TIME ASC");
        //String query = "SELECT * FROM Tuntikirjaus ORDER BY START_TIME ASC";
        LOGGER.debug("Query: {}", query);

        ObservableList<HourRecord> returnObject = FXCollections.observableArrayList();
        try {
            ResultSet resultSet = DBUtil.dbExecuteQuery(query);

            while (resultSet.next()){
                LOGGER.debug(String.format("%s, %s, %s, %s, %b",resultSet.getInt("ROWID"), resultSet.getString("START_TIME"),resultSet.getString("END_TIME"), resultSet.getString("TOPIC"), resultSet.getString("DURATION_ENABLED")));
                String endTimeString = resultSet.getString("END_TIME");
                Optional<String> endTime = Optional.ofNullable(endTimeString.isEmpty() || endTimeString.equals("null") ? null : endTimeString);
                LocalDateTime localEndDateTime = endTime.map(s -> LocalDateTime.parse(s, dateTimeFormatter)).orElse(null);
                returnObject.add(
                        new HourRecord(
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
    public HourRecord save(HourRecord hourRecord) {
        String query = String.format("INSERT INTO Tuntikirjaus(START_TIME, END_TIME, TOPIC, DURATION_ENABLED) " +
                "VALUES ('%s', '%s', '%s', '%b') " +
                "RETURNING ROWID", hourRecord.getStartTime().format(dateTimeFormatter), hourRecord.getEndTime().map(localDateTime -> localDateTime.format(dateTimeFormatter)).orElse(null), hourRecord.getTopic(), hourRecord.isDurationEnabled());
        LOGGER.debug("Inserting Tuntikirjaus with sql query: {}", query);

        try{
            ResultSet resultSet = DBUtil.dbExecuteQuery(query);

            while (resultSet.next()){
                LOGGER.debug(String.format("%s",resultSet.getInt("ROWID")));
                hourRecord.setId(resultSet.getInt("ROWID"));
            }
        } catch (SQLException | ClassNotFoundException e){
            LOGGER.error("Couldn't save Tuntikirjaus to database: {}", e.getMessage());
        }

        return hourRecord;
    }

    @Override
    public void update(HourRecord hourRecord) {
        String query = String.format("UPDATE Tuntikirjaus " +
                "SET START_TIME='%s', END_TIME='%s', TOPIC='%s', DURATION_ENABLED='%b' " +
                "WHERE ROWID=%s", hourRecord.getStartTime().format(dateTimeFormatter), hourRecord.getEndTime().map(localDateTime -> localDateTime.format(dateTimeFormatter)).orElse(null), hourRecord.getTopic(), hourRecord.isDurationEnabled(), hourRecord.getId());
        LOGGER.debug("Updating Tuntikirjaus with sql query: {}", query);

        try{
            DBUtil.dbExecuteUpdate(query);
        } catch (SQLException | ClassNotFoundException e){
            LOGGER.error("Couldn't save Tuntikirjaus to database: {}", e.getMessage());
        }
    }

    @Override
    public void delete(HourRecord hourRecord) {
        String query = String.format("DELETE FROM Tuntikirjaus " +
                "WHERE ROWID=%s", hourRecord.getId());
        LOGGER.debug("Deleting Tuntikirjaus with sql query: {}", query);

        try{
            DBUtil.dbExecuteUpdate(query);
        } catch (SQLException | ClassNotFoundException e){
            LOGGER.error("Couldn't save Tuntikirjaus to database: {}", e.getMessage());
        }
    }

    @Override
    public Optional<HourRecord> get(int id) {
        String query = String.format("SELECT * FROM Tuntikirjaus WHERE ROWID=%s LIMIT 1", id);
        LOGGER.debug("Trying to find tuntikirjaus with sql query: {}", query);

        HourRecord hourRecord = null;
        try {
            ResultSet resultSet = DBUtil.dbExecuteQuery(query);

            while (resultSet.next()){
                LOGGER.debug(String.format("%s, %s, %s, %s, %b",resultSet.getInt("ROWID"), resultSet.getDate("START_TIME"),resultSet.getDate("END_TIME"), resultSet.getString("TOPIC"), resultSet.getBoolean("DURATION_ENABLED")));
                hourRecord = new HourRecord(
                        resultSet.getInt("ROWID"),
                        LocalDateTime.parse(resultSet.getString("START_TIME"), dateTimeFormatter),
                        LocalDateTime.parse(resultSet.getString("END_TIME"), dateTimeFormatter),
                        resultSet.getString("TOPIC"),
                        Boolean.parseBoolean(resultSet.getString("DURATION_ENABLED"))
                );
            }
        } catch (SQLException | ClassNotFoundException e) {
            LOGGER.error("Couldn't get all Tuntikirjaus' from database: {}", e.getMessage());
        }

        return Optional.ofNullable(hourRecord);
    }

    public static void initializeTableIfNotExisting() {
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
