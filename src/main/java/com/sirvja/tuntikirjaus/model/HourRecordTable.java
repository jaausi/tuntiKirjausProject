package com.sirvja.tuntikirjaus.model;


import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import lombok.Data;

import java.time.LocalTime;

@Data
public class HourRecordTable {
    private TableView<HourRecord> hourRecordTableView;
    private TableColumn<HourRecord, LocalTime> timeTableColumn;
    private TableColumn<HourRecord, String> topicTableColumn;
    private TableColumn<HourRecord, String> durationTableColumn;
}
