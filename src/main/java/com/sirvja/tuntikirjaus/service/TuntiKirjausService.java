package com.sirvja.tuntikirjaus.service;

import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;
import com.sirvja.tuntikirjaus.dao.Dao;
import com.sirvja.tuntikirjaus.dao.TuntiKirjausDao;
import com.sirvja.tuntikirjaus.utils.Constants;

import java.time.LocalDate;
import java.util.List;
import java.util.function.BiFunction;

public class TuntiKirjausService {
    private final Dao<TuntiKirjaus> tuntikirjausDao;
    private List<TuntiKirjaus> tuntikirjausCache;

    private boolean cacheEnabled;

    TuntiKirjausService() {
        this.tuntikirjausDao = new TuntiKirjausDao();
        this.cacheEnabled = true;
    }

    TuntiKirjausService(boolean cacheEnabled) {
        this.tuntikirjausDao = new TuntiKirjausDao();
        this.cacheEnabled = cacheEnabled;
    }

    TuntiKirjausService(Dao<TuntiKirjaus> tuntikirjausDao, boolean cacheEnabled) {
        this.tuntikirjausDao = tuntikirjausDao;
        this.cacheEnabled = cacheEnabled;
    }

    public List<TuntiKirjaus> getTuntiKirjausForDate(LocalDate localDate){
        BiFunction<TuntiKirjaus, LocalDate, Boolean> tuntiKirjausHasStartDate =
                (tuntikirjaus, dateToCompare) -> tuntikirjaus.getStartTime().toLocalDate().equals(dateToCompare);

        return getAllTuntikirjaus().stream()
                .filter(tuntikirjaus -> tuntiKirjausHasStartDate.apply(tuntikirjaus, localDate))
                .toList();
    }

    public List<TuntiKirjaus> getAllTuntikirjaus() {
        if(tuntikirjausCache==null || !cacheEnabled) {
            tuntikirjausCache = tuntikirjausDao.getAllFromToList(Constants.FETCH_DAYS_SINCE);
        }

        return tuntikirjausCache;
    }

    public List<TuntiKirjaus> getAllTuntikirjausWithoutLimit() {
        return tuntikirjausDao.getAllToList();
    }

    public void clearTuntikirjausCache() {
        tuntikirjausCache = null;
    }

    public TuntiKirjaus save(TuntiKirjaus tuntiKirjaus) {
        clearTuntikirjausCache();
        return tuntikirjausDao.save(tuntiKirjaus);
    }

    public void update(TuntiKirjaus tuntiKirjaus) {
        clearTuntikirjausCache();
        tuntikirjausDao.update(tuntiKirjaus);
    }

    public void delete(TuntiKirjaus tuntiKirjaus) {
        clearTuntikirjausCache();
        tuntikirjausDao.delete(tuntiKirjaus);
    }
}
