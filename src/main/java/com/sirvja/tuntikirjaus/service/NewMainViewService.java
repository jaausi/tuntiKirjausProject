package com.sirvja.tuntikirjaus.service;

import com.sirvja.tuntikirjaus.customFields.AutoCompleteTextField;
import com.sirvja.tuntikirjaus.dao.InMemoryMainViewDAO;
import com.sirvja.tuntikirjaus.dao.MainViewDAO;
import com.sirvja.tuntikirjaus.exception.HourRecordNotFoundException;
import com.sirvja.tuntikirjaus.model.HourRecord;
import com.sirvja.tuntikirjaus.model.HourRecordTable;
import javafx.scene.control.TextField;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class NewMainViewService {
    private final MainViewDAO mainViewDAO;

    public NewMainViewService(InMemoryMainViewDAO mainViewDAO) {
        this.mainViewDAO = mainViewDAO;
    }

    public Optional<LocalDate> getCurrentDate() {
        return mainViewDAO.getCurrentDate();
    }

    public void setCurrentDate(LocalDate localDate) {
        mainViewDAO.setCurrentDate(localDate);
    }

    public List<HourRecord> getHourRecords(){
        return mainViewDAO.getHourRecords();
    }

    public void setHourRecords(List<HourRecord> hourRecords) {
        mainViewDAO.setHourRecords(hourRecords);
    }

    Optional<HourRecord> getHourRecord(int hourRecordId) {
        return mainViewDAO.getHourRecord(hourRecordId);
    }

    public void updateHourRecord(HourRecord hourRecord) throws HourRecordNotFoundException {
        mainViewDAO.updateHourRecord(hourRecord);
    }

    int addHourRecord(HourRecord hourRecord) {
        return mainViewDAO.addHourRecord(hourRecord);
    }
    void deleteHourRecord(int hourRecordId) throws HourRecordNotFoundException {
        mainViewDAO.deleteHourRecord(hourRecordId);
    }
    Optional<HourRecordTable> getHourRecordTable() {
        return mainViewDAO.getHourRecordTable();
    }
    void setHourRecordTable(HourRecordTable hourRecordTable) {
        mainViewDAO.setHourRecordTable(hourRecordTable);
    }
    Optional<LocalTime> getTimeField() {
        return mainViewDAO.getTimeField();
    }
    void setTimeField(LocalTime timeField) {
        mainViewDAO.setTimeField(timeField);
    }
    Optional<String> getTopicField() {
        return mainViewDAO.getTopicField();
    }
    void setTopicField(String topicField) {
        mainViewDAO.setTopicField(topicField);
    }
}
