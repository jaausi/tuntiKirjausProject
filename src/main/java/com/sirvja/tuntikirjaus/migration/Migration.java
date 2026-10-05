package com.sirvja.tuntikirjaus.migration;

import com.sirvja.tuntikirjaus.exception.DataAccessException;
import com.sirvja.tuntikirjaus.utils.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public record Migration (String name, String checkSql, String migrationSql) {
    private static final Logger LOGGER = LoggerFactory.getLogger(Migration.class);

    public void run() {
        LOGGER.debug("Checking migration '{}'", name);
        try {
            LOGGER.debug("Checking if the migration was already applied to the database");
            ResultSet resultSet = DBUtil.dbExecuteQuery(checkSql);
            resultSet.next();
            if(resultSet.getInt("is_run") == 0) {
                LOGGER.info("Applying migration '{}'", name);
                List<String> statements = Arrays.stream(migrationSql.split(";"))
                        .map(String::trim)
                        .filter(statement -> !statement.isEmpty())
                        .toList();
                // All statements in one transaction so a failure can't leave the schema half migrated
                DBUtil.dbExecuteInTransaction(statements);
                LOGGER.info("Migration '{}' applied", name);
            } else {
                LOGGER.debug("Migration was already applied to the database, skipping...");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Migration '" + name + "' failed", e);
        }
    }
}
