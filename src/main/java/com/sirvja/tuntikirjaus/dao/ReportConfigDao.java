package com.sirvja.tuntikirjaus.dao;

import com.sirvja.tuntikirjaus.domain.ReportConfig;
import com.sirvja.tuntikirjaus.exception.DataAccessException;
import com.sirvja.tuntikirjaus.utils.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.sirvja.tuntikirjaus.utils.Constants.dateFormatter;

public class ReportConfigDao implements Dao<ReportConfig, Integer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportConfigDao.class);

    @Override
    public List<ReportConfig> getAllToList() {
        return executeFetchQuery("SELECT * FROM ReportConfig ORDER BY REPORT_NAME ASC");
    }

    @Override
    public List<ReportConfig> getAllFromToList(LocalDate localDate) {
        return List.of();
    }

    private List<ReportConfig> executeFetchQuery(String query, Object... params) {
        List<ReportConfig> reportConfigs = new ArrayList<>();
        try {
            ResultSet resultSet = DBUtil.dbExecuteQuery(query, params);

            while (resultSet.next()){
                reportConfigs.add(new ReportConfig(
                        resultSet.getInt("ROWID"),
                        parseDate(resultSet.getString("START_DATE")),
                        parseDate(resultSet.getString("END_DATE")),
                        resultSet.getString("SEARCH_QUERY"),
                        resultSet.getString("REPORT_NAME")
                ));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Couldn't get ReportConfigs from database", e);
        }

        return reportConfigs;
    }

    private static LocalDate parseDate(String date) {
        return date == null ? null : LocalDate.parse(date, dateFormatter);
    }

    private static String formatDate(Optional<LocalDate> date) {
        return date.map(dateFormatter::format).orElse(null);
    }

    @Override
    public ReportConfig save(ReportConfig reportConfig) {
        String query = "INSERT INTO ReportConfig(START_DATE, END_DATE, SEARCH_QUERY, REPORT_NAME) VALUES (?, ?, ?, ?) RETURNING ROWID";
        LOGGER.debug("Inserting ReportConfig: {}", reportConfig);

        try{
            ResultSet resultSet = DBUtil.dbExecuteQuery(query,
                    formatDate(reportConfig.getStartDate()),
                    formatDate(reportConfig.getEndDate()),
                    reportConfig.getSearchQuery(),
                    reportConfig.getReportName());

            if (resultSet.next()){
                reportConfig.setId(resultSet.getInt("ROWID"));
            }
        } catch (SQLException e){
            throw new DataAccessException("Couldn't save ReportConfig to database", e);
        }

        return reportConfig;
    }

    @Override
    public void update(ReportConfig reportConfig) {
        String query = "UPDATE ReportConfig SET START_DATE=?, END_DATE=?, SEARCH_QUERY=?, REPORT_NAME=? WHERE ROWID=?";
        LOGGER.debug("Updating ReportConfig: {}", reportConfig);

        try{
            DBUtil.dbExecuteUpdate(query,
                    formatDate(reportConfig.getStartDate()),
                    formatDate(reportConfig.getEndDate()),
                    reportConfig.getSearchQuery(),
                    reportConfig.getReportName(),
                    reportConfig.getId());
        } catch (SQLException e){
            throw new DataAccessException("Couldn't update ReportConfig in database", e);
        }
    }

    @Override
    public void delete(ReportConfig reportConfig) {
        LOGGER.debug("Deleting ReportConfig: {}", reportConfig);

        try{
            DBUtil.dbExecuteUpdate("DELETE FROM ReportConfig WHERE ROWID=?", reportConfig.getId());
        } catch (SQLException e){
            throw new DataAccessException("Couldn't delete ReportConfig from database", e);
        }
    }

    @Override
    public Optional<ReportConfig> get(Integer id) {
        return executeFetchQuery("SELECT * FROM ReportConfig WHERE ROWID=? LIMIT 1", id)
                .stream()
                .findFirst();
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
        } catch (SQLException e) {
            throw new DataAccessException("Couldn't initialize ReportConfig table", e);
        }
    }
}
