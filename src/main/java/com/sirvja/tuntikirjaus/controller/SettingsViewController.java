package com.sirvja.tuntikirjaus.controller;

import com.sirvja.tuntikirjaus.TuntikirjausApplication;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Single settings window: general settings (theme), project budgets and Kieku configuration as tabs.
 */
public class SettingsViewController implements Initializable {

    @FXML
    private TabPane settingsTabPane;
    @FXML
    private CheckBox darkThemeCheckBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        darkThemeCheckBox.setSelected(TuntikirjausApplication.isDarkTheme());
        darkThemeCheckBox.selectedProperty().addListener((observable, oldValue, dark) -> {
            TuntikirjausApplication.setDarkTheme(dark);
            TuntikirjausApplication.applyTheme(settingsTabPane.getScene());
        });
    }

    @FXML
    protected void onCloseButtonClick() {
        Stage stage = (Stage) settingsTabPane.getScene().getWindow();
        stage.close();
    }
}
