package com.sirvja.tuntikirjaus.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Constants {
    // CONFIGURATIONS
    public static final int AMOUNT_OF_DAYS_TO_FETCH = 30;

    // GLOBAL VARIABLES
    public static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    public static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Computed on every call so the window keeps moving if the application stays open over several days
    public static LocalDate fetchDaysSince() {
        return LocalDate.now().minusDays(AMOUNT_OF_DAYS_TO_FETCH);
    }
}
