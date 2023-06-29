package com.sirvja.tuntikirjaus.service;

import com.sirvja.tuntikirjaus.commandPattern.command.AddOperation;
import com.sirvja.tuntikirjaus.commandPattern.command.EditOperation;
import com.sirvja.tuntikirjaus.commandPattern.command.RemoveOperation;
import com.sirvja.tuntikirjaus.commandPattern.command.TuntiKirjausOperation;
import com.sirvja.tuntikirjaus.commandPattern.invoker.TuntiKirjausOperationExecutor;
import com.sirvja.tuntikirjaus.commandPattern.receiver.TuntiKirjausList;
import com.sirvja.tuntikirjaus.domain.Paiva;
import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;
import com.sirvja.tuntikirjaus.utils.ListUtils;
import com.sirvja.tuntikirjaus.utils.TimeUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.sirvja.tuntikirjaus.utils.Constants.FETCH_DAYS_SINCE;


public class MainViewService {
    private final Logger LOGGER = LoggerFactory.getLogger(MainViewService.class);
    private final TuntiKirjausOperationExecutor tuntiKirjausOperationExecutor;
    private final TuntiKirjausList tuntiKirjausList;

    public MainViewService(){
        this.tuntiKirjausOperationExecutor = TuntiKirjausOperationExecutor.getInstance();
        this.tuntiKirjausList = TuntiKirjausList.getInstance();
    }

    /**
     * Method to get all TuntiKirjaus objects from currentDate to the TableView.
     * @return ObservableList of TuntiKirjaus objects
     */
    public ObservableList<TuntiKirjaus> getTuntiDataForTable(LocalDate currentDate){
        Predicate<TuntiKirjaus> isForToday = tuntiKirjaus -> tuntiKirjaus.getLocalDateOfStartTime().equals(currentDate);

        return tuntiKirjausList.getAll().stream()
                .filter(isForToday)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    /**
     * Method to get all Paiva objects based on tuntiKirjausList fetched form DB.
     * @return ObservableList of Paiva objects
     */
    public ObservableList<Paiva> getPaivaDataForTable() {
        return tuntiKirjausList.getAll().stream()
                .map(TuntiKirjaus::getLocalDateOfStartTime)
                .distinct()
                .map(Paiva::new)
                .sorted()
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    /**
     * Method to persist TuntiKirjaus object to db and program memory.
     * @param tuntiKirjaus
     */
    public void addTuntikirjaus(TuntiKirjaus tuntiKirjaus){
        List<TuntiKirjausOperation> operationList = new ArrayList<>();
        operationList.add(new AddOperation(tuntiKirjaus));
        Optional<TuntiKirjaus> previousKirjaus = handlePreviousKirjausAfterAdd(tuntiKirjaus);
        previousKirjaus.map(EditOperation::new)
                .ifPresent(operationList::add);
        tuntiKirjausOperationExecutor.executeOperations(operationList);
    }

    /**
     * Method to remove TuntiKirjaus object from db and program memory.
     * @param tuntiKirjaus
     */
    public void removeTuntikirjaus(TuntiKirjaus tuntiKirjaus){
        List<TuntiKirjausOperation> operationList = new ArrayList<>();
        operationList.add(new RemoveOperation(tuntiKirjaus));
        Optional<TuntiKirjaus> previousKirjaus = handlePreviousKirjausAfterRemove(tuntiKirjaus);
        previousKirjaus.map(EditOperation::new)
                .ifPresent(operationList::add);
        tuntiKirjausOperationExecutor.executeOperations(operationList);
    }

    public void updateTuntikirjaus(TuntiKirjaus tuntiKirjaus){
        List<TuntiKirjausOperation> operationList = new ArrayList<>();
        operationList.add(new EditOperation(tuntiKirjaus));
        Optional<TuntiKirjaus> previousKirjaus = handlePreviousKirjausAfterUpdate();
        previousKirjaus.map(EditOperation::new)
                .ifPresent(operationList::add);
        tuntiKirjausOperationExecutor.executeOperations(operationList);
    }

    public void undoTuntikirjausChange() {
        tuntiKirjausOperationExecutor.undo();
    }
    public void redoTuntikirjausChange() {
        tuntiKirjausOperationExecutor.redo();
    }
    public boolean canUndoTuntikirjausChange() {
        return tuntiKirjausOperationExecutor.canUndo();
    }
    public boolean canRedoTuntikirjausChange() {
        return tuntiKirjausOperationExecutor.canRedo();
    }

    /**
     * Method to parse LocalDateTime object from String object and currentDate.
     * Uses TimeUtils method to parse LocalTime.
     * @param time in one of the following formats: h, hh, hmm, hhmm, h:mm, hh:mm, h.mm, hh.mm
     * @return LocalDateTime of currentDate and parsed time
     * @throws DateTimeParseException if time couldn't be parsed from String object
     */
    public LocalDateTime parseTimeFromString(String time, LocalDate currentDate) throws DateTimeParseException{
        if(time.isEmpty()){
            return LocalDateTime.of(currentDate, LocalTime.now());
        } else {
            return LocalDateTime.of(currentDate, TimeUtils.parseTimeFromString(time));
        }
    }

    public String getYhteenvetoText(LocalDate currentDate) {
        ObservableList<TuntiKirjaus> tuntiKirjausListForDay = getTuntiDataForTable(currentDate);
        return getYhteenvetoText(tuntiKirjausListForDay);
    }

    public Set<String> getAiheEntries(){
        return tuntiKirjausList.getAll().stream()
                .map(TuntiKirjaus::getTopic)
                .collect(Collectors.toSet());
    }

    private Optional<TuntiKirjaus> handlePreviousKirjausAfterUpdate() {
        // TODO: Handle previous properly
        return Optional.empty();
    }

    private Optional<TuntiKirjaus> handlePreviousKirjausAfterAdd(TuntiKirjaus tuntiKirjaus){
        // TODO: Fix me
        ObservableList<TuntiKirjaus> tuntiKirjausListForDay = getTuntiDataForTable(LocalDate.now()); // TODO: this doesn't work

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

    private String getYhteenvetoText(ObservableList<TuntiKirjaus> tuntiKirjausListForDay) {
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

    private Optional<TuntiKirjaus> handlePreviousKirjausAfterRemove(TuntiKirjaus tuntiKirjaus){
        // TODO: Fix me
        Predicate<TuntiKirjaus> isPrevious = previousTk -> previousTk.getEndTime()
                .map(previousEndTime -> previousEndTime.equals(tuntiKirjaus.getStartTime()))
                .orElse(false);
        Predicate<TuntiKirjaus> isNext = nextTk -> tuntiKirjaus.getEndTime()
                .map(currentEndTime -> currentEndTime.equals(nextTk.getStartTime()))
                .orElse(false);

        Optional<TuntiKirjaus> previousKirjaus = tuntiKirjausList.getAll().stream()
                .filter(isPrevious)
                .findAny();

        Optional<TuntiKirjaus> nextKirjaus = tuntiKirjausList.getAll().stream()
                .filter(isNext)
                .findAny();

        if(previousKirjaus.isPresent() && nextKirjaus.isPresent()){
            return Optional.of(handleRemovedInMiddle(previousKirjaus.get(), nextKirjaus.get()));
        } else return previousKirjaus.map(this::handleRemovedInEnd);
    }

    private TuntiKirjaus handleRemovedInMiddle(TuntiKirjaus previousKirjaus, TuntiKirjaus nextKirjaus) {
        previousKirjaus.setEndTime(nextKirjaus.getStartTime());
        return previousKirjaus;
    }

    private TuntiKirjaus handleRemovedInEnd(TuntiKirjaus previousKirjaus) {
        previousKirjaus.setEndTime(null);
        return previousKirjaus;
    }
}
