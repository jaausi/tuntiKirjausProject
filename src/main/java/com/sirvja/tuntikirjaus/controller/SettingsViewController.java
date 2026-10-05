package com.sirvja.tuntikirjaus.controller;

import com.sirvja.tuntikirjaus.TuntikirjausApplication;
import com.sirvja.tuntikirjaus.domain.Configuration;
import com.sirvja.tuntikirjaus.logging.LogFiles;
import com.sirvja.tuntikirjaus.logging.LogUploadException;
import com.sirvja.tuntikirjaus.logging.LogUploadService;
import com.sirvja.tuntikirjaus.service.AlertService;
import com.sirvja.tuntikirjaus.service.ConfigurationService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Single settings window: general settings (theme, logs), project budgets and Kieku configuration as tabs.
 */
public class SettingsViewController implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SettingsViewController.class);

    @FXML
    private TabPane settingsTabPane;
    @FXML
    private CheckBox darkThemeCheckBox;
    @FXML
    private Label logDirectoryLabel;
    @FXML
    private TextField logUploadUrlField;
    @FXML
    private Button sendLogsButton;

    private final ConfigurationService configurationService = new ConfigurationService();
    private final AlertService alertService = new AlertService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        darkThemeCheckBox.setSelected(TuntikirjausApplication.isDarkTheme());
        darkThemeCheckBox.selectedProperty().addListener((observable, oldValue, dark) -> {
            TuntikirjausApplication.setDarkTheme(dark);
            TuntikirjausApplication.applyTheme(settingsTabPane.getScene());
        });

        logDirectoryLabel.setText("Lokitiedostot tallentuvat kansioon: " + LogFiles.logDirectory());
        configurationService.getConfiguration(LogUploadService.UPLOAD_URL_KEY)
                .map(Configuration::getValue)
                .ifPresent(logUploadUrlField::setText);
    }

    @FXML
    protected void onSendLogsButtonClick() {
        String url = logUploadUrlField.getText() == null ? "" : logUploadUrlField.getText().trim();
        try {
            LogUploadService.validateUrl(url);
        } catch (LogUploadException e) {
            alertService.showGeneralAlert(e.getMessage());
            return;
        }
        configurationService.insertOrUpdate(new Configuration(LogUploadService.UPLOAD_URL_KEY, url));

        if (!alertService.showConfirmationAlert("Lähetetäänkö lokit?",
                "Viimeisen viikon lokitiedostot lähetetään osoitteeseen " + url + ". "
                        + "Lokeista on poistettu arkaluontoiset tiedot, kuten käyttäjätunnus ja kirjausten aiheet.")) {
            return;
        }

        LOGGER.info("User requested sending logs");
        sendLogsButton.setDisable(true);
        // Sending can take a while, so it is done outside the JavaFX thread to keep the window responsive
        Task<Void> uploadTask = new Task<>() {
            @Override
            protected Void call() {
                new LogUploadService().uploadLogs(url);
                return null;
            }
        };
        uploadTask.setOnSucceeded(event -> {
            sendLogsButton.setDisable(false);
            alertService.showNotificationAlert("Lokit lähetettiin onnistuneesti.");
        });
        uploadTask.setOnFailed(event -> {
            sendLogsButton.setDisable(false);
            Throwable error = uploadTask.getException();
            if (!(error instanceof LogUploadException)) {
                LOGGER.error("Sending logs failed", error);
            }
            alertService.showGeneralAlert(error.getMessage());
        });
        Thread uploadThread = new Thread(uploadTask, "log-upload");
        uploadThread.setDaemon(true);
        uploadThread.start();
    }

    @FXML
    protected void onCloseButtonClick() {
        Stage stage = (Stage) settingsTabPane.getScene().getWindow();
        stage.close();
    }
}
