package com.sirvja.tuntikirjaus.service;

import com.sirvja.tuntikirjaus.domain.Incident;
import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;
import com.sirvja.tuntikirjaus.domain.TuntikirjausIncident;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class IncidentService {
    private static final String LUNCH_TOPIC = "lounas";
    private static final String BREAK_TOPIC = "tauko";


    public List<TuntikirjausIncident> parseTuntikirjausIncidents(List<TuntiKirjaus> tuntiKirjausList) {
        List<TuntikirjausIncident> tuntikirjausIncidents = new ArrayList<>();

        tuntikirjausIncidents.addAll(parseStartTimeIncidents(tuntiKirjausList));
        tuntikirjausIncidents.addAll(parseEndTimeIncidents(tuntiKirjausList));
        tuntikirjausIncidents.addAll(parseLunchIncidents(tuntiKirjausList));
        tuntikirjausIncidents.addAll(parseBreakIncidents(tuntiKirjausList));

        tuntikirjausIncidents.sort(Comparator.comparing(TuntikirjausIncident::localDateTime));

        return tuntikirjausIncidents;
    }

    protected Collection<TuntikirjausIncident> parseLunchIncidents(Collection<TuntiKirjaus> tuntiKirjausList) {
        return tuntiKirjausList.stream()
                .filter(tk -> tk.getTopic().toLowerCase().startsWith(LUNCH_TOPIC))
                .<TuntikirjausIncident>mapMulti((tk, consumer) -> {
                    consumer.accept(new TuntikirjausIncident(tk.getStartTime(), Incident.START_OF_LUNCH));
                    tk.getEndTime().ifPresent(endTime -> consumer.accept(new TuntikirjausIncident(endTime, Incident.END_OF_LUNCH)));
                }).toList();
    }

    protected Collection<TuntikirjausIncident> parseBreakIncidents(Collection<TuntiKirjaus> tuntiKirjausList) {
        return tuntiKirjausList.stream()
                .filter(tk -> tk.getTopic().toLowerCase().startsWith(BREAK_TOPIC))
                .<TuntikirjausIncident>mapMulti((tk, consumer) -> {
                    consumer.accept(new TuntikirjausIncident(tk.getStartTime(), Incident.START_OF_BREAK));
                    tk.getEndTime().ifPresent(endTime -> consumer.accept(new TuntikirjausIncident(endTime, Incident.END_OF_BREAK)));
                }).toList();
    }

    protected Collection<TuntikirjausIncident> parseStartTimeIncidents(Collection<TuntiKirjaus> tkCollection) {
        Function<List<TuntiKirjaus>, TuntikirjausIncident> findStartOfDayIncidentFromList =
                tkList -> tkList.stream()
                        .min(Comparator.naturalOrder())
                        .map(TuntiKirjaus::getStartTime)
                        .map(ldt -> new TuntikirjausIncident(ldt, Incident.START_OF_DAY))
                        .orElse(null);

        return parseIncidents(tkCollection, findStartOfDayIncidentFromList);
    }

    protected Collection<TuntikirjausIncident> parseEndTimeIncidents(Collection<TuntiKirjaus> tkCollection) {
        Function<List<TuntiKirjaus>, TuntikirjausIncident> findEndOfDayIncidentFromList =
                tkList -> tkList.stream()
                .max(Comparator.naturalOrder())
                .flatMap(TuntiKirjaus::getEndTime)
                .map(ldt -> new TuntikirjausIncident(ldt, Incident.END_OF_DAY))
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
