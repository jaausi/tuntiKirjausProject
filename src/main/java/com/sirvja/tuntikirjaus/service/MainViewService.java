package com.sirvja.tuntikirjaus.service;

import com.sirvja.tuntikirjaus.domain.Paiva;
import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;
import com.sirvja.tuntikirjaus.dao.TuntiKirjausDao;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MainViewService {
    private TuntiKirjausService tuntikirjausService;

    private List<TuntiKirjaus> tuntiKirjausData; // TODO: SortedSet could be used here
    private List<Paiva> paivaData; // TODO: SortedSet could be used here
    private LocalDate currentDate;

    private final Logger log = LoggerFactory.getLogger(TuntiKirjausDao.class);

    public MainViewService() {
        this.tuntikirjausService = new TuntiKirjausService();
        this.tuntiKirjausData = tuntikirjausService.getAllTuntikirjausWithCache();
        this.paivaData = getAllPaivas(tuntiKirjausData);
        this.currentDate = LocalDate.now();
    }

    public ObservableList<TuntiKirjaus> getTuntiDataForTable(){
        return tuntikirjausService.getTuntiKirjausForDate(currentDate)
                .stream()
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }
    public ObservableList<Paiva> getPaivaDataForTable(){
        return getAllPaivas(tuntikirjausService.getAllTuntikirjausWithCache());
    }

    public void addTuntikirjaus(TuntiKirjaus tuntiKirjaus){
        tuntiKirjausData.add(tuntikirjausService.save(tuntiKirjaus));
        paivaData = getAllPaivas(tuntiKirjausData);
        Optional<TuntiKirjaus> previousKirjaus = addEndTimeToSecondLatestTuntikirjaus();
        previousKirjaus.ifPresent(tuntikirjausService::update);
    }

    public void removeTuntikirjaus(TuntiKirjaus tuntiKirjaus){
        tuntikirjausService.delete(tuntiKirjaus);
        tuntiKirjausData.remove(tuntiKirjaus);
        handlePreviousKirjausAfterRemove(tuntiKirjaus);
    }

    public LocalDateTime parseTimeFromString(String time) throws DateTimeParseException{
        LocalDateTime localDateTime;

        log.debug("Received {} from time field. Trying to parse...", time);

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

    public void setCurrentDate(Paiva paiva){
        boolean paivaExists = paivaData.stream()
                .anyMatch(tempPaiva -> tempPaiva.getLocalDate().equals(paiva.getLocalDate()));
        if(!paivaExists){
            paivaData.add(paiva);
        }
        currentDate = paiva.getLocalDate();
    }

    public LocalDate getCurrentDate(){
        return currentDate;
    }

    public String getYhteenvetoText(){
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

    private Optional<TuntiKirjaus> addEndTimeToSecondLatestTuntikirjaus(){
        ObservableList<TuntiKirjaus> tuntiKirjausListForDay = getTuntiDataForTable();
        log.debug("Adding endtime for previous kirjaus");
        int indexA = tuntiKirjausListForDay.size()-1;
        int indexB = tuntiKirjausListForDay.size()-2;
        if(indexA < 0 || indexB < 0){
            log.debug("Indexes are out of bounds returning");
            return Optional.empty();
        }

        TuntiKirjaus currentKirjaus = tuntiKirjausListForDay.get(indexA);
        TuntiKirjaus previousKirjaus = tuntiKirjausListForDay.get(indexB);
        log.debug("Current kirjaus: {}", currentKirjaus);
        log.debug("Previous kirjaus: {}", previousKirjaus);

        if(previousKirjaus.isEndTimeNull()){
            log.debug("Allowed to add endtime.");
            previousKirjaus.setEndTime(currentKirjaus.getStartTime());
        }

        return Optional.of(previousKirjaus);
    }

    private void handlePreviousKirjausAfterRemove(TuntiKirjaus tuntiKirjaus){
        Predicate<TuntiKirjaus> isPrevious = previousTk -> previousTk.getEndTime()
                .map(previousEndTime -> previousEndTime.equals(tuntiKirjaus.getStartTime()))
                .orElse(false);
        Predicate<TuntiKirjaus> isNext = nextTk -> tuntiKirjaus.getEndTime()
                .map(currentEndTime -> currentEndTime.equals(nextTk.getStartTime()))
                .orElse(false);

        Optional<TuntiKirjaus> previousKirjaus = tuntiKirjausData.stream()
                .filter(isPrevious)
                .findAny();

        Optional<TuntiKirjaus> nextKirjaus = tuntiKirjausData.stream()
                .filter(isNext)
                .findAny();

        if(previousKirjaus.isPresent() && nextKirjaus.isPresent()){
            handleRemovedInMiddle(previousKirjaus.get(), nextKirjaus.get());
        } else previousKirjaus.ifPresent(this::handleRemovedInEnd);
    }

    private void handleRemovedInMiddle(TuntiKirjaus previousKirjaus, TuntiKirjaus nextKirjaus) {
        previousKirjaus.setEndTime(nextKirjaus.getStartTime());
        update(previousKirjaus); // TODO: Should be done through TuntikirjausService
    }

    private void handleRemovedInEnd(TuntiKirjaus previousKirjaus) {
        previousKirjaus.setEndTime(null);
        update(previousKirjaus); // TODO: Should be done through TuntikirjausService
    }


    private ObservableList<Paiva> getAllPaivas(List<TuntiKirjaus> tuntiKirjausList) {
        return tuntiKirjausList.stream()
                .map(TuntiKirjaus::getLocalDateOfStartTime)
                .distinct()
                .map(Paiva::new)
                .sorted()
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    public Map<LocalDate, ObservableList<TuntiKirjaus>> getDateToTuntikirjausMap(){
        List<TuntiKirjaus> allTuntikirjaus = tuntikirjausService.getAllTuntikirjausWithCache();

        return allTuntikirjaus.stream()
                .collect(
                        Collectors.groupingBy(
                                TuntiKirjaus::getLocalDateOfStartTime,
                                Collectors.toCollection(FXCollections::observableArrayList)
                        )
                );
    }

    public Optional<Set<String>> getAiheEntries(){
        Set<String> alltopics = tuntikirjausService.getAllTuntikirjausWithCache().stream()
                .map(TuntiKirjaus::getTopic)
                .collect(Collectors.toSet());

        log.debug(String.format("Got all topics from db: %s", alltopics));

        return Optional.of(alltopics);
    }

    public void update(TuntiKirjaus tuntiKirjaus){
        tuntikirjausService.update(tuntiKirjaus);
    }
}
