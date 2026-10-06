package com.sirvja.tuntikirjaus.exporter.impl;

import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Hours of one project on one day for the Kieku work time allocation.
 *
 * @param project  project of the application ({@link TuntiKirjaus#getClassification()})
 * @param date     day
 * @param duration time to allocate
 */
public record KiekuHoursItem(String project, LocalDate date, Duration duration) {

    /**
     * Sums the entries by project and day. Unfinished entries (no end time) are skipped.
     */
    public static List<KiekuHoursItem> fromTuntikirjausList(List<TuntiKirjaus> tuntiKirjausList) {
        record Key(String project, LocalDate date) {}

        Map<Key, Duration> summed = tuntiKirjausList.stream()
                .filter(tk -> tk.getEndTime().isPresent())
                .collect(Collectors.groupingBy(
                        tk -> new Key(tk.getClassification(), tk.getLocalDateOfStartTime()),
                        Collectors.reducing(Duration.ZERO, TuntiKirjaus::getDurationInDuration, Duration::plus)));

        return summed.entrySet().stream()
                .map(e -> new KiekuHoursItem(e.getKey().project(), e.getKey().date(), e.getValue()))
                .sorted(Comparator.comparing(KiekuHoursItem::date).thenComparing(KiekuHoursItem::project))
                .toList();
    }

    /** Time in the format of the Kieku hour fields (HH:mm), e.g. 01:30. */
    public String durationAsKiekuTime() {
        return KiekuHoursExporter.toKiekuTime(duration);
    }
}
