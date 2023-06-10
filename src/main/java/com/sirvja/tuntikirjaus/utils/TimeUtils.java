package com.sirvja.tuntikirjaus.utils;

import com.sirvja.tuntikirjaus.service.MainViewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TimeUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainViewService.class);
    public static LocalTime parseTimeFromString(String timeString) throws DateTimeParseException{
        LocalTime localTime;

        LOGGER.debug("Received {} from time field. Trying to parse...", timeString);

        if(!timeString.isEmpty()){
            if(timeString.contains(":")){ // Parse hours and minutes '9:00' or '12:00'
                localTime = LocalTime.parse(timeString, DateTimeFormatter.ofPattern("H:mm"));
            } else if (timeString.contains(".")) {
                localTime = LocalTime.parse(timeString, DateTimeFormatter.ofPattern("H.mm"));
            } else {
                if (timeString.length() <= 2){ // Parse only hours '9' or '12'
                    localTime = LocalTime.parse(timeString, DateTimeFormatter.ofPattern("H"));
                } else if(timeString.length() <= 4){ // Parse hours and minutes '922' -> 9:22 or '1222' -> '12:22'
                    localTime = LocalTime.parse(timeString, DateTimeFormatter.ofPattern("Hmm"));
                } else {
                    throw new DateTimeParseException("Couldn't parse time from String", timeString, 5);
                }
            }
        } else {
            throw new DateTimeParseException("Couldn't parse time from empty String.", timeString, 0);
        }

        return localTime;
    }
}
