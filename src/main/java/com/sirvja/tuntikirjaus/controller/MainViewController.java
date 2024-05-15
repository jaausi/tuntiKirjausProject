package com.sirvja.tuntikirjaus.controller;

import com.sirvja.tuntikirjaus.TuntikirjausApplication;
import com.sirvja.tuntikirjaus.customFields.AutoCompleteTextField;
import com.sirvja.tuntikirjaus.model.DayRecord;
import com.sirvja.tuntikirjaus.model.HourRecord;
import com.sirvja.tuntikirjaus.model.HourRecordTable;
import com.sirvja.tuntikirjaus.service.AlertService;
import com.sirvja.tuntikirjaus.service.MainViewService;
import com.sirvja.tuntikirjaus.service.HourRecordTableService;
import com.sirvja.tuntikirjaus.utils.CustomLocalTimeStringConverter;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
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
import java.util.TreeSet;

@Component
public class MainViewController implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainViewController.class);

    private final HourRecordTableService hourRecordTableService;
    private final AlertService alertService;

    // Main hour record table which shows the saved hour records
    @FXML
    private TableView<HourRecord> hourRecordTableView = new TableView<>();
    @FXML
    private TableColumn<HourRecord, LocalTime> timeTableColumn;
    @FXML
    private TableColumn<HourRecord, String> topicTableColumn;
    @FXML
    private TableColumn<HourRecord, String> durationTableColumn;


    @FXML
    private AutoCompleteTextField<String> aiheField;
    @FXML
    private ListView<DayRecord> daysListView = new ListView<>();
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
    private TextField kellonAikaField;
    @FXML
    private Button tallennaLeikepoydalleButton;
    @FXML
    private Button tallennaTaulukkoonButton;
    @FXML
    private Button uusiPaivaButton;
    @FXML
    private Button poistaKirjausButton;
    @FXML
    private Font x3;
    @FXML
    private Color x4;
    @FXML
    private TextArea yhteenvetoTextArea;
    private Object valueBeforeEdit;

    public MainViewController(HourRecordTableService hourRecordTableService, AlertService alertService) {
        this.hourRecordTableService = hourRecordTableService;
        this.alertService = alertService;
    }

    @FXML
    @Override
    public void initialize (URL url, ResourceBundle rb){

        hourRecordTableService.initializeHourRecordTable(new HourRecordTable(
                hourRecordTableView,
                timeTableColumn,
                topicTableColumn,
                durationTableColumn
        ));

        setEditListenerToKellonaikaColumn();

        setEditListenetToAiheColumn();

        updateView();

        setListenerForDayListView();

        daysListView.getSelectionModel().selectFirst();

        MainViewService.setCurrentDate(Optional.ofNullable(daysListView.getSelectionModel().getSelectedItem()).orElse(new DayRecord(LocalDate.now())));

        initializeAutoCompleteAiheField();
    }

    private void initializeAutoCompleteAiheField() {
        aiheField.getEntries().addAll(MainViewService.getAiheEntries().orElse(new TreeSet<>()));
        aiheField.getLastSelectedObject().addListener((observableValue, oldValue, newValue) -> {
            if(newValue != null){
                aiheField.setText(newValue);
                aiheField.positionCaret(newValue.length());
                aiheField.setLastSelectedItem(null);
            }
        });
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

    private void setEditListenetToAiheColumn() {
        topicTableColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        topicTableColumn.setOnEditCommit(
                t -> {
                    HourRecord kirjausToEdit = t.getTableView().getItems().get(t.getTablePosition().getRow());
                    kirjausToEdit.setTopic(t.getNewValue());
                    MainViewService.update(kirjausToEdit);
                }
        );
    }

    private void setEditListenerToKellonaikaColumn() {
        timeTableColumn.setCellFactory(TextFieldTableCell.forTableColumn(new CustomLocalTimeStringConverter()));
        timeTableColumn.setOnEditCommit(
                t -> {
                    int tablePosition = t.getTablePosition().getRow();
                    int lastPosition = t.getTableView().getItems().size() - 1;
                    LocalDateTime newValue = LocalDateTime.of(MainViewService.getCurrentDate(), t.getNewValue());
                    boolean facedError = false;

                    if(tablePosition < lastPosition){
                        HourRecord followingKirjausToEdit = t.getTableView().getItems().get(tablePosition + 1);
                        // If edited time is after next kirjaus start time, abort.
                        if(newValue.isAfter(followingKirjausToEdit.getStartTime())){
                            alertService.showNotCorrectTimeAlert(true);
                            facedError = true;
                        }
                    }

                    // If not the first row of a day. Edit also the previous row end time.
                    if(tablePosition > 0){
                        HourRecord previousKirjausToEdit = t.getTableView().getItems().get(tablePosition - 1);
                        // If edited time is before previous kirjaus start time, abort.
                        if(newValue.isBefore(previousKirjausToEdit.getStartTime())){
                            alertService.showNotCorrectTimeAlert(false);
                            facedError = true;
                        }
                        if(!facedError){
                            previousKirjausToEdit.setEndTime(newValue);
                            MainViewService.update(previousKirjausToEdit);
                        }
                    }

                    HourRecord kirjausToEdit = t.getTableView().getItems().get(tablePosition);
                    if(!facedError){
                        kirjausToEdit.setStartTime(newValue);
                        MainViewService.update(kirjausToEdit);
                    }
                    hourRecordTableView.refresh();
                }
        );
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
        LOGGER.debug("Update durations clicked!");

    }

    @FXML
    protected void onKeyPressedToAiheField(){
        aiheField.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER){
                LOGGER.debug("Enter was pressed");
                onTallennaTaulukkoonButtonClick();
            }
        });
    }

    @FXML
    protected void onKeyPressedToKellonaikaField(){
        kellonAikaField.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER){
                LOGGER.debug("Enter was pressed");
                onTallennaTaulukkoonButtonClick();
            }
        });
    }

    @FXML
    protected void onChangeThemeMenuItemAction(){
        LOGGER.debug("Change theme clicked!");

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
        LOGGER.debug("Open reports clicked!");

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
        LOGGER.debug("Save to clipboard button pushed!");

        String yhteenvetoText = MainViewService.getYhteenvetoText();
        ClipboardContent content = new ClipboardContent();
        content.putString(yhteenvetoText);
        Clipboard clipboard = Clipboard.getSystemClipboard();
        clipboard.setContent(content);
    }

    @FXML
    protected void onTallennaTaulukkoonButtonClick() {
        String time = kellonAikaField.getText();
        String topic = aiheField.getText();
        LOGGER.debug("Save to table button pushed!");

        if(topic.isEmpty()){
            aiheField.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");
            alertService.showFieldNotFilledAlert();
            return;
        }

        LocalDateTime localDateTime;
        try {
            localDateTime = MainViewService.parseTimeFromString(time);
        } catch (DateTimeParseException e){
            LOGGER.error("Error in parsing time from String: {}. Exception message: {}", time, e.getMessage());
            kellonAikaField.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");
            alertService.showTimeInWrongFormatAlert(e.getMessage());
            return;
        }

        HourRecord hourRecord = new HourRecord(localDateTime, null, topic, true);

        ObservableList<HourRecord> tuntidata = MainViewService.getTuntiDataForTable();

        if(!tuntidata.isEmpty() && tuntidata.get(tuntidata.size()-1).compareTo(hourRecord) > 0){
            kellonAikaField.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");
            alertService.showNotCorrectTimeAlert();
            return;
        }

        MainViewService.addTuntikirjaus(hourRecord);

        aiheField.getEntries().add(hourRecord.getTopic());

        updateView();
    }

    @FXML
    protected void onPoistaKirjausButtonClick() {
        LOGGER.debug("Poista kirjaus painettu!");
        HourRecord selectedKirjaus = hourRecordTableView.getSelectionModel().getSelectedItem();
        LOGGER.debug("Following kirjaus selected: {}", selectedKirjaus);
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
        LOGGER.debug("Uusi päivä painettu!");
        MainViewService.setCurrentDate(new DayRecord(LocalDate.now()));
        updateView();
    }

    @FXML
    protected void onKellonaikaFieldClick(){
        LOGGER.debug("Kellonaika field clicked!");
        kellonAikaField.setStyle("-fx-border-color: none ; -fx-border-width: 0px ;");
    }

    @FXML
    protected void onAiheFieldClick(){
        LOGGER.debug("Aihe field clicked!");
        aiheField.setStyle("-fx-border-color: none ; -fx-border-width: 0px ;");
    }

    private void updateView(){
        hourRecordTableView.setItems(MainViewService.getTuntiDataForTable());
        hourRecordTableView.refresh();
        daysListView.setItems(MainViewService.getPaivaDataForTable());
        yhteenvetoTextArea.setText(MainViewService.getYhteenvetoText());
        kellonAikaField.clear();
        aiheField.clear();
    }

    public static void showTimeInWrongFormatAlert(String problem){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Varoitus!");
        alert.setHeaderText("Syötetty aika on väärässä formaatissa");
        alert.setContentText("Virhe: "+problem);
        alert.showAndWait();
    }

}
