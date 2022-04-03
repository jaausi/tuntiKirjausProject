package com.sirvja.tuntikirjaus.domain;

import java.time.LocalDate;

public class Paiva implements Comparable<Paiva> {
    private LocalDate localDate;

    public Paiva() {
        this.localDate = LocalDate.now();
    }

    public Paiva(LocalDate localDate) {
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
    public int compareTo(Paiva paiva) {
        return this.localDate.compareTo(paiva.localDate);
    }
}
