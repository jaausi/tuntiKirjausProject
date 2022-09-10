package com.sirvja.tuntikirjaus.utils;

import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.util.Optional;

public interface Dao<T> {

    Optional<T> get(int id);

    Optional<ObservableList<T>> getAll();

    Optional<ObservableList<T>> getAllFrom(LocalDate localDate);

    T save(T t);

    void update(T t);

    void delete(T t);
}
