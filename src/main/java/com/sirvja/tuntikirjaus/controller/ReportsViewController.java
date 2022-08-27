package com.sirvja.tuntikirjaus.controller;

import com.sirvja.tuntikirjaus.domain.Paiva;
import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;
import com.sirvja.tuntikirjaus.service.MainViewService;
import com.sirvja.tuntikirjaus.service.ReportsViewService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
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
    private TableView<?> raportitTuntiTaulukko;
    @FXML
    private Button tallennaRaportti;
    @FXML
    private ListView tallennetutRaportitListView;
    @FXML
    private TextField tunnitYhteensaField;

    @Override
    public void initialize (URL url, ResourceBundle rb){
        raportitKellonaikaColumn.setCellValueFactory(new PropertyValueFactory<TuntiKirjaus, LocalTime>("time"));
        raportitAiheColumn.setCellValueFactory(new PropertyValueFactory<TuntiKirjaus, String>("topic"));
        raportitTunnitColumn.setCellValueFactory(new PropertyValueFactory<TuntiKirjaus, String>("durationString"));

        updateView();
        tallennetutRaportitListView.getSelectionModel().selectFirst();
        ReportsViewService.setCurrentDate(Optional.ofNullable(tallennetutRaportitListView.getSelectionModel().getSelectedItem()).orElse(new Paiva(LocalDate.now())));

        // Add listener for ListView changes: https://stackoverflow.com/questions/12459086/how-to-perform-an-action-by-selecting-an-item-from-listview-in-javafx-2
        tallennetutRaportitListView.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            if(newValue != null){
                ReportsViewService.setCurrentDate(newValue);
                updateView();
            }
        });
    }

    private void updateView(){
        raportitTuntiTaulukko.setItems(ReportsViewService.getTuntiDataForTable());
        tallennetutRaportitListView.setItems(ReportsViewService.getPaivaDataForTable());
    }

    @FXML
    protected void onAiheFieldClick() {

    }

    @FXML
    protected void onAvaaRaporttiButtonClick() {

    }

    @FXML
    protected void onHaeButtonClick() {

    }

    @FXML
    protected void onKellonaikaFieldClick() {

    }

    @FXML
    protected void onKeyPressedToAiheField() {

    }

    @FXML
    protected void onKeyPressedToKellonaikaField() {

    }

    @FXML
    protected void onTallennaRaporttiButtonClick() {

    }



}
