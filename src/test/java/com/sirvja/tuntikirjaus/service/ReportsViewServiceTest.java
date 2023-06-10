package com.sirvja.tuntikirjaus.service;

import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class ReportsViewServiceTest {

    static final LocalDateTime zeroDateTime = LocalDateTime.of(
            LocalDate.of(2023, 1, 1),
            LocalTime.of(0,0,0)
    );
    static ObservableList<TuntiKirjaus> tuntiKirjausList = FXCollections.observableArrayList();

    @BeforeAll
    static void setup(){
        tuntiKirjausList.add(new TuntiKirjaus(1, zeroDateTime, zeroDateTime.plusHours(1), "IBD-1 Koodaus", true)); // 1h
        tuntiKirjausList.add(new TuntiKirjaus(2, zeroDateTime.plusHours(1), zeroDateTime.plusHours(2).plusMinutes(30), "IBD-2 Katselmointi", true)); // 2h30min
        tuntiKirjausList.add(new TuntiKirjaus(2, zeroDateTime.plusHours(2).plusMinutes(30), zeroDateTime.plusHours(4), "oaw Lounas", true)); // 4h
        tuntiKirjausList.add(new TuntiKirjaus(2, zeroDateTime.plusHours(4), zeroDateTime.plusHours(6).plusMinutes(23), "IBD-3 Suunnittelu", true)); // 6h23min
        tuntiKirjausList.add(new TuntiKirjaus(2, zeroDateTime.plusHours(6).plusMinutes(23), zeroDateTime.plusHours(8).plusMinutes(23), "IBD-4 Koodaus", true)); // 8h23min
    }

    @Test
    void givenTuntikirjausList_whenGetSumOfHoursFromTuntikirjausList_thenReturnSumOfHoursInMinutes(){
        // when
        Long minutes = ReportsViewService.getSumOfHoursFromTuntikirjausList(tuntiKirjausList);

        // then
        assertEquals(503, minutes);
    }

    @Test
    void givenMinutes_whenGetHoursStringFromMinutes_thenReturnHoursInString(){
        // when
        long minutes1 = 119L; // 1h 59min
        long minutes2 = 120L; // 2h 0min
        long minutes3 = 179L; // 2h 59min

        // then
        assertEquals("1", ReportsViewService.getHoursStringFromMinutes(minutes1));
        assertEquals("2", ReportsViewService.getHoursStringFromMinutes(minutes2));
        assertEquals("2", ReportsViewService.getHoursStringFromMinutes(minutes3));
    }

    @Test
    void givenMinutes_whenGetMinutesStringFromMinutes_thenReturnHoursInString(){
        // when
        long minutes1 = 119L; // 1h 59min
        long minutes2 = 120L; // 2h 0min
        long minutes3 = 179L; // 2h 59min

        // then
        assertEquals("59", ReportsViewService.getMinutesStringFromMinutes(minutes1));
        assertEquals("0", ReportsViewService.getMinutesStringFromMinutes(minutes2));
        assertEquals("59", ReportsViewService.getMinutesStringFromMinutes(minutes3));
    }

    @Test
    void givenMinutes_whenGetHtpsStringFromMinutes_thenReturnHoursInString(){
        // when
        long minutes1 = 8 * 60 - 30; // 1htp
        long minutes2 = minutes1 * 2 + 1; // 2htp
        long minutes3 = minutes1 * 3 + 225; // 3,5htp

        // then
        assertTrue(List.of("1.0", "1,0").contains(ReportsViewService.getHtpsStringFromMinutes(minutes1)));
        assertTrue(List.of("2.0", "2,0").contains(ReportsViewService.getHtpsStringFromMinutes(minutes2)));
        assertTrue(List.of("3.5", "3,5").contains(ReportsViewService.getHtpsStringFromMinutes(minutes3)));
    }



}
