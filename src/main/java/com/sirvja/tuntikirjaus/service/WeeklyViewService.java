package com.sirvja.tuntikirjaus.service;

import com.sirvja.tuntikirjaus.domain.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class WeeklyViewService {
    private final IncidentService incidentService;

    public WeeklyViewService(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    /**
     * Parses the given list of TuntiKirjaus entries into a list of WeeklyIncidents, which contains the incidents for each day of the week.
     *
     * @param tuntiKirjausListForWeek a list of TuntiKirjaus entries for a single week, must not be empty and must contain dates for only one week
     * @return
     */
    public ObservableList<WeeklyIncidents> parseWeeklyIncidents(List<TuntiKirjaus> tuntiKirjausListForWeek) {

        // Check that list is not empty and contains dates for only one week, otherwise throw exception
        checkHasItemsFromOneWeek(tuntiKirjausListForWeek);

        List<TuntikirjausIncident> incidentsForWeek = incidentService.parseTuntikirjausIncidents(tuntiKirjausListForWeek);

        Collector<TuntikirjausIncident, ?, Map<Incident, List<LocalDateTime>>> groupByIncident = Collectors.groupingBy(
                TuntikirjausIncident::getIncident,
                HashMap::new,
                Collectors.mapping(TuntikirjausIncident::getTime, Collectors.toList())
        );

        Function<Map<Incident, List<LocalDateTime>>, ObservableList<WeeklyIncidents>> mapToWeeklyIncidents = map -> map.entrySet().stream()
                .map(entry -> new WeeklyIncidents(entry.getKey(), entry.getValue()))
                .sorted()
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        return incidentsForWeek.stream()
                .collect(Collectors.collectingAndThen(
                        groupByIncident,
                        mapToWeeklyIncidents
                ));
    }

    protected void checkHasItemsFromOneWeek(List<TuntiKirjaus> tuntiKirjausListForWeek) {
        if (tuntiKirjausListForWeek == null || tuntiKirjausListForWeek.isEmpty()) {
            throw new IllegalArgumentException("TuntiKirjaus list must not be empty");
        }

        long distinctWeeks = tuntiKirjausListForWeek.stream()
                .map(tk -> tk.getStartTime().toLocalDate())
                .map(date -> date.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear()) + "-" + date.getYear())
                .distinct()
                .count();

        if (distinctWeeks > 1) {
            throw new IllegalArgumentException("TuntiKirjaus list must contain dates for only one week, but contained " + distinctWeeks + " weeks");
        }
    }

    public ObservableList<WeeklyProjectHours> mapTuntikirjausListToWeeklyProjectHours(List<TuntiKirjaus> tuntiKirjausListForWeek) {
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
}
