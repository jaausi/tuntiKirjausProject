package com.sirvja.tuntikirjaus.commandPattern.receiver;

import com.sirvja.tuntikirjaus.commandPattern.invoker.TuntiKirjausOperationExecutor;
import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;
import com.sirvja.tuntikirjaus.utils.Dao;
import com.sirvja.tuntikirjaus.utils.TuntiKirjausDao;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public class TuntiKirjausList {
    private static TuntiKirjausList instance = null;
    private final ObservableList<TuntiKirjaus> tuntiKirjausObservableList;
    private final Dao<TuntiKirjaus> tuntiKirjausDao;

    private TuntiKirjausList() {
        this.tuntiKirjausObservableList = FXCollections.observableArrayList();
        this.tuntiKirjausDao = new TuntiKirjausDao();
        tuntiKirjausObservableList.setAll(tuntiKirjausDao.getAll());
    }

    public static TuntiKirjausList getInstance() {
        if(instance != null) {
            return instance;
        }
        return new TuntiKirjausList();
    }

    public Optional<TuntiKirjaus> getItem(final int id) {
        return tuntiKirjausObservableList.stream()
                .filter(tuntiKirjaus -> id == tuntiKirjaus.getId())
                .findAny();
    }

    public ObservableList<TuntiKirjaus> getAll() {
        return tuntiKirjausObservableList;
    }

    public boolean addItem(TuntiKirjaus tuntiKirjaus){
        tuntiKirjausDao.save(tuntiKirjaus);
        return tuntiKirjausObservableList.add(tuntiKirjaus);
    }

    public boolean removeItem(TuntiKirjaus tuntiKirjaus) {
        tuntiKirjausDao.delete(tuntiKirjaus);
        return tuntiKirjausObservableList.remove(tuntiKirjaus);
    }

    public boolean editItem(TuntiKirjaus tuntiKirjaus) {
        if (removeItem(tuntiKirjaus)) {
            return addItem(tuntiKirjaus);
        } else {
            throw new RuntimeException(String.format("No item with id %s found", tuntiKirjaus.getId()));
        }
    }
}
