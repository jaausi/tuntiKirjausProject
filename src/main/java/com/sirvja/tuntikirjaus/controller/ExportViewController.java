package com.sirvja.tuntikirjaus.controller;

import com.sirvja.tuntikirjaus.domain.Incident;
import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;
import com.sirvja.tuntikirjaus.domain.TuntikirjausIncident;
import com.sirvja.tuntikirjaus.exporter.Exporter;
import com.sirvja.tuntikirjaus.exporter.impl.KiekuConfiguration;
import com.sirvja.tuntikirjaus.exporter.impl.KiekuEvent;
import com.sirvja.tuntikirjaus.exporter.impl.KiekuExporter;
import com.sirvja.tuntikirjaus.exporter.impl.KiekuItem;
import com.sirvja.tuntikirjaus.service.*;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
    private TableColumn<TuntikirjausIncident, LocalDateTime> export_column_timestamp;
    @FXML
    private TableColumn<TuntikirjausIncident, Incident> export_column_event;
    @FXML
    private TableView<TuntikirjausIncident> export_table = new TableView<>();

    private final Exporter<KiekuConfiguration, KiekuItem> exporter;

    private final IncidentService incidentService;
    private final AlertService alertService;
    private final ConfigurationService configurationService;

    public ExportViewController() {
        exporter = new KiekuExporter();
        incidentService = new IncidentService();
        alertService = new AlertService();
        configurationService = new ConfigurationService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        export_column_timestamp.setCellValueFactory(new PropertyValueFactory<TuntikirjausIncident, LocalDateTime>("time"));
        export_column_event.setCellValueFactory(new PropertyValueFactory<TuntikirjausIncident, Incident>("incident"));
        export_table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        export_button.setOnAction(this::onExportAction);
        search_button.setOnAction(this::onSearchAction);
    }

    private void initializeKiekuExporter() {
        exporter.setConfiguration(configurationService.getKiekuConfiguration());
        exporter.prepareExporter();
    }

    private void destroyKiekuExporter() {
        exporter.destroyExporter();
    }

    private void onExportAction(ActionEvent event) {
        List<TuntikirjausIncident> selectedItems = export_table.getSelectionModel().getSelectedItems();
        if(selectedItems.isEmpty()) {
            alertService.showGeneralAlert("Valitse vähintään yksi rivi taulusta ennen vientiä!");
            return;
        }

        try {
            initializeKiekuExporter();
            alertService.showConfirmationAlert("Kirjaudu sisään", "Kirjaudu sisään ja valitse sen jälkeen jatka");

            List<KiekuItem> kiekuItems = selectedItems.stream()
                    .map(this::tuntikirjausIncidentToKiekuItem)
                    .filter(Objects::nonNull)
                    .toList();

            exporter.exportItems(kiekuItems);
            destroyKiekuExporter();
            alertService.showNotificationAlert("Tuntien exportointi onnistui!");
        } catch (Exception e) {
            alertService.showGeneralAlert("Kohdattiin virhe kun yritettiin exportoida tunteja: " + e.getMessage());
        }
    }

    private KiekuItem tuntikirjausIncidentToKiekuItem(TuntikirjausIncident tuntikirjausIncident) {
        switch (tuntikirjausIncident.incident()) {
            case START_OF_DAY, END_OF_LUNCH, END_OF_BREAK -> {
                return new KiekuItem(tuntikirjausIncident.time(), KiekuEvent.IN);
            }
            case END_OF_DAY, START_OF_LUNCH, START_OF_BREAK -> {
                return new KiekuItem(tuntikirjausIncident.time(), KiekuEvent.OUT);
            }
            default -> {
                return null;
            }
        }
    }

    private void onSearchAction(ActionEvent event) {
        LocalDate startDate = Optional.ofNullable(start_date_field.getValue()).orElse(LocalDate.now().minusMonths(1));
        LocalDate endDate = Optional.ofNullable(end_date_field.getValue()).orElse(LocalDate.now());

        List<TuntiKirjaus> tuntiKirjausList = ReportsViewService.getAllTuntikirjausAsList(Optional.of(startDate), Optional.of(endDate), Optional.empty());
        List<TuntikirjausIncident> tuntikirjausIncidents = incidentService.parseTuntikirjausIncidents(tuntiKirjausList);
        export_table.setItems(FXCollections.observableArrayList(tuntikirjausIncidents));
    }
}

