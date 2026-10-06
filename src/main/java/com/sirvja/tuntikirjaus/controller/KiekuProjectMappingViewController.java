package com.sirvja.tuntikirjaus.controller;

import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;
import com.sirvja.tuntikirjaus.service.ConfigurationService;
import com.sirvja.tuntikirjaus.service.TuntiKirjausService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Maps the projects of the application to Kieku projects for the project hours export. A project without a
 * mapping is searched from Kieku with its own name.
 */
public class KiekuProjectMappingViewController implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(KiekuProjectMappingViewController.class);

    @FXML
    private TableView<KiekuProjectMappingRow> mappingTable;
    @FXML
    private TableColumn<KiekuProjectMappingRow, String> projectColumn;
    @FXML
    private TableColumn<KiekuProjectMappingRow, String> kiekuProjectColumn;

    private final ConfigurationService configurationService;
    private final TuntiKirjausService tuntiKirjausService;

    public KiekuProjectMappingViewController() {
        this.configurationService = new ConfigurationService();
        this.tuntiKirjausService = new TuntiKirjausService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mappingTable.setEditable(true);

        projectColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().projectName()));
        projectColumn.setEditable(false);

        kiekuProjectColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().kiekuProject()));
        kiekuProjectColumn.setEditable(true);
        kiekuProjectColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        kiekuProjectColumn.setOnEditCommit(event -> {
            KiekuProjectMappingRow row = event.getRowValue();
            String newValue = event.getNewValue() == null ? "" : event.getNewValue().trim();
            if (newValue.isEmpty()) {
                configurationService.removeKiekuProjectMapping(row.projectName());
            } else {
                configurationService.saveKiekuProjectMapping(row.projectName(), newValue);
            }
            row.setKiekuProject(newValue);
            // Project names are not logged, they may contain customer names
            LOGGER.info("Kieku project mapping {}", newValue.isEmpty() ? "removed" : "changed");
        });

        reload();
    }

    /**
     * Shows the projects of the recent entries and the projects that already have a mapping.
     */
    public void reload() {
        Map<String, String> mappings = configurationService.getKiekuProjectMappings();
        SortedSet<String> projects = tuntiKirjausService.getAllTuntikirjaus().stream()
                .map(TuntiKirjaus::getClassification)
                .collect(Collectors.toCollection(TreeSet::new));
        projects.addAll(mappings.keySet());

        mappingTable.setItems(projects.stream()
                .map(project -> new KiekuProjectMappingRow(project, mappings.getOrDefault(project, "")))
                .collect(Collectors.toCollection(FXCollections::observableArrayList)));
    }

    /**
     * Simple mutable row model for the mapping table.
     */
    public static class KiekuProjectMappingRow {
        private final String projectName;
        private String kiekuProject;

        public KiekuProjectMappingRow(String projectName, String kiekuProject) {
            this.projectName = projectName;
            this.kiekuProject = kiekuProject;
        }

        public String projectName() { return projectName; }
        public String kiekuProject() { return kiekuProject; }
        public void setKiekuProject(String kiekuProject) { this.kiekuProject = kiekuProject; }
    }
}
