package com.sirvja.tuntikirjaus.domain;

import com.sirvja.tuntikirjaus.controller.ReportsWeekViewController;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public class WeeklyIncidents implements Comparable<WeeklyIncidents> {
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
