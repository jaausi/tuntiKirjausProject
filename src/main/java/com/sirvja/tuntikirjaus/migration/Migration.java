package com.sirvja.tuntikirjaus.migration;

import com.sirvja.tuntikirjaus.utils.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

public record Migration (String name, String checkSql, String migrationSql) {
    private static final Logger LOGGER = LoggerFactory.getLogger(Migration.class);
    public void run() {
        LOGGER.info("Running migration '{}'", name);
        try {
            LOGGER.debug("Checking if the migration was already applied to the database");
            ResultSet resultSet = DBUtil.dbExecuteQuery(checkSql);
            resultSet.next();
            if(resultSet.getInt("is_run") == 0) {
                LOGGER.debug("Migration was not applied to the database, applying now...");
                DBUtil.dbExecuteUpdate(migrationSql());
            } else {
                LOGGER.debug("Migration was already applied to the database, skipping...");
            }
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
