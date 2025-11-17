package com.sirvja.tuntikirjaus.exporter.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public record KiekuConfiguration(
        Browser browser,
        String loginPortalUrl,
        String kiekuUrl,
        String weekDropdownId,
        String weekOptionsCssSelector,
        String addWorkHoursButtonId,
        String dateFieldId,
        String timeFieldId,
        String eventDropdownId,
        String reasonCodeDropdownId,
        String saveButtonId,
        String closeButtonId,
        String toihinTuloOptionXpath,
        String toistaLahtoOptionXpath,
        String toihinTuloSyyllaOptionXpath,
        String toistaLahtoSyyllaOptionXpath,
        String etatyoOptionXpath,
        String loginButtonCssSelector,
        String loginToOrganizationOptionCssSelector,
        String loginButtonName
) {

    public static final String BROWSER_KEY = "browser";

    public Function<Integer, String> weekOptionCssSelectorFunction(){
        return weekNumber -> weekOptionsCssSelector.replace("week_index", String.valueOf(weekNumber));
    }

    public static Map<String, String> toMap(KiekuConfiguration kiekuConfiguration) {
        Map<String, String> configMap = new HashMap<>();
        configMap.put(BROWSER_KEY, kiekuConfiguration.browser() != null ? kiekuConfiguration.browser.name() : null);
        configMap.put("loginPortalUrl", kiekuConfiguration.loginPortalUrl());
        configMap.put("kiekuUrl", kiekuConfiguration.kiekuUrl());
        configMap.put("weekDropdownId", kiekuConfiguration.weekDropdownId());
        configMap.put("weekOptionsCssSelector", kiekuConfiguration.weekOptionsCssSelector());
        configMap.put("addWorkHoursButtonId", kiekuConfiguration.addWorkHoursButtonId());
        configMap.put("dateFieldId", kiekuConfiguration.dateFieldId());
        configMap.put("timeFieldId", kiekuConfiguration.timeFieldId());
        configMap.put("eventDropdownId", kiekuConfiguration.eventDropdownId());
        configMap.put("reasonCodeDropdownId", kiekuConfiguration.reasonCodeDropdownId());
        configMap.put("saveButtonId", kiekuConfiguration.saveButtonId());
        configMap.put("closeButtonId", kiekuConfiguration.closeButtonId());
        configMap.put("toihinTuloOptionXpath", kiekuConfiguration.toihinTuloOptionXpath());
        configMap.put("toistaLahtoOptionXpath", kiekuConfiguration.toistaLahtoOptionXpath());
        configMap.put("toihinTuloSyyllaOptionXpath", kiekuConfiguration.toihinTuloSyyllaOptionXpath());
        configMap.put("toistaLahtoSyyllaOptionXpath", kiekuConfiguration.toistaLahtoSyyllaOptionXpath());
        configMap.put("etatyoOptionXpath", kiekuConfiguration.etatyoOptionXpath());
        configMap.put("loginButtonCssSelector", kiekuConfiguration.loginButtonCssSelector());
        configMap.put("loginToOrganizationOptionCssSelector", kiekuConfiguration.loginToOrganizationOptionCssSelector());
        configMap.put("loginButtonName", kiekuConfiguration.loginButtonName());

        return configMap;
    }

    public static KiekuConfiguration mapToConfiguration(Map<String, String> configurations) {
        return new KiekuConfiguration(
                configurations.get(BROWSER_KEY) != null ? Browser.valueOf(configurations.get(BROWSER_KEY)) : null,
                configurations.get("loginPortalUrl"),
                configurations.get("kiekuUrl"),
                configurations.get("weekDropdownId"),
                configurations.get("weekOptionsCssSelector"),
                configurations.get("addWorkHoursButtonId"),
                configurations.get("dateFieldId"),
                configurations.get("timeFieldId"),
                configurations.get("eventDropdownId"),
                configurations.get("reasonCodeDropdownId"),
                configurations.get("saveButtonId"),
                configurations.get("closeButtonId"),
                configurations.get("toihinTuloOptionXpath"),
                configurations.get("toistaLahtoOptionXpath"),
                configurations.get("toihinTuloSyyllaOptionXpath"),
                configurations.get("toistaLahtoSyyllaOptionXpath"),
                configurations.get("etatyoOptionXpath"),
                configurations.get("loginButtonCssSelector"),
                configurations.get("loginToOrganizationOptionCssSelector"),
                configurations.get("loginButtonName")
        );
    }

    public static boolean isValidBrowserConfig(String config) {
        for (Browser value : Browser.values()) {
            if(value.toString().equals(config)) return true;
        }
        return false;
    }
}
