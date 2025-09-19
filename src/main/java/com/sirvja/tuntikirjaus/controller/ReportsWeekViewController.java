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

import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ReportsWeekViewController implements Initializable {

    private final static int NUM_OF_WEEKS = 15;

    @FXML
    private TableView<WeeklyProjectHours> weekTable;
    @FXML
    private TableColumn<WeeklyProjectHours, String> projectColumn;
    @FXML
    private TableColumn<WeeklyProjectHours, String> mondayColumn;
    @FXML
    private TableColumn<WeeklyProjectHours, String> tuesdayColumn;
    @FXML
    private TableColumn<WeeklyProjectHours, String> wednesdayColumn;
    @FXML
    private TableColumn<WeeklyProjectHours, String> thursdayColumn;
    @FXML
    private TableColumn<WeeklyProjectHours, String> fridayColumn;

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

    private List<LocalDate> weekList;
    private final WeekFields weekFields = WeekFields.of(Locale.getDefault());

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initTable();
        initWeekSelector();
    }

    private void initTable() {
        projectColumn.setCellValueFactory(new PropertyValueFactory<>("projectName"));
        mondayColumn.setCellValueFactory(new PropertyValueFactory<>("moHours"));
        tuesdayColumn.setCellValueFactory(new PropertyValueFactory<>("tuHours"));
        wednesdayColumn.setCellValueFactory(new PropertyValueFactory<>("weHours"));
        thursdayColumn.setCellValueFactory(new PropertyValueFactory<>("thHours"));
        fridayColumn.setCellValueFactory(new PropertyValueFactory<>("frHours"));
    }

    private void initWeekSelector() {
        weekList = IntStream.iterate(0, i -> i+1)
                .mapToObj(i -> LocalDate.now().minusDays(i))
                .limit(NUM_OF_WEEKS * 7)
                .toList();

        SortedMap<Integer, List<LocalDate>> weekNumToDate = groupLocalDateBasedOnWeekNumber(weekList);

        List<WeekSelectorItem> weekAndFirstAndLastDate = createWeekOptionsList(weekNumToDate);

        weekSelector.setItems(FXCollections.observableArrayList(weekAndFirstAndLastDate));

        weekSelector.getSelectionModel().selectedItemProperty().addListener(((observable, oldValue, newValue) -> {
            ObservableList<TuntiKirjaus> tuntiKirjausList = ReportsViewService.getAllTuntikirjaus(Optional.empty(), Optional.empty(), Optional.empty());
            ObservableList<WeeklyProjectHours> weeklyProjectHours = mapTuntikirjausListToWeeklyProjectHours(tuntiKirjausList, observable.getValue());
            weekTable.setItems(weeklyProjectHours);
        }));
    }

    private ObservableList<WeeklyProjectHours> mapTuntikirjausListToWeeklyProjectHours(ObservableList<TuntiKirjaus> tuntiKirjausList, WeekSelectorItem value) {
        Map<String, List<TuntiKirjaus>> projectToTuntikirjausList = tuntiKirjausList.stream()
                .filter(tuntiKirjaus -> tuntiKirjaus.getEndTime().isPresent())
                .filter(tuntiKirjaus -> tuntiKirjaus.getEndTime().map(tk -> tk.get(ChronoField.ALIGNED_WEEK_OF_YEAR) == value.weekNum).orElse(false))
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

    private List<WeekSelectorItem> createWeekOptionsList(SortedMap<Integer, List<LocalDate>> weekNumToDate) {
        return weekNumToDate.entrySet().stream()
                .map(entry -> new WeekSelectorItem(entry.getKey(), entry.getValue()))
                .toList();
    }

    private SortedMap<Integer, List<LocalDate>> groupLocalDateBasedOnWeekNumber(List<LocalDate> dateList) {
        return dateList.stream()
                .collect(Collectors.groupingBy(
                        localDate -> localDate.get(weekFields.weekOfWeekBasedYear()),
                        () -> new TreeMap<Integer, List<LocalDate>>(Comparator.reverseOrder()),
                        Collectors.toList()
                ));
    }

    public static class WeeklyProjectHours implements Comparable<WeeklyProjectHours> {
        String projectName;
        List<TuntiKirjaus> moHours;
        List<TuntiKirjaus> tuHours;
        List<TuntiKirjaus> weHours;
        List<TuntiKirjaus> thHours;
        List<TuntiKirjaus> frHours;

        public WeeklyProjectHours(String projectName, List<TuntiKirjaus> tuntiKirjausList) {
            this.projectName = projectName;

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

        public String getProjectName() {
            return projectName;
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
            return projectName.compareTo(o.projectName);
        }
    }

    public static class WeekSelectorItem implements Comparable<WeekSelectorItem>{
        int weekNum;
        List<LocalDate> dates;

        public WeekSelectorItem(int weekNum, List<LocalDate> dates) {
            this.weekNum = weekNum;
            this.dates = dates;
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

            return String.format("week %s (%s-%s)", weekNum, dateToString.apply(dates.getFirst()), dateToString.apply(dates.getLast()));
        }

        @Override
        public int compareTo(WeekSelectorItem o) {
            return Integer.compare(o.weekNum, weekNum);
        }
    }

    @FXML
    void onDateBackwardClick(ActionEvent event) {
        LocalDate.now().get(ChronoField.ALIGNED_WEEK_OF_YEAR);
    }

    @FXML
    void onDateForwardClick(ActionEvent event) {

    }

    @FXML
    void onDateNowClick(ActionEvent event) {

    }

    @FXML
    void onNaytaPaivakohtainenYhteenveto(ActionEvent event) {

    }


}
