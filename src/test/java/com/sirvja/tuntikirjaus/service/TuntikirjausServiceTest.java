package com.sirvja.tuntikirjaus.service;

import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;
import com.sirvja.tuntikirjaus.utils.Dao;
import com.sirvja.tuntikirjaus.utils.TuntiKirjausDao;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.random.RandomGenerator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@Slf4j
public class TuntikirjausServiceTest {

    private static TuntiKirjausService tuntiKirjausService;
    private static final Dao<TuntiKirjaus> mockTuntikirjausDao = mock(TuntiKirjausDao.class);
    private static List<TuntiKirjaus> mockTuntikirjausList;


    @BeforeAll
    static void setup(){
        tuntiKirjausService = new TuntiKirjausService(mockTuntikirjausDao);
        mockTuntikirjausList = generateTuntikirjausList(5, LocalTime.of(8, 0), LocalTime.of(16, 0), Duration.ofHours(4));
    }

    @Test
    void testFetchingAllTuntikirjausForToday() {
        log.info("Tuntikirjaus list: {}", mockTuntikirjausList.toString());

        Mockito.when(mockTuntikirjausDao.getAllToList()).thenReturn(mockTuntikirjausList);

        List<TuntiKirjaus> kirjausListOfToday = tuntiKirjausService.getTuntiKirjausForDate(LocalDate.now());

        Assertions.assertTrue(mockTuntikirjausList.size() > kirjausListOfToday.size());
        Assertions.assertEquals(mockTuntikirjausList.stream()
                .filter(tuntiKirjaus -> tuntiKirjaus.getStartTime().toLocalDate().equals(LocalDate.now()))
                .toList(), kirjausListOfToday);
        verify(mockTuntikirjausDao).getAllToList();
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
