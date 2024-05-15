package com.sirvja.tuntikirjaus.dao;

import com.sirvja.tuntikirjaus.exception.HourRecordNotFoundException;
import com.sirvja.tuntikirjaus.model.HourRecord;
import com.sirvja.tuntikirjaus.model.MainView;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
        return null;
    }

    @Override
    public List<HourRecord> setHourRecords(List<HourRecord> hourRecordList) {
        return null;
    }

    @Override
    public Optional<HourRecord> getHourRecord(int hourRecordId) {
        return Optional.empty();
    }

    @Override
    public void updateHourRecord(HourRecord hourRecord) throws HourRecordNotFoundException {

    }

    @Override
    public int addHourRecord(HourRecord hourRecord) {
        return 0;
    }

    @Override
    public void deleteHourRecord(int hourRecordId) throws HourRecordNotFoundException {

    }
}
