package com.sirvja.tuntikirjaus.controller;

import com.sirvja.tuntikirjaus.domain.*;
import com.sirvja.tuntikirjaus.service.IncidentService;
import com.sirvja.tuntikirjaus.service.ReportsViewService;
import com.sirvja.tuntikirjaus.service.WeeklyViewService;
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
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
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

    private List<TuntiKirjaus> tuntiKirjausListAll;
    private final IncidentService incidentService;
    private final WeeklyViewService weeklyViewService;

    public ReportsWeekViewController() {
        this.incidentService = new IncidentService();
        this.weeklyViewService = new WeeklyViewService(incidentService);
    }

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
                    .filter(tuntiKirjaus -> tuntiKirjaus.getStartTime().getYear() == observable.getValue().getYear())
                    .filter(tuntiKirjaus -> tuntiKirjaus.getStartTime().get(WeekFields.ISO.weekOfWeekBasedYear()) == observable.getValue().getWeekNum())
                    .toList();

            ObservableList<WeeklyProjectHours> weeklyProjectHours = weeklyViewService.mapTuntikirjausListToWeeklyProjectHours(tuntiKirjausListForWeek);
            ObservableList<WeeklyIncidents> weeklyIncidents = weeklyViewService.parseWeeklyIncidents(tuntiKirjausListForWeek);
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
                        localDate -> new WeekSelectorItem.YearWeekNum(localDate.getYear(), localDate.get(WeekFields.ISO.weekOfWeekBasedYear())),
                        HashMap::new,
                        Collectors.toList()
                ));

        return yearAndWeekToDateList.entrySet().stream()
                .map(entry -> new WeekSelectorItem(entry.getKey().year(), entry.getKey().weekNum(), entry.getValue()))
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
