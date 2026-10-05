package com.sirvja.tuntikirjaus.dao;

import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;
import com.sirvja.tuntikirjaus.exception.DataAccessException;
import com.sirvja.tuntikirjaus.utils.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.sirvja.tuntikirjaus.utils.Constants.dateFormatter;
import static com.sirvja.tuntikirjaus.utils.Constants.dateTimeFormatter;
import static com.sirvja.tuntikirjaus.utils.DBUtil.dbExecuteQuery;

public class TuntiKirjausDao implements Dao<TuntiKirjaus, Integer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TuntiKirjausDao.class);

    @Override
    public List<TuntiKirjaus> getAllToList() {
        return executeTuntikirjausFetchQuery("SELECT * FROM Tuntikirjaus ORDER BY START_TIME ASC");
    }

    @Override
    public List<TuntiKirjaus> getAllFromToList(LocalDate localDate) {
        String queryAllFromDate = """
                SELECT * FROM Tuntikirjaus
                WHERE CAST(strftime('%s', START_TIME) AS integer) > CAST(strftime('%s', ?) AS integer)
                ORDER BY START_TIME ASC
                """;

        return executeTuntikirjausFetchQuery(queryAllFromDate, localDate.format(dateFormatter));
    }

    private List<TuntiKirjaus> executeTuntikirjausFetchQuery(String query, Object... params) {
        List<TuntiKirjaus> tuntiKirjausList = new ArrayList<>();
        try {
            ResultSet resultSet = dbExecuteQuery(query, params);

            while (resultSet.next()) {
                tuntiKirjausList.add(mapToTuntikirjaus(resultSet));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Couldn't get Tuntikirjaus' from database", e);
        }

        return tuntiKirjausList;
    }

    private static TuntiKirjaus mapToTuntikirjaus(ResultSet resultSet) throws SQLException {
        String endTime = resultSet.getString("END_TIME");
        return new TuntiKirjaus(
                resultSet.getInt("ROWID"),
                LocalDateTime.parse(resultSet.getString("START_TIME"), dateTimeFormatter),
                endTime == null ? null : LocalDateTime.parse(endTime, dateTimeFormatter),
                resultSet.getString("TOPIC"),
                resultSet.getBoolean("IS_REMOTE")
        );
    }

    private static String formatEndTime(TuntiKirjaus tuntiKirjaus) {
        return tuntiKirjaus.getEndTime().map(dateTimeFormatter::format).orElse(null);
    }

    @Override
    public TuntiKirjaus save(TuntiKirjaus tuntiKirjaus) {
        String query = "INSERT INTO Tuntikirjaus(START_TIME, END_TIME, TOPIC, IS_REMOTE) VALUES (?, ?, ?, ?) RETURNING ROWID";
        LOGGER.debug("Inserting Tuntikirjaus: {}", tuntiKirjaus.toLogString());

        try{
            ResultSet resultSet = dbExecuteQuery(query,
                    tuntiKirjaus.getStartTime().format(dateTimeFormatter),
                    formatEndTime(tuntiKirjaus),
                    tuntiKirjaus.getTopic(),
                    tuntiKirjaus.isRemote());

            if (resultSet.next()){
                tuntiKirjaus.setId(resultSet.getInt("ROWID"));
            }
        } catch (SQLException e){
            throw new DataAccessException("Couldn't save Tuntikirjaus to database", e);
        }

        return tuntiKirjaus;
    }

    @Override
    public void update(TuntiKirjaus tuntiKirjaus) {
        String query = "UPDATE Tuntikirjaus SET START_TIME=?, END_TIME=?, TOPIC=?, IS_REMOTE=? WHERE ROWID=?";
        LOGGER.debug("Updating Tuntikirjaus: {}", tuntiKirjaus.toLogString());

        try{
            DBUtil.dbExecuteUpdate(query,
                    tuntiKirjaus.getStartTime().format(dateTimeFormatter),
                    formatEndTime(tuntiKirjaus),
                    tuntiKirjaus.getTopic(),
                    tuntiKirjaus.isRemote(),
                    tuntiKirjaus.getId());
        } catch (SQLException e){
            throw new DataAccessException("Couldn't update Tuntikirjaus in database", e);
        }
    }

    @Override
    public void delete(TuntiKirjaus tuntiKirjaus) {
        LOGGER.debug("Deleting Tuntikirjaus: {}", tuntiKirjaus.toLogString());

        try{
            DBUtil.dbExecuteUpdate("DELETE FROM Tuntikirjaus WHERE ROWID=?", tuntiKirjaus.getId());
        } catch (SQLException e){
            throw new DataAccessException("Couldn't delete Tuntikirjaus from database", e);
        }
    }

    @Override
    public Optional<TuntiKirjaus> get(Integer id) {
        return executeTuntikirjausFetchQuery("SELECT * FROM Tuntikirjaus WHERE ROWID=? LIMIT 1", id)
                .stream()
                .findFirst();
    }

    public static void initializeTableIfNotExisting() {
        String sqlQuery = "CREATE TABLE IF NOT EXISTS Tuntikirjaus(" +
                "ROWID              INTEGER                     PRIMARY KEY," +
                "START_TIME         TEXT                        NOT NULL," +
                "END_TIME           TEXT                                ," +
                "TOPIC              TEXT                        NOT NULL," +
                "IS_REMOTE          INTEGER                     NOT NULL DEFAULT 0)";
        LOGGER.debug("Initializing table with sql query: {}", sqlQuery);

        try {
            DBUtil.dbExecuteUpdate(sqlQuery);
        } catch (SQLException e) {
            throw new DataAccessException("Couldn't initialize Tuntikirjaus table", e);
        }
    }
}
