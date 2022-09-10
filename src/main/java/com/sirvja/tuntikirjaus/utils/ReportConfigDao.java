package com.sirvja.tuntikirjaus.utils;

import com.sirvja.tuntikirjaus.domain.ReportConfig;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static com.sirvja.tuntikirjaus.utils.Constants.dateFormatter;

public class ReportConfigDao implements Dao<ReportConfig> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportConfigDao.class);

    @Override
    public Optional<ObservableList<ReportConfig>> getAll() {
        return getAllInternal(Optional.empty());
    }

    @Override
    public Optional<ObservableList<ReportConfig>> getAllFrom(LocalDate localDate) {
        return getAllInternal(Optional.of(localDate));
    }

    public Optional<ObservableList<ReportConfig>> getAllInternal(Optional<LocalDate> optionalLocalDate) {
        String query = "SELECT * FROM ReportConfig ORDER BY REPORT_NAME ASC";
        LOGGER.debug("Query: {}", query);
        ObservableList<ReportConfig> returnObject = FXCollections.observableArrayList();
        try {
            ResultSet resultSet = DBUtil.dbExecuteQuery(query);

            while (resultSet.next()){
                LOGGER.debug(String.format("%s, %s, %s, %s, %s",resultSet.getInt("ROWID"), resultSet.getString("START_DATE"),resultSet.getString("END_DATE"), resultSet.getString("SEARCH_QUERY"), resultSet.getString("REPORT_NAME")));
                returnObject.add(
                        new ReportConfig(
                                resultSet.getInt("ROWID"),
                                ! "null".equals(resultSet.getString("START_DATE")) ? LocalDate.parse(resultSet.getString("START_DATE"), dateFormatter) : null,
                                ! "null".equals(resultSet.getString("END_DATE")) ? LocalDate.parse(resultSet.getString("END_DATE"), dateFormatter) : null,
                                resultSet.getString("SEARCH_QUERY"),
                                resultSet.getString("REPORT_NAME")
                        )
                );
            }
        } catch (SQLException | ClassNotFoundException e) {
            LOGGER.error("Couldn't get all ReportConfig' from database: {}", e.getMessage());
            return Optional.empty();
        }

        return Optional.of(returnObject);
    }


    @Override
    public ReportConfig save(ReportConfig reportConfig) {
        String query = String.format("INSERT INTO ReportConfig(START_DATE, END_DATE, SEARCH_QUERY, REPORT_NAME) " +
                "VALUES ('%s', '%s', '%s', '%s') " +
                "RETURNING ROWID", reportConfig.getStartDate().map(localDate -> localDate.format(dateFormatter)).orElse(null), reportConfig.getEndDate().map(localDate -> localDate.format(dateFormatter)).orElse(null), reportConfig.getSearchQuery(), reportConfig.getReportName());
        LOGGER.debug("Inserting ReportConfig with sql query: {}", query);

        try{
            ResultSet resultSet = DBUtil.dbExecuteQuery(query);

            while (resultSet.next()){
                LOGGER.debug(String.format("%s",resultSet.getInt("ROWID")));
                reportConfig.setId(resultSet.getInt("ROWID"));
            }
        } catch (SQLException | ClassNotFoundException e){
            LOGGER.error("Couldn't save ReportConfig to database: {}", e.getMessage());
        }

        return reportConfig;
    }

    @Override
    public void update(ReportConfig reportConfig) {
        String query = String.format("UPDATE ReportConfig " +
                "SET START_DATE='%s', END_DATE='%s', SEARCH_QUERY='%s', REPORT_NAME='%s' " +
                "WHERE ROWID=%s", reportConfig.getStartDate().map(localDate -> localDate.format(dateFormatter)).orElse(null), reportConfig.getEndDate().map(localDate -> localDate.format(dateFormatter)).orElse(null), reportConfig.getSearchQuery(), reportConfig.getReportName(), reportConfig.getId());
        LOGGER.debug("Updating ReportConfig with sql query: {}", query);

        try{
            DBUtil.dbExecuteUpdate(query);
        } catch (SQLException | ClassNotFoundException e){
            LOGGER.error("Couldn't save ReportConfig to database: {}", e.getMessage());
        }
    }

    @Override
    public void delete(ReportConfig reportConfig) {
        String query = String.format("DELETE FROM ReportConfig " +
                "WHERE ROWID='%s' LIMIT 1", reportConfig.getId());
        LOGGER.debug("Deleting ReportConfig with sql query: {}", query);

        try{
            DBUtil.dbExecuteUpdate(query);
        } catch (SQLException | ClassNotFoundException e){
            LOGGER.error("Couldn't save ReportConfig to database: {}", e.getMessage());
        }
    }

    @Override
    public Optional<ReportConfig> get(int id) {
        String query = String.format("SELECT * FROM ReportConfig WHERE ROWID=%s LIMIT 1", id);
        LOGGER.debug("Trying to find tuntikirjaus with sql query: {}", query);

        ReportConfig reportConfig = null;
        try {
            ResultSet resultSet = DBUtil.dbExecuteQuery(query);

            while (resultSet.next()){
                LOGGER.debug(String.format("%s, %s, %s, %s, %b",resultSet.getInt("ROWID"), resultSet.getDate("START_TIME"),resultSet.getDate("END_TIME"), resultSet.getString("TOPIC"), resultSet.getBoolean("DURATION_ENABLED")));
                reportConfig = new ReportConfig(
                        resultSet.getInt("ROWID"),
                        LocalDate.parse(resultSet.getString("START_DATE"), dateFormatter),
                        LocalDate.parse(resultSet.getString("END_DATE"), dateFormatter),
                        resultSet.getString("SEARCH_QUERY"),
                        resultSet.getString("REPORT_NAME")
                );
            }
        } catch (SQLException | ClassNotFoundException e) {
            LOGGER.error("Couldn't get all ReportConfig' from database: {}", e.getMessage());
        }

        return Optional.ofNullable(reportConfig);
    }

    public static void initializeTableIfNotExisting() {
        String sqlQuery = "CREATE TABLE IF NOT EXISTS ReportConfig(" +
                "ROWID              INTEGER                     PRIMARY KEY," +
                "START_DATE         TEXT                                ," +
                "END_DATE           TEXT                                ," +
                "SEARCH_QUERY       TEXT                        NOT NULL," +
                "REPORT_NAME        TEXT                     NOT NULL)";
        LOGGER.debug("Initializing table with sql query: {}", sqlQuery);

        try {
            DBUtil.dbExecuteUpdate(sqlQuery);
        } catch (SQLException | ClassNotFoundException e) {
            LOGGER.error("Couldn't initialize table: {}", e.getMessage());
        }
    }

    public static boolean dropTable() {
        String sqlQuery = "DROP TABLE IF EXISTS ReportConfig";
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
