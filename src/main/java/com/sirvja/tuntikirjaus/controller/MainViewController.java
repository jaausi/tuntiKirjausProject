package com.sirvja.tuntikirjaus.controller;

import com.sirvja.tuntikirjaus.TuntikirjausApplication;
import com.sirvja.tuntikirjaus.domain.Paiva;
import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;
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

        tunnitColumn.setEditable(true);
        tuntiTaulukko.setEditable(true);

        getTuntiDataFromDb();

        updateEndTimes();
        generateYhteenveto();

        tuntiTaulukko.setItems(tuntiData);
        daysListView.setItems(paivaData);
    }

    private void getTuntiDataFromDb(){
        LOGGER.debug("Gettin kirjaus' from database...");
        ObservableList<TuntiKirjaus> allKirjaus = tuntiKirjausDao.getAll();
        LOGGER.debug("Found {} kirjaus' from database.", allKirjaus.size());

        dateToTuntidata = allKirjaus.stream()
                .collect(
                        Collectors.groupingBy(
                                TuntiKirjaus::getLocalDateOfStartTime,
                                Collectors.toCollection(FXCollections::observableArrayList)
                        )
                );

        LOGGER.debug("Kirjaus' map: {}", dateToTuntidata);

        paivaData = dateToTuntidata.keySet().stream()
                .map(Paiva::new)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        Optional<Paiva> latestDay = paivaData.stream()
                        .max(Paiva::compareTo);

        tuntiData = latestDay.map(paiva -> dateToTuntidata.get(paiva.getLocalDate())).orElse(FXCollections.observableArrayList());

    }

    private void populateTestData(){
        tuntiData.add(new TuntiKirjaus(LocalDateTime.now().minusHours(3), LocalDateTime.now().minusHours(4), "IBD-1234 Migraatiot", true));
        tuntiData.add(new TuntiKirjaus(LocalDateTime.now().minusHours(4), LocalDateTime.now().minusHours(5), "IBD-1334 Lomakemuutokset", true));
        tuntiData.add(new TuntiKirjaus(LocalDateTime.now().minusHours(5), LocalDateTime.now().minusHours(6), "IBD-1234 Migraatiot", true));
        tuntiData.add(new TuntiKirjaus(LocalDateTime.now().minusHours(6), LocalDateTime.now().minusHours(7), "IBD-1234 Migraatiot", true));
        tuntiData.add(new TuntiKirjaus(LocalDateTime.now().minusHours(7), null, "IBD-1234 Migraatiot", true));

        daysListView.getItems().add(new Paiva(LocalDate.now()));
        daysListView.getItems().add(new Paiva(LocalDate.now().minusDays(1)));
        daysListView.getItems().add(new Paiva(LocalDate.now().minusDays(2)));
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

        Optional<LocalDateTime> optionalTime = parseTimeFromString(time);
        if(optionalTime.isEmpty()){
            return;
        }

        TuntiKirjaus tuntiKirjaus = new TuntiKirjaus(optionalTime.get(), null, topic, true);

        if(!tuntiData.isEmpty() && tuntiData.get(tuntiData.size()-1).compareTo(tuntiKirjaus) > 0){
            kellonAikaField.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");
            showNotCorrectTimeAlert();
            return;
        }

        setStuffToTable(tuntiKirjaus);
        generateYhteenveto();
        tuntiKirjausDao.save(tuntiKirjaus);
        kellonAikaField.clear();
        aiheField.clear();
    }

    private Optional<LocalDateTime> parseTimeFromString(String time){
        LocalDateTime localDateTime;

        LOGGER.debug("Received {} from time field. Trying to parse...", time);
        try{
            if(!time.isEmpty()){
                if(time.contains(":")){ // Parse hours and minutes '9:00' or '12:00'
                    localDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.parse(time, DateTimeFormatter.ofPattern("H:mm")));
                } else {
                    if (time.length() <= 2){ // Parse only hours '9' or '12'
                        localDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.parse(time, DateTimeFormatter.ofPattern("H")));
                    } else if(time.length() <= 4){ // Parse hours and minutes '922' -> 9:22 or '1222' -> '12:22'
                        localDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.parse(time, DateTimeFormatter.ofPattern("Hmm")));
                    } else {
                        LOGGER.error("Couldn't parse time from String: {}", time);
                        return Optional.empty();
                    }
                }
            } else {
                localDateTime = LocalDateTime.now();
            }
        } catch (DateTimeParseException e){
            LOGGER.error("Error in parsing time from String: {}. Exception message: {}", time, e.getMessage());
            kellonAikaField.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");
            showTimeInWrongFormatAlert(e.getMessage());
            return Optional.empty();
        }

        return Optional.of(localDateTime);
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

    private void setStuffToTable(TuntiKirjaus tuntiKirjaus){
        LOGGER.debug("Setting stuff to the table!");

        updateEndTimes();

        tuntiData.add(tuntiKirjaus);

        tuntiTaulukko.setItems(tuntiData);
    }

    private void updateEndTimes(){
        tuntiData.sort(TuntiKirjaus::compareTo);

        IntStream.range(0, tuntiData.size()-1)
                .forEach(index ->
                        tuntiData.get(index).setEndTime(tuntiData.get(index+1).getStartTime()));


    }

    private void generateYhteenveto(){
        Predicate<TuntiKirjaus> predicate = Predicate.not(TuntiKirjaus::isEndTimeNull).and(TuntiKirjaus::isDurationEnabled);

        Map<String, String> topicToDuration = tuntiData.stream()
                .filter(predicate)
                .collect(
                        Collectors.groupingBy(
                                TuntiKirjaus::getTopic,
                                Collectors.collectingAndThen(
                                        Collectors.summingLong(t -> t.getDuration().toMinutes()),
                                        minutes -> String.format("%s:%s", minutes/60, (minutes%60 < 10 ? "0"+minutes%60 : minutes%60))
                                )
                        )
                );

        StringBuilder returnValue = new StringBuilder();
        for(Map.Entry<String, String> entry: topicToDuration.entrySet()){
            returnValue.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }

        yhteenvetoTextArea.setText(returnValue.toString());
    }


}
