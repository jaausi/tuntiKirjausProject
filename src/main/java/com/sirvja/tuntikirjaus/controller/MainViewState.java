package com.sirvja.tuntikirjaus.controller;

import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;

import java.time.LocalDate;
import java.util.List;

public class MainViewState {
    private static final Integer DEFAULT_DAYS_TO_FETCH = 35;
    private static LocalDate currentDate;
    private static List<TuntiKirjaus> tuntikirjausList;

    public static LocalDate getCurrentDate() {
        return currentDate;
    }

    public static void setCurrentDate(LocalDate currentDate) {
        MainViewState.currentDate = currentDate;
    }

    public static List<TuntiKirjaus> getTuntikirjausList() {
        return tuntikirjausList;
    }

    public static void setTuntikirjausList(List<TuntiKirjaus> tuntikirjausList) {
        MainViewState.tuntikirjausList = tuntikirjausList;
    }
}
