package com.sirvja.tuntikirjaus.repository;

import com.sirvja.tuntikirjaus.model.HourRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;

import static com.sirvja.tuntikirjaus.utils.Constants.dateTimeFormatter;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest(classes = {JdbcTemplate.class, HourRecordRepositoryInMemoryImpl.class})
public class HourRecordRepositoryIT {
    @Autowired
    private HourRecordRepository hourRecordRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Test that hourRecord can be fetched")
    void testThatHourRecordCanBeFetched() {

        HourRecord hourRecord = new HourRecord(1, LocalDateTime.now().minusHours(2), LocalDateTime.now(), "Test topic", true);
        jdbcTemplate.update(
                "INSERT INTO Tuntikirjaus(START_TIME, END_TIME, TOPIC, DURATION_ENABLED) VALUES (?,?,?,?)",
                hourRecord.getStartTime().format(dateTimeFormatter),
                hourRecord.getEndTime().map(localDateTime -> localDateTime.format(dateTimeFormatter)).orElse(null),
                hourRecord.getTopic(),
                hourRecord.isDurationEnabled()
        );

        Assertions.assertEquals(hourRecord, hourRecordRepository.get(1).orElse(null));
    }
}
