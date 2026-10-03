package com.sirvja.tuntikirjaus.dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface Dao<T, I> {

    Optional<T> get(I id);

    List<T> getAllToList();
    List<T> getAllFromToList(LocalDate localDate);

    T save(T t);

    void update(T t);

    void delete(T t);
}
