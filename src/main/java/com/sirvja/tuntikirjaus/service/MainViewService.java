package com.sirvja.tuntikirjaus.service;

import com.sirvja.tuntikirjaus.domain.Paiva;
import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;
import com.sirvja.tuntikirjaus.exception.EmptyTopicException;
import com.sirvja.tuntikirjaus.exception.MalformatedTimeException;
import com.sirvja.tuntikirjaus.exception.StartTimeNotAfterLastTuntikirjausException;
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
    private final TuntiKirjausService tuntikirjausService;
    private LocalDate currentDate;

    private final Logger log = LoggerFactory.getLogger(MainViewService.class);

    public MainViewService() {
        this.tuntikirjausService = new TuntiKirjausService();
        this.currentDate = LocalDate.now();
    }

    public ObservableList<TuntiKirjaus> getTuntiDataForTable(){
        return FXCollections.observableArrayList(getTuntiKirjausDataForDate(currentDate));
    }
    public ObservableList<Paiva> getPaivaDataForTable(){
        return tuntikirjausService.getAllTuntikirjausWithCache().stream()
                .map(TuntiKirjaus::getLocalDateOfStartTime)
                .distinct()
                .map(Paiva::new)
                .sorted()
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    public void addTuntikirjaus(TuntiKirjaus tuntiKirjaus){
        tuntikirjausService.save(tuntiKirjaus);
        Optional<TuntiKirjaus> previousKirjaus = addEndTimeToSecondLatestTuntikirjaus();
        previousKirjaus.ifPresent(tuntikirjausService::update);
    }

    public void removeTuntikirjaus(TuntiKirjaus tuntiKirjaus){
        tuntikirjausService.delete(tuntiKirjaus);
        handlePreviousKirjausAfterRemove(tuntiKirjaus);
    }

    public LocalDateTime parseTimeFromString(String time) throws MalformatedTimeException {
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
                    throw new MalformatedTimeException("Couldn't parse time from String");
                }
            }
        } else {
            localDateTime = LocalDateTime.of(currentDate, LocalTime.now());
        }

        return localDateTime;
    }

    public void setCurrentDate(Paiva paiva){
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

    private List<TuntiKirjaus> getTuntiKirjausDataForDate(LocalDate localDate) {
        return tuntikirjausService.getTuntiKirjausForDate(localDate)
                .stream()
                .toList();
    }

    private Optional<TuntiKirjaus> addEndTimeToSecondLatestTuntikirjaus(){
        List<TuntiKirjaus> tuntiKirjausListForDay = getTuntiKirjausDataForDate(currentDate);
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

        Optional<TuntiKirjaus> previousKirjaus = tuntikirjausService.getTuntiKirjausForDate(currentDate).stream()
                .filter(isPrevious)
                .findAny();

        Optional<TuntiKirjaus> nextKirjaus = tuntikirjausService.getTuntiKirjausForDate(currentDate).stream()
                .filter(isNext)
                .findAny();

        if(previousKirjaus.isPresent() && nextKirjaus.isPresent()){
            handleRemovedInMiddle(previousKirjaus.get(), nextKirjaus.get());
        } else previousKirjaus.ifPresent(this::handleRemovedInEnd);
    }

    private void handleRemovedInMiddle(TuntiKirjaus previousKirjaus, TuntiKirjaus nextKirjaus) {
        previousKirjaus.setEndTime(nextKirjaus.getStartTime());
        updateTuntikirjaus(previousKirjaus);
    }

    private void handleRemovedInEnd(TuntiKirjaus previousKirjaus) {
        previousKirjaus.setEndTime(null);
        updateTuntikirjaus(previousKirjaus);
    }

    public Optional<Set<String>> getAiheEntries(){
        Set<String> alltopics = tuntikirjausService.getAllTuntikirjausWithCache().stream()
                .map(TuntiKirjaus::getTopic)
                .collect(Collectors.toSet());

        log.debug(String.format("Got all topics from db: %s", alltopics));

        return Optional.of(alltopics);
    }

    public void updateTuntikirjaus(TuntiKirjaus tuntiKirjaus){
        tuntikirjausService.update(tuntiKirjaus);
    }

    public TuntiKirjaus addNewTuntikirjaus(String time, String topic) throws EmptyTopicException, MalformatedTimeException, StartTimeNotAfterLastTuntikirjausException {
        if(topic.isEmpty()){
            throw new EmptyTopicException("Topic was empty when tried to save new Tuntikirjaus");
        }

        LocalDateTime localDateTime = parseTimeFromString(time);

        TuntiKirjaus tuntiKirjaus = new TuntiKirjaus(localDateTime, null, topic, true);

        ObservableList<TuntiKirjaus> tuntidata = getTuntiDataForTable();

        if(!tuntidata.isEmpty() && tuntidata.get(tuntidata.size()-1).compareTo(tuntiKirjaus) > 0){
            throw new StartTimeNotAfterLastTuntikirjausException("Start time of the latest Tuntikirjaus was not before Tuntikirjaus to be saved");
        }

        tuntiKirjaus = tuntikirjausService.save(tuntiKirjaus);
        Optional<TuntiKirjaus> previousKirjaus = addEndTimeToSecondLatestTuntikirjaus();
        previousKirjaus.ifPresent(tuntikirjausService::update);

        return tuntiKirjaus;
    }
}
