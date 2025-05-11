package com.sirvja.tuntikirjaus.domain;

import java.time.LocalDate;
import java.util.Objects;

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
        return paiva.localDate.compareTo(this.localDate);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Paiva paiva = (Paiva) o;
        return Objects.equals(localDate, paiva.localDate);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(localDate);
    }
}
