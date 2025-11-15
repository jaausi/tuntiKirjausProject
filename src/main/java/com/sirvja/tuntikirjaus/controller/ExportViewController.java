package com.sirvja.tuntikirjaus.controller;

import com.sirvja.tuntikirjaus.exporter.Exporter;
import com.sirvja.tuntikirjaus.exporter.impl.KiekuConfiguration;
import com.sirvja.tuntikirjaus.exporter.impl.KiekuEvent;
import com.sirvja.tuntikirjaus.exporter.impl.KiekuExporter;
import com.sirvja.tuntikirjaus.exporter.impl.KiekuItem;
import com.sirvja.tuntikirjaus.service.IncidentService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class ExportViewController implements Initializable {

    @FXML
    private DatePicker start_date_field;
    @FXML
    private DatePicker end_date_field;
    @FXML
    private Button search_button;
    @FXML
    private Button export_button;
    @FXML
    private TableColumn<KiekuItem, LocalDateTime> export_column_timestamp;
    @FXML
    private TableColumn<KiekuItem, KiekuEvent> export_column_event;
    @FXML
    private TableView<KiekuItem> export_table = new TableView<>();

    private final Exporter<KiekuConfiguration, KiekuItem> exporter;

    private final IncidentService eventService;

    public ExportViewController() {
        exporter = new KiekuExporter();
        eventService = new IncidentService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        export_column_timestamp.setCellValueFactory(new PropertyValueFactory<KiekuItem, LocalDateTime>("time"));
        export_column_event.setCellValueFactory(new PropertyValueFactory<KiekuItem, KiekuEvent>("event"));

        export_table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        export_button.setOnAction(event -> exporter.exportItems(export_table.getSelectionModel().getSelectedItems()));

        //search_button.setOnAction(event -> );
    }
}

