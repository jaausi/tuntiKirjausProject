package com.sirvja.tuntikirjaus.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class WeeklyViewServiceTest {

    private final WeeklyViewService weeklyViewService = new WeeklyViewService(new IncidentService());

    @Test
    @DisplayName("Test that a week without finished tuntikirjaus entries gives empty incidents instead of failing")
    void testParseWeeklyIncidentsWithEmptyList() {
        assertTrue(weeklyViewService.parseWeeklyIncidents(List.of()).isEmpty());
        assertTrue(weeklyViewService.parseWeeklyIncidents(null).isEmpty());
    }

    @Test
    @DisplayName("Test that a week without finished tuntikirjaus entries gives empty project hours")
    void testMapTuntikirjausListToWeeklyProjectHoursWithEmptyList() {
        assertTrue(weeklyViewService.mapTuntikirjausListToWeeklyProjectHours(List.of()).isEmpty());
    }
}
