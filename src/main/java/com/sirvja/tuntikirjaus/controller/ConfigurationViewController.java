package com.sirvja.tuntikirjaus.controller;

import com.sirvja.tuntikirjaus.domain.Configuration;
import com.sirvja.tuntikirjaus.exporter.impl.KiekuConfiguration;
import com.sirvja.tuntikirjaus.exporter.impl.KiekuHoursConfiguration;
import com.sirvja.tuntikirjaus.service.AlertService;
import com.sirvja.tuntikirjaus.service.ConfigurationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConfigurationViewController implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationViewController.class);

    @FXML
    private TableView<Configuration> confTable;
    @FXML
    private TableColumn<Configuration, String> keyColumn;
    @FXML
    private TableColumn<Configuration, String> valueColumn;

    private final ConfigurationService configurationService;
    private final AlertService alertService;

    public ConfigurationViewController() {
        this.configurationService = new ConfigurationService();
        this.alertService = new AlertService();
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        confTable.setEditable(true);

        keyColumn.setCellValueFactory(new PropertyValueFactory<Configuration, String>("key"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<Configuration, String>("value"));

        keyColumn.setSortable(false);
        valueColumn.setSortable(false);

        valueColumn.setEditable(true);
        valueColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        valueColumn.setOnEditCommit(this::saveNewValue);

        reload();
    }

    /**
     * Shows the Kieku configurations: first the ones used for the work time events and then the ones used for
     * the project hours.
     */
    public void reload() {
        KiekuHoursConfiguration kiekuHoursConfiguration = configurationService.getKiekuHoursConfiguration();
        Map<String, String> kiekuConfigurationMap = KiekuConfiguration.toMap(kiekuHoursConfiguration.base());
        ObservableList<Configuration> configurationList = Stream.concat(
                        kiekuConfigurationMap.entrySet().stream().sorted(Map.Entry.comparingByKey()),
                        KiekuHoursConfiguration.toMap(kiekuHoursConfiguration).entrySet().stream())
                .map(entry -> new Configuration(entry.getKey(), entry.getValue()))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        confTable.setItems(configurationList);
    }

    private void saveNewValue(TableColumn.CellEditEvent<Configuration, String> editEvent) {
        int rowInTableToBeEdited = editEvent.getTablePosition().getRow();
        Configuration confToBeEdited = editEvent.getTableView().getItems().get(rowInTableToBeEdited);
        if(KiekuConfiguration.BROWSER_KEY.equals(confToBeEdited.getKey())) {
            if(!KiekuConfiguration.isValidBrowserConfig(editEvent.getNewValue())) {
                LOGGER.warn("Invalid browser configuration entered");
                alertService.showGeneralAlert("Browser configuration not valid. Valid values are: 'SAFARI', 'CHROME' and 'FIREFOX'.");
                confTable.refresh();
                return;
            }
        }
        if(KiekuHoursConfiguration.SAVE_AUTOMATICALLY_KEY.equals(confToBeEdited.getKey())) {
            if(!KiekuHoursConfiguration.isValidBooleanConfig(editEvent.getNewValue())) {
                alertService.showGeneralAlert("Arvon pitää olla 'true' tai 'false'.");
                confTable.refresh();
                return;
            }
        }
        confToBeEdited.setValue(editEvent.getNewValue());
        configurationService.insertOrUpdate(confToBeEdited);
        // Only the key is logged, values may contain organization specific addresses
        LOGGER.info("Kieku configuration '{}' changed", confToBeEdited.getKey());
    }
}
