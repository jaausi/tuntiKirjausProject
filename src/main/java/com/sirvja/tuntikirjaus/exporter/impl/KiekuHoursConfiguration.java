package com.sirvja.tuntikirjaus.exporter.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Settings for filling project hours to the Kieku work time allocation page.
 * <p>
 * All page specific details (address, element ids and selectors) come from the configuration of the
 * application, so they are not stored in version control. See the Kieku tab of the settings window.
 *
 * @param base                      existing Kieku configuration (browser)
 * @param hoursUrl                  address of the work time allocation page
 * @param rowTitleCssSelector       css selector matching the title element of every project row
 * @param rowNumberRegex            regex applied to the id of a row title element, group 1 is the row number
 * @param dayFieldIdPattern         id of an hour field, {@code {day}} and {@code {row}} are replaced
 * @param dayIdPrefixes             comma separated values for {@code {day}}, starting from Monday
 * @param mondayHeaderXpath         xpath of the element showing the date of the Monday of the shown week
 * @param previousWeekCssSelector   css selector of the "previous week" button
 * @param nextWeekCssSelector       css selector of the "next week" button
 * @param saveButtonXpath           xpath of the save button
 * @param disabledButtonCssClass    css class a disabled save button has (optional)
 * @param busyIndicatorCssSelector  css selector of busy indicators to wait for (optional)
 * @param saveAutomatically         true: save each week without asking, false: ask before saving
 * @param projectToKiekuRow         project of the application -> Kieku row title (or the beginning of it)
 */
public record KiekuHoursConfiguration(
        KiekuConfiguration base,
        String hoursUrl,
        String rowTitleCssSelector,
        String rowNumberRegex,
        String dayFieldIdPattern,
        String dayIdPrefixes,
        String mondayHeaderXpath,
        String previousWeekCssSelector,
        String nextWeekCssSelector,
        String saveButtonXpath,
        String disabledButtonCssClass,
        String busyIndicatorCssSelector,
        boolean saveAutomatically,
        Map<String, String> projectToKiekuRow
) {

    public static final String HOURS_URL_KEY = "kiekuHoursUrl";
    public static final String ROW_TITLE_CSS_SELECTOR_KEY = "kiekuHoursRowTitleCssSelector";
    public static final String ROW_NUMBER_REGEX_KEY = "kiekuHoursRowNumberRegex";
    public static final String DAY_FIELD_ID_PATTERN_KEY = "kiekuHoursDayFieldIdPattern";
    public static final String DAY_ID_PREFIXES_KEY = "kiekuHoursDayIdPrefixes";
    public static final String MONDAY_HEADER_XPATH_KEY = "kiekuHoursMondayHeaderXpath";
    public static final String PREVIOUS_WEEK_CSS_SELECTOR_KEY = "kiekuHoursPreviousWeekCssSelector";
    public static final String NEXT_WEEK_CSS_SELECTOR_KEY = "kiekuHoursNextWeekCssSelector";
    public static final String SAVE_BUTTON_XPATH_KEY = "kiekuHoursSaveButtonXpath";
    public static final String DISABLED_BUTTON_CSS_CLASS_KEY = "kiekuHoursDisabledButtonCssClass";
    public static final String BUSY_INDICATOR_CSS_SELECTOR_KEY = "kiekuHoursBusyIndicatorCssSelector";
    public static final String SAVE_AUTOMATICALLY_KEY = "kiekuHoursSaveAutomatically";

    /** Project mappings are stored as separate configurations: {@code kiekuProjectMapping.<project>=<Kieku row>}. */
    public static final String PROJECT_MAPPING_KEY_PREFIX = "kiekuProjectMapping.";

    public static final String DAY_PLACEHOLDER = "{day}";
    public static final String ROW_PLACEHOLDER = "{row}";

    private static final List<String> REQUIRED_KEYS = List.of(
            HOURS_URL_KEY,
            ROW_TITLE_CSS_SELECTOR_KEY,
            ROW_NUMBER_REGEX_KEY,
            DAY_FIELD_ID_PATTERN_KEY,
            DAY_ID_PREFIXES_KEY,
            MONDAY_HEADER_XPATH_KEY,
            PREVIOUS_WEEK_CSS_SELECTOR_KEY,
            NEXT_WEEK_CSS_SELECTOR_KEY,
            SAVE_BUTTON_XPATH_KEY);

    public KiekuHoursConfiguration {
        projectToKiekuRow = projectToKiekuRow == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(projectToKiekuRow));
    }

    /** Kieku row searched for the project: the mapping if there is one, otherwise the name of the project as is. */
    public String kiekuRowFor(String project) {
        return Optional.ofNullable(projectToKiekuRow.get(project))
                .filter(row -> !row.isBlank())
                .orElse(project);
    }

    /** Values for {@code {day}} in the order Monday, Tuesday... */
    public List<String> dayIdPrefixList() {
        if (dayIdPrefixes == null || dayIdPrefixes.isBlank()) {
            return List.of();
        }
        return Arrays.stream(dayIdPrefixes.split(","))
                .map(String::trim)
                .toList();
    }

    public String dayFieldId(int dayIndex, String rowNumber) {
        return dayFieldIdPattern
                .replace(DAY_PLACEHOLDER, dayIdPrefixList().get(dayIndex))
                .replace(ROW_PLACEHOLDER, rowNumber);
    }

    /** Keys of the settings that have to be filled in before hours can be exported. */
    public List<String> missingSettings() {
        Map<String, String> values = toMap(this);
        List<String> missing = new ArrayList<>();
        if (base == null || base.browser() == null) {
            missing.add(KiekuConfiguration.BROWSER_KEY);
        }
        REQUIRED_KEYS.stream()
                .filter(key -> values.get(key) == null || values.get(key).isBlank())
                .forEach(missing::add);
        return missing;
    }

    public static KiekuHoursConfiguration mapToConfiguration(Map<String, String> configurations) {
        Map<String, String> projectMapping = new LinkedHashMap<>();
        configurations.forEach((key, value) -> {
            if (key.startsWith(PROJECT_MAPPING_KEY_PREFIX) && value != null && !value.isBlank()) {
                projectMapping.put(key.substring(PROJECT_MAPPING_KEY_PREFIX.length()), value);
            }
        });

        return new KiekuHoursConfiguration(
                KiekuConfiguration.mapToConfiguration(configurations),
                configurations.get(HOURS_URL_KEY),
                configurations.get(ROW_TITLE_CSS_SELECTOR_KEY),
                configurations.get(ROW_NUMBER_REGEX_KEY),
                configurations.get(DAY_FIELD_ID_PATTERN_KEY),
                configurations.get(DAY_ID_PREFIXES_KEY),
                configurations.get(MONDAY_HEADER_XPATH_KEY),
                configurations.get(PREVIOUS_WEEK_CSS_SELECTOR_KEY),
                configurations.get(NEXT_WEEK_CSS_SELECTOR_KEY),
                configurations.get(SAVE_BUTTON_XPATH_KEY),
                configurations.get(DISABLED_BUTTON_CSS_CLASS_KEY),
                configurations.get(BUSY_INDICATOR_CSS_SELECTOR_KEY),
                Boolean.parseBoolean(configurations.get(SAVE_AUTOMATICALLY_KEY)),
                projectMapping
        );
    }

    /**
     * Settings of the hours page without the base configuration and project mappings, which are edited elsewhere.
     */
    public static Map<String, String> toMap(KiekuHoursConfiguration configuration) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put(HOURS_URL_KEY, configuration.hoursUrl());
        map.put(ROW_TITLE_CSS_SELECTOR_KEY, configuration.rowTitleCssSelector());
        map.put(ROW_NUMBER_REGEX_KEY, configuration.rowNumberRegex());
        map.put(DAY_FIELD_ID_PATTERN_KEY, configuration.dayFieldIdPattern());
        map.put(DAY_ID_PREFIXES_KEY, configuration.dayIdPrefixes());
        map.put(MONDAY_HEADER_XPATH_KEY, configuration.mondayHeaderXpath());
        map.put(PREVIOUS_WEEK_CSS_SELECTOR_KEY, configuration.previousWeekCssSelector());
        map.put(NEXT_WEEK_CSS_SELECTOR_KEY, configuration.nextWeekCssSelector());
        map.put(SAVE_BUTTON_XPATH_KEY, configuration.saveButtonXpath());
        map.put(DISABLED_BUTTON_CSS_CLASS_KEY, configuration.disabledButtonCssClass());
        map.put(BUSY_INDICATOR_CSS_SELECTOR_KEY, configuration.busyIndicatorCssSelector());
        map.put(SAVE_AUTOMATICALLY_KEY, String.valueOf(configuration.saveAutomatically()));
        return map;
    }

    public static boolean isValidBooleanConfig(String value) {
        return "true".equals(value) || "false".equals(value);
    }
}
