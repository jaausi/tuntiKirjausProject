package com.sirvja.tuntikirjaus.service;

import com.sirvja.tuntikirjaus.domain.ReportConfig;
import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;
import com.sirvja.tuntikirjaus.utils.ReportConfigDao;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Collectors;

public class ReportsViewService {

    private static final ReportConfigDao reportConfigDao = new ReportConfigDao();
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportsViewService.class);

    private static final LocalDate currentDate = LocalDate.now();
    private static final ObservableList<ReportConfig> reportConfigList = getInitialReportConfigs();

    private static ObservableList<ReportConfig> getInitialReportConfigs(){
        return reportConfigDao.getAll().orElse(null);
    }

    public static ObservableList<ReportConfig> getReportConfigDataForList(){
        return reportConfigList;
    }

    public static ObservableList<TuntiKirjaus> getAllTuntikirjaus(Optional<LocalDate> optionalAlkuPaiva, Optional<LocalDate> optionalLoppupaiva, Optional<String> optionalSearchQuery) {
        return MainViewService.getTuntiDataForTable().stream()
                .filter(tuntiKirjaus -> tuntiKirjaus.getStartTime().isAfter(optionalAlkuPaiva.orElse(LocalDate.MIN).atStartOfDay()))
                .filter(tuntiKirjaus -> tuntiKirjaus.getStartTime().isBefore(optionalLoppupaiva.orElse(LocalDate.MAX).atTime(23, 59)))
                .filter(tuntiKirjaus -> tuntiKirjaus.getTopic().contains(optionalSearchQuery.orElse("")))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    public static long getSumOfHoursFromTuntikirjausList(ObservableList<TuntiKirjaus> tuntikirjausList){
        return tuntikirjausList.stream()
                .map(TuntiKirjaus::getDurationInDuration)
                .mapToLong(Duration::toMinutes)
                .sum();
    }

    public static ReportConfig addReportConfigToDb(ReportConfig reportConfig){
        ReportConfig savedReportConfig = reportConfigDao.save(reportConfig);
        reportConfigList.add(savedReportConfig);

        return savedReportConfig;
    }
}
