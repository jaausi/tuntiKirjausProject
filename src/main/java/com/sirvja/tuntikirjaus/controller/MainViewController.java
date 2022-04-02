package com.sirvja.tuntikirjaus.controller;

import com.sirvja.tuntikirjaus.TuntikirjausApplication;
import com.sirvja.tuntikirjaus.domain.Paiva;
import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;
import com.sirvja.tuntikirjaus.utils.DBUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MainViewController implements Initializable {
    @FXML
    private TableView<TuntiKirjaus> tuntiTaulukko = new TableView<>();

    ObservableList<TuntiKirjaus> tuntiData = FXCollections.observableArrayList();

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

        populateDataToTable();

        tuntiTaulukko.setItems(tuntiData);
    }

    private void populateDataToTable(){
        tuntiData = DBUtil.getAllTuntikirjaus();
        updateDurations();
        generateYhteenveto();
    }

    private void populateTestData(){
        tuntiData.add(new TuntiKirjaus(LocalDateTime.now().minusHours(3), "IBD-1234 Migraatiot", Duration.ofHours(1).plusMinutes(5), true));
        tuntiData.add(new TuntiKirjaus(LocalDateTime.now().minusHours(4), "IBD-1334 Lomakemuutokset", Duration.ofHours(1).plusMinutes(5), true));
        tuntiData.add(new TuntiKirjaus(LocalDateTime.now().minusHours(5), "IBD-1234 Migraatiot", Duration.ofHours(1).plusMinutes(5), true));
        tuntiData.add(new TuntiKirjaus(LocalDateTime.now().minusHours(6), "IBD-1234 Migraatiot", Duration.ofHours(1).plusMinutes(5), true));
        tuntiData.add(new TuntiKirjaus(LocalDateTime.now().minusHours(7), "IBD-1234 Migraatiot", Duration.ofHours(1).plusMinutes(5), true));

        daysListView.getItems().add(new Paiva(LocalDate.now()));
        daysListView.getItems().add(new Paiva(LocalDate.now().minusDays(1)));
        daysListView.getItems().add(new Paiva(LocalDate.now().minusDays(2)));
    }

    @FXML
    protected void onKeyPressedToAiheField(){
        aiheField.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER){
                System.out.println("Enter was pressed");
                onTallennaTaulukkoonButtonClick();
            }
        });
    }

    @FXML
    protected void onChangeThemeMenuItemAction(){
        System.out.println("Change theme clicked!");

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
        String kellonaika = kellonAikaField.getText();
        String aihe = aiheField.getText();
        System.out.println("Tallenna taulukkoon painettu!");

        if(aihe.isEmpty()){
            aiheField.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");
            showFieldNotFilledAlert();
            return;
        }

        if(kellonaika.isEmpty()){
            kellonaika = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        }

        LocalDateTime ajankohta = LocalDateTime.of(LocalDate.now(), LocalTime.parse(kellonaika));
        TuntiKirjaus tuntiKirjaus = new TuntiKirjaus(ajankohta, aihe, true);

        if(!tuntiData.isEmpty() && tuntiData.get(tuntiData.size()-1).compareTo(tuntiKirjaus) > 0){
            kellonAikaField.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");
            showNotCorrectTimeAlert();
            return;
        }

        setStuffToTable(tuntiKirjaus);
        generateYhteenveto();
        try{
            DBUtil.insertTuntikirjaus(tuntiKirjaus);
        } catch (Exception e){
            System.out.println("Ongelmia kirjauksen tallentamisessa: " + e);
        }
        kellonAikaField.clear();
        aiheField.clear();
    }

    @FXML
    protected void onValitsePaivaButtonClick() {
        System.out.println("Valitse päivä painettu!");
    }

    @FXML
    protected void onUusiPaivaButtonClick() {
        System.out.println("Uusi päivä painettu!");
    }

    @FXML
    protected void onKellonaikaFieldClick(){
        System.out.println("Kellonaika field clicked!");
        kellonAikaField.setStyle("-fx-border-color: none ; -fx-border-width: 0px ;");
    }

    @FXML
    protected void onAiheFieldClick(){
        System.out.println("Aihe field clicked!");
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

    private void setStuffToTable(TuntiKirjaus tuntiKirjaus){
        System.out.println("Setting stuff to the table!");
        TuntiKirjaus lastKirjaus = null;
        if(!tuntiData.isEmpty()){
            lastKirjaus = tuntiData.get(tuntiData.size()-1);
        }

        if(lastKirjaus != null && lastKirjaus.getDuration() == null){
            System.out.println("No last kirjaus set");
            Duration duration = Duration.between(lastKirjaus.getTime(), tuntiKirjaus.getTime());
            lastKirjaus.setDuration(duration);
            tuntiData.set(tuntiData.size()-1, lastKirjaus);
        }

        tuntiData.add(tuntiKirjaus);
    }

    private void updateDurations(){
        tuntiData.sort(TuntiKirjaus::compareTo);

        TuntiKirjaus previousKirjaus = null;
        int index = 0;
        for(TuntiKirjaus tuntiKirjaus: tuntiData){
            if(previousKirjaus != null && previousKirjaus.getDurationEnabled()){
                Duration duration = Duration.between(previousKirjaus.getTime(), tuntiKirjaus.getTime());
                previousKirjaus.setDuration(duration);
                tuntiData.set(index, previousKirjaus);
            }
            // FIXME: mutating original array
            previousKirjaus = tuntiKirjaus.clone();
            index++;
        }
    }

    private String calculateDuration(String uusiKirjaus, String vanhaKirjaus){
        int uusiTunnit = Integer.parseInt(uusiKirjaus.split(":")[0]);
        int uusiMinuutit = Integer.parseInt(uusiKirjaus.split(":")[1]);
        int vanhaTunnit = Integer.parseInt(vanhaKirjaus.split(":")[0]);
        int vanhaMinuutit = Integer.parseInt(vanhaKirjaus.split(":")[1]);

        int tunnit = uusiTunnit - vanhaTunnit;
        int minuutit = uusiMinuutit - vanhaMinuutit;

        if(minuutit < 0){
            tunnit--;
            minuutit+=60;
        }

        String minuutitString = minuutit < 10 ? "0"+minuutit : ""+minuutit;
        return tunnit + ":" + minuutitString;
    }

    private void generateYhteenveto(){
        Predicate<TuntiKirjaus> predicate = Predicate.not(TuntiKirjaus::isEndTimeNull);

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
