package com.sirvja.tuntikirjaus.utils;

import com.sirvja.tuntikirjaus.exception.MalformatedTimeException;
import com.sirvja.tuntikirjaus.service.AlertService;
import com.sirvja.tuntikirjaus.service.MainViewService;
import javafx.util.converter.LocalTimeStringConverter;

import java.time.LocalTime;

public class CustomLocalTimeStringConverter extends LocalTimeStringConverter {
    private static CustomLocalTimeStringConverter INSTANCE;
    private final MainViewService mainViewService;
    private final AlertService alertService;

    public static CustomLocalTimeStringConverter getInstance(){
        if (INSTANCE==null){
            INSTANCE = new CustomLocalTimeStringConverter();
        }
        return INSTANCE;
    }

    private CustomLocalTimeStringConverter() {
        this.mainViewService = MainViewService.getInstance();
        this.alertService = AlertService.getInstance();
    }

    @Override
    public LocalTime fromString(String value) {
        try {
            return mainViewService.parseTimeFromString(value).toLocalTime();
        } catch (MalformatedTimeException e){
            alertService.showTimeInWrongFormatAlert(e.getMessage());
            return null;
        }
    }
}
