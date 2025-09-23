package com.sirvja.tuntikirjaus.service;

import com.sirvja.tuntikirjaus.domain.ReportConfig;
import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;
import com.sirvja.tuntikirjaus.dao.ReportConfigDao;
import com.sirvja.tuntikirjaus.dao.TuntiKirjausDao;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ReportsViewService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportsViewService.class);
    private static final ReportConfigDao reportConfigDao = new ReportConfigDao();
    private static final TuntiKirjausDao tuntiKirjausDao = new TuntiKirjausDao();

    private static final LocalDate currentDate = LocalDate.now();
    private static final ObservableList<ReportConfig> reportConfigList = FXCollections.observableArrayList();

    private static void getInitialReportConfigsFromDb(){
        reportConfigDao.getAll().map(reportConfigList::addAll);
    }

    public static ObservableList<ReportConfig> getReportConfigDataForList(){
        if(reportConfigList.isEmpty()){
            getInitialReportConfigsFromDb();
        }
        return reportConfigList;
    }

    public static ObservableList<TuntiKirjaus> getAllTuntikirjaus(Optional<LocalDate> optionalAlkuPaiva, Optional<LocalDate> optionalLoppupaiva, Optional<String> optionalSearchQuery) {
        Optional<ObservableList<TuntiKirjaus>> allTuntikirjaus = tuntiKirjausDao.getAll();

        if(allTuntikirjaus.isPresent()){
            return tuntiKirjausDao.getAll().get().stream()
                    .filter(tuntiKirjaus -> tuntiKirjaus.getStartTime().isAfter(optionalAlkuPaiva.orElse(LocalDate.MIN).atStartOfDay()))
                    .filter(tuntiKirjaus -> tuntiKirjaus.getStartTime().isBefore(optionalLoppupaiva.orElse(LocalDate.MAX).atTime(23, 59)))
                    .filter(tuntiKirjaus -> tuntiKirjaus.getTopic().toLowerCase().contains(optionalSearchQuery.orElse("").toLowerCase()))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
        } else {
            return FXCollections.observableArrayList();
        }
    }

    public static List<TuntiKirjaus> getAllTuntikirjausAsList(Optional<LocalDate> optionalAlkuPaiva, Optional<LocalDate> optionalLoppupaiva, Optional<String> optionalSearchQuery) {
        Optional<ObservableList<TuntiKirjaus>> allTuntikirjaus = tuntiKirjausDao.getAll();

        if(allTuntikirjaus.isPresent()){
            return tuntiKirjausDao.getAll().get().stream()
                    .filter(tuntiKirjaus -> tuntiKirjaus.getStartTime().isAfter(optionalAlkuPaiva.orElse(LocalDate.MIN).atStartOfDay()))
                    .filter(tuntiKirjaus -> tuntiKirjaus.getStartTime().isBefore(optionalLoppupaiva.orElse(LocalDate.MAX).atTime(23, 59)))
                    .filter(tuntiKirjaus -> tuntiKirjaus.getTopic().toLowerCase().contains(optionalSearchQuery.orElse("").toLowerCase()))
                    .toList();
        } else {
            return List.of();
        }
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

    public static String getHtpsStringFromMinutes(long sumOfHoursInMinutes) {
        DecimalFormat decimalFormat = new DecimalFormat("0.0");
        return decimalFormat.format(sumOfHoursInMinutes /60.0f/7.5);
    }

    public static String getMinutesStringFromMinutes(long sumOfHoursInMinutes) {
        return String.valueOf((int) (sumOfHoursInMinutes % 60f));
    }

    public static String getHoursStringFromMinutes(long sumOfHoursInMinutes) {
        return String.valueOf((int) (sumOfHoursInMinutes / 60));
    }

    public static String getYhteenvetoText(List<TuntiKirjaus> tuntiKirjausList){

        if(tuntiKirjausList == null || tuntiKirjausList.isEmpty()){
            return "";
        }

        Predicate<TuntiKirjaus> predicate = Predicate.not(TuntiKirjaus::isEndTimeNull).and(TuntiKirjaus::isDurationEnabled);

        Map<String, String> topicToDuration = tuntiKirjausList.stream()
                .filter(predicate)
                .collect(
                        Collectors.groupingBy(
                                TuntiKirjaus::getClassification,
                                Collectors.collectingAndThen(
                                        Collectors.summingLong(t -> t.getDurationInDuration().toMinutes()),
                                        minutes -> String.format("%s:%s", minutes/60, (minutes%60 < 10 ? "0"+minutes%60 : minutes%60))
                                )
                        )
                );

        StringBuilder returnValue = new StringBuilder();
        for(Map.Entry<String, String> entry: topicToDuration.entrySet()){
            returnValue.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }

        return returnValue.toString();
    }
}
