package com.sirvja.tuntikirjaus.dao;


import com.sirvja.tuntikirjaus.exception.HourRecordNotFoundException;
import com.sirvja.tuntikirjaus.model.HourRecord;
import com.sirvja.tuntikirjaus.model.HourRecordTable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MainViewDAO {
    Optional<LocalDate> getCurrentDate();
    void setCurrentDate(LocalDate localDate);
    List<HourRecord> getHourRecords();
    List<HourRecord> setHourRecords(List<HourRecord> hourRecordList);
    Optional<HourRecord> getHourRecord(int hourRecordId);
    void updateHourRecord(HourRecord hourRecord) throws HourRecordNotFoundException;
    int addHourRecord(HourRecord hourRecord);
    void deleteHourRecord(int hourRecordId) throws HourRecordNotFoundException;
    Optional<HourRecordTable> getHourRecordTable();
}
