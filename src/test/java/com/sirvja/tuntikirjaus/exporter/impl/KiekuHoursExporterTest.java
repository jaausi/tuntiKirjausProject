package com.sirvja.tuntikirjaus.exporter.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class KiekuHoursExporterTest {

    @Test
    @DisplayName("should parse the Monday of the shown week")
    void shouldParseMonday() {
        assertEquals(LocalDate.of(2026, 10, 5), KiekuHoursExporter.parseMonday("MA 05.10", LocalDate.of(2026, 10, 5)));
        assertEquals(LocalDate.of(2026, 9, 28), KiekuHoursExporter.parseMonday("Ma 28.9.", LocalDate.of(2026, 10, 5)));
    }

    @Test
    @DisplayName("should take the turn of the year into account when parsing the Monday")
    void shouldParseMondayOverTurnOfYear() {
        assertEquals(LocalDate.of(2025, 12, 29), KiekuHoursExporter.parseMonday("MA 29.12", LocalDate.of(2026, 1, 5)));
        assertEquals(LocalDate.of(2027, 1, 4), KiekuHoursExporter.parseMonday("MA 04.01", LocalDate.of(2026, 12, 28)));
    }

    @Test
    @DisplayName("should find the row by exact title, then by the beginning of the title and then by a part of it")
    void shouldFindRow() {
        Map<String, String> rows = new LinkedHashMap<>();
        rows.put("SUPPORT", "1");
        rows.put("SUPPORT 2", "2");
        rows.put("Kehitys (jatkuva)", "3");
        rows.put("Hallinto", "4");

        assertEquals("1", KiekuHoursExporter.findRow(rows, "support"));
        assertEquals("3", KiekuHoursExporter.findRow(rows, "Kehitys"));
        assertEquals("3", KiekuHoursExporter.findRow(rows, "jatkuva"));
        assertNull(KiekuHoursExporter.findRow(rows, "SUPP"), "ambiguous match");
        assertNull(KiekuHoursExporter.findRow(rows, "Unknown"));
    }

    @Test
    @DisplayName("should convert times between Duration and the format of Kieku")
    void shouldConvertTimes() {
        assertEquals("01:30", KiekuHoursExporter.toKiekuTime(Duration.ofMinutes(90)));
        assertEquals("10:05", KiekuHoursExporter.toKiekuTime(Duration.ofMinutes(605)));
        assertEquals(90, KiekuHoursExporter.toMinutes("1:30"));
        assertEquals(90, KiekuHoursExporter.toMinutes("01:30"));
        assertEquals(0, KiekuHoursExporter.toMinutes(""));
        assertEquals(-1, KiekuHoursExporter.toMinutes("1,5"));
    }
}
