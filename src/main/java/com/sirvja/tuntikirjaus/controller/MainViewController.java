package com.sirvja.tuntikirjaus.controller;

import com.sirvja.tuntikirjaus.TuntikirjausApplication;
import com.sirvja.tuntikirjaus.customFields.AutoCompleteTextField;
import com.sirvja.tuntikirjaus.model.DayRecord;
import com.sirvja.tuntikirjaus.model.HourRecord;
import com.sirvja.tuntikirjaus.model.HourRecordTable;
import com.sirvja.tuntikirjaus.service.AlertService;
import com.sirvja.tuntikirjaus.service.HourRecordInputService;
import com.sirvja.tuntikirjaus.service.MainViewService;
import com.sirvja.tuntikirjaus.service.HourRecordTableService;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.ResourceBundle;

@Log4j2
@Component
public class MainViewController implements Initializable {

    private final HourRecordTableService hourRecordTableService;
    private final AlertService alertService;
    private final HourRecordInputService hourRecordInputService;

    // Main hour record table which shows the saved hour records
    @FXML
    private TableView<HourRecord> hourRecordTableView = new TableView<>();
    @FXML
    private TableColumn<HourRecord, LocalTime> timeTableColumn;
    @FXML
    private TableColumn<HourRecord, String> topicTableColumn;
    @FXML
    private TableColumn<HourRecord, String> durationTableColumn;

    // Fields from top center that are used to save hour records
    @FXML
    private TextField timeField;
    @FXML
    private AutoCompleteTextField<String> topicField;
    @FXML
    private Button saveToTableButton;
    @FXML
    private Button deleteFromTableButton;

    // List and button from left that shows days
    @FXML
    private ListView<DayRecord> daysListView = new ListView<>();
    @FXML
    private Button addCurrentDayButton;

    // Text area and button on right that shows summary of the project for the day
    @FXML
    private TextArea summaryTextArea;
    @FXML
    private Button saveToClipboardButton;

    @FXML
    private MenuItem changeThemeMenuItem;
    @FXML
    private MenuItem updateDurationsMenuItem;
    @FXML
    private MenuItem reportsMenuItem;
    @FXML
    private MenuItem undoMenuItem;
    @FXML
    private MenuItem redoMenuItem;
    @FXML
    private MenuItem aboutMenuItem;
    @FXML
    private Font x3;
    @FXML
    private Color x4;
    private Object valueBeforeEdit;

    public MainViewController(HourRecordTableService hourRecordTableService, AlertService alertService, HourRecordInputService hourRecordInputService) {
        this.hourRecordTableService = hourRecordTableService;
        this.alertService = alertService;
        this.hourRecordInputService = hourRecordInputService;
    }

    @FXML
    @Override
    public void initialize (URL url, ResourceBundle rb){

        hourRecordTableService.initializeHourRecordTable(
                new HourRecordTable(hourRecordTableView, timeTableColumn, topicTableColumn, durationTableColumn)
        );

        updateView();

        setListenerForDayListView();

        daysListView.getSelectionModel().selectFirst();

        MainViewService.setCurrentDate(Optional.ofNullable(daysListView.getSelectionModel().getSelectedItem()).orElse(new DayRecord(LocalDate.now())));

        hourRecordInputService.initializeTopicField(topicField);
    }

    private void setListenerForDayListView() {
        // Add listener for ListView changes: https://stackoverflow.com/questions/12459086/how-to-perform-an-action-by-selecting-an-item-from-listview-in-javafx-2
        daysListView.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            if(newValue != null){
                MainViewService.setCurrentDate(newValue);
                updateView();
            }
        });
    }

    @FXML
    protected void onChangeAboutMenuItem() {

    }

    @FXML
    protected void onChangeRedoMenuItem() {

    }

    @FXML
    protected void onChangeUndoMenuItem() {

    }

    @FXML
    protected void onChangeUpdateDurationsMenuItemAction() {
        log.debug("Update durations clicked!");

    }

    @FXML
    protected void onKeyPressedToAiheField(){

    }

    @FXML
    protected void onKeyPressedToKellonaikaField(){
        timeField.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER){
                log.debug("Enter was pressed");
                onTallennaTaulukkoonButtonClick();
            }
        });
    }

    @FXML
    protected void onChangeThemeMenuItemAction(){
        log.debug("Change theme clicked!");

        ObservableList<String> styleSheets = TuntikirjausApplication.stage.getScene().getStylesheets();
        String darkThemeFile = String.valueOf(TuntikirjausApplication.class.getResource("main-view_dark.css"));
        if(styleSheets.contains(darkThemeFile)){
            styleSheets.remove(darkThemeFile);
        } else {
            styleSheets.add(darkThemeFile);
        }
    }

    @FXML
    protected void onOpenReportsMenuItem(){
        log.debug("Open reports clicked!");

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(TuntikirjausApplication.class.getResource("reports_view.fxml"));
            Parent root1 = fxmlLoader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Tuntikirjaus reporting");
            Scene scene = new Scene(root1);

            ObservableList<String> styleSheets = TuntikirjausApplication.stage.getScene().getStylesheets();
            String darkThemeFile = String.valueOf(TuntikirjausApplication.class.getResource("main-view_dark.css"));
            if(styleSheets.contains(darkThemeFile)){
                scene.getStylesheets().add(String.valueOf(TuntikirjausApplication.class.getResource("main-view_dark.css")));
            }

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    protected void onTallennaLeikepoydalleButtonClick(){
        log.debug("Save to clipboard button pushed!");

        String yhteenvetoText = MainViewService.getYhteenvetoText();
        ClipboardContent content = new ClipboardContent();
        content.putString(yhteenvetoText);
        Clipboard clipboard = Clipboard.getSystemClipboard();
        clipboard.setContent(content);
    }

    @FXML
    protected void onTallennaTaulukkoonButtonClick() {

    }

    @FXML
    protected void onPoistaKirjausButtonClick() {
        log.debug("Poista kirjaus painettu!");
        HourRecord selectedKirjaus = hourRecordTableView.getSelectionModel().getSelectedItem();
        log.debug("Following kirjaus selected: {}", selectedKirjaus);
        if(!alertService.showConfirmationAlert("Oletko varma että haluat poistaa kirjauksen",
                String.format("Poistettava kirjaus: \n%s" +
                        "\nKirjauksen poistaminen muokkaa, poistettavaa edeltävän kirjauksen kestoa " +
                "siirtämällä lopetusajan poistettavan kirjauksen lopetusaikaan.", selectedKirjaus))){
            return;
        }
        MainViewService.removeTuntikirjaus(selectedKirjaus);
        updateView();
    }

    @FXML
    protected void onUusiPaivaButtonClick() {
        log.debug("Uusi päivä painettu!");
        MainViewService.setCurrentDate(new DayRecord(LocalDate.now()));
        updateView();
    }

    @FXML
    protected void onKellonaikaFieldClick(){
        log.debug("Kellonaika field clicked!");
        timeField.setStyle("-fx-border-color: none ; -fx-border-width: 0px ;");
    }

    @FXML
    protected void onAiheFieldClick(){
        log.debug("Aihe field clicked!");
        topicField.setStyle("-fx-border-color: none ; -fx-border-width: 0px ;");
    }

    private void updateView(){
        // hourRecordTableService.setHourRecordsToTable(); // TODO: Fix me

        hourRecordTableView.setItems(MainViewService.getTuntiDataForTable());
        hourRecordTableView.refresh();
        daysListView.setItems(MainViewService.getPaivaDataForTable());
        summaryTextArea.setText(MainViewService.getYhteenvetoText());

        timeField.clear();
        topicField.clear();
    }

    public static void showTimeInWrongFormatAlert(String problem){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Varoitus!");
        alert.setHeaderText("Syötetty aika on väärässä formaatissa");
        alert.setContentText("Virhe: "+problem);
        alert.showAndWait();
    }

}
