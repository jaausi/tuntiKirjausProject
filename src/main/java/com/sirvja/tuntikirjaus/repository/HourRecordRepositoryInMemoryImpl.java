package com.sirvja.tuntikirjaus.repository;

import com.sirvja.tuntikirjaus.model.HourRecord;
import lombok.extern.log4j.Log4j2;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.sirvja.tuntikirjaus.utils.Constants.dateTimeFormatter;

@Log4j2
@Repository
public class HourRecordRepositoryInMemoryImpl implements HourRecordRepository {
    private final JdbcTemplate jdbcTemplate;

    public HourRecordRepositoryInMemoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<HourRecord> get(int id) {
        log.debug("Fetching HourRecord with id: {}, from in memory database", id);
        String selectWithIdQuery = "SELECT * FROM Tuntikirjaus WHERE id=?";
        HourRecord hourRecord = jdbcTemplate.queryForObject(selectWithIdQuery, HourRecord.class, id);
        return Optional.ofNullable(hourRecord);
    }

    @Override
    public List<HourRecord> getAll() {
        String selectAllQuery = "SELECT * FROM Tuntikirjaus ORDER BY START_TIME ASC";
        return jdbcTemplate.query(selectAllQuery, HourRecord::of);
    }

    @Override
    public List<HourRecord> getAllFrom(LocalDate localDate) {
        String selectAllFromDateQuery = "SELECT * FROM Tuntikirjaus WHERE START_TIME > ? ORDER BY START_TIME ASC";
        return jdbcTemplate.query(selectAllFromDateQuery, HourRecord::of, localDate);
    }

    @Override
    public HourRecord save(HourRecord hourRecord) {
        GeneratedKeyHolder idHolder = new GeneratedKeyHolder();
        String insertQuery = """
                INSERT INTO Tuntikirjaus(START_TIME, END_TIME, TOPIC, DURATION_ENABLED)
                VALUES (?, ?, ?, ?)
                """;
        jdbcTemplate.update(
                insertQuery,
                hourRecord.getStartTime().format(dateTimeFormatter),
                hourRecord.getEndTime().map(localDateTime -> localDateTime.format(dateTimeFormatter)).orElse(null),
                hourRecord.getTopic(),
                hourRecord.isDurationEnabled(),
                idHolder
        );

        int id = ((Number) Objects.requireNonNull(idHolder.getKeys()).get("ROWID")).intValue();

        return HourRecord.of(id, hourRecord);
    }

    @Override
    public void update(HourRecord hourRecord) {
        String updateQuery = """
                UPDATE Tuntikirjaus
                SET START_TIME = '?', END_TIME = '?', TOPIC = '?', DURATION_ENABLED = '?'
                WHERE ROWID = '?'
                LIMIT 1
                """;
        jdbcTemplate.update(
                updateQuery,
                hourRecord.getStartTime().format(dateTimeFormatter),
                hourRecord.getEndTime().map(localDateTime -> localDateTime.format(dateTimeFormatter)).orElse(null),
                hourRecord.getTopic(),
                hourRecord.isDurationEnabled(),
                hourRecord.getId()
        );
    }

    @Override
    public void delete(HourRecord hourRecord) {
        String deleteQuery = """
                DELETE FROM Tuntikirjaus
                WHERE ROWID = ?
                LIMIT 1
                """;
        jdbcTemplate.update(deleteQuery, hourRecord.getId());
    }
}
