package com.sirvja.tuntikirjaus.exporter.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KiekuHoursConfigurationTest {

    @Test
    @DisplayName("should read project mappings from separate configurations")
    void shouldReadProjectMappings() {
        Map<String, String> configurations = new HashMap<>();
        configurations.put("kiekuProjectMapping.TUNTI", "Kehitys");
        configurations.put("kiekuProjectMapping.Other admin work", "Hallinto");
        configurations.put("kiekuProjectMapping.EMPTY", " ");
        configurations.put("projectBudget.TUNTI", "40.0");

        KiekuHoursConfiguration configuration = KiekuHoursConfiguration.mapToConfiguration(configurations);

        assertEquals(Map.of("TUNTI", "Kehitys", "Other admin work", "Hallinto"), configuration.projectToKiekuRow());
    }

    @Test
    @DisplayName("should use the mapping of the project or the name of the project when there is no mapping")
    void shouldFindKiekuRowForProject() {
        KiekuHoursConfiguration configuration = KiekuHoursConfiguration.mapToConfiguration(
                Map.of("kiekuProjectMapping.TUNTI", "Kehitys"));

        assertEquals("Kehitys", configuration.kiekuRowFor("TUNTI"));
        assertEquals("OTHER", configuration.kiekuRowFor("OTHER"));
    }

    @Test
    @DisplayName("should build the id of an hour field from the pattern and day prefixes")
    void shouldBuildDayFieldId() {
        KiekuHoursConfiguration configuration = KiekuHoursConfiguration.mapToConfiguration(Map.of(
                KiekuHoursConfiguration.DAY_FIELD_ID_PATTERN_KEY, "hours-{day}-{row}",
                KiekuHoursConfiguration.DAY_ID_PREFIXES_KEY, "mon, tue,wed,thu,fri"));

        assertEquals(List.of("mon", "tue", "wed", "thu", "fri"), configuration.dayIdPrefixList());
        assertEquals("hours-mon-3", configuration.dayFieldId(0, "3"));
        assertEquals("hours-fri-12", configuration.dayFieldId(4, "12"));
    }

    @Test
    @DisplayName("should keep the values of the hours page when converted to a map and back")
    void shouldConvertToMapAndBack() {
        KiekuHoursConfiguration configuration = KiekuHoursExporterFakePageTest.fakeHoursConfiguration(Map.of());

        Map<String, String> map = new HashMap<>(KiekuConfiguration.toMap(configuration.base()));
        map.putAll(KiekuHoursConfiguration.toMap(configuration));

        assertEquals(configuration, KiekuHoursConfiguration.mapToConfiguration(map));
    }

    @Test
    @DisplayName("should list the settings that are missing")
    void shouldListMissingSettings() {
        assertTrue(KiekuHoursExporterFakePageTest.fakeHoursConfiguration(Map.of()).missingSettings().isEmpty());

        List<String> missing = KiekuHoursConfiguration.mapToConfiguration(Map.of(
                KiekuHoursConfiguration.HOURS_URL_KEY, "https://kieku.example")).missingSettings();

        assertTrue(missing.contains(KiekuConfiguration.BROWSER_KEY));
        assertTrue(missing.contains(KiekuHoursConfiguration.ROW_TITLE_CSS_SELECTOR_KEY));
        assertFalse(missing.contains(KiekuHoursConfiguration.HOURS_URL_KEY));
        // Optional settings
        assertFalse(missing.contains(KiekuHoursConfiguration.DISABLED_BUTTON_CSS_CLASS_KEY));
        assertFalse(missing.contains(KiekuHoursConfiguration.BUSY_INDICATOR_CSS_SELECTOR_KEY));
    }
}
