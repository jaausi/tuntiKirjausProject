package com.sirvja.tuntikirjaus.service;

import com.sirvja.tuntikirjaus.model.HourRecord;
import com.sirvja.tuntikirjaus.model.DayRecord;
import com.sirvja.tuntikirjaus.utils.TuntiKirjausDao;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.sirvja.tuntikirjaus.utils.Constants.FETCH_DAYS_SINCE;

public class MainViewService {
    private static final TuntiKirjausDao tuntiKirjausDao = new TuntiKirjausDao();
    private static final Logger LOGGER = LoggerFactory.getLogger(MainViewService.class);
    private static LocalDate currentDate = LocalDate.now();
    private static final ObservableList<HourRecord> HOUR_RECORD_LIST = getInitialTuntiData();
    private static ObservableList<DayRecord> dayRecordList = getInitialPaivaData();

    private static ObservableList<HourRecord> getInitialTuntiData(){
        return getAllKirjausFromDb();
    }
    private static ObservableList<DayRecord> getInitialPaivaData(){
        return getAllPaivas(HOUR_RECORD_LIST);
    }

    public static ObservableList<HourRecord> getTuntiDataForTable(){
        Predicate<HourRecord> isForToday = tuntiKirjaus -> tuntiKirjaus.getLocalDateOfStartTime().equals(currentDate);

        return HOUR_RECORD_LIST.stream()
                .filter(isForToday)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }
    public static ObservableList<DayRecord> getPaivaDataForTable(){
        return dayRecordList;
    }

    public static void addTuntikirjaus(HourRecord hourRecord){
        HOUR_RECORD_LIST.add(tuntiKirjausDao.save(hourRecord));
        dayRecordList = getAllPaivas(HOUR_RECORD_LIST);
        Optional<HourRecord> previousKirjaus = addEndTimeToSecondLatestTuntikirjaus();
        previousKirjaus.ifPresent(tuntiKirjausDao::update);
    }

    public static void removeTuntikirjaus(HourRecord hourRecord){
        tuntiKirjausDao.delete(hourRecord);
        HOUR_RECORD_LIST.remove(hourRecord);
        handlePreviousKirjausAfterRemove(hourRecord);
    }

    public static LocalDateTime parseTimeFromString(String time) throws DateTimeParseException{
        LocalDateTime localDateTime;

        LOGGER.debug("Received {} from time field. Trying to parse...", time);

        if(!time.isEmpty()){
            if(time.contains(":")){ // Parse hours and minutes '9:00' or '12:00'
                localDateTime = LocalDateTime.of(currentDate, LocalTime.parse(time, DateTimeFormatter.ofPattern("H:mm")));
            } else if (time.contains(".")) {
                localDateTime = LocalDateTime.of(currentDate, LocalTime.parse(time, DateTimeFormatter.ofPattern("H.mm")));
            } else {
                if (time.length() <= 2){ // Parse only hours '9' or '12'
                    localDateTime = LocalDateTime.of(currentDate, LocalTime.parse(time, DateTimeFormatter.ofPattern("H")));
                } else if(time.length() <= 4){ // Parse hours and minutes '922' -> 9:22 or '1222' -> '12:22'
                    localDateTime = LocalDateTime.of(currentDate, LocalTime.parse(time, DateTimeFormatter.ofPattern("Hmm")));
                } else {
                    throw new DateTimeParseException("Couldn't parse time from String", time, 5);
                }
            }
        } else {
            localDateTime = LocalDateTime.of(currentDate, LocalTime.now());
        }

        return localDateTime;
    }

    public static void setCurrentDate(DayRecord dayRecord){
        boolean paivaExists = dayRecordList.stream()
                .anyMatch(tempDayRecord -> tempDayRecord.getLocalDate().equals(dayRecord.getLocalDate()));
        if(!paivaExists){
            dayRecordList.add(dayRecord);
            dayRecordList = dayRecordList.sorted();
        }
        currentDate = dayRecord.getLocalDate();
    }

    public static LocalDate getCurrentDate(){
        return currentDate;
    }

    public static String getYhteenvetoText(){
        ObservableList<HourRecord> hourRecordListForDay = getTuntiDataForTable();

        if(hourRecordListForDay == null || hourRecordListForDay.isEmpty()){
            return "";
        }

        Predicate<HourRecord> predicate = Predicate.not(HourRecord::isEndTimeNull).and(HourRecord::isDurationEnabled);

        Map<String, String> topicToDuration = hourRecordListForDay.stream()
                .filter(predicate)
                .collect(
                        Collectors.groupingBy(
                                HourRecord::getClassification,
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

    private static Optional<HourRecord> addEndTimeToSecondLatestTuntikirjaus(){
        ObservableList<HourRecord> hourRecordListForDay = getTuntiDataForTable();

        LOGGER.debug("Adding endtime for previous kirjaus");
        int indexA = hourRecordListForDay.size()-1;
        int indexB = hourRecordListForDay.size()-2;
        if(indexA < 0 || indexB < 0){
            LOGGER.debug("Indexes are out of bounds returning");
            return Optional.empty();
        }

        HourRecord currentKirjaus = hourRecordListForDay.get(indexA);
        HourRecord previousKirjaus = hourRecordListForDay.get(indexB);
        LOGGER.debug("Current kirjaus: {}", currentKirjaus);
        LOGGER.debug("Previous kirjaus: {}", previousKirjaus);

        if(previousKirjaus.isEndTimeNull()){
            LOGGER.debug("Allowed to add endtime.");
            previousKirjaus.setEndTime(currentKirjaus.getStartTime());
        }

        return Optional.of(previousKirjaus);
    }

    private static void handlePreviousKirjausAfterRemove(HourRecord hourRecord){
        Predicate<HourRecord> isPrevious = previousTk -> previousTk.getEndTime()
                .map(previousEndTime -> previousEndTime.equals(hourRecord.getStartTime()))
                .orElse(false);
        Predicate<HourRecord> isNext = nextTk -> hourRecord.getEndTime()
                .map(currentEndTime -> currentEndTime.equals(nextTk.getStartTime()))
                .orElse(false);

        Optional<HourRecord> previousKirjaus = HOUR_RECORD_LIST.stream()
                .filter(isPrevious)
                .findAny();

        Optional<HourRecord> nextKirjaus = HOUR_RECORD_LIST.stream()
                .filter(isNext)
                .findAny();

        if(previousKirjaus.isPresent() && nextKirjaus.isPresent()){
            handleRemovedInMiddle(previousKirjaus.get(), nextKirjaus.get());
        } else previousKirjaus.ifPresent(MainViewService::handleRemovedInEnd);
    }

    private static void handleRemovedInMiddle(HourRecord previousKirjaus, HourRecord nextKirjaus) {
        previousKirjaus.setEndTime(nextKirjaus.getStartTime());
        update(previousKirjaus);
    }

    private static void handleRemovedInEnd(HourRecord previousKirjaus) {
        previousKirjaus.setEndTime(null);
        update(previousKirjaus);
    }


    private static ObservableList<HourRecord> getAllKirjausFromDb(){
        LOGGER.debug("Getting kirjaus' from database...");
        ObservableList<HourRecord> allKirjaus = tuntiKirjausDao.getAllFrom(FETCH_DAYS_SINCE).orElse(FXCollections.observableArrayList());
        LOGGER.debug("Found {} kirjaus' from database.", allKirjaus.size());

        return allKirjaus;
    }

    private static ObservableList<DayRecord> getAllPaivas(ObservableList<HourRecord> hourRecordList){
        return hourRecordList.stream()
                .map(HourRecord::getLocalDateOfStartTime)
                .distinct()
                .map(DayRecord::new)
                .sorted()
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    public static Map<LocalDate, ObservableList<HourRecord>> getDateToTuntikirjausMap(){
        ObservableList<HourRecord> allTuntikirjaus = getAllKirjausFromDb();

        return allTuntikirjaus.stream()
                .collect(
                        Collectors.groupingBy(
                                HourRecord::getLocalDateOfStartTime,
                                Collectors.toCollection(FXCollections::observableArrayList)
                        )
                );
    }

    public static Optional<Set<String>> getAiheEntries(){
        Set<String> alltopics = getAllKirjausFromDb().stream()
                .map(HourRecord::getTopic)
                .collect(Collectors.toSet());

        LOGGER.debug(String.format("Got all topics from db: %s", alltopics));

        return Optional.of(alltopics);
    }

    public static void update(HourRecord hourRecord){
        tuntiKirjausDao.update(hourRecord);
    }
}
