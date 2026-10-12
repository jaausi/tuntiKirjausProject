package com.sirvja.tuntikirjaus.service;

import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class MainViewServiceEditHandlerTest {

    TuntiKirjausService tuntiKirjausService = mock(TuntiKirjausService.class);
    AlertService alertService = mock(AlertService.class);
    MainViewService mainViewService = new MainViewService(tuntiKirjausService, alertService);

    static final LocalDate DATE = LocalDate.of(2023, 1, 1);

    TuntiKirjaus first;
    TuntiKirjaus second;
    TuntiKirjaus third;

    @BeforeEach
    void setUp() {
        mainViewService.setCurrentDate(DATE);
        first = new TuntiKirjaus(1, at(8, 0), at(9, 0), "Topic 1", false);
        second = new TuntiKirjaus(2, at(9, 0), at(10, 0), "Topic 2", false);
        third = new TuntiKirjaus(3, at(10, 0), null, "Topic 3", false);
    }

    private static LocalDateTime at(int hour, int minute) {
        return LocalDateTime.of(DATE, LocalTime.of(hour, minute));
    }

    @Test
    @DisplayName("Editing the time saves the edited kirjaus and the end time of the previous one, and updates the shown duration")
    void testEditKellonaika() {
        mainViewService.editKellonaika(List.of(first, second, third), 1, LocalTime.of(9, 30));

        assertEquals(at(9, 30), second.getStartTime());
        assertEquals(LocalTime.of(9, 30), second.timeProperty().get());
        assertEquals(at(9, 30), first.getEndTime().orElseThrow());
        assertEquals("1:30", first.durationStringProperty().get());
        verify(tuntiKirjausService).update(first);
        verify(tuntiKirjausService).update(second);
    }

    @Test
    @DisplayName("Editing the time after the next kirjaus start is rejected")
    void testEditKellonaika_afterNext() {
        mainViewService.editKellonaika(List.of(first, second, third), 1, LocalTime.of(10, 30));

        assertEquals(at(9, 0), second.getStartTime());
        verify(alertService).showNotCorrectTimeAlert(true);
        verify(tuntiKirjausService, never()).update(any());
    }

    @Test
    @DisplayName("Unparseable time (null from the converter) saves nothing")
    void testEditKellonaika_unparseable() {
        mainViewService.editKellonaika(List.of(first, second, third), 1, null);

        assertEquals(at(9, 0), second.getStartTime());
        verify(tuntiKirjausService, never()).update(any());
    }

    @Test
    @DisplayName("Editing the topic saves it and returns the old topic")
    void testEditAihe() {
        String oldTopic = mainViewService.editAihe(first, "New topic");

        assertEquals("Topic 1", oldTopic);
        assertEquals("New topic", first.getTopic());
        assertEquals("New topic", first.topicProperty().get());
        verify(tuntiKirjausService).update(first);
    }
}
