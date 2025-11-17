package com.sirvja.tuntikirjaus.controller;

import com.sirvja.tuntikirjaus.domain.Configuration;
import com.sirvja.tuntikirjaus.exporter.impl.KiekuConfiguration;
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

import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ConfigurationViewController implements Initializable {

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

        initializeTable();
    }

    private void initializeTable() {
        KiekuConfiguration kiekuConfiguration = configurationService.getKiekuConfiguration();
        ObservableList<Configuration> configurationList = KiekuConfiguration.toMap(kiekuConfiguration).entrySet()
                .stream()
                .map(entry -> new Configuration(entry.getKey(), entry.getValue()))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        confTable.setItems(configurationList);
    }

    private void saveNewValue(TableColumn.CellEditEvent<Configuration, String> editEvent) {
        int rowInTableToBeEdited = editEvent.getTablePosition().getRow();
        Configuration confToBeEdited = editEvent.getTableView().getItems().get(rowInTableToBeEdited);
        if(KiekuConfiguration.BROWSER_KEY.equals(confToBeEdited.getKey())) {
            if(!KiekuConfiguration.isValidBrowserConfig(editEvent.getNewValue())) {
                alertService.showGeneralAlert("Browser configuration not valid. Valid values are: 'SAFARI', 'CHROME' and 'FIREFOX'.");
                return;
            }
        }
        confToBeEdited.setValue(editEvent.getNewValue());
        configurationService.insertOrUpdate(confToBeEdited);
    }
}
