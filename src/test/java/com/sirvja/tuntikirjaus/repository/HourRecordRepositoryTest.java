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
import org.springframework.jdbc.core.RowMapper;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;

@SpringBootTest(classes = HourRecordRepositoryInMemoryImpl.class)
public class HourRecordRepositoryTest {
    @Autowired
    private HourRecordRepository hourRecordRepository;
    @MockBean
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Test that hourRecord can be fetched")
    void testThatHourRecordCanBeFetched() {
        HourRecord hourRecord = new HourRecord(1, LocalDateTime.now().minusHours(2), LocalDateTime.now(), "Test topic", true);
        Mockito.when(jdbcTemplate.queryForObject(any(), any(Class.class), ArgumentMatchers.eq(1))).thenReturn(hourRecord);

        Assertions.assertEquals(hourRecord, hourRecordRepository.get(1).orElse(null));
    }
}
