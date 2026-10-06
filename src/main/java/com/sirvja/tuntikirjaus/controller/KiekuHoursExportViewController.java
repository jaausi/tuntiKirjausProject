package com.sirvja.tuntikirjaus.controller;

import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;
import com.sirvja.tuntikirjaus.exporter.Exporter;
import com.sirvja.tuntikirjaus.exporter.impl.KiekuHoursConfiguration;
import com.sirvja.tuntikirjaus.exporter.impl.KiekuHoursExporter;
import com.sirvja.tuntikirjaus.exporter.impl.KiekuHoursItem;
import com.sirvja.tuntikirjaus.service.AlertService;
import com.sirvja.tuntikirjaus.service.ConfigurationService;
import com.sirvja.tuntikirjaus.service.ReportsViewService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Exports the hours of each project to the Kieku work time allocation.
 */
public class KiekuHoursExportViewController implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(KiekuHoursExportViewController.class);

    @FXML
    private DatePicker startDateField;
    @FXML
    private DatePicker endDateField;
    @FXML
    private Button searchButton;
    @FXML
    private Button exportButton;
    @FXML
    private TableView<KiekuHoursItem> hoursTable;
    @FXML
    private TableColumn<KiekuHoursItem, LocalDate> dateColumn;
    @FXML
    private TableColumn<KiekuHoursItem, String> projectColumn;
    @FXML
    private TableColumn<KiekuHoursItem, String> kiekuProjectColumn;
    @FXML
    private TableColumn<KiekuHoursItem, String> hoursColumn;

    private final Exporter<KiekuHoursConfiguration, KiekuHoursItem> exporter;
    private final AlertService alertService;
    private final ConfigurationService configurationService;

    public KiekuHoursExportViewController() {
        exporter = new KiekuHoursExporter();
        alertService = new AlertService();
        configurationService = new ConfigurationService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dateColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().date()));
        projectColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().project()));
        hoursColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().durationAsKiekuTime()));
        hoursTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        startDateField.setValue(LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)));
        endDateField.setValue(LocalDate.now());

        searchButton.setOnAction(this::onSearchAction);
        exportButton.setOnAction(this::onExportAction);
    }

    private void onSearchAction(ActionEvent event) {
        LocalDate startDate = Optional.ofNullable(startDateField.getValue()).orElse(LocalDate.now().minusMonths(1));
        LocalDate endDate = Optional.ofNullable(endDateField.getValue()).orElse(LocalDate.now());

        // The mappings may have been changed in the settings since the last search
        KiekuHoursConfiguration configuration = configurationService.getKiekuHoursConfiguration();
        kiekuProjectColumn.setCellValueFactory(data -> new SimpleStringProperty(configuration.kiekuRowFor(data.getValue().project())));

        List<TuntiKirjaus> tuntiKirjausList = ReportsViewService.getAllTuntikirjausAsList(Optional.of(startDate), Optional.of(endDate), Optional.empty());
        List<KiekuHoursItem> items = KiekuHoursItem.fromTuntikirjausList(tuntiKirjausList);
        LOGGER.debug("Found {} project hour item(s) to export between {} and {}", items.size(), startDate, endDate);
        hoursTable.setItems(FXCollections.observableArrayList(items));
    }

    private void onExportAction(ActionEvent event) {
        List<KiekuHoursItem> items = List.copyOf(hoursTable.getSelectionModel().getSelectedItems());
        if (items.isEmpty()) {
            items = List.copyOf(hoursTable.getItems());
            if (items.isEmpty()) {
                alertService.showGeneralAlert("Hae ensin vietävät tunnit!");
                return;
            }
            if (!alertService.showConfirmationAlert("Viedäänkö kaikki rivit?",
                    "Yhtään riviä ei ole valittu. Viedäänkö kaikki " + items.size() + " riviä Kiekuun?")) {
                return;
            }
        }

        KiekuHoursConfiguration configuration = configurationService.getKiekuHoursConfiguration();
        List<String> missingSettings = configuration.missingSettings();
        if (!missingSettings.isEmpty()) {
            alertService.showGeneralAlert("Kieku-asetuksista puuttuu arvoja: " + String.join(", ", missingSettings)
                    + ". Täytä ne asetusten Kieku-välilehdellä tai tuo asetukset tiedostosta.");
            return;
        }

        try {
            exporter.setConfiguration(configuration);
            exporter.prepareExporter();
            if (!alertService.showConfirmationAlert("Kirjaudu sisään", "Kirjaudu sisään Kiekuun ja valitse sen jälkeen Kyllä")) {
                LOGGER.info("User cancelled the Kieku hours export");
                return;
            }
            exporter.exportItems(items);
            alertService.showNotificationAlert("Tuntien kohdistus Kiekuun onnistui!");
        } catch (Exception e) {
            LOGGER.error("Exporting project hours to Kieku failed", e);
            alertService.showGeneralAlert("Kohdattiin virhe kun yritettiin kohdistaa tunteja Kiekuun: " + e.getMessage());
        } finally {
            exporter.destroyExporter();
        }
    }
}
