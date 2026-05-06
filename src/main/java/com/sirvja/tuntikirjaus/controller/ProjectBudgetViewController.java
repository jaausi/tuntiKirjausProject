package com.sirvja.tuntikirjaus.controller;

import com.sirvja.tuntikirjaus.domain.ProjectBudgetItem;
import com.sirvja.tuntikirjaus.service.ConfigurationService;
import com.sirvja.tuntikirjaus.service.MainViewService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ProjectBudgetViewController implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(ProjectBudgetViewController.class);

    @FXML
    private TableView<ProjectBudgetRow> budgetTable;
    @FXML
    private TableColumn<ProjectBudgetRow, String> projectColumn;
    @FXML
    private TableColumn<ProjectBudgetRow, String> budgetColumn;

    private final ConfigurationService configurationService;
    private final MainViewService mainViewService;

    public ProjectBudgetViewController() {
        this.configurationService = new ConfigurationService();
        this.mainViewService = new MainViewService();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        budgetTable.setEditable(true);

        projectColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().projectName()));
        projectColumn.setEditable(false);

        budgetColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().budgetHours()));
        budgetColumn.setEditable(true);
        budgetColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        budgetColumn.setOnEditCommit(event -> {
            ProjectBudgetRow row = event.getRowValue();
            String newValue = event.getNewValue().trim();
            if (newValue.isEmpty()) {
                configurationService.removeProjectBudget(row.projectName());
                row.setBudgetHours("");
            } else {
                try {
                    double hours = Double.parseDouble(newValue.replace(",", "."));
                    configurationService.saveProjectBudget(row.projectName(), hours);
                    row.setBudgetHours(String.valueOf(hours));
                } catch (NumberFormatException e) {
                    log.warn("Invalid budget value: {}", newValue);
                    // revert display
                    budgetTable.refresh();
                }
            }
        });

        loadData();
    }

    private void loadData() {
        List<ProjectBudgetItem> items = mainViewService.getMonthlyProjectBudgetItems();

        // Also include projects that have a saved budget but no entries this month
        List<String> budgetedProjects = configurationService.getProjectsWithBudget();
        ObservableList<ProjectBudgetRow> rows = FXCollections.observableArrayList();

        // Add projects from current month's entries
        items.forEach(item -> {
            String budgetStr = item.budgetMinutes()
                    .stream()
                    .mapToObj(minutes -> {
                        double hours = minutes / 60.0;
                        // Show as integer if whole number
                        return hours == Math.floor(hours) ? String.valueOf((long) hours) : String.valueOf(hours);
                    })
                    .findFirst()
                    .orElse("");
            rows.add(new ProjectBudgetRow(item.projectName(), budgetStr));
        });

        // Add budgeted projects not present this month
        budgetedProjects.stream()
                .filter(p -> items.stream().noneMatch(i -> i.projectName().equals(p)))
                .forEach(p -> {
                    String budgetStr = configurationService.getProjectBudgetMinutes(p)
                            .stream()
                            .mapToObj(minutes -> {
                                double hours = minutes / 60.0;
                                return hours == Math.floor(hours) ? String.valueOf((long) hours) : String.valueOf(hours);
                            })
                            .findFirst()
                            .orElse("");
                    rows.add(new ProjectBudgetRow(p, budgetStr));
                });

        budgetTable.setItems(rows);
    }

    @FXML
    protected void onCloseButtonClick() {
        Stage stage = (Stage) budgetTable.getScene().getWindow();
        stage.close();
    }

    /**
     * Simple mutable row model for the budget table.
     */
    public static class ProjectBudgetRow {
        private final String projectName;
        private String budgetHours;

        public ProjectBudgetRow(String projectName, String budgetHours) {
            this.projectName = projectName;
            this.budgetHours = budgetHours;
        }

        public String projectName() { return projectName; }
        public String budgetHours() { return budgetHours; }
        public void setBudgetHours(String budgetHours) { this.budgetHours = budgetHours; }
    }
}


