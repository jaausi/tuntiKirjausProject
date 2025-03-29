package com.sirvja.tuntikirjaus.model;

import java.time.LocalDate;

public class DayRecord implements Comparable<DayRecord> {
    private LocalDate localDate;

    public DayRecord() {
        this.localDate = LocalDate.now();
    }

    public DayRecord(LocalDate localDate) {
        this.localDate = localDate;
    }

    public LocalDate getLocalDate() {
        return localDate;
    }

    public void setLocalDate(LocalDate localDate) {
        this.localDate = localDate;
    }

    public String toString(){
        return this.localDate.toString();
    }

    @Override
    public int compareTo(DayRecord dayRecord) {
        return dayRecord.localDate.compareTo(this.localDate);
    }
}
