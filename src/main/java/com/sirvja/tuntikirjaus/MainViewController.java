package com.sirvja.tuntikirjaus;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MainViewController implements Initializable {
    @FXML
    private TableView<TableTuntiKirjaus> tuntiTaulukko = new TableView<>();

    ObservableList<TableTuntiKirjaus> tuntiData =
            FXCollections.observableArrayList(
                    new TableTuntiKirjaus("12:05", "IBD-1234 Migraatiot", "1:01"),
                    new TableTuntiKirjaus("13:05", "IBD-1334 Lomakemuutokset", "1:02"),
                    new TableTuntiKirjaus("14:05", "IBD-1234 Migraatiot", "1:03"),
                    new TableTuntiKirjaus("15:05", "IBD-1234 Migraatiot", "1:04"),
                    new TableTuntiKirjaus("16:05", "IBD-1234 Migraatiot", "")
            );

    @FXML
    private TableColumn<TableTuntiKirjaus, String> kellonaikaColumn;

    @FXML
    private TableColumn<TableTuntiKirjaus, String> aiheColumn;

    @FXML
    private TableColumn<TableTuntiKirjaus, String> tunnitColumn;

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


        kellonaikaColumn.setCellValueFactory(new PropertyValueFactory<TableTuntiKirjaus, String>("time"));
        aiheColumn.setCellValueFactory(new PropertyValueFactory<TableTuntiKirjaus, String>("topic"));
        tunnitColumn.setCellValueFactory(new PropertyValueFactory<TableTuntiKirjaus, String>("duration"));

        tunnitColumn.setEditable(true);
        tuntiTaulukko.setEditable(true);
        tuntiTaulukko.setItems(tuntiData);

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

        if(tuntiData.get(tuntiData.size()-1).compareTime(kellonaika) > 0){
            kellonAikaField.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");
            showNotCorrectTimeAlert();
            return;
        }

        setStuffToTable(kellonaika, aihe);
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

    private void setStuffToTable(String kellonaika, String aihe){
        System.out.println("Setting stuff to the table!");
        TableTuntiKirjaus kirjaus = new TableTuntiKirjaus(kellonaika, aihe, "");
        TableTuntiKirjaus lastKirjaus = tuntiData.get(tuntiData.size()-1);
        System.out.println("Kirjaus: "+lastKirjaus.getDuration());

        if(lastKirjaus.getDuration().isEmpty()){
            System.out.println("No last kirjaus set");
            String duration = calculateDuration(kellonaika, lastKirjaus.getTime());
            lastKirjaus.setDuration(duration);
            tuntiData.set(tuntiData.size()-1, lastKirjaus);
        }

        tuntiData.add(kirjaus);
        yhteenvetoTextArea.setText(generateYhteenveto(tuntiData));
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

    private String generateYhteenveto(ObservableList<TableTuntiKirjaus> tuntiList){
        Predicate<TableTuntiKirjaus> predicate = Predicate.not(TableTuntiKirjaus::isDurationEmpty);

        Map<String, String> topicToDuration = tuntiList.stream()
                .filter(predicate)
                .collect(
                        Collectors.groupingBy(
                                TableTuntiKirjaus::getTopic,
                                Collectors.collectingAndThen(
                                        Collectors.summingInt(TableTuntiKirjaus::getIntDuration),
                                        TableTuntiKirjaus::durationOfMinutes
                                )
                        )
                );

        StringBuilder returnValue = new StringBuilder();
        for(Map.Entry<String, String> entry: topicToDuration.entrySet()){
            returnValue.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }

        return returnValue.toString();
    }


}
