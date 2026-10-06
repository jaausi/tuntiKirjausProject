package com.sirvja.tuntikirjaus.exporter.impl;

import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KiekuHoursItemTest {

    private static final LocalDate MONDAY = LocalDate.of(2026, 10, 5);

    @Test
    @DisplayName("should sum the entries by project and day and skip unfinished entries")
    void shouldSumByProjectAndDay() {
        List<TuntiKirjaus> entries = List.of(
                entry(MONDAY, 8, 0, 9, 30, "TUNTI-12 Koodaus"),
                entry(MONDAY, 9, 30, 10, 0, "OAW Viikkopalaveri"),
                entry(MONDAY, 10, 0, 11, 15, "TUNTI-13 Katselmointi"),
                entry(MONDAY.plusDays(1), 8, 0, 12, 0, "TUNTI-14 Koodaus"),
                new TuntiKirjaus(MONDAY.plusDays(1).atTime(12, 0), null, "TUNTI-15 Kesken", false)
        );

        List<KiekuHoursItem> items = KiekuHoursItem.fromTuntikirjausList(entries);

        assertEquals(List.of(
                new KiekuHoursItem("Other admin work", MONDAY, Duration.ofMinutes(30)),
                new KiekuHoursItem("TUNTI", MONDAY, Duration.ofMinutes(165)),
                new KiekuHoursItem("TUNTI", MONDAY.plusDays(1), Duration.ofHours(4))
        ), items);
        assertEquals("02:45", items.get(1).durationAsKiekuTime());
    }

    private static TuntiKirjaus entry(LocalDate date, int startHour, int startMinute, int endHour, int endMinute, String topic) {
        return new TuntiKirjaus(date.atTime(startHour, startMinute), date.atTime(endHour, endMinute), topic, false);
    }
}
