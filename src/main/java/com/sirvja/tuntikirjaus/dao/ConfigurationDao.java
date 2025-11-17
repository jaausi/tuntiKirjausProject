package com.sirvja.tuntikirjaus.dao;

import com.sirvja.tuntikirjaus.domain.Configuration;
import com.sirvja.tuntikirjaus.utils.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConfigurationDao implements Dao<Configuration, String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationDao.class);
    @Override
    public Optional<Configuration> get(String id) {
        String query = String.format("SELECT * FROM Configuration WHERE CONF_KEY='%s' LIMIT 1", id);
        LOGGER.debug("Trying to find configuration with sql query: {}", query);

        Configuration configuration = null;
        try {
            ResultSet resultSet = DBUtil.dbExecuteQuery(query);

            while (resultSet.next()){
                LOGGER.debug(String.format("%s, %s", resultSet.getString("CONF_KEY"), resultSet.getString("CONF_VALUE")));
                configuration = new Configuration(resultSet.getString("CONF_KEY"), resultSet.getString("CONF_VALUE"));
            }
        } catch (SQLException | ClassNotFoundException e) {
            LOGGER.error("Couldn't get all Configuration' from database: {}", e.getMessage());
        }

        return Optional.ofNullable(configuration);
    }

    @Override
    public List<Configuration> getAllToList() {
        String queryAll = "SELECT * FROM Configuration";

        try {
            ResultSet resultSet = DBUtil.dbExecuteQuery(queryAll);
            List<Configuration> confList = new ArrayList<>();

            while (resultSet.next()) {
                confList.add(new Configuration(
                        resultSet.getString("CONF_KEY"),
                        resultSet.getString("CONF_VALUE")
                ));
            }

            return confList;
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public List<Configuration> getAllFromToList(LocalDate localDate) {
        return List.of();
    }

    @Override
    public Configuration save(Configuration configuration) {
        String query = String.format("INSERT INTO Configuration(CONF_KEY, CONF_VALUE) " +
                "VALUES ('%s', '%s') ", configuration.getKey(), configuration.getValue());
        LOGGER.debug("Inserting Configuration with sql query: {}", query);

        try{
            DBUtil.dbExecuteUpdate(query);
        } catch (SQLException | ClassNotFoundException e){
            LOGGER.error("Couldn't save Configuration to database: {}", e.getMessage());
        }

        return configuration;
    }

    @Override
    public void update(Configuration configuration) {
        String query = """
                UPDATE Configuration
                SET CONF_VALUE='%s'
                WHERE CONF_KEY='%s'
                """;
        query = String.format(query, configuration.getValue(), configuration.getKey());
        LOGGER.debug("Updating Configuration with sql query: {}", query);

        try{
            DBUtil.dbExecuteUpdate(query);
        } catch (SQLException | ClassNotFoundException e){
            LOGGER.error("Couldn't save Configuration to database: {}", e.getMessage());
        }
    }

    @Override
    public void delete(Configuration configuration) {

    }

    public static void initializeTableIfNotExisting() {
        String sqlQuery = "CREATE TABLE IF NOT EXISTS Configuration(" +
                "CONF_KEY                TEXT                     PRIMARY KEY," +
                "CONF_VALUE              TEXT)";
        LOGGER.debug("Initializing table with sql query: {}", sqlQuery);

        try {
            DBUtil.dbExecuteUpdate(sqlQuery);
        } catch (SQLException | ClassNotFoundException e) {
            LOGGER.error("Couldn't initialize table: {}", e.getMessage());
        }
    }

    public static boolean dropTable() {
        String sqlQuery = "DROP TABLE IF EXISTS Configuration";
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
