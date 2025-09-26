package com.sirvja.tuntikirjaus.controller;

import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;
import com.sirvja.tuntikirjaus.service.ReportsViewService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class ReportsWeekViewController implements Initializable {

    private static final Logger log = LoggerFactory.getLogger(ReportsWeekViewController.class);
    private static final String LUNCH_TOPIC = "lounas";

    @FXML
    private TableView<WeeklyProjectHours> weeklyProjectHoursTable;
    @FXML
    private TableColumn<WeeklyProjectHours, String> projectColumn;
    @FXML
    private TableColumn<WeeklyProjectHours, String> projectMoColumn;
    @FXML
    private TableColumn<WeeklyProjectHours, String> projectTuColumn;
    @FXML
    private TableColumn<WeeklyProjectHours, String> projectWeColumn;
    @FXML
    private TableColumn<WeeklyProjectHours, String> projectThColumn;
    @FXML
    private TableColumn<WeeklyProjectHours, String> projectFrColumn;

    @FXML
    private TableView<WeeklyIncidents> weeklyIncidentsTable;
    @FXML
    private TableColumn<WeeklyIncidents, String> incidentColumn;
    @FXML
    private TableColumn<WeeklyIncidents, String> incidentMoColumn;
    @FXML
    private TableColumn<WeeklyIncidents, String> incidentTuColumn;
    @FXML
    private TableColumn<WeeklyIncidents, String> incidentWeColumn;
    @FXML
    private TableColumn<WeeklyIncidents, String> incidentThColumn;
    @FXML
    private TableColumn<WeeklyIncidents, String> incidentFrColumn;


    @FXML
    private Button backButton;
    @FXML
    private Button dateBackward;
    @FXML
    private Button dateForward;
    @FXML
    private Button dateNow;
    @FXML
    private TextField weekHoursSumField;
    @FXML
    private ComboBox<WeekSelectorItem> weekSelector;

    private final int thisYear = LocalDate.now().getYear();
    private List<TuntiKirjaus> tuntiKirjausListAll;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initTuntikirjausList();
        initProjectTable();
        initIncidentTable();
        initWeekSelector();
    }

    private void initTuntikirjausList() {
        tuntiKirjausListAll = ReportsViewService.getAllTuntikirjausAsList(Optional.empty(), Optional.empty(), Optional.empty());
    }

    private void initProjectTable() {
        projectColumn.setCellValueFactory(new PropertyValueFactory<>("project"));
        projectMoColumn.setCellValueFactory(new PropertyValueFactory<>("moHours"));
        projectTuColumn.setCellValueFactory(new PropertyValueFactory<>("tuHours"));
        projectWeColumn.setCellValueFactory(new PropertyValueFactory<>("weHours"));
        projectThColumn.setCellValueFactory(new PropertyValueFactory<>("thHours"));
        projectFrColumn.setCellValueFactory(new PropertyValueFactory<>("frHours"));
    }

    private void initIncidentTable() {
        incidentColumn.setCellValueFactory(new PropertyValueFactory<>("incident"));
        incidentMoColumn.setCellValueFactory(new PropertyValueFactory<>("moTime"));
        incidentTuColumn.setCellValueFactory(new PropertyValueFactory<>("tuTime"));
        incidentWeColumn.setCellValueFactory(new PropertyValueFactory<>("weTime"));
        incidentThColumn.setCellValueFactory(new PropertyValueFactory<>("thTime"));
        incidentFrColumn.setCellValueFactory(new PropertyValueFactory<>("frTime"));
    }

    private void initWeekSelector() {
        List<LocalDate> dateList = tuntiKirjausListAll.stream()
                .map(TuntiKirjaus::getStartTime)
                .map(LocalDateTime::toLocalDate)
                .distinct()
                .toList();

        List<WeekSelectorItem> weekSelectorList = createWeekSelectorList(dateList);

        weekSelector.setItems(FXCollections.observableArrayList(weekSelectorList));

        weekSelector.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            List<TuntiKirjaus> tuntiKirjausListForWeek = tuntiKirjausListAll.stream()
                    .filter(tuntiKirjaus -> tuntiKirjaus.getEndTime().isPresent())
                    .filter(tuntiKirjaus -> tuntiKirjaus.getStartTime().getYear() == thisYear)
                    .filter(tuntiKirjaus -> tuntiKirjaus.getStartTime().get(ChronoField.ALIGNED_WEEK_OF_YEAR) == observable.getValue().getWeekNum())
                    .toList();

            ObservableList<WeeklyProjectHours> weeklyProjectHours = mapTuntikirjausListToWeeklyProjectHours(tuntiKirjausListForWeek);
            ObservableList<WeeklyIncidents> weeklyIncidents = mapTuntikirjausListToWeeklyIncidents(tuntiKirjausListForWeek);
            String summaryString = getSummaryString(tuntiKirjausListForWeek);

            weeklyProjectHoursTable.setItems(weeklyProjectHours);
            weeklyIncidentsTable.setItems(weeklyIncidents);
            weekHoursSumField.setText(summaryString);
        }));

        weekSelector.getSelectionModel().select(0);
    }

    private List<WeekSelectorItem> createWeekSelectorList(List<LocalDate> dateList) {
        Map<WeekSelectorItem.YearWeekNum, List<LocalDate>> yearAndWeekToDateList = dateList.stream()
                .collect(Collectors.groupingBy(
                        localDate -> new WeekSelectorItem.YearWeekNum(localDate.getYear(), localDate.get(ChronoField.ALIGNED_WEEK_OF_YEAR)),
                        HashMap::new,
                        Collectors.toList()
                ));

        return yearAndWeekToDateList.entrySet().stream()
                .map(entry -> new WeekSelectorItem(entry.getKey().year, entry.getKey().weekNum, entry.getValue()))
                .sorted(Comparator.reverseOrder())
                .toList();
    }

    private String getSummaryString(List<TuntiKirjaus> tuntiKirjausListForWeek) {
        long sumOfHoursInMinutes = tuntiKirjausListForWeek.stream()
                .filter(tk -> !tk.getTopic().toLowerCase().contains(LUNCH_TOPIC))
                .map(TuntiKirjaus::getDurationInDuration)
                .mapToLong(Duration::toMinutes)
                .sum();

        String hours = ReportsViewService.getHoursStringFromMinutes(sumOfHoursInMinutes);
        String minutes = ReportsViewService.getMinutesStringFromMinutes(sumOfHoursInMinutes);
        String htps = ReportsViewService.getHtpsStringFromMinutes(sumOfHoursInMinutes);

        return String.format("%sh %sm (%s htp)", hours, minutes, htps);
    }

    private ObservableList<WeeklyProjectHours> mapTuntikirjausListToWeeklyProjectHours(List<TuntiKirjaus> tuntiKirjausListForWeek) {
        Map<String, List<TuntiKirjaus>> projectToTuntikirjausList = tuntiKirjausListForWeek.stream()
                .filter(tuntiKirjaus -> tuntiKirjaus.getEndTime().isPresent())
                .collect(Collectors.groupingBy(
                        TuntiKirjaus::getClassification,
                        HashMap::new,
                        Collectors.toList()
                ));

        return projectToTuntikirjausList.entrySet().stream()
                .map(entry -> new WeeklyProjectHours(entry.getKey(), entry.getValue()))
                .sorted()
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    private ObservableList<WeeklyIncidents> mapTuntikirjausListToWeeklyIncidents(List<TuntiKirjaus> tuntiKirjausListForWeek) {
        Map<Incident, List<LocalDateTime>> incidentToTimeMap = new HashMap<>();

        parseStartTimeIncidents(tuntiKirjausListForWeek, incidentToTimeMap);

        parseLunchTimeIncidents(tuntiKirjausListForWeek, incidentToTimeMap);

        parseEntTimeIncident(tuntiKirjausListForWeek, incidentToTimeMap);

        return incidentToTimeMap.entrySet().stream()
                .map(entry -> new WeeklyIncidents(entry.getKey(), entry.getValue()))
                .sorted()
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
    }

    private static void parseEntTimeIncident(List<TuntiKirjaus> currentWeekTuntikirjausList, Map<Incident, List<LocalDateTime>> incidentToTimeMap) {
        List<LocalDateTime> endTimesForWeek = currentWeekTuntikirjausList.stream()
                .filter(tuntiKirjaus -> tuntiKirjaus.getEndTime().isPresent())
                .collect(Collectors.groupingBy(tuntiKirjaus -> tuntiKirjaus.getStartTime().getDayOfWeek(),
                        HashMap::new,
                        Collectors.collectingAndThen(Collectors.toList(), tkList -> tkList.stream().max(Comparator.naturalOrder()).map(tk -> tk.getEndTime().orElseThrow()).orElseThrow())
                )).values().stream().toList();

        incidentToTimeMap.put(Incident.END_OF_DAY, endTimesForWeek);
    }

    private static void parseLunchTimeIncidents(List<TuntiKirjaus> currentWeekTuntikirjausList, Map<Incident, List<LocalDateTime>> incidentToTimeMap) {
        List<TuntiKirjaus> lunchTimesForWeek = currentWeekTuntikirjausList.stream()
                .filter(tuntiKirjaus -> tuntiKirjaus.getTopic().toLowerCase().contains(LUNCH_TOPIC))
                .collect(Collectors.groupingBy(
                        tuntiKirjaus -> tuntiKirjaus.getStartTime().getDayOfWeek(),
                        HashMap::new,
                        Collectors.collectingAndThen(Collectors.toList(), tkList -> tkList.stream().sorted().findFirst().orElse(null))
                )).values().stream().toList();

        incidentToTimeMap.put(Incident.START_OF_LUNCH, lunchTimesForWeek.stream().map(TuntiKirjaus::getStartTime).toList());
        incidentToTimeMap.put(Incident.END_OF_LUNCH, lunchTimesForWeek.stream().map(TuntiKirjaus::getEndTime).filter(Optional::isPresent).map(Optional::get).toList());
    }

    private void parseStartTimeIncidents(List<TuntiKirjaus> tuntiKirjausList, Map<Incident, List<LocalDateTime>> incidentToTimeMap) {
        List<LocalDateTime> startTimesForWeek = tuntiKirjausList.stream()
                .collect(Collectors.groupingBy(
                        tuntiKirjaus -> tuntiKirjaus.getStartTime().getDayOfWeek(),
                        HashMap::new,
                        Collectors.collectingAndThen(Collectors.toList(), tkList -> tkList.stream().sorted().findFirst().map(TuntiKirjaus::getStartTime).orElse(null))
                )).values().stream().toList();

        incidentToTimeMap.put(Incident.START_OF_DAY, startTimesForWeek);
    }

    public static class WeeklyIncidents implements Comparable<WeeklyIncidents> {
        Incident incident;
        LocalDateTime moTime;
        LocalDateTime tuTime;
        LocalDateTime weTime;
        LocalDateTime thTime;
        LocalDateTime frTime;

        public WeeklyIncidents(Incident incident, List<LocalDateTime> incidentTimes) {
            this.incident = incident;

            BiFunction<List<LocalDateTime>, DayOfWeek, LocalDateTime> filterWithDay =  (ldtList, dayOfWeek) -> ldtList.stream()
                    .filter(ldt -> ldt.getDayOfWeek().equals(dayOfWeek))
                    .findFirst().orElse(null);

            moTime = filterWithDay.apply(incidentTimes, DayOfWeek.MONDAY);
            tuTime = filterWithDay.apply(incidentTimes, DayOfWeek.TUESDAY);
            weTime = filterWithDay.apply(incidentTimes, DayOfWeek.WEDNESDAY);
            thTime = filterWithDay.apply(incidentTimes, DayOfWeek.THURSDAY);
            frTime = filterWithDay.apply(incidentTimes, DayOfWeek.FRIDAY);
        }

        private final Function<LocalDateTime, String> dateToStringTime = localDateTime -> {
            if(localDateTime!= null) {
                return localDateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
            }
            return "";
        };

        public String getIncident() {
            return incident.toString();
        }

        public String getMoTime() {
            return dateToStringTime.apply(moTime);
        }

        public String getTuTime() {
            return dateToStringTime.apply(tuTime);
        }

        public String getWeTime() {
            return dateToStringTime.apply(weTime);
        }

        public String getThTime() {
            return dateToStringTime.apply(thTime);
        }

        public String getFrTime() {
            return dateToStringTime.apply(frTime);
        }

        private final BiFunction<LocalDateTime, LocalDateTime, Optional<Integer>> nullCheckAndCompare = (ldt1, ldt2) -> {
            if(ldt1 == null || ldt2 == null) {
                return Optional.empty();
            }
            return Optional.of(ldt1.compareTo(ldt2));
        };

        @Override
        public int compareTo(WeeklyIncidents o) {
            // Compare based on first time that is found from both objects, if non is found, then based on incident name
            return nullCheckAndCompare.apply(moTime, o.moTime)
                    .or(() -> nullCheckAndCompare.apply(tuTime, o.tuTime))
                    .or(() -> nullCheckAndCompare.apply(weTime, o.weTime))
                    .or(() -> nullCheckAndCompare.apply(thTime, o.thTime))
                    .or(() -> nullCheckAndCompare.apply(frTime, o.frTime))
                    .orElse(incident.compareTo(o.incident));
        }
    }

    public enum Incident {
        START_OF_DAY,
        END_OF_DAY,
        START_OF_LUNCH,
        END_OF_LUNCH,
        START_OF_BREAK,
        END_OF_BREAK
    }

    public static class WeeklyProjectHours implements Comparable<WeeklyProjectHours> {
        String project;
        List<TuntiKirjaus> moHours;
        List<TuntiKirjaus> tuHours;
        List<TuntiKirjaus> weHours;
        List<TuntiKirjaus> thHours;
        List<TuntiKirjaus> frHours;

        public WeeklyProjectHours(String projectName, List<TuntiKirjaus> tuntiKirjausList) {
            this.project = projectName;

            BiFunction<List<TuntiKirjaus>, DayOfWeek, List<TuntiKirjaus>> filterWithDay = (tkList, dayOfWeek) -> tkList.stream()
                    .filter(tk -> tk.getEndTime().map(tk2 -> tk2.getDayOfWeek().equals(dayOfWeek)).orElseThrow())
                    .toList();

            moHours = filterWithDay.apply(tuntiKirjausList, DayOfWeek.MONDAY);
            tuHours = filterWithDay.apply(tuntiKirjausList, DayOfWeek.TUESDAY);
            weHours = filterWithDay.apply(tuntiKirjausList, DayOfWeek.WEDNESDAY);
            thHours = filterWithDay.apply(tuntiKirjausList, DayOfWeek.THURSDAY);
            frHours = filterWithDay.apply(tuntiKirjausList, DayOfWeek.FRIDAY);
        }

        private final Collector<TuntiKirjaus, ?, Long> sumDurations = Collectors.summingLong(t -> t.getDurationInDuration().toMinutes());
        private final Function<Long, String> fullHoursFromMinutes = minutes -> String.valueOf(minutes/60);
        private final Function<Long, String> remainderMinutesWithPrecedingZero = minutes -> String.valueOf(minutes%60 < 10 ? "0"+minutes%60 : minutes%60);
        private final Function<Long, String> minutesToHoursAndMinutes = minutes -> String.format("%s:%s", fullHoursFromMinutes.apply(minutes), remainderMinutesWithPrecedingZero.apply(minutes));
        private final Function<List<TuntiKirjaus>, String> tuntikirjausListToSumString = tuntiKirjausList -> tuntiKirjausList.stream()
                .collect(Collectors.collectingAndThen(
                        sumDurations,
                        minutesToHoursAndMinutes
                ));

        public String getProject() {
            return project;
        }

        public String getMoHours() {
            return tuntikirjausListToSumString.apply(moHours);
        }

        public String getTuHours() {
            return tuntikirjausListToSumString.apply(tuHours);
        }

        public String getWeHours() {
            return tuntikirjausListToSumString.apply(weHours);
        }

        public String getThHours() {
            return tuntikirjausListToSumString.apply(thHours);
        }

        public String getFrHours() {
            return tuntikirjausListToSumString.apply(frHours);
        }

        @Override
        public int compareTo(WeeklyProjectHours o) {
            return project.compareTo(o.project);
        }
    }

    public static class WeekSelectorItem implements Comparable<WeekSelectorItem> {
        int year;
        int weekNum;
        List<LocalDate> dates;

        public WeekSelectorItem(int year, int weekNum, List<LocalDate> dates) {
            this.year = year;
            this.weekNum = weekNum;
            this.dates = dates;
        }

        public int getYear() {
            return year;
        }

        public int getWeekNum() {
            return weekNum;
        }

        public List<LocalDate> getDates() {
            return dates;
        }

        @Override
        public String toString() {
            Function<LocalDate, String> dateToString = localDate -> localDate.format(DateTimeFormatter.ofPattern("dd.MM"));
            dates = dates.stream().sorted().toList();

            if (year == LocalDate.now().getYear()) {
                return String.format("week %s (%s-%s)", weekNum, dateToString.apply(dates.getFirst()), dateToString.apply(dates.getLast()));
            }
            return String.format("%s week %s (%s-%s)", year, weekNum, dateToString.apply(dates.getFirst()), dateToString.apply(dates.getLast()));
        }

        @Override
        public int compareTo(WeekSelectorItem o) {
            if(year != o.year) {
                return Integer.compare(year, o.year);
            }
            return Integer.compare(weekNum, o.weekNum);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;

            WeekSelectorItem that = (WeekSelectorItem) o;
            return year == that.year && weekNum == that.weekNum && dates.equals(that.dates);
        }

        @Override
        public int hashCode() {
            int result = year;
            result = 31 * result + weekNum;
            result = 31 * result + dates.hashCode();
            return result;
        }

        record YearWeekNum(int year, int weekNum) {}
    }

    @FXML
    void onDateBackwardClick(ActionEvent event) {
        int selectedIndex = weekSelector.getSelectionModel().getSelectedIndex();
        if(selectedIndex >= weekSelector.getItems().size()) {
            log.info("Last element selected, can't go backward");
        } else {
            weekSelector.getSelectionModel().select(selectedIndex + 1);
        }
    }

    @FXML
    void onDateForwardClick(ActionEvent event) {
        int selectedIndex = weekSelector.getSelectionModel().getSelectedIndex();
        if(selectedIndex <= 0) {
            log.info("Current week selected, can't go forward");
        } else {
            weekSelector.getSelectionModel().select(selectedIndex - 1);
        }
    }

    @FXML
    void onDateNowClick(ActionEvent event) {
        weekSelector.getSelectionModel().select(0);
    }


}
