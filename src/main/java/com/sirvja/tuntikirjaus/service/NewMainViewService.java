package com.sirvja.tuntikirjaus.service;

import com.sirvja.tuntikirjaus.dao.InMemoryMainViewDAO;
import com.sirvja.tuntikirjaus.dao.MainViewDAO;
import com.sirvja.tuntikirjaus.exception.HourRecordNotFoundException;
import com.sirvja.tuntikirjaus.model.HourRecord;
import com.sirvja.tuntikirjaus.model.HourRecordTable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

    public void updateHourRecord(HourRecord hourRecord) throws HourRecordNotFoundException {
        mainViewDAO.updateHourRecord(hourRecord);
    }
}
