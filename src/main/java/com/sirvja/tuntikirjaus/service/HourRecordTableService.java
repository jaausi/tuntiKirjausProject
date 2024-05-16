package com.sirvja.tuntikirjaus.service;

import com.sirvja.tuntikirjaus.dao.InMemoryMainViewDAO;
import com.sirvja.tuntikirjaus.dao.MainViewDAO;
import com.sirvja.tuntikirjaus.exception.HourRecordNotFoundException;
import com.sirvja.tuntikirjaus.model.HourRecord;
import com.sirvja.tuntikirjaus.model.HourRecordTable;
import com.sirvja.tuntikirjaus.utils.CustomLocalTimeStringConverter;
import javafx.event.EventHandler;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Service for handling all the actions related to hour record table view of the UI
 */
@Log4j2
@Service
public class HourRecordTableService {

    private final NewMainViewService newMainViewService;
    private final AlertService alertService;
    private final MainViewDAO mainViewDAO;

    public HourRecordTableService(NewMainViewService newMainViewService, AlertService alertService, InMemoryMainViewDAO mainViewDAO) {
        this.newMainViewService = newMainViewService;
        this.alertService = alertService;
        this.mainViewDAO = mainViewDAO;
    }

    public void initializeHourRecordTable(HourRecordTable hourRecordTable) {
        log.info("Initializing hour record table view");
        mainViewDAO.setHourRecordTable(hourRecordTable);
        hourRecordTable.getHourRecordTableView().setEditable(true);

        initializeTimeTableColumn(hourRecordTable.getTimeTableColumn());
        initializeTopicTableColumn(hourRecordTable.getTopicTableColumn());
        initializeDurationTableColumn(hourRecordTable.getDurationTableColumn());
    }

    public void refreshHourRecordTable() {
        log.info("Refreshing hour record table view");
        mainViewDAO.getHourRecordTable().ifPresent(hourRecordTable -> hourRecordTable.getHourRecordTableView().refresh());
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
                HourRecord followingHourRecord = cellEditEvent.getTableView().getItems().get(tablePosition + 1);
                // If edited time is after next kirjaus start time, abort.
                if(newValue.isAfter(followingHourRecord.getStartTime())){
                    alertService.showNotCorrectTimeAlert(true);
                    facedError = true;
                }
            }

            // If not the first row of a day. Edit also the previous row end time.
            if(tablePosition > 0){
                HourRecord previousHourRecord = cellEditEvent.getTableView().getItems().get(tablePosition - 1);
                // If edited time is before previous kirjaus start time, abort.
                if(newValue.isBefore(previousHourRecord.getStartTime())){
                    alertService.showNotCorrectTimeAlert(false);
                    facedError = true;
                }
                if(!facedError){
                    previousHourRecord.setEndTime(newValue);
                    try {
                        newMainViewService.updateHourRecord(previousHourRecord);
                    } catch (HourRecordNotFoundException e) {
                        log.error("Tried to update previous hour record but it was not found.");
                        alertService.showSomethingWentWrongAlert("Yritettiin päivittää edellistä tuntikirjausta, mutta sitä ei löytynyt.");
                        facedError = true;
                    }
                }
            }

            HourRecord hourRecordToEdit = cellEditEvent.getTableView().getItems().get(tablePosition);
            if(!facedError){
                hourRecordToEdit.setStartTime(newValue);
                MainViewService.update(hourRecordToEdit);
                try {
                    newMainViewService.updateHourRecord(hourRecordToEdit);
                } catch (HourRecordNotFoundException e) {
                    log.error("Tried to update current hour record but it was not found.");
                    alertService.showSomethingWentWrongAlert("Yritettiin päivittää valittua tuntikirjausta, mutta sitä ei löytynyt.");
                }
            }
            refreshHourRecordTable();
        };
    }

    private static void initializeTopicTableColumn(TableColumn<HourRecord, String> topicTableColumn) {
        topicTableColumn.setCellValueFactory(new PropertyValueFactory<>("topic"));
        topicTableColumn.setSortable(false);

        topicTableColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        topicTableColumn.setOnEditCommit( cellEditEvent -> {
            HourRecord hourRecordToEdit = cellEditEvent.getTableView().getItems().get(cellEditEvent.getTablePosition().getRow());
            hourRecordToEdit.setTopic(cellEditEvent.getNewValue());
            MainViewService.update(hourRecordToEdit);
        });
    }

    private static void initializeDurationTableColumn(TableColumn<HourRecord, String> durationTableColumn) {
        durationTableColumn.setCellValueFactory(new PropertyValueFactory<>("durationString"));
        durationTableColumn.setSortable(false);
    }
}
