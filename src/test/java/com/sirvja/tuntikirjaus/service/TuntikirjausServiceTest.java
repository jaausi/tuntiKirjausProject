package com.sirvja.tuntikirjaus.service;

import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;
import com.sirvja.tuntikirjaus.dao.Dao;
import com.sirvja.tuntikirjaus.dao.TuntiKirjausDao;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.random.RandomGenerator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TuntikirjausServiceTest {

    private final Dao<TuntiKirjaus> tuntikirjausDao = mock(TuntiKirjausDao.class);
    private TuntiKirjausService tuntiKirjausService;
    private List<TuntiKirjaus> tuntikirjausList;

    private static final Logger log = LoggerFactory.getLogger(TuntiKirjausDao.class);


    @BeforeEach
    void setup(){
        tuntikirjausList = generateTuntikirjausList(5, LocalTime.of(8, 0), LocalTime.of(16, 0), Duration.ofHours(4));
        when(tuntikirjausDao.getAllFromToList(any())).thenReturn(tuntikirjausList);
        when(tuntikirjausDao.getAllToList()).thenReturn(tuntikirjausList);
        tuntiKirjausService = new TuntiKirjausService(tuntikirjausDao, true);
    }

    @Test
    @DisplayName("Test to fetch all tuntikirjaus for date")
    void testFetchingAllTuntikirjausForDate() {
        List<TuntiKirjaus> kirjausListOfToday = tuntiKirjausService.getTuntiKirjausForDate(LocalDate.now());

        assertTrue(tuntikirjausList.size() > kirjausListOfToday.size(), "List of all tuntikirjaus events should be bigger that list of tuntikirjaus events of one day");
        assertEquals(tuntikirjausList.stream()
                .filter(tuntiKirjaus -> tuntiKirjaus.getStartTime().toLocalDate().equals(LocalDate.now()))
                .toList(), kirjausListOfToday, "Tuntikirjaus list for date should match to the list created manually from original list");
        verify(tuntikirjausDao, times(1)).getAllFromToList(any());
    }

    @Test
    @DisplayName("Test to fetch all tuntikirjaus with cache, should call database only once")
    void testFetchingAllTuntikirjausWithCache() {
        assertEquals(tuntikirjausList, tuntiKirjausService.getAllTuntikirjaus(), "Tuntikirjaus list should match the whole list");
        verify(tuntikirjausDao, times(1)).getAllFromToList(any());
        tuntiKirjausService.getAllTuntikirjaus();
        verify(tuntikirjausDao, times(1)).getAllFromToList(any());
    }

    @Test
    @DisplayName("Test to fetch all tuntikirjaus without cache, should call database everytime")
    void testFetchingAllTuntikirjausWithoutCache() {
        tuntiKirjausService = new TuntiKirjausService(tuntikirjausDao, false);
        assertEquals(tuntikirjausList, tuntiKirjausService.getAllTuntikirjaus(), "Tuntikirjaus list should match the whole list");
        verify(tuntikirjausDao, times(1)).getAllFromToList(any());
        tuntiKirjausService.getAllTuntikirjaus();
        verify(tuntikirjausDao, times(2)).getAllFromToList(any());
    }

    @Test
    @DisplayName("Test to fetch all tuntikirjaus with cache, should call database only once, and then after cache is cleared")
    void testFetchingAllTuntikirjausWithCacheAndClearingCache() {
        tuntiKirjausService.getAllTuntikirjaus();
        verify(tuntikirjausDao, times(1)).getAllFromToList(any());
        tuntiKirjausService.getAllTuntikirjaus();
        verify(tuntikirjausDao, times(1)).getAllFromToList(any());
        tuntiKirjausService.clearTuntikirjausCache();
        tuntiKirjausService.getAllTuntikirjaus();
        verify(tuntikirjausDao, times(2)).getAllFromToList(any());
    }

    @Test
    @DisplayName("Test to fetch all tuntikirjaus with no limit")
    void testFetchingAllTuntikirjausWithoutLimit() {
        assertEquals(tuntikirjausList, tuntiKirjausService.getAllTuntikirjausWithoutLimit(), "Tuntikirjaus list should match the whole list");
        verify(tuntikirjausDao, times(1)).getAllToList();
        tuntiKirjausService.getAllTuntikirjausWithoutLimit();
        verify(tuntikirjausDao, times(2)).getAllToList();
    }

    @Test
    @DisplayName("Test to save new Tuntikirjaus")
    void testSavingTuntikirjaus() {
        TuntiKirjaus lastTuntikirjaus = tuntikirjausList.getLast();
        TuntiKirjaus tuntiKirjaus = new TuntiKirjaus(lastTuntikirjaus.getEndTime().get(), null, "Manual test kirjaus", true);
        TuntiKirjaus tuntiKirjausWithId = new TuntiKirjaus(lastTuntikirjaus.getId() + 1, tuntiKirjaus.getStartTime(), tuntiKirjaus.getEndTime().orElse(null), tuntiKirjaus.getTopic(), tuntiKirjaus.isDurationEnabled());
        when(tuntikirjausDao.save(eq(tuntiKirjaus))).thenReturn(tuntiKirjausWithId);

        assertEquals(tuntiKirjausWithId, tuntiKirjausService.save(tuntiKirjaus), "Saving should return tuntikirjaus with id");
        verify(tuntikirjausDao, times(1)).save(any());
    }

    @Test
    @DisplayName("Test to delete Tuntikirjaus")
    void testDeletingTuntikirjaus() {
        TuntiKirjaus lastTuntikirjaus = tuntikirjausList.getLast();
        doNothing().when(tuntikirjausDao).delete(eq(lastTuntikirjaus));

        assertDoesNotThrow(() -> tuntiKirjausService.delete(lastTuntikirjaus), "Deleting of tuntikirjaus shouldn't throw exception");
        verify(tuntikirjausDao, times(1)).delete(any());
    }

    @Test
    @DisplayName("Test to update Tuntikirjaus")
    void testUpdatingTuntikirjaus() {
        TuntiKirjaus lastTuntikirjaus = tuntikirjausList.getLast();
        doNothing().when(tuntikirjausDao).update(eq(lastTuntikirjaus));

        assertDoesNotThrow(() -> tuntiKirjausService.update(lastTuntikirjaus), "Updating of tuntikirjaus shouldn't throw exception");
        verify(tuntikirjausDao, times(1)).update(any());
    }

    /*********** HELPER FUNCTIONS ***********/
    private static List<TuntiKirjaus> generateTuntikirjausList(int amountOfDays, LocalTime startTime, LocalTime endTime, final Duration maxDuration){
        log.info("starttime: {} endtime: {}", startTime, endTime);
        assert startTime.isBefore(endTime);
        final RandomGenerator randomGenerator = RandomGenerator.getDefault();
        Function<LocalTime, LocalTime> getNextEndTime = st -> st.plus(Duration.ofMinutes(randomGenerator.nextLong(maxDuration.toMinutes())));

        LocalDate currentDate = LocalDate.now();
        List<TuntiKirjaus> tuntiKirjausList = new ArrayList<>();
        LocalTime tempTime = startTime;
        int counter = 0;
        for (int i = 0; i < amountOfDays; i++) {
            currentDate = currentDate.plusDays(i);
            do {
                LocalTime startTimeOfKirjaus = tempTime;
                tempTime = getNextEndTime.apply(startTimeOfKirjaus);
                tuntiKirjausList.add(new TuntiKirjaus(
                        counter,
                        LocalDateTime.of(currentDate, startTimeOfKirjaus),
                        LocalDateTime.of(currentDate, tempTime),
                        String.format("Topic of kirjaus %s", counter),
                        true
                                ));
                counter++;
            } while (tempTime.isBefore(endTime));
            tempTime = startTime;
        }

        return tuntiKirjausList;
    }
}
