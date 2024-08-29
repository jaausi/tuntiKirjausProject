package com.sirvja.tuntikirjaus.repository;

import com.sirvja.tuntikirjaus.model.HourRecord;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HourRecordRepository extends CrudRepository<HourRecord, Integer> {
}
