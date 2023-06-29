package com.sirvja.tuntikirjaus.utils;

import com.sirvja.tuntikirjaus.service.MainViewService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ListUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainViewService.class);

    public static <T> ObservableList<T> toObservableList(List<T> list) {
        return FXCollections.observableList(list);
    }

    public static <T> List<T> toList(ObservableList<T> observableList) {
        return observableList.stream().toList();
    }
}
