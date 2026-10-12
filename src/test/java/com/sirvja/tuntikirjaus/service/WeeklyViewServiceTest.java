package com.sirvja.tuntikirjaus.service;

import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WeeklyViewServiceTest {
    WeeklyViewService weeklyViewService = new WeeklyViewService(new IncidentService());

    LocalDateTime zeroTime = LocalDateTime.parse("2026-01-05T08:00:00");

    @Test
    @DisplayName("Test that lunches and breaks are not counted in weekly working time")
    void testWorkingMinutesExcludeLunchAndBreaks() {
        List<TuntiKirjaus> tuntiKirjausList = List.of(
                new TuntiKirjaus(0, zeroTime, zeroTime.plusHours(1), "Topic 1", false),
                new TuntiKirjaus(0, zeroTime.plusHours(1), zeroTime.plusHours(1).plusMinutes(15), "Tauko", false),
                new TuntiKirjaus(0, zeroTime.plusHours(1).plusMinutes(15), zeroTime.plusHours(3), "Topic 2", false),
                new TuntiKirjaus(0, zeroTime.plusHours(3), zeroTime.plusHours(3).plusMinutes(30), "Lounas", false),
                new TuntiKirjaus(0, zeroTime.plusHours(3).plusMinutes(30), zeroTime.plusHours(5), "Topic 1", false),
                new TuntiKirjaus(0, zeroTime.plusDays(1), zeroTime.plusDays(1).plusHours(2), "Topic 3", true),
                new TuntiKirjaus(0, zeroTime.plusDays(1).plusHours(2), zeroTime.plusDays(1).plusHours(2).plusMinutes(10), "tauko", true),
                new TuntiKirjaus(0, zeroTime.plusDays(1).plusHours(2).plusMinutes(10), zeroTime.plusDays(1).plusHours(4), "Topic 3", true)
        );

        // Day 1: 60 + 105 + 90 = 255 min, day 2: 120 + 110 = 230 min
        assertEquals(485, weeklyViewService.getWorkingMinutes(tuntiKirjausList));
    }
}
