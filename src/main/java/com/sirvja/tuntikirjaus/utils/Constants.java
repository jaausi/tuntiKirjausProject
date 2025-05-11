package com.sirvja.tuntikirjaus.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Constants {
    // CONFIGURATIONS
    public static final boolean USE_CACHE_IN_DB_QUERIES = true;
    public static final boolean DROP_TABLE_ON_START = false;
    public static final int AMOUNT_OF_DAYS_TO_FETCH = 30;

    // GLOBAL VARIABLES
    public static final LocalDate FETCH_DAYS_SINCE = LocalDate.now().minusDays(AMOUNT_OF_DAYS_TO_FETCH);
    public static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    public static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
}
