package com.sirvja.tuntikirjaus.domain;

import java.time.DayOfWeek;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class WeeklyProjectHours implements Comparable<WeeklyProjectHours> {
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
