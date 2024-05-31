package com.sirvja.tuntikirjaus.repository;

import com.sirvja.tuntikirjaus.model.HourRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HourRecordRepository {
    Optional<HourRecord> get(int id);
    List<HourRecord> getAll();
    List<HourRecord> getAllFrom(LocalDate localDate);
    HourRecord save(HourRecord hourRecord);
    void update(HourRecord hourRecord);
    void delete(HourRecord hourRecord);
}
