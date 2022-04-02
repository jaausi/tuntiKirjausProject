package com.sirvja.tuntikirjaus.utils;

import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface Dao<T> {

    Optional<T> get(String[] params);

    List<T> getAll();

    void save(T t);

    void update(T t, String[] params);

    void delete(T t);
}
