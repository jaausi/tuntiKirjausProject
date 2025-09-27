package com.sirvja.tuntikirjaus.controller;

import com.sirvja.tuntikirjaus.TuntikirjausApplication;
import com.sirvja.tuntikirjaus.domain.Paiva;
import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;
import com.sirvja.tuntikirjaus.exception.EmptyTopicException;
import com.sirvja.tuntikirjaus.exception.MalformatedTimeException;
import com.sirvja.tuntikirjaus.exception.StartTimeNotAfterLastTuntikirjausException;
import com.sirvja.tuntikirjaus.exception.TuntikirjausDatabaseInInconsistentStage;
import com.sirvja.tuntikirjaus.service.AlertService;
import com.sirvja.tuntikirjaus.service.MainViewService;
import com.sirvja.tuntikirjaus.customFields.AutoCompleteTextField;
import com.sirvja.tuntikirjaus.utils.CustomLocalTimeStringConverter;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.function.BiConsumer;

public class MainViewController implements Initializable {

    private MainViewService mainViewService;
    private AlertService alertService;

    private static final Logger log = LoggerFactory.getLogger(MainViewController.class);

    @FXML
    private TableView<TuntiKirjaus> tuntiTaulukko = new TableView<>();
    @FXML
    private TableColumn<TuntiKirjaus, LocalTime> kellonaikaColumn;
    @FXML
    private TableColumn<TuntiKirjaus, String> aiheColumn;
    @FXML
    private TableColumn<TuntiKirjaus, String> tunnitColumn;
    @FXML
    private AutoCompleteTextField<String> aiheField;
    @FXML
    private ListView<Paiva> daysListView = new ListView<>();
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

    public MainViewController() {
        this.mainViewService = new MainViewService();
        this.alertService = new AlertService();
    }

    @Override
    public void initialize (URL url, ResourceBundle rb){
        tuntiTaulukko.setEditable(true);

        kellonaikaColumn.setCellValueFactory(new PropertyValueFactory<TuntiKirjaus, LocalTime>("time"));
        aiheColumn.setCellValueFactory(new PropertyValueFactory<TuntiKirjaus, String>("topic"));
        tunnitColumn.setCellValueFactory(new PropertyValueFactory<TuntiKirjaus, String>("durationString"));

        kellonaikaColumn.setSortable(false);
        aiheColumn.setSortable(false);
        tunnitColumn.setSortable(false);

        setEditListenerToKellonaikaColumn();

        setEditListenerToAiheColumn();

        setListenerForDayListView();

        updateDayList();

        daysListView.getSelectionModel().selectFirst();

        initializeAutoCompleteAiheField();
    }

    private void initializeAutoCompleteAiheField() {
        aiheField.getEntries().addAll(mainViewService.getAiheEntries());
        aiheField.getLastSelectedObject().addListener((observableValue, oldValue, newValue) -> {
            if(newValue != null){
                aiheField.setText(newValue);
                aiheField.positionCaret(newValue.length());
                aiheField.setLastSelectedItem(null);
            }
        });
    }

    private void setListenerForDayListView() {
        daysListView.getSelectionModel().selectedItemProperty().addListener(mainViewService.getDayListChangeListener(() -> {
            updateTuntiTaulukko();
            updateYhteenveto();
        }));
    }

    private void setEditListenerToAiheColumn() {
        BiConsumer<String, String> updateAiheFieldEntry = (oldTopic, newTopic) -> {
            aiheField.getEntries().remove(oldTopic);
            aiheField.getEntries().add(newTopic);
        };

        aiheColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        aiheColumn.setOnEditCommit(mainViewService.getAiheColumnEditHandler(updateAiheFieldEntry));
    }

    private void setEditListenerToKellonaikaColumn() {
        kellonaikaColumn.setCellFactory(TextFieldTableCell.forTableColumn(new CustomLocalTimeStringConverter(mainViewService)));
        kellonaikaColumn.setOnEditCommit(mainViewService.getKellonaikaColumnEditHandler(tuntiTaulukko::refresh));
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
        aiheField.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER){
                log.debug("Enter was pressed");
                onTallennaTaulukkoonButtonClick();
            }
        });
    }

    @FXML
    protected void onKeyPressedToKellonaikaField(){
        kellonAikaField.setOnKeyPressed(event -> {
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
    protected void onOpenWeeklyReportMenuItem() {
        log.debug("Open day summary clicked!");

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(TuntikirjausApplication.class.getResource("reports_view_day_summary.fxml"), ResourceBundle.getBundle("com.sirvja.tuntikirjaus.i18n"));
            Parent root1 = fxmlLoader.load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Tuntikirjaus day view summary");
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

        String yhteenvetoText = mainViewService.getYhteenvetoText();
        ClipboardContent content = new ClipboardContent();
        content.putString(yhteenvetoText);
        Clipboard clipboard = Clipboard.getSystemClipboard();
        clipboard.setContent(content);
    }

    @FXML
    protected void onTallennaTaulukkoonButtonClick() {
        try {
            log.debug("Save to table button pushed!");
            TuntiKirjaus tuntiKirjaus = mainViewService.addTuntikirjaus(kellonAikaField.getText(), aiheField.getText());
            aiheField.getEntries().add(tuntiKirjaus.getTopic());
            initializeView();
        } catch (EmptyTopicException e) {
            log.error(e.getMessage());
            aiheField.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");
            alertService.showFieldNotFilledAlert();
        } catch (MalformatedTimeException e) {
            log.error(e.getMessage());
            kellonAikaField.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");
            alertService.showTimeInWrongFormatAlert(e.getMessage());
        } catch (StartTimeNotAfterLastTuntikirjausException e) {
            log.error(e.getMessage());
            kellonAikaField.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");
            alertService.showNotCorrectTimeAlert();
        } catch (TuntikirjausDatabaseInInconsistentStage e) {
            log.error(e.getMessage());
            alertService.showGeneralAlert(e.getMessage());
        }
    }

    @FXML
    protected void onPoistaKirjausButtonClick() {
        log.debug("Poista kirjaus painettu!");
        TuntiKirjaus selectedKirjaus = tuntiTaulukko.getSelectionModel().getSelectedItem();
        log.debug("Following kirjaus selected: {}", selectedKirjaus);
        if(!alertService.showConfirmationAlert("Oletko varma että haluat poistaa kirjauksen",
                String.format("Poistettava kirjaus: \n%s" +
                        "\nKirjauksen poistaminen muokkaa, poistettavaa edeltävän kirjauksen kestoa " +
                "siirtämällä lopetusajan poistettavan kirjauksen lopetusaikaan.", selectedKirjaus))){
            return;
        }
        mainViewService.removeTuntikirjaus(selectedKirjaus);
        initializeView();
    }

    @FXML
    protected void onUusiPaivaButtonClick() {
        log.debug("Uusi päivä clicked!");
        daysListView.getItems().addFirst(new Paiva(LocalDate.now()));
        daysListView.getSelectionModel().selectFirst();
    }

    @FXML
    protected void onKellonaikaFieldClick(){
        log.debug("Kellonaika field clicked!");
        kellonAikaField.setStyle("-fx-border-color: none ; -fx-border-width: 0px ;");
    }

    @FXML
    protected void onAiheFieldClick(){
        log.debug("Aihe field clicked!");
        aiheField.setStyle("-fx-border-color: none ; -fx-border-width: 0px ;");
    }

    private void initializeView(){
        updateTuntiTaulukko();
        updateDayList();
        updateYhteenveto();
        clearInputFields();
    }

    private void updateTuntiTaulukko() {
        tuntiTaulukko.setItems(mainViewService.getTuntiDataForTable());
        tuntiTaulukko.refresh();
    }

    private void updateYhteenveto() {
        yhteenvetoTextArea.setText(mainViewService.getYhteenvetoText());
    }

    private void clearInputFields() {
        kellonAikaField.clear();
        aiheField.clear();
    }

    private void updateDayList() {
        daysListView.setItems(mainViewService.getPaivaDataForTable());
        daysListView.refresh();
    }
}
