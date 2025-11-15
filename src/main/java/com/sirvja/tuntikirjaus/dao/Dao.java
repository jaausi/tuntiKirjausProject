package com.sirvja.tuntikirjaus.dao;

import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface Dao<T, I> {

    Optional<T> get(I id);

    @Deprecated(since = "1.0.2")
    // Use getAllToList instead
    default Optional<ObservableList<T>> getAll() {
        throw new RuntimeException("Used method is deprecated, use getAllToList() instead");
    }

    List<T> getAllToList();
    List<T> getAllFromToList(LocalDate localDate);

    T save(T t);

    void update(T t);

    void delete(T t);
}
