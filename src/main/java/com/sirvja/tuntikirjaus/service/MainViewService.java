package com.sirvja.tuntikirjaus.service;

import com.sirvja.tuntikirjaus.domain.Paiva;
import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;
import com.sirvja.tuntikirjaus.utils.TuntiKirjausDao;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MainViewService {

    private static final TuntiKirjausDao tuntiKirjausDao = new TuntiKirjausDao();
    private static final Logger LOGGER = LoggerFactory.getLogger(MainViewService.class);
    private static ObservableList<TuntiKirjaus> tuntiKirjausList = getAllKirjausFromDb().orElse(FXCollections.observableArrayList());
    private static LocalDate currentDate = LocalDate.now();

    public static ObservableList<TuntiKirjaus> getTuntiDataForTable(){
        return getTuntiDataForTable(currentDate);
    }

    public static ObservableList<TuntiKirjaus> getTuntiDataForTable(LocalDate localDate){

        return getAllKirjausForPaiva(getDateToTuntikirjausMap(tuntiKirjausList), localDate);
    }

    public static ObservableList<Paiva> getPaivaDataForTable(){
        return getAllPaivas(tuntiKirjausList);
    }


    public static String getYhteenvetoText(){
        return getYhteenvetoText(currentDate);
    }

    public static String getYhteenvetoText(LocalDate localDate){
        ObservableList<TuntiKirjaus> tuntiDataForTable = getTuntiDataForTable(localDate);
        if(tuntiDataForTable == null || tuntiDataForTable.isEmpty()){
            return "";
        }

        Predicate<TuntiKirjaus> predicate = Predicate.not(TuntiKirjaus::isEndTimeNull).and(TuntiKirjaus::isDurationEnabled);

        Map<String, String> topicToDuration = tuntiDataForTable.stream()
                .filter(predicate)
                .collect(
                        Collectors.groupingBy(
                                TuntiKirjaus::getTopic,
                                Collectors.collectingAndThen(
                                        Collectors.summingLong(t -> t.getDuration().toMinutes()),
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

    public static void addTuntikirjaus(TuntiKirjaus tuntiKirjaus){
        tuntiKirjausList.add(tuntiKirjaus);
        addEndTimeToPreviousTuntikirjaus();
        tuntiKirjausDao.save(tuntiKirjaus);
    }

    public static LocalDateTime parseTimeFromString(String time) throws DateTimeParseException{
        LocalDateTime localDateTime;

        LOGGER.debug("Received {} from time field. Trying to parse...", time);

        if(!time.isEmpty()){
            if(time.contains(":")){ // Parse hours and minutes '9:00' or '12:00'
                localDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.parse(time, DateTimeFormatter.ofPattern("H:mm")));
            } else {
                if (time.length() <= 2){ // Parse only hours '9' or '12'
                    localDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.parse(time, DateTimeFormatter.ofPattern("H")));
                } else if(time.length() <= 4){ // Parse hours and minutes '922' -> 9:22 or '1222' -> '12:22'
                    localDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.parse(time, DateTimeFormatter.ofPattern("Hmm")));
                } else {
                    throw new DateTimeParseException("Couldn't parse time from String", time, 5);
                }
            }
        } else {
            localDateTime = LocalDateTime.now();
        }

        return localDateTime;
    }

    private static void addEndTimeToPreviousTuntikirjaus(){
        LOGGER.debug("Adding endtime for previous kirjaus");
        int indexA = tuntiKirjausList.size()-1;
        int indexB = tuntiKirjausList.size()-2;
        if(indexA < 0 || indexB < 0){
            LOGGER.debug("Indexes are out of bounds returning");
            return;
        }

        TuntiKirjaus currentKirjaus = tuntiKirjausList.get(indexA);
        TuntiKirjaus previousKirjaus = tuntiKirjausList.get(indexB);
        LOGGER.debug("Current kirjaus: {}", currentKirjaus);
        LOGGER.debug("Previous kirjaus: {}", previousKirjaus);

        if(previousKirjaus.isEndTimeNull() && previousKirjaus.isDurationEnabled()){
            LOGGER.debug("Allowed to add endtime.");
            previousKirjaus.setEndTime(currentKirjaus.getStartTime());
        }
    }

    private static Optional<ObservableList<TuntiKirjaus>> getAllKirjausFromDb(){
        LOGGER.debug("Getting kirjaus' from database...");
        Optional<ObservableList<TuntiKirjaus>> allKirjaus = tuntiKirjausDao.getAll();
        LOGGER.debug("Found {} kirjaus' from database.", allKirjaus.map(List::size).orElse(0));

        allKirjaus.ifPresent(kirjaus -> kirjaus.stream()
                .filter(Predicate.not(TuntiKirjaus::isEndTimeNull).and(TuntiKirjaus::isDurationEnabled))
                .forEach(tuntiKirjaus -> tuntiKirjaus.setDuration(Duration.between(tuntiKirjaus.getStartTime(), tuntiKirjaus.getEndTime().get()))));

        return allKirjaus;
    }

    private static ObservableList<Paiva> getAllPaivas(ObservableList<TuntiKirjaus> tuntiKirjausList){
        return tuntiKirjausList.stream()
                .map(TuntiKirjaus::getLocalDateOfStartTime)
                .distinct()
                .map(Paiva::new)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    private static ObservableList<Paiva> getAllPaivas(Map<LocalDate, ObservableList<TuntiKirjaus>> dateToTuntikirjausMap){
        return dateToTuntikirjausMap.keySet().stream()
                .map(Paiva::new)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    private static ObservableList<TuntiKirjaus> getAllKirjausForPaiva(Map<LocalDate, ObservableList<TuntiKirjaus>> dateToTuntikirjausMap, LocalDate localDate){
        return dateToTuntikirjausMap.get(localDate);
    }

    private static Map<LocalDate, ObservableList<TuntiKirjaus>> getDateToTuntikirjausMap(ObservableList<TuntiKirjaus> allTuntikirjaus){
        return allTuntikirjaus.stream()
                .collect(
                        Collectors.groupingBy(
                                TuntiKirjaus::getLocalDateOfStartTime,
                                Collectors.toCollection(FXCollections::observableArrayList)
                        )
                );
    }

}
