package com.sirvja.tuntikirjaus.dao;

import com.sirvja.tuntikirjaus.domain.Configuration;
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

public class ConfigurationDao implements Dao<Configuration, String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationDao.class);

    @Override
    public Optional<Configuration> get(String id) {
        return executeFetchQuery("SELECT * FROM Configuration WHERE CONF_KEY=? LIMIT 1", id)
                .stream()
                .findFirst();
    }

    @Override
    public List<Configuration> getAllToList() {
        return executeFetchQuery("SELECT * FROM Configuration");
    }

    @Override
    public List<Configuration> getAllFromToList(LocalDate localDate) {
        return List.of();
    }

    private List<Configuration> executeFetchQuery(String query, Object... params) {
        try {
            ResultSet resultSet = DBUtil.dbExecuteQuery(query, params);
            List<Configuration> confList = new ArrayList<>();

            while (resultSet.next()) {
                confList.add(new Configuration(
                        resultSet.getString("CONF_KEY"),
                        resultSet.getString("CONF_VALUE")
                ));
            }

            return confList;
        } catch (SQLException e) {
            throw new DataAccessException("Couldn't get Configuration from database", e);
        }
    }

    @Override
    public Configuration save(Configuration configuration) {
        LOGGER.debug("Inserting Configuration with key: {}", configuration.getKey());

        try{
            DBUtil.dbExecuteUpdate("INSERT INTO Configuration(CONF_KEY, CONF_VALUE) VALUES (?, ?)",
                    configuration.getKey(), configuration.getValue());
        } catch (SQLException e){
            throw new DataAccessException("Couldn't save Configuration to database", e);
        }

        return configuration;
    }

    @Override
    public void update(Configuration configuration) {
        LOGGER.debug("Updating Configuration with key: {}", configuration.getKey());

        try{
            DBUtil.dbExecuteUpdate("UPDATE Configuration SET CONF_VALUE=? WHERE CONF_KEY=?",
                    configuration.getValue(), configuration.getKey());
        } catch (SQLException e){
            throw new DataAccessException("Couldn't update Configuration in database", e);
        }
    }

    @Override
    public void delete(Configuration configuration) {
        LOGGER.debug("Deleting Configuration with key: {}", configuration.getKey());
        try {
            DBUtil.dbExecuteUpdate("DELETE FROM Configuration WHERE CONF_KEY=?", configuration.getKey());
        } catch (SQLException e) {
            throw new DataAccessException("Couldn't delete Configuration from database", e);
        }
    }

    public static void initializeTableIfNotExisting() {
        String sqlQuery = "CREATE TABLE IF NOT EXISTS Configuration(" +
                "CONF_KEY                TEXT                     PRIMARY KEY," +
                "CONF_VALUE              TEXT)";
        LOGGER.debug("Initializing table with sql query: {}", sqlQuery);

        try {
            DBUtil.dbExecuteUpdate(sqlQuery);
        } catch (SQLException e) {
            throw new DataAccessException("Couldn't initialize Configuration table", e);
        }
    }
}
