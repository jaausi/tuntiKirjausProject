package com.sirvja.tuntikirjaus.service;

import com.sirvja.tuntikirjaus.domain.Paiva;
import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;
import com.sirvja.tuntikirjaus.utils.TuntiKirjausDao;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final ObservableList<TuntiKirjaus> tuntiKirjausList = getInitialTuntiData();
    private static ObservableList<Paiva> paivaList = getInitialPaivaData();

    private static ObservableList<TuntiKirjaus> getInitialTuntiData(){
        return getAllKirjausFromDb();
    }
    private static ObservableList<Paiva> getInitialPaivaData(){
        return getAllPaivas(tuntiKirjausList);
    }

    public static ObservableList<TuntiKirjaus> getTuntiDataForTable(){
        Predicate<TuntiKirjaus> isForToday = tuntiKirjaus -> tuntiKirjaus.getLocalDateOfStartTime().equals(currentDate);

        return tuntiKirjausList.stream()
                .filter(isForToday)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }
    public static ObservableList<Paiva> getPaivaDataForTable(){
        return paivaList;
    }

    public static void addTuntikirjaus(TuntiKirjaus tuntiKirjaus){
        tuntiKirjausList.add(tuntiKirjausDao.save(tuntiKirjaus));
        paivaList = getAllPaivas(tuntiKirjausList);
        Optional<TuntiKirjaus> previousKirjaus = addEndTimeToSecondLatestTuntikirjaus();
        previousKirjaus.ifPresent(tuntiKirjausDao::update);
    }

    public static void removeTuntikirjaus(TuntiKirjaus tuntiKirjaus){
        tuntiKirjausDao.delete(tuntiKirjaus);
        tuntiKirjausList.remove(tuntiKirjaus);
        handlePreviousKirjausAfterRemove(tuntiKirjaus);
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

    public static void setCurrentDate(Paiva paiva){
        boolean paivaExists = paivaList.stream()
                .anyMatch(tempPaiva -> tempPaiva.getLocalDate().equals(paiva.getLocalDate()));
        if(!paivaExists){
            paivaList.add(paiva);
            paivaList = paivaList.sorted();
        }
        currentDate = paiva.getLocalDate();
    }

    public static LocalDate getCurrentDate(){
        return currentDate;
    }

    public static String getYhteenvetoText(){
        ObservableList<TuntiKirjaus> tuntiKirjausListForDay = getTuntiDataForTable();

        if(tuntiKirjausListForDay == null || tuntiKirjausListForDay.isEmpty()){
            return "";
        }

        Predicate<TuntiKirjaus> predicate = Predicate.not(TuntiKirjaus::isEndTimeNull).and(TuntiKirjaus::isDurationEnabled);

        Map<String, String> topicToDuration = tuntiKirjausListForDay.stream()
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

    private static Optional<TuntiKirjaus> addEndTimeToSecondLatestTuntikirjaus(){
        ObservableList<TuntiKirjaus> tuntiKirjausListForDay = getTuntiDataForTable();

        LOGGER.debug("Adding endtime for previous kirjaus");
        int indexA = tuntiKirjausListForDay.size()-1;
        int indexB = tuntiKirjausListForDay.size()-2;
        if(indexA < 0 || indexB < 0){
            LOGGER.debug("Indexes are out of bounds returning");
            return Optional.empty();
        }

        TuntiKirjaus currentKirjaus = tuntiKirjausListForDay.get(indexA);
        TuntiKirjaus previousKirjaus = tuntiKirjausListForDay.get(indexB);
        LOGGER.debug("Current kirjaus: {}", currentKirjaus);
        LOGGER.debug("Previous kirjaus: {}", previousKirjaus);

        if(previousKirjaus.isEndTimeNull()){
            LOGGER.debug("Allowed to add endtime.");
            previousKirjaus.setEndTime(currentKirjaus.getStartTime());
        }

        return Optional.of(previousKirjaus);
    }

    private static void handlePreviousKirjausAfterRemove(TuntiKirjaus tuntiKirjaus){
        Predicate<TuntiKirjaus> isPrevious = previousTk -> previousTk.getEndTime()
                .map(previousEndTime -> previousEndTime.equals(tuntiKirjaus.getStartTime()))
                .orElse(false);
        Predicate<TuntiKirjaus> isNext = nextTk -> tuntiKirjaus.getEndTime()
                .map(currentEndTime -> currentEndTime.equals(nextTk.getStartTime()))
                .orElse(false);

        Optional<TuntiKirjaus> previousKirjaus = tuntiKirjausList.stream()
                .filter(isPrevious)
                .findAny();

        Optional<TuntiKirjaus> nextKirjaus = tuntiKirjausList.stream()
                .filter(isNext)
                .findAny();

        if(previousKirjaus.isPresent() && nextKirjaus.isPresent()){
            handleRemovedInMiddle(previousKirjaus.get(), nextKirjaus.get());
        } else previousKirjaus.ifPresent(MainViewService::handleRemovedInEnd);
    }

    private static void handleRemovedInMiddle(TuntiKirjaus previousKirjaus, TuntiKirjaus nextKirjaus) {
        previousKirjaus.setEndTime(nextKirjaus.getStartTime());
        update(previousKirjaus);
    }

    private static void handleRemovedInEnd(TuntiKirjaus previousKirjaus) {
        previousKirjaus.setEndTime(null);
        update(previousKirjaus);
    }


    private static ObservableList<TuntiKirjaus> getAllKirjausFromDb(){
        LOGGER.debug("Getting kirjaus' from database...");
        ObservableList<TuntiKirjaus> allKirjaus = tuntiKirjausDao.getAllFrom(FETCH_DAYS_SINCE).orElse(FXCollections.observableArrayList());
        LOGGER.debug("Found {} kirjaus' from database.", allKirjaus.size());

        return allKirjaus;
    }

    private static ObservableList<Paiva> getAllPaivas(ObservableList<TuntiKirjaus> tuntiKirjausList){
        return tuntiKirjausList.stream()
                .map(TuntiKirjaus::getLocalDateOfStartTime)
                .distinct()
                .map(Paiva::new)
                .sorted()
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    public static Map<LocalDate, ObservableList<TuntiKirjaus>> getDateToTuntikirjausMap(){
        ObservableList<TuntiKirjaus> allTuntikirjaus = getAllKirjausFromDb();

        return allTuntikirjaus.stream()
                .collect(
                        Collectors.groupingBy(
                                TuntiKirjaus::getLocalDateOfStartTime,
                                Collectors.toCollection(FXCollections::observableArrayList)
                        )
                );
    }

    public static Optional<Set<String>> getAiheEntries(){
        Set<String> alltopics = getAllKirjausFromDb().stream()
                .map(TuntiKirjaus::getTopic)
                .collect(Collectors.toSet());

        LOGGER.debug(String.format("Got all topics from db: %s", alltopics));

        return Optional.of(alltopics);
    }

    public static void update(TuntiKirjaus tuntiKirjaus){
        tuntiKirjausDao.update(tuntiKirjaus);
    }
}
