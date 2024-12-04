package com.sirvja.tuntikirjaus.dao;

import com.sirvja.tuntikirjaus.customFields.AutoCompleteTextField;
import com.sirvja.tuntikirjaus.exception.HourRecordNotFoundException;
import com.sirvja.tuntikirjaus.model.HourRecord;
import com.sirvja.tuntikirjaus.model.HourRecordTable;
import javafx.scene.control.TextField;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class InMemoryMainViewDAO implements MainViewDAO {

    @Override
    public Optional<LocalDate> getCurrentDate() {
        return Optional.ofNullable(MainView.getInstance().getCurrentDate());
    }

    @Override
    public void setCurrentDate(LocalDate localDate) {
        MainView.getInstance().setCurrentDate(localDate);
    }

    @Override
    public List<HourRecord> getHourRecords() {
        return MainView.getInstance().getHourRecordList();
    }

    @Override
    public void setHourRecords(List<HourRecord> hourRecordList) {
        MainView.getInstance().setHourRecordList(hourRecordList);
    }

    @Override
    public Optional<HourRecord> getHourRecord(int hourRecordId) {
        return MainView.getInstance().getHourRecordList().stream()
                .filter(hourRecord -> hourRecordId == hourRecord.getId())
                .findAny();
    }

    @Override
    public void updateHourRecord(HourRecord hourRecord) throws HourRecordNotFoundException {
        getHourRecord(hourRecord.getId()).orElseThrow(HourRecordNotFoundException::new);

        List<HourRecord> updatedHourRecordList = MainView.getInstance().hourRecordList.stream()
                .map(hourRecordToBeEdited -> hourRecordToBeEdited.getId() == hourRecord.getId() ? hourRecord : hourRecordToBeEdited)
                .collect(Collectors.toList());

        setHourRecords(updatedHourRecordList);
    }

    @Override
    public int addHourRecord(HourRecord hourRecord) {
        return 0;
    }

    @Override
    public void deleteHourRecord(int hourRecordId) throws HourRecordNotFoundException {

    }

    @Override
    public Optional<HourRecordTable> getHourRecordTable() {
        return Optional.ofNullable(MainView.getInstance().getHourRecordTable());
    }

    @Override
    public void setHourRecordTable(HourRecordTable hourRecordTable) {
        MainView.getInstance().setHourRecordTable(hourRecordTable);
    }

    @Override
    public Optional<LocalTime> getTimeField() {
        return Optional.empty();
    }

    @Override
    public void setTimeField(LocalTime timeField) {

    }

    @Override
    public Optional<String> getTopicField() {
        return Optional.empty();
    }

    @Override
    public void setTopicField(String topicField) {

    }

    /**
     * Singleton class to hold state of MainView
     */
    @Getter
    @Setter
    private static class MainView {
        private LocalDate currentDate;
        private List<HourRecord> hourRecordList;
        private HourRecordTable hourRecordTable;
        private TextField timeField;
        private AutoCompleteTextField<String> topicField;

        private static class MainViewHolder {
            private static final MainView INSTANCE = new MainView();
        }
        public static MainView getInstance() {
            return MainViewHolder.INSTANCE;
        }
    }

}
