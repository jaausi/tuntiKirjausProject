package com.sirvja.tuntikirjaus.dao;


import com.sirvja.tuntikirjaus.exception.HourRecordNotFoundException;
import com.sirvja.tuntikirjaus.model.HourRecord;
import com.sirvja.tuntikirjaus.model.HourRecordTable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface MainViewDAO {
    Optional<LocalDate> getCurrentDate();
    void setCurrentDate(LocalDate localDate);
    List<HourRecord> getHourRecords();
    void setHourRecords(List<HourRecord> hourRecordList);
    Optional<HourRecord> getHourRecord(int hourRecordId);
    void updateHourRecord(HourRecord hourRecord) throws HourRecordNotFoundException;
    int addHourRecord(HourRecord hourRecord);
    void deleteHourRecord(int hourRecordId) throws HourRecordNotFoundException;
    Optional<HourRecordTable> getHourRecordTable();
    void setHourRecordTable(HourRecordTable hourRecordTable);
    Optional<LocalTime> getTimeField();
    void setTimeField(LocalTime timeField);
    Optional<String> getTopicField();
    void setTopicField(String topicField);
}
