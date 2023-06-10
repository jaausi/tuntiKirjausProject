package com.sirvja.tuntikirjaus.service;

import com.sirvja.tuntikirjaus.domain.Paiva;
import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;
import com.sirvja.tuntikirjaus.utils.TimeUtils;
import com.sirvja.tuntikirjaus.utils.TuntiKirjausDao;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.sirvja.tuntikirjaus.utils.Constants.FETCH_DAYS_SINCE;


public class MainViewService {
    private final Logger LOGGER = LoggerFactory.getLogger(MainViewService.class);
    private final TuntiKirjausDao tuntiKirjausDao;
    private LocalDate currentDate;
    private List<TuntiKirjaus> tuntiKirjausList;

    public MainViewService(TuntiKirjausDao tuntiKirjausDao){
        this.tuntiKirjausDao = tuntiKirjausDao;
        currentDate = LocalDate.now();
        tuntiKirjausList = getAllKirjausFromDb();
    }

    /**
     * Method to get all TuntiKirjaus rows to the table.
     * @return ObservableList of TuntiKirjaus objects
     */
    public ObservableList<TuntiKirjaus> getTuntiDataForTable(){
        Predicate<TuntiKirjaus> isForToday = tuntiKirjaus -> tuntiKirjaus.getLocalDateOfStartTime().equals(currentDate);

        return tuntiKirjausList.stream()
                .filter(isForToday)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }
    public ObservableList<Paiva> getPaivaDataForTable(){
        return getAllPaivas().stream()
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    public void addTuntikirjaus(TuntiKirjaus tuntiKirjaus){
        tuntiKirjausList.add(tuntiKirjausDao.save(tuntiKirjaus));
        Optional<TuntiKirjaus> previousKirjaus = addEndTimeToSecondLatestTuntikirjaus();
        previousKirjaus.ifPresent(tuntiKirjausDao::update);
    }

    public void removeTuntikirjaus(TuntiKirjaus tuntiKirjaus){
        tuntiKirjausDao.delete(tuntiKirjaus);
        tuntiKirjausList.remove(tuntiKirjaus);
        handlePreviousKirjausAfterRemove(tuntiKirjaus);
    }

    public LocalDateTime parseTimeFromString(String time) throws DateTimeParseException{
        if(time.isEmpty()){
            return LocalDateTime.of(currentDate, LocalTime.now());
        } else {
            return LocalDateTime.of(currentDate, TimeUtils.parseTimeFromString(time));
        }
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

    public Optional<Set<String>> getAiheEntries(){
        Set<String> alltopics = getAllKirjausFromDb().stream()
                .map(TuntiKirjaus::getTopic)
                .collect(Collectors.toSet());

        LOGGER.debug(String.format("Got all topics from db: %s", alltopics));

        return Optional.of(alltopics);
    }

    public void update(TuntiKirjaus tuntiKirjaus){
        tuntiKirjausDao.update(tuntiKirjaus);
    }

    private Optional<TuntiKirjaus> addEndTimeToSecondLatestTuntikirjaus(){
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

    private void handlePreviousKirjausAfterRemove(TuntiKirjaus tuntiKirjaus){
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
        } else previousKirjaus.ifPresent(this::handleRemovedInEnd);
    }

    private void handleRemovedInMiddle(TuntiKirjaus previousKirjaus, TuntiKirjaus nextKirjaus) {
        previousKirjaus.setEndTime(nextKirjaus.getStartTime());
        update(previousKirjaus);
    }

    private void handleRemovedInEnd(TuntiKirjaus previousKirjaus) {
        previousKirjaus.setEndTime(null);
        update(previousKirjaus);
    }

    /**
     * Method to get all TuntiKirjaus data from the database. Uses AMOUNT_OF_DAYS_TO_FETCH configuration variable
     * from constants to deduce how old rows will be fetched from the database.
     *
     * @return
     */
    private ObservableList<TuntiKirjaus> getAllKirjausFromDb(){
        LOGGER.debug("Getting kirjaus' from database...");
        ObservableList<TuntiKirjaus> allKirjaus = tuntiKirjausDao.getAllFrom(FETCH_DAYS_SINCE).orElse(FXCollections.observableArrayList());
        LOGGER.debug("Found {} kirjaus' from database.", allKirjaus.size());

        return allKirjaus;
    }

    private ObservableList<Paiva> getAllPaivas(){
        return tuntiKirjausList.stream()
                .map(TuntiKirjaus::getLocalDateOfStartTime)
                .distinct()
                .map(Paiva::new)
                .sorted()
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }
}
