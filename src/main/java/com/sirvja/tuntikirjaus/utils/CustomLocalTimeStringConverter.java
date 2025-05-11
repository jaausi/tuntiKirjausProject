package com.sirvja.tuntikirjaus.utils;

import com.sirvja.tuntikirjaus.controller.MainViewController;
import com.sirvja.tuntikirjaus.exception.MalformatedTimeException;
import com.sirvja.tuntikirjaus.service.AlertService;
import com.sirvja.tuntikirjaus.service.MainViewService;
import javafx.util.converter.LocalTimeStringConverter;

import java.time.LocalTime;

public class CustomLocalTimeStringConverter extends LocalTimeStringConverter {
    private final MainViewService mainViewService;
    private final AlertService alertService;

    public CustomLocalTimeStringConverter(MainViewService mainViewService) {
        this.mainViewService = mainViewService;
        this.alertService = new AlertService();
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
