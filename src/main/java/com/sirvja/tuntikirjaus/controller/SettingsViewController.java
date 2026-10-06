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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

/**
 * Single settings window: general settings (theme, logs, configuration export and import), project budgets,
 * Kieku configuration and Kieku project mappings as tabs.
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
    @FXML
    private ProjectBudgetViewController projectBudgetViewController;
    @FXML
    private ConfigurationViewController kiekuConfigurationViewController;
    @FXML
    private KiekuProjectMappingViewController kiekuProjectMappingViewController;

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
        loadLogUploadUrl();
    }

    private void loadLogUploadUrl() {
        configurationService.getConfiguration(LogUploadService.UPLOAD_URL_KEY)
                .map(Configuration::getValue)
                .ifPresent(logUploadUrlField::setText);
    }

    @FXML
    protected void onExportConfigurationButtonClick() {
        FileChooser fileChooser = configurationFileChooser("Vie asetukset tiedostoon");
        fileChooser.setInitialFileName("tuntikirjaus-asetukset-" + LocalDate.now() + ".properties");
        File file = fileChooser.showSaveDialog(settingsTabPane.getScene().getWindow());
        if (file == null) {
            return;
        }
        try {
            int count = configurationService.exportConfiguration(file.toPath());
            alertService.showNotificationAlert(count + " asetusta vietiin tiedostoon " + file.getName()
                    + ". Tiedosto voi sisältää organisaatiokohtaisia tietoja, joten älä lisää sitä versionhallintaan.");
        } catch (IOException e) {
            LOGGER.error("Exporting configuration failed", e);
            alertService.showGeneralAlert("Asetusten vienti epäonnistui: " + e.getMessage());
        }
    }

    @FXML
    protected void onImportConfigurationButtonClick() {
        File file = configurationFileChooser("Tuo asetukset tiedostosta")
                .showOpenDialog(settingsTabPane.getScene().getWindow());
        if (file == null) {
            return;
        }
        if (!alertService.showConfirmationAlert("Tuodaanko asetukset?",
                "Tiedoston " + file.getName() + " asetukset korvaavat samannimiset nykyiset asetukset.")) {
            return;
        }
        try {
            int count = configurationService.importConfiguration(file.toPath());
            reloadTabs();
            alertService.showNotificationAlert(count + " asetusta tuotiin tiedostosta " + file.getName() + ".");
        } catch (IOException | IllegalArgumentException e) {
            LOGGER.warn("Importing configuration failed: {}", e.getMessage());
            alertService.showGeneralAlert("Asetusten tuonti epäonnistui: " + e.getMessage());
        }
    }

    private static FileChooser configurationFileChooser(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Asetustiedosto (*.properties)", "*.properties"),
                new FileChooser.ExtensionFilter("Kaikki tiedostot", "*.*"));
        return fileChooser;
    }

    private void reloadTabs() {
        loadLogUploadUrl();
        projectBudgetViewController.reload();
        kiekuConfigurationViewController.reload();
        kiekuProjectMappingViewController.reload();
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
