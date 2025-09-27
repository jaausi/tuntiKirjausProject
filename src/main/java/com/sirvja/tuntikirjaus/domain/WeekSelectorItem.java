package com.sirvja.tuntikirjaus.domain;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

public class WeekSelectorItem implements Comparable<WeekSelectorItem> {
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

    public record YearWeekNum(int year, int weekNum) {}
}