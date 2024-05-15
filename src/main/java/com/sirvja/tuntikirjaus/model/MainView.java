package com.sirvja.tuntikirjaus.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * Singleton class to hold state of MainView
 */
@Getter
@Setter
public class MainView {
    private LocalDate currentDate;
    private List<HourRecord> hourRecordList;

    private static class MainViewHolder {
        private static final MainView INSTANCE = new MainView();
    }
    public static MainView getInstance() {
        return MainViewHolder.INSTANCE;
    }
}
