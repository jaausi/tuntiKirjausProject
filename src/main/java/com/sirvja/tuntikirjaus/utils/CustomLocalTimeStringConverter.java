package com.sirvja.tuntikirjaus.utils;

import com.sirvja.tuntikirjaus.controller.MainViewController;
import com.sirvja.tuntikirjaus.exception.MalformatedTimeException;
import com.sirvja.tuntikirjaus.service.MainViewService;
import javafx.util.converter.LocalTimeStringConverter;

import java.time.LocalTime;

public class CustomLocalTimeStringConverter extends LocalTimeStringConverter {
    private final MainViewService mainViewService;

    public CustomLocalTimeStringConverter(MainViewService mainViewService) {
        this.mainViewService = mainViewService;
    }

    @Override
    public LocalTime fromString(String value) {
        try {
            return mainViewService.parseTimeFromString(value).toLocalTime();
        } catch (MalformatedTimeException e){
            MainViewController.showTimeInWrongFormatAlert(e.getMessage());
            return null;
        }
    }
}
