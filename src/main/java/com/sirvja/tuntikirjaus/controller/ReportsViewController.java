package com.sirvja.tuntikirjaus.controller;

import com.sirvja.tuntikirjaus.model.HourRecord;
import com.sirvja.tuntikirjaus.model.ReportConfig;
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
import java.util.Optional;
import java.util.ResourceBundle;

public class ReportsViewController implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainViewController.class);

    @FXML
    private Button dateBackward;
    @FXML
    private Button dateForward;
    @FXML
    private Button dateNow;
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
    private TableColumn<HourRecord, LocalDateTime> raportitKellonaikaColumn;
    @FXML
    private TableColumn<HourRecord, String> raportitAiheColumn;
    @FXML
    private TableColumn<HourRecord, String> raportitTunnitColumn;
    @FXML
    private TableView<HourRecord> raportitTuntiTaulukko = new TableView<>();
    @FXML
    private Button tallennaRaportti;
    @FXML
    private ListView<ReportConfig> tallennetutRaportitListView = new ListView<>();
    @FXML
    private TextArea reportsYhteenvetoTextArea;
    @FXML
    private TextField tunnitYhteensaField;
    @FXML
    private Button tyhjennaKentatButton;

    @Override
    public void initialize (URL url, ResourceBundle rb){
        raportitKellonaikaColumn.setCellValueFactory(new PropertyValueFactory<HourRecord, LocalDateTime>("dateTime"));
        raportitAiheColumn.setCellValueFactory(new PropertyValueFactory<HourRecord, String>("topic"));
        raportitTunnitColumn.setCellValueFactory(new PropertyValueFactory<HourRecord, String>("durationString"));

        updateView();
    }


    @FXML
    protected void onDateBackwardClick() {
        LocalDate currentAlkupaiva = alkupaivaDatePicker.valueProperty().getValue();
        LocalDate currentLoppupaiva = loppupaivaDatePicker.valueProperty().getValue();

        alkupaivaDatePicker.valueProperty().setValue(currentAlkupaiva.minusDays(1));
        loppupaivaDatePicker.valueProperty().setValue(currentLoppupaiva.minusDays(1));

        onHaeButtonClick();
    }

    @FXML
    protected void onDateForwardClick() {
        LocalDate currentAlkupaiva = alkupaivaDatePicker.valueProperty().getValue();
        LocalDate currentLoppupaiva = loppupaivaDatePicker.valueProperty().getValue();

        alkupaivaDatePicker.valueProperty().setValue(currentAlkupaiva.plusDays(1));
        loppupaivaDatePicker.valueProperty().setValue(currentLoppupaiva.plusDays(1));

        onHaeButtonClick();
    }

    @FXML
    protected void onDateNowClick() {
        alkupaivaDatePicker.valueProperty().setValue(LocalDate.now());
        loppupaivaDatePicker.valueProperty().setValue(LocalDate.now());
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
        Optional<ReportConfig> optionalReportConfig = Optional.ofNullable(tallennetutRaportitListView.getSelectionModel().getSelectedItem());

        optionalReportConfig.ifPresent(reportConfig -> {
            alkupaivaDatePicker.valueProperty().setValue(reportConfig.getStartDate().orElse(null));
            loppupaivaDatePicker.valueProperty().setValue(reportConfig.getEndDate().orElse(null));
            hakusanaField.setText(reportConfig.getSearchQuery());
        });

        onHaeButtonClick();
    }

    @FXML
    protected void onTyhjennaKentatButtonClick(){
        alkupaivaDatePicker.valueProperty().setValue(null);
        loppupaivaDatePicker.valueProperty().setValue(null);
        hakusanaField.setText(null);
    }

    @FXML
    protected void onHaeButtonClick() {
        LOGGER.debug("Hae button clicked.");
        Optional<LocalDate> optionalAlkuPaiva = Optional.ofNullable(alkupaivaDatePicker.valueProperty().getValue());
        Optional<LocalDate> optionalLoppupaiva = Optional.ofNullable(loppupaivaDatePicker.valueProperty().getValue());
        Optional<String> optionalSearchQuery = Optional.ofNullable(hakusanaField.getText());

        ObservableList<HourRecord> hourRecordList = ReportsViewService.getAllTuntikirjaus(optionalAlkuPaiva, optionalLoppupaiva, optionalSearchQuery);
        String yhteenvetoText = ReportsViewService.getYhteenvetoText(hourRecordList);
        long sumOfHoursInMinutes = ReportsViewService.getSumOfHoursFromTuntikirjausList(hourRecordList);
        String hours = ReportsViewService.getHoursStringFromMinutes(sumOfHoursInMinutes);
        String minutes = ReportsViewService.getMinutesStringFromMinutes(sumOfHoursInMinutes);
        String htps = ReportsViewService.getHtpsStringFromMinutes(sumOfHoursInMinutes);

        raportitTuntiTaulukko.setItems(hourRecordList);
        reportsYhteenvetoTextArea.setText(yhteenvetoText);
        tunnitYhteensaField.setText(String.format("%sh %sm (%s htp)", hours, minutes, htps));
    }

    @FXML
    protected void onTallennaRaporttiButtonClick() {
        LOGGER.debug("Tallenna raportti field clicked.");
        Optional<LocalDate> optionalAlkuPaiva = Optional.ofNullable(alkupaivaDatePicker.valueProperty().getValue());
        Optional<LocalDate> optionalLoppupaiva = Optional.ofNullable(loppupaivaDatePicker.valueProperty().getValue());
        Optional<String> optionalSearchQuery = Optional.ofNullable(hakusanaField.getText());

        // create a text input dialog
        TextInputDialog td = new TextInputDialog("Syötä raportin nimi");

        // setHeaderText
        td.setHeaderText("Raportin talennus");
        td.showAndWait().ifPresent(reportName -> {
            ReportConfig reportConfig = new ReportConfig(
                    optionalAlkuPaiva.orElse(null),
                    optionalLoppupaiva.orElse(null),
                    optionalSearchQuery.orElse(null),
                    reportName
            );

            ReportsViewService.addReportConfigToDb(reportConfig);

            updateView();
        });
    }

    private void updateView(){
        tallennetutRaportitListView.setItems(ReportsViewService.getReportConfigDataForList());
    }

}
