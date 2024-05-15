package com.sirvja.tuntikirjaus.service;

import com.sirvja.tuntikirjaus.exception.HourRecordNotFoundException;
import com.sirvja.tuntikirjaus.model.HourRecord;
import com.sirvja.tuntikirjaus.model.HourRecordTable;
import com.sirvja.tuntikirjaus.utils.CustomLocalTimeStringConverter;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Log4j2
@Service
public class HourRecordTableService {

    private final NewMainViewService newMainViewService;
    private final AlertService alertService;

    public HourRecordTableService(NewMainViewService newMainViewService, AlertService alertService) {
        this.newMainViewService = newMainViewService;
        this.alertService = alertService;
    }

    public void initializeHourRecordTable(HourRecordTable hourRecordTable) {
        hourRecordTable.getHourRecordTableView().setEditable(true);

        initializeTimeTableColumn(hourRecordTable.getTimeTableColumn());
        initializeTopicTableColumn(hourRecordTable.getTopicTableColumn());
        initializeDurationTableColumn(hourRecordTable.getDurationTableColumn());
    }

    private void initializeTimeTableColumn(TableColumn<HourRecord, LocalTime> timeTableColumn) {
        timeTableColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        timeTableColumn.setSortable(false);

        timeTableColumn.setCellFactory(TextFieldTableCell.forTableColumn(new CustomLocalTimeStringConverter()));

        timeTableColumn.setOnEditCommit(createEditEventHandlerForTimeTableColumn());
    }

    private EventHandler<TableColumn.CellEditEvent<HourRecord, LocalTime>> createEditEventHandlerForTimeTableColumn() {
        return cellEditEvent -> {
            int tablePosition = cellEditEvent.getTablePosition().getRow();
            int lastPosition = cellEditEvent.getTableView().getItems().size() - 1;
            LocalDateTime newValue = LocalDateTime.of(newMainViewService.getCurrentDate().orElse(LocalDate.now()), cellEditEvent.getNewValue());
            boolean facedError = false;

            if(tablePosition < lastPosition){
                HourRecord followingKirjausToEdit = cellEditEvent.getTableView().getItems().get(tablePosition + 1);
                // If edited time is after next kirjaus start time, abort.
                if(newValue.isAfter(followingKirjausToEdit.getStartTime())){
                    alertService.showNotCorrectTimeAlert(true);
                    facedError = true;
                }
            }

            // If not the first row of a day. Edit also the previous row end time.
            if(tablePosition > 0){
                HourRecord previousKirjausToEdit = cellEditEvent.getTableView().getItems().get(tablePosition - 1);
                // If edited time is before previous kirjaus start time, abort.
                if(newValue.isBefore(previousKirjausToEdit.getStartTime())){
                    alertService.showNotCorrectTimeAlert(false);
                    facedError = true;
                }
                if(!facedError){
                    previousKirjausToEdit.setEndTime(newValue);
                    try {
                        newMainViewService.updateHourRecord(previousKirjausToEdit);
                    } catch (HourRecordNotFoundException e) {
                        log.error("Tried to update previous hour record but it was not found.");
                        alertService.showSomethingWentWrongAlert("Yritettiin päivittää edellistä tuntikirjausta, mutta sitä ei löytynyt.");
                        facedError = true;
                    }
                }
            }

            HourRecord kirjausToEdit = cellEditEvent.getTableView().getItems().get(tablePosition);
            if(!facedError){
                kirjausToEdit.setStartTime(newValue);
                MainViewService.update(kirjausToEdit);
                try {
                    newMainViewService.updateHourRecord(kirjausToEdit);
                } catch (HourRecordNotFoundException e) {
                    log.error("Tried to update current hour record but it was not found.");
                    alertService.showSomethingWentWrongAlert("Yritettiin päivittää valittua tuntikirjausta, mutta sitä ei löytynyt.");
                }
            }
            refreshHourRecordTable();
        };
    }

    private static void showNotCorrectTimeAlert(boolean isTooLarge){
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

    private static void initializeTopicTableColumn(TableColumn<HourRecord, String> topicTableColumn) {
        topicTableColumn.setCellValueFactory(new PropertyValueFactory<>("topic"));
        topicTableColumn.setSortable(false);
    }

    private static void initializeDurationTableColumn(TableColumn<HourRecord, String> durationTableColumn) {
        durationTableColumn.setCellValueFactory(new PropertyValueFactory<>("durationString"));
        durationTableColumn.setSortable(false);
    }

    public void refreshHourRecordTable() {
        newMainViewService.g
    }
}
