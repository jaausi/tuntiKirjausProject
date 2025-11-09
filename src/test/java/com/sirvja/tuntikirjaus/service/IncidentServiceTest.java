package com.sirvja.tuntikirjaus.service;

import com.sirvja.tuntikirjaus.domain.Incident;
import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;
import com.sirvja.tuntikirjaus.domain.TuntikirjausIncident;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IncidentServiceTest {
    IncidentService incidentService = new IncidentService();

    LocalDateTime zeroTime = LocalDateTime.parse("2026-01-01T08:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME);

    @Test
    @DisplayName("Test that START_OF_DAY incidents are parsed correctly from tuntikirjausList")
    void testParsingStartOfDayIncidents() {
        List<TuntikirjausIncident> expectedIncidents = List.of(
                new TuntikirjausIncident(zeroTime, Incident.START_OF_DAY),
                new TuntikirjausIncident(zeroTime.plusDays(1), Incident.START_OF_DAY),
                new TuntikirjausIncident(zeroTime.plusDays(2), Incident.START_OF_DAY)
        );

        Collection<TuntikirjausIncident> actualIncidents = incidentService.parseStartTimeIncidents(getTestData());
        assertIncidentsAreEqual(expectedIncidents, actualIncidents);
    }

    @Test
    @DisplayName("Test that END_OF_DAY incidents are parsed correctly from tuntikirjausList")
    void testParsingEndOfDayIncidents() {
        List<TuntikirjausIncident> expectedIncidents = List.of(
                new TuntikirjausIncident(zeroTime.plusHours(6), Incident.END_OF_DAY),
                new TuntikirjausIncident(zeroTime.plusDays(1).plusHours(6), Incident.END_OF_DAY),
                new TuntikirjausIncident(zeroTime.plusDays(2).plusHours(4), Incident.END_OF_DAY)
        );

        Collection<TuntikirjausIncident> actualIncidents = incidentService.parseEndTimeIncidents(getTestData());
        assertIncidentsAreEqual(expectedIncidents, actualIncidents);
    }

    @Test
    @DisplayName("Test that START_OF_LUNCH and END_OF_LUNCH incidents are parsed correctly from tuntikirjausList")
    void testParsingLunchIncidents() {
        List<TuntikirjausIncident> expectedIncidents = List.of(
                new TuntikirjausIncident(zeroTime.plusHours(3), Incident.START_OF_LUNCH),
                new TuntikirjausIncident(zeroTime.plusHours(4), Incident.END_OF_LUNCH),
                new TuntikirjausIncident(zeroTime.plusDays(1).plusHours(3), Incident.START_OF_LUNCH),
                new TuntikirjausIncident(zeroTime.plusDays(1).plusHours(3).plusMinutes(30), Incident.END_OF_LUNCH),
                new TuntikirjausIncident(zeroTime.plusDays(2).plusHours(1).plusMinutes(30), Incident.START_OF_LUNCH),
                new TuntikirjausIncident(zeroTime.plusDays(2).plusHours(3), Incident.END_OF_LUNCH)
        );

        Collection<TuntikirjausIncident> actualIncidents = incidentService.parseLunchIncidents(getTestData());
        assertIncidentsAreEqual(expectedIncidents, actualIncidents);
    }

    @Test
    @DisplayName("Test that START_OF_BREAK and END_OF_BREAK incidents are parsed correctly from tuntikirjausList")
    void testParsingBreakIncidents() {
        List<TuntikirjausIncident> expectedIncidents = List.of(
                new TuntikirjausIncident(zeroTime.plusHours(1), Incident.START_OF_BREAK),
                new TuntikirjausIncident(zeroTime.plusHours(1).plusMinutes(30), Incident.END_OF_BREAK),
                new TuntikirjausIncident(zeroTime.plusDays(2).plusHours(1), Incident.START_OF_BREAK),
                new TuntikirjausIncident(zeroTime.plusDays(2).plusHours(1).plusMinutes(30), Incident.END_OF_BREAK)
        );

        Collection<TuntikirjausIncident> actualIncidents = incidentService.parseBreakIncidents(getTestData());
        assertIncidentsAreEqual(expectedIncidents, actualIncidents);
    }

    private void assertIncidentsAreEqual(Collection<TuntikirjausIncident> expected, Collection<TuntikirjausIncident> actual) {
        assertEquals(expected.size(), actual.size(), "TuntikirjausIncident list lengths are not equal");
        actual.forEach(ti -> assertTrue(expected.contains(ti), String.format("Expected TuntikirjausIncident list (%s) doesn't contain element: %s", expected, ti)));
    }

    private Collection<TuntiKirjaus> getTestData() {
        LocalDateTime time = zeroTime;
        return List.of(
                new TuntiKirjaus(0, time, time.plusHours(1), "Topic 1", true),
                new TuntiKirjaus(0, time.plusHours(1), time.plusHours(1).plusMinutes(30), "Tauko", true),
                new TuntiKirjaus(0, time.plusHours(1).plusMinutes(30), time.plusHours(3), "Topic 1", true),
                new TuntiKirjaus(0, time.plusHours(3), time.plusHours(4), "Lounas", true),
                new TuntiKirjaus(0, time.plusHours(4), time.plusHours(6), "Topic 3", true),
                new TuntiKirjaus(0, time.plusHours(6), null, "Lopetus", true),
                new TuntiKirjaus(0, time=time.plusDays(1), time.plusHours(1), "Topic 1", true),
                new TuntiKirjaus(0, time.plusHours(1), time.plusHours(1).plusMinutes(30), "Topic 1", true),
                new TuntiKirjaus(0, time.plusHours(1).plusMinutes(30), time.plusHours(3), "Topic 1", true),
                new TuntiKirjaus(0, time.plusHours(3), time.plusHours(3).plusMinutes(30), "Lounas", true),
                new TuntiKirjaus(0, time.plusHours(3).plusMinutes(30), time.plusHours(4), "Topic 2", true),
                new TuntiKirjaus(0, time.plusHours(4), time.plusHours(6), "Topic 2", true),
                new TuntiKirjaus(0, time.plusHours(6), null, "Lopetus", true),
                new TuntiKirjaus(0, time=time.plusDays(1), time.plusHours(1), "Topic 1", true),
                new TuntiKirjaus(0, time.plusHours(1), time.plusHours(1).plusMinutes(30), "Tauko", true),
                new TuntiKirjaus(0, time.plusHours(1).plusMinutes(30), time.plusHours(3), "Lounas", true),
                new TuntiKirjaus(0, time.plusHours(3), time.plusHours(4), "Topic 1", true),
                new TuntiKirjaus(0, time.plusHours(4), null, "Topic 1", true)
        );
    }
}
