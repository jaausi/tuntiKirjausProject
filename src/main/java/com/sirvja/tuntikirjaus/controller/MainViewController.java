package com.sirvja.tuntikirjaus.controller;

import com.sirvja.tuntikirjaus.TuntikirjausApplication;
import com.sirvja.tuntikirjaus.domain.Paiva;
import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;
import com.sirvja.tuntikirjaus.exception.EmptyTopicException;
import com.sirvja.tuntikirjaus.exception.MalformatedTimeException;
import com.sirvja.tuntikirjaus.exception.StartTimeNotAfterLastTuntikirjausException;
import com.sirvja.tuntikirjaus.service.MainViewService;
import com.sirvja.tuntikirjaus.customFields.AutoCompleteTextField;
import com.sirvja.tuntikirjaus.utils.CustomLocalTimeStringConverter;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.BiConsumer;

public class MainViewController implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainViewController.class);

    private MainViewService mainViewService;

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

        updateView();

        setListenerForDayListView();

        daysListView.getSelectionModel().selectFirst();

        mainViewService.setCurrentDate(Optional.ofNullable(daysListView.getSelectionModel().getSelectedItem()).orElse(new Paiva(LocalDate.now())));

        initializeAutoCompleteAiheField();
    }

    private void initializeAutoCompleteAiheField() {
        aiheField.getEntries().addAll(mainViewService.getAiheEntries().orElse(new TreeSet<>()));
        aiheField.getLastSelectedObject().addListener((observableValue, oldValue, newValue) -> {
            if(newValue != null){
                aiheField.setText(newValue);
                aiheField.positionCaret(newValue.length());
                aiheField.setLastSelectedItem(null);
            }
        });
    }

    private void setListenerForDayListView() {
        daysListView.getSelectionModel().selectedItemProperty().addListener(mainViewService.getDayListChangeListener(this::updateView));
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
        kellonaikaColumn.setOnEditCommit(mainViewService.getKellonaikaColumnEditHandler(tuntiTaulukko::refresh, this::showNotCorrectTimeAlert));
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

        String yhteenvetoText = mainViewService.getYhteenvetoText();
        ClipboardContent content = new ClipboardContent();
        content.putString(yhteenvetoText);
        Clipboard clipboard = Clipboard.getSystemClipboard();
        clipboard.setContent(content);
    }

    @FXML
    protected void onTallennaTaulukkoonButtonClick() {
        try {
            LOGGER.debug("Save to table button pushed!");
            TuntiKirjaus tuntiKirjaus = mainViewService.addTuntikirjaus(kellonAikaField.getText(), aiheField.getText());
            aiheField.getEntries().add(tuntiKirjaus.getTopic());
            updateView();
        } catch (EmptyTopicException e) {
            LOGGER.error(e.getMessage());
            aiheField.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");
            showFieldNotFilledAlert();
        } catch (MalformatedTimeException e) {
            LOGGER.error(e.getMessage());
            kellonAikaField.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");
            showTimeInWrongFormatAlert(e.getMessage());
        } catch (StartTimeNotAfterLastTuntikirjausException e) {
            LOGGER.error(e.getMessage());
            kellonAikaField.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");
            showNotCorrectTimeAlert();
        }
    }

    @FXML
    protected void onPoistaKirjausButtonClick() {
        LOGGER.debug("Poista kirjaus painettu!");
        TuntiKirjaus selectedKirjaus = tuntiTaulukko.getSelectionModel().getSelectedItem();
        LOGGER.debug("Following kirjaus selected: {}", selectedKirjaus);
        if(!showConfirmationAlert("Oletko varma että haluat poistaa kirjauksen",
                String.format("Poistettava kirjaus: \n%s" +
                        "\nKirjauksen poistaminen muokkaa, poistettavaa edeltävän kirjauksen kestoa " +
                "siirtämällä lopetusajan poistettavan kirjauksen lopetusaikaan.", selectedKirjaus))){
            return;
        }
        mainViewService.removeTuntikirjaus(selectedKirjaus);
        updateView();
    }

    @FXML
    protected void onUusiPaivaButtonClick() {
        LOGGER.debug("Uusi päivä painettu!");
        mainViewService.setCurrentDate(new Paiva(LocalDate.now()));
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
        tuntiTaulukko.setItems(mainViewService.getTuntiDataForTable());
        tuntiTaulukko.refresh();
        daysListView.setItems(mainViewService.getPaivaDataForTable());
        yhteenvetoTextArea.setText(mainViewService.getYhteenvetoText());
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

    private void showNotCorrectTimeAlert(boolean isTooLarge){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Varoitus!");
        if(isTooLarge){
            alert.setHeaderText("Syötetty aika on suurempi kuin seuraava syötetty aika");
            alert.setContentText("Syötä aika, joka on ennen ajanhetkeä joka on seuraavan listalla.");
        } else {
            alert.setHeaderText("Syötetty aika on pienempi kuin edellinen aika");
            alert.setContentText("Syötä aika, joka on edellisen syötetyn ajanhetken jälkeen.");
        }
        alert.showAndWait();
    }

    public static void showTimeInWrongFormatAlert(String problem){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Varoitus!");
        alert.setHeaderText("Syötetty aika on väärässä formaatissa");
        alert.setContentText("Virhe: "+problem);
        alert.showAndWait();
    }

    public static boolean showConfirmationAlert(String confirmationHeader, String confirmationText){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, confirmationText, ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
        alert.setTitle("Vahvista valinta!");
        alert.setHeaderText(confirmationHeader);
        alert.showAndWait();

        return alert.getResult() == ButtonType.YES;
    }

}
