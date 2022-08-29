package com.sirvja.tuntikirjaus.controller;

import com.sirvja.tuntikirjaus.domain.ReportConfig;
import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;
import com.sirvja.tuntikirjaus.service.ReportsViewService;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

public class ReportsViewController implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainViewController.class);

    @FXML
    private DatePicker alkupaivaDatePicker;
    @FXML
    private Button avaaRaporttiButton;
    @FXML
    private Button haeButton;
    @FXML
    private TextField hakusanaField;
    @FXML
    private DatePicker loppupaivaDatePicker;
    @FXML
    private TableColumn<TuntiKirjaus, LocalTime> raportitKellonaikaColumn;
    @FXML
    private TableColumn<TuntiKirjaus, String> raportitAiheColumn;
    @FXML
    private TableColumn<TuntiKirjaus, String> raportitTunnitColumn;
    @FXML
    private TableView<TuntiKirjaus> raportitTuntiTaulukko = new TableView<>();
    @FXML
    private Button tallennaRaportti;
    @FXML
    private ListView<ReportConfig> tallennetutRaportitListView = new ListView<>();
    @FXML
    private TextField tunnitYhteensaField;

    @Override
    public void initialize (URL url, ResourceBundle rb){
        raportitKellonaikaColumn.setCellValueFactory(new PropertyValueFactory<TuntiKirjaus, LocalTime>("time"));
        raportitAiheColumn.setCellValueFactory(new PropertyValueFactory<TuntiKirjaus, String>("topic"));
        raportitTunnitColumn.setCellValueFactory(new PropertyValueFactory<TuntiKirjaus, String>("durationString"));

        updateView();
    }

    @FXML
    protected void onAiheFieldClick() {
        LOGGER.debug("Aihe field clicked.");
    }

    @FXML
    protected void onAlkupaivaDatePickerClicked() {
        LOGGER.debug("Alkupäivä field clicked.");
    }
    @FXML
    protected void onHakusanaFieldClicked() {
        LOGGER.debug("Hakusana field clicked.");
    }

    @FXML
    protected void onLoppupaivaDatePickerClicked() {
        LOGGER.debug("Loppupäivä field clicked.");
    }


    @FXML
    protected void onAvaaRaporttiButtonClick() {
        LOGGER.debug("Avaa raportti clicked.");
    }

    @FXML
    protected void onHaeButtonClick() {
        LOGGER.debug("Hae button clicked.");
        Optional<LocalDate> optionalAlkuPaiva = Optional.ofNullable(alkupaivaDatePicker.valueProperty().getValue());
        Optional<LocalDate> optionalLoppupaiva = Optional.ofNullable(loppupaivaDatePicker.valueProperty().getValue());
        Optional<String> optionalSearchQuery = Optional.ofNullable(hakusanaField.getText());

        ObservableList<TuntiKirjaus> tuntiKirjausList = ReportsViewService.getAllTuntikirjaus(optionalAlkuPaiva, optionalLoppupaiva, optionalSearchQuery);
        long sumOfHoursInMinutes = ReportsViewService.getSumOfHoursFromTuntikirjausList(tuntiKirjausList);
        String hours = String.valueOf(Math.round(sumOfHoursInMinutes/60f));
        String minutes = String.valueOf(Math.round(sumOfHoursInMinutes%60f));

        raportitTuntiTaulukko.setItems(tuntiKirjausList);
        tunnitYhteensaField.setText(String.format("%sh %sm", hours, minutes));
    }

    @FXML
    protected void onTallennaRaporttiButtonClick() {
        LOGGER.debug("Tallenna raportti field clicked.");
        Optional<LocalDate> optionalAlkuPaiva = Optional.ofNullable(alkupaivaDatePicker.valueProperty().getValue());
        Optional<LocalDate> optionalLoppupaiva = Optional.ofNullable(loppupaivaDatePicker.valueProperty().getValue());
        Optional<String> optionalSearchQuery = Optional.ofNullable(hakusanaField.getText());

        // create a text input dialog
        TextInputDialog td = new TextInputDialog("enter any text");

        // setHeaderText
        td.setHeaderText("enter your name");
        td.showAndWait();

        Optional<String> optionalReportName = Optional.of(td.getEditor().getText());

        ReportConfig reportConfig = new ReportConfig(
                optionalAlkuPaiva.orElse(null),
                optionalLoppupaiva.orElse(null),
                optionalSearchQuery.orElse(null),
                optionalReportName.orElse(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")))
        );

        ReportsViewService.addReportConfigToDb(reportConfig);

        updateView();
    }

    private void updateView(){
        tallennetutRaportitListView.setItems(ReportsViewService.getReportConfigDataForList());
    }

}
