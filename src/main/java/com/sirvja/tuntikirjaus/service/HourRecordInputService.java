package com.sirvja.tuntikirjaus.service;

import com.sirvja.tuntikirjaus.customFields.AutoCompleteTextField;
import com.sirvja.tuntikirjaus.dao.MainViewDAO;
import com.sirvja.tuntikirjaus.exception.FieldNotInitializedException;
import com.sirvja.tuntikirjaus.model.HourRecord;
import javafx.collections.ObservableList;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.TreeSet;

/**
 * Service for handling saving of the hour records in UI.
 * Contains following elements: Time and Topic field and the save to table button.
 */
@Log4j2
@Service
public class HourRecordInputService {

    private final AlertService alertService;
    private final MainViewDAO mainViewDAO;

    public HourRecordInputService(AlertService alertService, MainViewDAO mainViewDAO) {
        this.alertService = alertService;
        this.mainViewDAO = mainViewDAO;
    }

    public void initializeTopicField(AutoCompleteTextField<String> topicField) {
        topicField.getEntries().addAll(MainViewService.getAiheEntries().orElse(new TreeSet<>()));
        topicField.getLastSelectedObject().addListener((observableValue, oldValue, newValue) -> {
            if(newValue != null){
                topicField.setText(newValue);
                topicField.positionCaret(newValue.length());
                topicField.setLastSelectedItem(null);
            }
        });
        topicField.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER){
                log.debug("Enter was pressed");
                handleHourRecordSave();
            }
        });
    }

    private void handleHourRecordSave() {
        TextField timeField = new TextField();
        AutoCompleteTextField<String> topicField = new AutoCompleteTextField<>();
        try {
            timeField.setText(mainViewDAO.getTimeField().orElseThrow(() -> new FieldNotInitializedException("time field not initialized")).toString());
            topicField.setText( mainViewDAO.getTopicField().orElseThrow(() -> new FieldNotInitializedException("topic field not initialized")));
        } catch (FieldNotInitializedException e) {
            alertService.showSomethingWentWrongAlert(String.format("Tallennus ei onnistunut, syy: %s", e.getMessage()));
            return;
        }

        String time = timeField.getText();
        String topic = topicField.getText();
        log.debug("Save to table button pushed!");

        if(topic.isEmpty()){
            topicField.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");
            alertService.showFieldNotFilledAlert();
            return;
        }

        LocalDateTime localDateTime;
        try {
            localDateTime = MainViewService.parseTimeFromString(time);
        } catch (DateTimeParseException e){
            log.error("Error in parsing time from String: {}. Exception message: {}", time, e.getMessage());
            timeField.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");
            alertService.showTimeInWrongFormatAlert(e.getMessage());
            return;
        }

        HourRecord hourRecord = new HourRecord(localDateTime, null, topic, true);

        ObservableList<HourRecord> tuntidata = MainViewService.getTuntiDataForTable();

        if(!tuntidata.isEmpty() && tuntidata.get(tuntidata.size()-1).compareTo(hourRecord) > 0){
            timeField.setStyle("-fx-border-color: red ; -fx-border-width: 2px ;");
            alertService.showNotCorrectTimeAlert();
            return;
        }

        MainViewService.addTuntikirjaus(hourRecord);

        topicField.getEntries().add(hourRecord.getTopic()); // TODO: remove and add to entries also when topic column cell modified

        // updateView(); // TODO: Do something for this
    }
}
