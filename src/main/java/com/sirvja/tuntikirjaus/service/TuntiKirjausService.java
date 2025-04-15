package com.sirvja.tuntikirjaus.service;

import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;
import com.sirvja.tuntikirjaus.utils.Dao;
import com.sirvja.tuntikirjaus.utils.TuntiKirjausDao;

import java.time.LocalDate;
import java.util.List;
import java.util.function.BiFunction;

public class TuntiKirjausService {
    private final Dao<TuntiKirjaus> tuntikirjausDao;
    private List<TuntiKirjaus> tuntikirjausCache;

    TuntiKirjausService() {
        tuntikirjausDao = new TuntiKirjausDao();
    }

    TuntiKirjausService(Dao<TuntiKirjaus> tuntikirjausDao) {
        this.tuntikirjausDao = tuntikirjausDao;
    }

    public List<TuntiKirjaus> getTuntiKirjausForDate(LocalDate localDate){
        BiFunction<TuntiKirjaus, LocalDate, Boolean> tuntiKirjausHasStartDate =
                (tuntikirjaus, dateToCompare) -> tuntikirjaus.getStartTime().toLocalDate().equals(dateToCompare);

        return getAllTuntikirjaus().stream()
                .filter(tuntikirjaus -> tuntiKirjausHasStartDate.apply(tuntikirjaus, localDate))
                .toList();
    }

    private List<TuntiKirjaus> getAllTuntikirjaus() {
        if(tuntikirjausCache==null) {
            tuntikirjausCache = tuntikirjausDao.getAllToList();
        }

        return tuntikirjausCache;
    }

    public void clearTuntikirjausCache() {
        tuntikirjausCache = null;
    }
}
