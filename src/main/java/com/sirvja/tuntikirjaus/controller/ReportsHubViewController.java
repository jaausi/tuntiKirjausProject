package com.sirvja.tuntikirjaus.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Single reports window: search and saved reports, weekly report and Kieku export as tabs.
 */
public class ReportsHubViewController implements Initializable {

    @FXML
    private TabPane reportsTabPane;
    @FXML
    private Tab weeklyTab;
    @FXML
    private ReportsViewController reportsViewController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        reportsViewController.setOnShowDaySummary(() -> reportsTabPane.getSelectionModel().select(weeklyTab));
    }
}
