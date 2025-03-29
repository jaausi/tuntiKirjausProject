package com.sirvja.tuntikirjaus.service;

import com.sirvja.tuntikirjaus.TestUtils;
import com.sirvja.tuntikirjaus.dao.InMemoryMainViewDAO;
import com.sirvja.tuntikirjaus.exception.HourRecordNotFoundException;
import com.sirvja.tuntikirjaus.model.HourRecord;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@ExtendWith(MockitoExtension.class)
public class NewMainViewServiceTest {

    @Mock
    InMemoryMainViewDAO inMemoryMainViewDAO;

    @InjectMocks
    NewMainViewService mainViewService;


    @Test
    @DisplayName("Test getting current date")
    void testGetCurrentDate() {
        LocalDate today = LocalDate.now();

        Mockito.when(inMemoryMainViewDAO.getCurrentDate()).thenReturn(Optional.of(today));

        Assertions.assertEquals(today, mainViewService.getCurrentDate().get());
    }

    @Test
    @DisplayName("Test setting current date")
    void testSetCurrentDate() {
        LocalDate today = LocalDate.now();

        Mockito.doNothing().when(inMemoryMainViewDAO).setCurrentDate(today);

        Assertions.assertDoesNotThrow(() -> mainViewService.setCurrentDate(today));
        Mockito.verify(inMemoryMainViewDAO, Mockito.atLeastOnce()).setCurrentDate(Mockito.any(LocalDate.class));
    }

    @Test
    @DisplayName("Test updating hour record")
    void testUpdateHourRecord() throws HourRecordNotFoundException {
        Mockito.doNothing().when(inMemoryMainViewDAO).updateHourRecord(Mockito.any(HourRecord.class));

        Assertions.assertDoesNotThrow(() -> mainViewService.updateHourRecord(TestUtils.generateHourRecord(1, LocalDateTime.now())));
    }

    @Test
    @DisplayName("Test updating hour record, that's missing")
    void testUpdateHourRecordMissing() throws HourRecordNotFoundException {
        Mockito.doThrow(HourRecordNotFoundException.class).when(inMemoryMainViewDAO).updateHourRecord(Mockito.any(HourRecord.class));

        Assertions.assertThrows(HourRecordNotFoundException.class, () -> mainViewService.updateHourRecord(TestUtils.generateHourRecord(1, LocalDateTime.now())));
    }

}
