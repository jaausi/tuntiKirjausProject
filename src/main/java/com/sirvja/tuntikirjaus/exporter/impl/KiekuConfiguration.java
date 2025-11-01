package com.sirvja.tuntikirjaus.exporter.impl;

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

    Function<Integer, String> weekOptionCssSelectorFunction(){
        return weekNumber -> weekOptionsCssSelector.replace("week_index", String.valueOf(weekNumber));
    }
}
