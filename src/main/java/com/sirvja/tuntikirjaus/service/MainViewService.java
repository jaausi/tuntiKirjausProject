package com.sirvja.tuntikirjaus.service;

import com.sirvja.tuntikirjaus.domain.Paiva;
import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;
import com.sirvja.tuntikirjaus.exception.EmptyTopicException;
import com.sirvja.tuntikirjaus.exception.MalformatedTimeException;
import com.sirvja.tuntikirjaus.exception.StartTimeNotAfterLastTuntikirjausException;
import com.sirvja.tuntikirjaus.exception.TuntikirjausDatabaseInInconsistentStage;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.TableColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class MainViewService {
    private final TuntiKirjausService tuntikirjausService;
    private final AlertService alertService;
    private LocalDate currentDate;

    private static final Logger log = LoggerFactory.getLogger(MainViewService.class);

    public MainViewService() {
        this.tuntikirjausService = new TuntiKirjausService();
        this.alertService = new AlertService();
        this.currentDate = LocalDate.now();
    }

    public MainViewService(TuntiKirjausService tuntikirjausService, AlertService alertService) {
        this.tuntikirjausService = tuntikirjausService;
        this.alertService = alertService;
        this.currentDate = LocalDate.now();
    }

    public ObservableList<TuntiKirjaus> getTuntiDataForTable(){
        return FXCollections.observableArrayList(tuntikirjausService.getTuntiKirjausForDate(currentDate)).sorted();
    }
    public ObservableList<Paiva> getPaivaDataForTable(){
        return tuntikirjausService.getAllTuntikirjaus().stream()
                .map(TuntiKirjaus::getLocalDateOfStartTime)
                .distinct()
                .map(Paiva::new)
                .sorted()
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    public TuntiKirjaus addTuntikirjaus(String time, String topic) throws EmptyTopicException, MalformatedTimeException, StartTimeNotAfterLastTuntikirjausException, TuntikirjausDatabaseInInconsistentStage {
        if(topic == null || topic.isBlank()){
            throw new EmptyTopicException("Topic was empty when tried to save new Tuntikirjaus");
        }

        LocalDateTime localDateTime = parseTimeFromString(time);

        TuntiKirjaus tuntiKirjaus = new TuntiKirjaus(localDateTime, null, topic, true);

        ObservableList<TuntiKirjaus> tuntidata = getTuntiDataForTable();

        if(!tuntidata.isEmpty() && tuntidata.getLast().compareTo(tuntiKirjaus) > 0){
            throw new StartTimeNotAfterLastTuntikirjausException("Start time of the latest Tuntikirjaus was not before Tuntikirjaus to be saved");
        }

        Optional<TuntiKirjaus> previousKirjaus = Optional.empty();
        if(!tuntidata.isEmpty()) {
            previousKirjaus = addEndTimeToSecondLatestTuntikirjaus(tuntidata.getLast(), tuntiKirjaus);
        }

        tuntiKirjaus = tuntikirjausService.save(tuntiKirjaus);
        previousKirjaus.ifPresent(tuntikirjausService::update);

        return tuntiKirjaus;
    }

    public void removeTuntikirjaus(TuntiKirjaus tuntiKirjaus){
        tuntikirjausService.delete(tuntiKirjaus);
        handlePreviousKirjausAfterRemove(tuntiKirjaus);
    }

    public LocalDateTime parseTimeFromString(String time) throws MalformatedTimeException {
        LocalDateTime localDateTime;

        log.debug("Received {} from time field. Trying to parse...", time);

        try {
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
                        throw new MalformatedTimeException(String.format("Couldn't parse time from String: %s", time));
                    }
                }
            } else {
                localDateTime = LocalDateTime.of(currentDate, LocalTime.now().withSecond(0).withNano(0));
            }
        } catch (DateTimeParseException e) {
            throw new MalformatedTimeException(String.format("Couldn't parse time from String: %s", time));
        }

        return localDateTime;
    }

    public void setCurrentDate(LocalDate localDate){
        log.debug("Setting currentDate to: {}", localDate);
        currentDate = localDate;
    }

    public LocalDate getCurrentDate(){
        return currentDate;
    }

    public String getYhteenvetoText(){
        List<TuntiKirjaus> tuntiKirjausListForDay = tuntikirjausService.getTuntiKirjausForDate(currentDate);

        if(tuntiKirjausListForDay == null || tuntiKirjausListForDay.isEmpty()){
            return "";
        }

        SortedMap<String, String> projectToDuration = groupTuntikirjausListBasedOnClassification(tuntiKirjausListForDay);

        return topicToHoursMapIntoStringFormat(projectToDuration);
    }

    private SortedMap<String, String> groupTuntikirjausListBasedOnClassification(List<TuntiKirjaus> tuntiKirjausList) {
        Predicate<TuntiKirjaus> endTimeNotNull = Predicate.not(TuntiKirjaus::isEndTimeNull).and(TuntiKirjaus::isDurationEnabled);

        Collector<TuntiKirjaus, ?, Long> sumDurations = Collectors.summingLong(t -> t.getDurationInDuration().toMinutes());

        Function<Long, String> fullHoursFromMinutes = minutes -> String.valueOf(minutes/60);
        Function<Long, String> remainderMinutesWithPrecedingZero = minutes -> String.valueOf(minutes%60 < 10 ? "0"+minutes%60 : minutes%60);
        Function<Long, String> minutesToHoursAndMinutes = minutes -> String.format("%s:%s", fullHoursFromMinutes.apply(minutes), remainderMinutesWithPrecedingZero.apply(minutes));


        return tuntiKirjausList.stream()
                .filter(endTimeNotNull)
                .collect(
                        Collectors.groupingBy(
                                TuntiKirjaus::getClassification,
                                TreeMap::new,
                                Collectors.collectingAndThen(
                                        sumDurations,
                                        minutesToHoursAndMinutes
                                )
                        )
                );
    }

    private String topicToHoursMapIntoStringFormat(SortedMap<String, String> topicToDuration) {
        StringBuilder returnValue = new StringBuilder();
        for(Map.Entry<String, String> entry: topicToDuration.entrySet()){
            returnValue.append(entry.getKey()).append(": ").append(entry.getValue()).append(System.lineSeparator());
        }

        return returnValue.toString();
    }

    public SortedSet<String> getAiheEntries(){
        return tuntikirjausService.getAllTuntikirjaus().stream()
                .map(TuntiKirjaus::getTopic)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private Optional<TuntiKirjaus> addEndTimeToSecondLatestTuntikirjaus(TuntiKirjaus previousKirjaus, TuntiKirjaus currentKirjaus) throws TuntikirjausDatabaseInInconsistentStage {
        log.debug("Current kirjaus: {}", currentKirjaus);
        log.debug("Previous kirjaus: {}", previousKirjaus);

        if(previousKirjaus.isEndTimeNull()){
            log.debug("Allowed to add endtime.");
            previousKirjaus.setEndTime(currentKirjaus.getStartTime());
            return Optional.of(previousKirjaus);
        } else {
            throw new TuntikirjausDatabaseInInconsistentStage(String.format("Not able to set endTime (%s) for previous kirjaus, because it already contained endtime: %s", currentKirjaus.getStartTime(), previousKirjaus.getEndTime()));
        }
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
        tuntikirjausService.update(previousKirjaus);
        tuntikirjausService.update(nextKirjaus);
    }

    private void handleRemovedInEnd(TuntiKirjaus previousKirjaus) {
        previousKirjaus.setEndTime(null);
        tuntikirjausService.update(previousKirjaus);
    }

    public ChangeListener<Paiva> getDayListChangeListener(Runnable updateView) {
        return (observableValue, oldValue, newValue) -> {
            if(newValue != null){
                setCurrentDate(newValue.getLocalDate());
                updateView.run();
            }
        };
    }

    public EventHandler<TableColumn.CellEditEvent<TuntiKirjaus, LocalTime>> getKellonaikaColumnEditHandler(Runnable refreshTuntitaulukko){
        return editEvent -> {
            int tablePosition = editEvent.getTablePosition().getRow();
            int lastPosition = editEvent.getTableView().getItems().size() - 1;
            LocalDateTime newValue = LocalDateTime.of(getCurrentDate(), editEvent.getNewValue());
            boolean facedError = false;

            if(tablePosition < lastPosition){
                TuntiKirjaus followingKirjausToEdit = editEvent.getTableView().getItems().get(tablePosition + 1);
                // If edited time is after next kirjaus start time, abort.
                if(newValue.isAfter(followingKirjausToEdit.getStartTime())){
                    alertService.showNotCorrectTimeAlert(true);
                    facedError = true;
                }
            }

            // If not the first row of a day. Edit also the previous row end time.
            if(tablePosition > 0){
                TuntiKirjaus previousKirjausToEdit = editEvent.getTableView().getItems().get(tablePosition - 1);
                // If edited time is before previous kirjaus start time, abort.
                if(newValue.isBefore(previousKirjausToEdit.getStartTime())){
                    alertService.showNotCorrectTimeAlert(false);
                    facedError = true;
                }
                if(!facedError){
                    previousKirjausToEdit.setEndTime(newValue);
                    tuntikirjausService.update(previousKirjausToEdit);
                }
            }

            TuntiKirjaus kirjausToEdit = editEvent.getTableView().getItems().get(tablePosition);
            if(!facedError){
                kirjausToEdit.setStartTime(newValue);
                tuntikirjausService.update(kirjausToEdit);
            }
            refreshTuntitaulukko.run();
        };
    }

    public EventHandler<TableColumn.CellEditEvent<TuntiKirjaus, String>> getAiheColumnEditHandler(BiConsumer<String, String> updateAiheFieldEntry) {
        return editEvent -> {
            int rowInTableToBeEdited = editEvent.getTablePosition().getRow();
            TuntiKirjaus kirjausToBeEdited = editEvent.getTableView().getItems().get(rowInTableToBeEdited);
            String oldTopic = kirjausToBeEdited.getTopic();
            kirjausToBeEdited.setTopic(editEvent.getNewValue());
            tuntikirjausService.update(kirjausToBeEdited);
            updateAiheFieldEntry.accept(oldTopic, editEvent.getNewValue());
        };
    }
}
