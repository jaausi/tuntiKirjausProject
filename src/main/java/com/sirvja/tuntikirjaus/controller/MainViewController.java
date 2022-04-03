package com.sirvja.tuntikirjaus.controller;

import com.sirvja.tuntikirjaus.TuntikirjausApplication;
import com.sirvja.tuntikirjaus.domain.Paiva;
import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;
import com.sirvja.tuntikirjaus.service.MainViewService;
import com.sirvja.tuntikirjaus.utils.TuntiKirjausDao;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MainViewController implements Initializable {

    private final TuntiKirjausDao tuntiKirjausDao = new TuntiKirjausDao();
    private Map<LocalDate, ObservableList<TuntiKirjaus>> dateToTuntidata = new HashMap<>();
    private ObservableList<Paiva> paivaData = FXCollections.observableArrayList();
    private ObservableList<TuntiKirjaus> tuntiData = FXCollections.observableArrayList();
    private static final Logger LOGGER = LoggerFactory.getLogger(MainViewController.class);

    @FXML
    private TableView<TuntiKirjaus> tuntiTaulukko = new TableView<>();
    @FXML
    private TableColumn<TuntiKirjaus, LocalTime> kellonaikaColumn;
    @FXML
    private TableColumn<TuntiKirjaus, String> aiheColumn;
    @FXML
    private TableColumn<TuntiKirjaus, Duration> tunnitColumn;
    @FXML
    private TextField aiheField;
    @FXML
    private ListView<Paiva> daysListView = new ListView<>();
    @FXML
    private MenuItem changeThemeMenuItem;
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
        tunnitColumn.setCellValueFactory(new PropertyValueFactory<TuntiKirjaus, Duration>("duration"));

        updateView();
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

        if(!tuntiData.isEmpty() && tuntiData.get(tuntiData.size()-1).compareTo(tuntiKirjaus) > 0){
            kellonAikaField.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");
            showNotCorrectTimeAlert();
            return;
        }

        MainViewService.addTuntikirjaus(tuntiKirjaus);

        updateView();
    }

    private void updateView(){
        tuntiTaulukko.setItems(MainViewService.getTuntiDataForTable());
        daysListView.setItems(MainViewService.getPaivaDataForTable());
        yhteenvetoTextArea.setText(MainViewService.getYhteenvetoText());
        kellonAikaField.clear();
        aiheField.clear();
    }


    @FXML
    protected void onValitsePaivaButtonClick() {
        LOGGER.debug("Valitse päivä painettu!");
    }

    @FXML
    protected void onUusiPaivaButtonClick() {
        LOGGER.debug("Uusi päivä painettu!");
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
