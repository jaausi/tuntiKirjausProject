package com.sirvja.tuntikirjaus.service;

import com.sirvja.tuntikirjaus.model.HourRecord;
import com.sirvja.tuntikirjaus.model.HourRecordTable;
import com.sirvja.tuntikirjaus.utils.CustomLocalTimeStringConverter;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class HourRecordTableService {

    public void initializeHourRecordTable(HourRecordTable hourRecordTable) {
        hourRecordTable.getHourRecordTableView().setEditable(true);

        initializeTimeTableColumn(hourRecordTable.getTimeTableColumn());
        initializeTopicTableColumn(hourRecordTable.getTopicTableColumn());
        initializeDurationTableColumn(hourRecordTable.getDurationTableColumn());
    }

    private static void initializeTimeTableColumn(TableColumn<HourRecord, LocalTime> timeTableColumn) {
        timeTableColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        timeTableColumn.setSortable(false);

        timeTableColumn.setCellFactory(TextFieldTableCell.forTableColumn(new CustomLocalTimeStringConverter()));

        timeTableColumn.setOnEditCommit(createEditEventHandlerForTimeTableColumn());
    }

    private static EventHandler<TableColumn.CellEditEvent<HourRecord, LocalTime>> createEditEventHandlerForTimeTableColumn() {
        return t -> {
            int tablePosition = t.getTablePosition().getRow();
            int lastPosition = t.getTableView().getItems().size() - 1;
            LocalDateTime newValue = LocalDateTime.of(MainViewService.getCurrentDate(), t.getNewValue());
            boolean facedError = false;

            if(tablePosition < lastPosition){
                HourRecord followingKirjausToEdit = t.getTableView().getItems().get(tablePosition + 1);
                // If edited time is after next kirjaus start time, abort.
                if(newValue.isAfter(followingKirjausToEdit.getStartTime())){
                    showNotCorrectTimeAlert(true);
                    facedError = true;
                }
            }

            // If not the first row of a day. Edit also the previous row end time.
            if(tablePosition > 0){
                HourRecord previousKirjausToEdit = t.getTableView().getItems().get(tablePosition - 1);
                // If edited time is before previous kirjaus start time, abort.
                if(newValue.isBefore(previousKirjausToEdit.getStartTime())){
                    showNotCorrectTimeAlert(false);
                    facedError = true;
                }
                if(!facedError){
                    previousKirjausToEdit.setEndTime(newValue);
                    MainViewService.update(previousKirjausToEdit); // TODO: Refactor me
                }
            }

            HourRecord kirjausToEdit = t.getTableView().getItems().get(tablePosition);
            if(!facedError){
                kirjausToEdit.setStartTime(newValue);
                MainViewService.update(kirjausToEdit); // TODO: Refactor me
            }
            //hourRecordTableView.refresh(); // TODO: Refactor me
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
}
