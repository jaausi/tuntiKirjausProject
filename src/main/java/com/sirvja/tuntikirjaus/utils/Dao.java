package com.sirvja.tuntikirjaus.utils;

import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface Dao<T> {

    Optional<T> get(int id);

    @Deprecated(since = "1.0.2")
        // Use getAllToList instead
    Optional<ObservableList<T>> getAll();

    @Deprecated(since = "1.0.2")
        // Use getAllFromToList instead
    Optional<ObservableList<T>> getAllFrom(LocalDate localDate);
    List<T> getAllToList();
    List<T> getAllFromToList(LocalDate localDate);

    T save(T t);

    void update(T t);

    void delete(T t);
}
