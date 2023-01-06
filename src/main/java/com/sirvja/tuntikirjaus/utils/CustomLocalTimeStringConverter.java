package com.sirvja.tuntikirjaus.utils;

import com.sirvja.tuntikirjaus.controller.MainViewController;
import com.sirvja.tuntikirjaus.service.MainViewService;
import javafx.util.converter.LocalTimeStringConverter;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class CustomLocalTimeStringConverter extends LocalTimeStringConverter {
    @Override
    public LocalTime fromString(String value) {
        try {
            return MainViewService.parseTimeFromString(value).toLocalTime();
        } catch (DateTimeParseException e){
            MainViewController.showTimeInWrongFormatAlert(e.getMessage());
            return null;
        }
    }
}
