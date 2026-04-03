package com.sirvja.tuntikirjaus.service;

import com.sirvja.tuntikirjaus.domain.Incident;
import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;
import com.sirvja.tuntikirjaus.domain.TuntikirjausIncident;
import com.sirvja.tuntikirjaus.domain.WeeklyIncidents;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class IncidentService {
    private static final String LUNCH_TOPIC = "lounas";
    private static final String BREAK_TOPIC = "tauko";

    public Map<Incident, List<TuntikirjausIncident>> groupIncidentsByType(List<TuntikirjausIncident> incidents) {
        return incidents.stream()
                .collect(Collectors.groupingBy(TuntikirjausIncident::incident));
    }

    public List<TuntikirjausIncident> parseTuntikirjausIncidents(List<TuntiKirjaus> tuntiKirjausList) {
        List<TuntikirjausIncident> tuntikirjausIncidents = new ArrayList<>();

        tuntikirjausIncidents.addAll(parseStartTimeIncidents(tuntiKirjausList));
        tuntikirjausIncidents.addAll(parseEndTimeIncidents(tuntiKirjausList));
        tuntikirjausIncidents.addAll(parseLunchIncidents(tuntiKirjausList));
        tuntikirjausIncidents.addAll(parseBreakIncidents(tuntiKirjausList));

        tuntikirjausIncidents.sort(Comparator.comparing(TuntikirjausIncident::time));

        return tuntikirjausIncidents;
    }

    protected Collection<TuntikirjausIncident> parseLunchIncidents(Collection<TuntiKirjaus> tuntiKirjausList) {
        return tuntiKirjausList.stream()
                .filter(tk -> tk.getTopic().toLowerCase().startsWith(LUNCH_TOPIC))
                .<TuntikirjausIncident>mapMulti((tk, consumer) -> {
                    Incident startIncident = tk.isRemote() ? Incident.START_OF_LUNCH_REMOTE : Incident.START_OF_LUNCH;
                    Incident endIncident = tk.isRemote() ? Incident.END_OF_LUNCH_REMOTE : Incident.END_OF_LUNCH;
                    consumer.accept(new TuntikirjausIncident(tk.getStartTime(), startIncident));
                    tk.getEndTime().ifPresent(endTime -> consumer.accept(new TuntikirjausIncident(endTime, endIncident)));
                }).toList();
    }

    protected Collection<TuntikirjausIncident> parseBreakIncidents(Collection<TuntiKirjaus> tuntiKirjausList) {
        return tuntiKirjausList.stream()
                .filter(tk -> tk.getTopic().toLowerCase().startsWith(BREAK_TOPIC))
                .<TuntikirjausIncident>mapMulti((tk, consumer) -> {
                    Incident startIncident = tk.isRemote() ? Incident.START_OF_BREAK_REMOTE : Incident.START_OF_BREAK;
                    Incident endIncident = tk.isRemote() ? Incident.END_OF_BREAK_REMOTE : Incident.END_OF_BREAK;
                    consumer.accept(new TuntikirjausIncident(tk.getStartTime(), startIncident));
                    tk.getEndTime().ifPresent(endTime -> consumer.accept(new TuntikirjausIncident(endTime, endIncident)));
                }).toList();
    }

    protected Collection<TuntikirjausIncident> parseStartTimeIncidents(Collection<TuntiKirjaus> tkCollection) {
        Function<List<TuntiKirjaus>, TuntikirjausIncident> findStartOfDayIncidentFromList =
                tkList -> tkList.stream()
                        .min(Comparator.naturalOrder())
                        .map(tk -> new TuntikirjausIncident(tk.getStartTime(), tk.isRemote() ? Incident.START_OF_DAY_REMOTE : Incident.START_OF_DAY))
                        .orElse(null);

        return parseIncidents(tkCollection, findStartOfDayIncidentFromList);
    }

    protected Collection<TuntikirjausIncident> parseEndTimeIncidents(Collection<TuntiKirjaus> tkCollection) {
        Function<List<TuntiKirjaus>, TuntikirjausIncident> findEndOfDayIncidentFromList =
                tkList -> tkList.stream()
                .max(Comparator.naturalOrder())
                .flatMap(tk -> tk.getEndTime().map(ldt -> new TuntikirjausIncident(ldt, tk.isRemote() ? Incident.END_OF_DAY_REMOTE : Incident.END_OF_DAY)))
                .orElse(null);

        return parseIncidents(tkCollection, findEndOfDayIncidentFromList);
    }

    private Collection<TuntikirjausIncident> parseIncidents(
            Collection<TuntiKirjaus> tkCollection,
            Function<List<TuntiKirjaus>, TuntikirjausIncident> findIncidentFromList
    ) {
        return tkCollection.stream()
                .filter(tk -> tk.getEndTime().isPresent())
                .collect(Collectors.groupingBy(
                        tk -> tk.getStartTime().toLocalDate(),
                        HashMap::new,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                findIncidentFromList
                        )
                )).values();
    }
}
