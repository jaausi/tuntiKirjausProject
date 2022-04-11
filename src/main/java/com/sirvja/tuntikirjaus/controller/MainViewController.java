package com.sirvja.tuntikirjaus.controller;

import com.sirvja.tuntikirjaus.TuntikirjausApplication;
import com.sirvja.tuntikirjaus.domain.Paiva;
import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;
import com.sirvja.tuntikirjaus.service.MainViewService;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

public class MainViewController implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainViewController.class);

    @FXML
    private TableView<TuntiKirjaus> tuntiTaulukko = new TableView<>();
    @FXML
    private TableColumn<TuntiKirjaus, LocalTime> kellonaikaColumn;
    @FXML
    private TableColumn<TuntiKirjaus, String> aiheColumn;
    @FXML
    private TableColumn<TuntiKirjaus, String> tunnitColumn;
    @FXML
    private TextField aiheField;
    @FXML
    private ListView<Paiva> daysListView = new ListView<>();
    @FXML
    private MenuItem changeThemeMenuItem;
    @FXML
    private MenuItem updateDurationsMenuItem;
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
    private Button valitsePaivaButton;
    @FXML
    private Font x3;
    @FXML
    private Color x4;
    @FXML
    private TextArea yhteenvetoTextArea;

    @Override
    public void initialize (URL url, ResourceBundle rb){
        kellonaikaColumn.setCellValueFactory(new PropertyValueFactory<TuntiKirjaus, LocalTime>("time"));
        aiheColumn.setCellValueFactory(new PropertyValueFactory<TuntiKirjaus, String>("topic"));
        tunnitColumn.setCellValueFactory(new PropertyValueFactory<TuntiKirjaus, String>("duration"));

        updateView();
        daysListView.getSelectionModel().selectFirst();
        MainViewService.setCurrentDate(Optional.ofNullable(daysListView.getSelectionModel().getSelectedItem()).orElse(new Paiva(LocalDate.now())));

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
            showFieldNotFilledAlert();
            return;
        }

        LocalDateTime localDateTime;
        try {
            localDateTime = MainViewService.parseTimeFromString(time);
        } catch (DateTimeParseException e){
            LOGGER.error("Error in parsing time from String: {}. Exception message: {}", time, e.getMessage());
            kellonAikaField.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");
            showTimeInWrongFormatAlert(e.getMessage());
            return;
        }

        TuntiKirjaus tuntiKirjaus = new TuntiKirjaus(localDateTime, null, topic, true);

        ObservableList<TuntiKirjaus> tuntidata = MainViewService.getTuntiDataForTable();

        if(!tuntidata.isEmpty() && tuntidata.get(tuntidata.size()-1).compareTo(tuntiKirjaus) > 0){
            kellonAikaField.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");
            showNotCorrectTimeAlert();
            return;
        }

        MainViewService.addTuntikirjaus(tuntiKirjaus);

        updateView();
    }

    @FXML
    protected void onValitsePaivaButtonClick() {
        LOGGER.debug("Valitse päivä painettu!");
        Paiva selectedPaiva = daysListView.getSelectionModel().getSelectedItem();
        LOGGER.debug("Following date selected: {}", selectedPaiva);
        MainViewService.setCurrentDate(selectedPaiva);
        updateView();
    }

    @FXML
    protected void onUusiPaivaButtonClick() {
        LOGGER.debug("Uusi päivä painettu!");
        MainViewService.setCurrentDate(new Paiva(LocalDate.now()));
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
        tuntiTaulukko.setItems(MainViewService.getTuntiDataForTable());
        daysListView.setItems(MainViewService.getPaivaDataForTable());
        yhteenvetoTextArea.setText(MainViewService.getYhteenvetoText());
        kellonAikaField.clear();
        aiheField.clear();
    }

    private void showFieldNotFilledAlert(){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Varoitus!");
        alert.setHeaderText("Pakollisia kenttiä täyttämättä");
        alert.setContentText("Punaisella korostettuihin kenttiin tulee syöttää" +
                " arvo ennen taulukkoon lisäämistä.");
        alert.showAndWait();
    }

    private void showNotCorrectTimeAlert(){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Varoitus!");
        alert.setHeaderText("Syötetty aika on pienempi kuin viimeisin aika");
        alert.setContentText("Syötä aika, joka on listan viimeisimmän ajan jälkeen.");
        alert.showAndWait();
    }

    private void showTimeInWrongFormatAlert(String problem){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Varoitus!");
        alert.setHeaderText("Syötetty aika on väärässä formaatissa");
        alert.setContentText("Virhe: "+problem);
        alert.showAndWait();
    }

}
