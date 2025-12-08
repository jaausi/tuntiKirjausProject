package com.sirvja.tuntikirjaus.service;

import com.sirvja.tuntikirjaus.domain.Paiva;
import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;
import com.sirvja.tuntikirjaus.exception.EmptyTopicException;
import com.sirvja.tuntikirjaus.exception.MalformatedTimeException;
import com.sirvja.tuntikirjaus.exception.StartTimeNotAfterLastTuntikirjausException;
import com.sirvja.tuntikirjaus.exception.TuntikirjausDatabaseInInconsistentStage;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class MainViewServiceTest {

    TuntiKirjausService tuntiKirjausService = mock(TuntiKirjausService.class);
    AlertService alertService = mock(AlertService.class);
    MainViewService mainViewService = new MainViewService(tuntiKirjausService, alertService);

    static final LocalDateTime zeroDateTime = LocalDateTime.of(
            LocalDate.of(2023, 1, 1),
            LocalTime.of(0,0,0)
    );

    @Test
    @DisplayName("Test that the Tuntikirjaus list is wrapped as ObservableArrayList and sorted by startTime")
    void testGettingTuntiDataForTable(){
        LocalDateTime time = LocalDateTime.now().getHour() > 18 ? LocalDateTime.now().minusHours(8) : LocalDateTime.now();
        List<TuntiKirjaus> tuntiKirjausListOrdered = List.of(
                new TuntiKirjaus(1, time, time.plusHours(1), "Topic 1", true),
                new TuntiKirjaus(2, time.plusHours(1), time.plusHours(1).plusMinutes(30), "Topic 1", true),
                new TuntiKirjaus(3, time.plusHours(1).plusMinutes(30), time.plusHours(3), "Topic 1", true),
                new TuntiKirjaus(4, time.plusHours(3), time.plusHours(4), "Topic 1", true),
                new TuntiKirjaus(5, time.plusHours(4), time.plusHours(4).plusHours(20), "Topic 1", true)
        );
        List<TuntiKirjaus> tuntiKirjausListUnOrdered = List.of(
                new TuntiKirjaus(1, time.plusHours(3), time.plusHours(4), "Topic 1", true),
                new TuntiKirjaus(2, time, time.plusHours(1), "Topic 1", true),
                new TuntiKirjaus(3, time.plusHours(1), time.plusHours(1).plusMinutes(30), "Topic 1", true),
                new TuntiKirjaus(4, time.plusHours(1).plusMinutes(30), time.plusHours(3), "Topic 1", true),
                new TuntiKirjaus(5, time.plusHours(4), time.plusHours(4).plusHours(20), "Topic 1", true)
                );

        when(tuntiKirjausService.getTuntiKirjausForDate(eq(LocalDate.now()))).thenReturn(tuntiKirjausListOrdered);
        assertEquals(SortedList.class, mainViewService.getTuntiDataForTable().getClass());
        ObservableList<TuntiKirjaus> responseList = mainViewService.getTuntiDataForTable();
        for (int i = 0; i < tuntiKirjausListOrdered.size(); i++) {
            assertEquals(tuntiKirjausListOrdered.get(i), responseList.get(i), "Element of the ordered list is not matching the expected element");
        }

        when(tuntiKirjausService.getTuntiKirjausForDate(eq(LocalDate.now()))).thenReturn(tuntiKirjausListUnOrdered);
        responseList = mainViewService.getTuntiDataForTable();
        List<Integer> correctOrder = List.of(1, 2, 3, 0, 4);
        for (int i = 0; i < tuntiKirjausListOrdered.size(); i++) {
            assertEquals(tuntiKirjausListUnOrdered.get(correctOrder.get(i)), responseList.get(i), "Element of the unordered list is not matching the expected element");
        }
    }

    @Test
    @DisplayName("Test that the Paiva list is wrapped as ObservableArrayList and sorted by localDate")
    void testGettingPaivaDataForTable() {
        LocalDateTime time = LocalDateTime.now().getHour() > 16 ? LocalDateTime.now().minusHours(10) : LocalDateTime.now();
        List<TuntiKirjaus> tuntiKirjausListOrdered = List.of(
                new TuntiKirjaus(1, time, time.plusHours(1), "Topic 1", true),
                new TuntiKirjaus(2, time.plusHours(1), time.plusHours(1).plusMinutes(30), "Topic 1", true),
                new TuntiKirjaus(3, time.plusHours(1).plusMinutes(30), time.plusHours(3), "Topic 1", true),
                new TuntiKirjaus(4, time.plusHours(3), time.plusHours(4), "Topic 1", true),
                new TuntiKirjaus(5, time.plusHours(4), time.plusHours(4).plusHours(20), "Topic 1", true),
                new TuntiKirjaus(6, time=time.plusDays(1), time.plusHours(1), "Topic 1", true),
                new TuntiKirjaus(7, time.plusHours(1), time.plusHours(1).plusMinutes(30), "Topic 1", true),
                new TuntiKirjaus(8, time.plusHours(1).plusMinutes(30), time.plusHours(3), "Topic 1", true),
                new TuntiKirjaus(9, time.plusHours(3), time.plusHours(4), "Topic 1", true),
                new TuntiKirjaus(10, time.plusHours(4), time.plusHours(4).plusHours(20), "Topic 1", true),
                new TuntiKirjaus(11, time=time.plusDays(1), time.plusHours(1), "Topic 1", true),
                new TuntiKirjaus(12, time.plusHours(1), time.plusHours(1).plusMinutes(30), "Topic 1", true),
                new TuntiKirjaus(13, time.plusHours(1).plusMinutes(30), time.plusHours(3), "Topic 1", true),
                new TuntiKirjaus(14, time.plusHours(3), time.plusHours(4), "Topic 1", true),
                new TuntiKirjaus(15, time.plusHours(4), time.plusHours(4).plusHours(20), "Topic 1", true)
        );
        time = LocalDateTime.now().getHour() > 16 ? LocalDateTime.now().minusHours(10) : LocalDateTime.now();
        List<TuntiKirjaus> tuntiKirjausListUnOrdered = List.of(
                new TuntiKirjaus(1, time.plusHours(3), time.plusHours(4), "Topic 1", true),
                new TuntiKirjaus(2, time, time.plusHours(1), "Topic 1", true),
                new TuntiKirjaus(3, time.plusHours(1), time.plusHours(1).plusMinutes(30), "Topic 1", true),
                new TuntiKirjaus(4, time.plusHours(1).plusMinutes(30), time.plusHours(3), "Topic 1", true),
                new TuntiKirjaus(5, time.plusHours(4), time.plusHours(4).plusHours(20), "Topic 1", true),
                new TuntiKirjaus(6, time=time.plusDays(2).plusHours(3), time.plusHours(4), "Topic 1", true),
                new TuntiKirjaus(7, time, time.plusHours(1), "Topic 1", true),
                new TuntiKirjaus(8, time.plusHours(1), time.plusHours(1).plusMinutes(30), "Topic 1", true),
                new TuntiKirjaus(9, time.plusHours(1).plusMinutes(30), time.plusHours(3), "Topic 1", true),
                new TuntiKirjaus(10, time.plusHours(4), time.plusHours(4).plusHours(20), "Topic 1", true),
                new TuntiKirjaus(11, time=time.minusDays(1).plusHours(3), time.plusHours(4), "Topic 1", true),
                new TuntiKirjaus(12, time, time.plusHours(1), "Topic 1", true),
                new TuntiKirjaus(13, time.plusHours(1), time.plusHours(1).plusMinutes(30), "Topic 1", true),
                new TuntiKirjaus(14, time.plusHours(1).plusMinutes(30), time.plusHours(3), "Topic 1", true),
                new TuntiKirjaus(15, time.plusHours(4), time.plusHours(4).plusHours(20), "Topic 1", true)
        );
        System.out.println(tuntiKirjausListUnOrdered);
        when(tuntiKirjausService.getAllTuntikirjaus()).thenReturn(tuntiKirjausListOrdered);
        List<Paiva> expectedResult = List.of(
                new Paiva(LocalDate.now().plusDays(2)),
                new Paiva(LocalDate.now().plusDays(1)),
                new Paiva(LocalDate.now())
        );

        ObservableList<Paiva> actualResult = mainViewService.getPaivaDataForTable();
        for (int i = 0; i < expectedResult.size(); i++) {
            assertEquals(expectedResult.get(i).getLocalDate(), actualResult.get(i).getLocalDate(), "Element of the ordered list is not matching the expected element");
        }

        when(tuntiKirjausService.getAllTuntikirjaus()).thenReturn(tuntiKirjausListUnOrdered);
        actualResult = mainViewService.getPaivaDataForTable();
        assertEquals(expectedResult, actualResult, "Unordered lists should match");
        for (int i = 0; i < expectedResult.size(); i++) {
            assertEquals(expectedResult.get(i).getLocalDate(), actualResult.get(i).getLocalDate(), "Element of the unordered list is not matching the expected element");
        }
    }

    private LocalDateTime getInitTime() {
        return LocalDateTime.now().getHour() > 16 ? LocalDateTime.now().minusHours(10) : LocalDateTime.now();
    }

    private List<TuntiKirjaus> getTuntikirjausList(LocalDateTime time) {
        return List.of(
                new TuntiKirjaus(1, time, time.plusHours(1), "Topic 1", true),
                new TuntiKirjaus(2, time.plusHours(1), time.plusHours(1).plusMinutes(30), "Topic 1", true),
                new TuntiKirjaus(3, time.plusHours(1).plusMinutes(30), time.plusHours(3), "Topic 1", true),
                new TuntiKirjaus(4, time.plusHours(3), time.plusHours(4), "Topic 1", true),
                new TuntiKirjaus(5, time.plusHours(4), null, "Topic 1", true)
        );
    }


    @Test
    @DisplayName("Test adding of Tuntikirjaus items, should save Tuntikirjaus and update the previous one")
    void testAddingTuntikirjaus() {
        LocalDateTime time = getInitTime();
        List<TuntiKirjaus> tuntiKirjausList = getTuntikirjausList(time);
        when(tuntiKirjausService.getTuntiKirjausForDate(any())).thenReturn(tuntiKirjausList);
        assertDoesNotThrow(() -> mainViewService.addTuntikirjaus(String.valueOf(time.plusHours(5).getHour()), "Test topic"));
        verify(tuntiKirjausService, times(1)).save(any());
        verify(tuntiKirjausService, times(1)).update(any());
    }

    @Test
    @DisplayName("Test adding of Tuntikirjaus item when no existing tuntikirjaus for date")
    void testAddingTuntikirjaus_noData() {
        LocalDateTime time = getInitTime();
        List<TuntiKirjaus> tuntiKirjausList = List.of();
        when(tuntiKirjausService.getTuntiKirjausForDate(any())).thenReturn(tuntiKirjausList);
        assertDoesNotThrow(() -> mainViewService.addTuntikirjaus(String.valueOf(time.plusHours(5).getHour()), "Test topic"));
        verify(tuntiKirjausService, times(1)).save(any());
        verify(tuntiKirjausService, times(0)).update(any());
    }

    @Test
    @DisplayName("Test adding of Tuntikirjaus item when endTime exists in second last kirjaus, should throw exception")
    void testAddingTuntikirjaus_endTimeExists() {
        LocalDateTime time = getInitTime();
        List<TuntiKirjaus> tuntiKirjausList = getTuntikirjausList(time);
        tuntiKirjausList.getLast().setEndTime(time.plusHours(5));
        when(tuntiKirjausService.getTuntiKirjausForDate(any())).thenReturn(tuntiKirjausList);
        assertThrows(TuntikirjausDatabaseInInconsistentStage.class, () -> mainViewService.addTuntikirjaus(String.valueOf(time.plusHours(5).getHour()), "Test topic"));
        verify(tuntiKirjausService, times(0)).save(any());
        verify(tuntiKirjausService, times(0)).update(any());
    }

    @Test
    @DisplayName("Test adding of Tuntikirjaus item when startTime is before previous kirjaus, should throw exception")
    void testAddingTuntikirjaus_startTimeBeforePrevious() {
        LocalDateTime time = getInitTime();
        List<TuntiKirjaus> tuntiKirjausList = getTuntikirjausList(time);
        when(tuntiKirjausService.getTuntiKirjausForDate(any())).thenReturn(tuntiKirjausList);
        assertThrows(StartTimeNotAfterLastTuntikirjausException.class, () -> mainViewService.addTuntikirjaus(String.valueOf(time.plusHours(3).getHour()), "Test topic"));
        verify(tuntiKirjausService, times(0)).save(any());
        verify(tuntiKirjausService, times(0)).update(any());
    }

    @Test
    @DisplayName("Test adding of Tuntikirjaus item when topic is empty, should throw EmptyTopicException")
    void testAddingTuntikirjaus_topicEmpty() {
        LocalDateTime time = getInitTime();
        List<TuntiKirjaus> tuntiKirjausList = getTuntikirjausList(time);
        when(tuntiKirjausService.getTuntiKirjausForDate(any())).thenReturn(tuntiKirjausList);
        assertThrows(EmptyTopicException.class, () -> mainViewService.addTuntikirjaus(String.valueOf(time.plusHours(5).getHour()), ""));
        assertThrows(EmptyTopicException.class, () -> mainViewService.addTuntikirjaus(String.valueOf(time.plusHours(5).getHour()), "  "));
        assertThrows(EmptyTopicException.class, () -> mainViewService.addTuntikirjaus(String.valueOf(time.plusHours(5).getHour()), null));
        verify(tuntiKirjausService, times(0)).save(any());
        verify(tuntiKirjausService, times(0)).update(any());
    }

    private List<TuntiKirjaus> getTuntikirjausList() {
        LocalDateTime time = getInitTime();
        return getTuntikirjausList(time);
    }

    @Test
    @DisplayName("Test adding of Tuntikirjaus item when time is malformed")
    void testAddingTuntikirjaus_timeInWrongFormat() {
        List<TuntiKirjaus> tuntiKirjausList = getTuntikirjausList();
        when(tuntiKirjausService.getTuntiKirjausForDate(any())).thenReturn(tuntiKirjausList);
        assertThrows(MalformatedTimeException.class, () -> mainViewService.addTuntikirjaus("999", "Test topic"));
        assertThrows(MalformatedTimeException.class, () -> mainViewService.addTuntikirjaus("123242", "Test topic"));
        verify(tuntiKirjausService, times(0)).save(any());
        verify(tuntiKirjausService, times(0)).update(any());
    }

    @Test
    @DisplayName("Test removing of Tuntikirjaus item from the end, check that the second last Tuntikirjaus is updated")
    void testRemovingTuntikirjaus_end() {
        LocalDateTime time = getInitTime();
        List<TuntiKirjaus> tuntiKirjausList = getTuntikirjausList(time);
        when(tuntiKirjausService.getTuntiKirjausForDate(any())).thenReturn(tuntiKirjausList);
        mainViewService.removeTuntikirjaus(tuntiKirjausList.getLast());
        verify(tuntiKirjausService, times(1)).delete(eq(tuntiKirjausList.getLast()));
        verify(tuntiKirjausService, times(1)).update(eq(tuntiKirjausList.get(tuntiKirjausList.size()-2)));
    }

    @Test
    @DisplayName("Test removing of Tuntikirjaus item from the middle, check that the previous and following Tuntikirjaus is updated")
    void testRemovingTuntikirjaus_middle() {
        LocalDateTime time = getInitTime();
        List<TuntiKirjaus> tuntiKirjausList = getTuntikirjausList(time);
        when(tuntiKirjausService.getTuntiKirjausForDate(any())).thenReturn(tuntiKirjausList);
        mainViewService.removeTuntikirjaus(tuntiKirjausList.get(tuntiKirjausList.size()-2));
        verify(tuntiKirjausService, times(1)).delete(eq(tuntiKirjausList.get(tuntiKirjausList.size()-2)));
        verify(tuntiKirjausService, times(2)).update(any());
        verify(tuntiKirjausService, times(1)).update(eq(tuntiKirjausList.getLast()));
        verify(tuntiKirjausService, times(1)).update(eq(tuntiKirjausList.get(tuntiKirjausList.size()-3)));
    }

    @Test
    @DisplayName("Test removing of Tuntikirjaus item from the middle, check that previous or following Tuntikirjaus is not updated")
    void testRemovingTuntikirjaus_first() {
        LocalDateTime time = getInitTime();
        List<TuntiKirjaus> tuntiKirjausList = getTuntikirjausList(time);
        when(tuntiKirjausService.getTuntiKirjausForDate(any())).thenReturn(tuntiKirjausList);
        mainViewService.removeTuntikirjaus(tuntiKirjausList.getFirst());
        verify(tuntiKirjausService, times(1)).delete(eq(tuntiKirjausList.getFirst()));
        verify(tuntiKirjausService, times(0)).update(any());
    }

    @Test
    @DisplayName("Test parse time from string method with different inputs")
    void testParseTimeFromStringMethod() throws MalformatedTimeException {
        mainViewService.setCurrentDate(zeroDateTime.toLocalDate());

        LocalDateTime expectedTime = zeroDateTime.plusHours(9);
        assertEquals(expectedTime, mainViewService.parseTimeFromString("9:00"), "failed to parse 9:00");
        assertEquals(expectedTime, mainViewService.parseTimeFromString("9.00"), "failed to parse 9.00");
        assertEquals(expectedTime, mainViewService.parseTimeFromString("9"), "failed to parse 9");
        assertEquals(expectedTime, mainViewService.parseTimeFromString("900"), "failed to parse 900");
        assertThrows(MalformatedTimeException.class, () -> mainViewService.parseTimeFromString("90"), "parsing of 90 should throw exception");
        assertThrows(MalformatedTimeException.class, () -> mainViewService.parseTimeFromString("90:00"), "parsing of 90:00 should throw exception");
        assertThrows(MalformatedTimeException.class, () -> mainViewService.parseTimeFromString("90.00"), "parsing of 90.00 should throw exception");
        assertThrows(MalformatedTimeException.class, () -> mainViewService.parseTimeFromString("9:000"), "parsing of 9:000 should throw exception");
        assertThrows(MalformatedTimeException.class, () -> mainViewService.parseTimeFromString("9.000"), "parsing of 9.000 should throw exception");

        expectedTime = zeroDateTime.plusHours(14).plusMinutes(23);
        assertEquals(expectedTime, mainViewService.parseTimeFromString("14:23"), "failed to parse 14:23");
        assertEquals(expectedTime, mainViewService.parseTimeFromString("1423"), "failed to parse 1423");
        assertEquals(expectedTime, mainViewService.parseTimeFromString("14.23"), "failed to parse 14.23");
        assertThrows(MalformatedTimeException.class, () -> mainViewService.parseTimeFromString("14.230"), "parsing of 14.230 should throw exception");
        assertThrows(MalformatedTimeException.class, () -> mainViewService.parseTimeFromString("14:230"), "parsing of 14:230 should throw exception");

        assertEquals(0, mainViewService.parseTimeFromString("").getSecond(), "parsing empty string should return 0 seconds");
        assertEquals(0, mainViewService.parseTimeFromString("").getNano(), "parsing empty string should return 0 nanoseconds");
    }

    @Test
    @DisplayName("Test getting the summary from TuntikirjausData, should check that summary contains projects in alphabetical order and hours for each project correctly")
    void testGetYhteenvetoText() {
        List<TuntiKirjaus> tuntiKirjausList = List.of(
                new TuntiKirjaus(1, zeroDateTime, zeroDateTime.plusHours(1), "PROJECT1-001 Tehtävä 001", true),
                new TuntiKirjaus(2, zeroDateTime.plusHours(1), zeroDateTime.plusHours(1).plusMinutes(30), "PROJECT1-002 Tehtävä 002", true),
                new TuntiKirjaus(3, zeroDateTime.plusHours(1).plusMinutes(30), zeroDateTime.plusHours(3), "PROJECT2-003 Tehtävä 003", true),
                new TuntiKirjaus(4, zeroDateTime.plusHours(3), zeroDateTime.plusHours(4), "PROJECT3-004 Tehtävä 004", true),
                new TuntiKirjaus(4, zeroDateTime.plusHours(4), zeroDateTime.plusHours(4).plusMinutes(30), "AAPROJECT3-004 Tehtävä 004", true),
                new TuntiKirjaus(5, zeroDateTime.plusHours(4).plusMinutes(30), null, "PROJECT2-005 Tehtävä 005", true)
        );

        when(tuntiKirjausService.getTuntiKirjausForDate(any())).thenReturn(tuntiKirjausList);

        String expectedSummary = """
                AAPROJECT3: 0:30
                PROJECT1: 1:30
                PROJECT2: 1:30
                PROJECT3: 1:00
                """;

        assertEquals(expectedSummary, mainViewService.getYhteenvetoText(), "Processed summary is not matching with the expected summary");
    }

    @Test
    @DisplayName("Test getting the topic entries from TuntikirjausData, check that list is in alphabethical order")
    void testGetAiheEntries() {
        List<TuntiKirjaus> tuntiKirjausList = List.of(
                new TuntiKirjaus(1, zeroDateTime, zeroDateTime.plusHours(1), "PROJECT1-001 Tehtävä 001", true),
                new TuntiKirjaus(2, zeroDateTime.plusHours(1), zeroDateTime.plusHours(1).plusMinutes(30), "PROJECT1-002 Tehtävä 002", true),
                new TuntiKirjaus(3, zeroDateTime.plusHours(1).plusMinutes(30), zeroDateTime.plusHours(3), "PROJECT2-003 Tehtävä 003", true),
                new TuntiKirjaus(4, zeroDateTime.plusHours(3), zeroDateTime.plusHours(4), "PROJECT3-004 Tehtävä 004", true),
                new TuntiKirjaus(4, zeroDateTime.plusHours(4), zeroDateTime.plusHours(4).plusMinutes(30), "AAPROJECT3-004 Tehtävä 004", true),
                new TuntiKirjaus(5, zeroDateTime.plusHours(4).plusMinutes(30), null, "PROJECT2-005 Tehtävä 005", true)
        );

        when(tuntiKirjausService.getAllTuntikirjaus()).thenReturn(tuntiKirjausList);

        List<String> expectedList = List.of(
                "AAPROJECT3-004 Tehtävä 004",
                "PROJECT1-001 Tehtävä 001",
                "PROJECT1-002 Tehtävä 002",
                "PROJECT2-003 Tehtävä 003",
                "PROJECT2-005 Tehtävä 005",
                "PROJECT3-004 Tehtävä 004"
        );

        SortedSet<String> actualList = mainViewService.getAiheEntries();

        for (int i = 0; i < expectedList.size(); i++) {
            assertEquals(expectedList.get(i), actualList.removeFirst(), "Expected element should match the actual element");
        }
    }

    @Test
    @DisplayName("Test getting the topic entries from TuntikirjausData, check that empty list works as should")
    void testGetAiheEntries_empty() {
        when(tuntiKirjausService.getAllTuntikirjaus()).thenReturn(List.of());

        assertEquals(new TreeSet<>(Set.of()), mainViewService.getAiheEntries());
    }
}
