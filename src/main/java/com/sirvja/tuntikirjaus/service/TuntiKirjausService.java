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

    private final boolean cacheEnabled = true;

    TuntiKirjausService() {
        tuntikirjausDao = new TuntiKirjausDao();
    }

    TuntiKirjausService(Dao<TuntiKirjaus> tuntikirjausDao) {
        this.tuntikirjausDao = tuntikirjausDao;
    }

    public List<TuntiKirjaus> getTuntiKirjausForDate(LocalDate localDate){
        BiFunction<TuntiKirjaus, LocalDate, Boolean> tuntiKirjausHasStartDate =
                (tuntikirjaus, dateToCompare) -> tuntikirjaus.getStartTime().toLocalDate().equals(dateToCompare);

        return getAllTuntikirjausWithCache().stream()
                .filter(tuntikirjaus -> tuntiKirjausHasStartDate.apply(tuntikirjaus, localDate))
                .toList();
    }

    public List<TuntiKirjaus> getAllTuntikirjausWithCache() {
        if(tuntikirjausCache==null || !cacheEnabled) {
            tuntikirjausCache = tuntikirjausDao.getAllFromToList(Constants.FETCH_DAYS_SINCE);
        }

        return tuntikirjausCache;
    }

    public List<TuntiKirjaus> getAllTuntikirjaus() {
        return tuntikirjausDao.getAllToList();
    }

    public void clearTuntikirjausCache() {
        tuntikirjausCache = null;
    }

    public TuntiKirjaus save(TuntiKirjaus tuntiKirjaus) {
        return tuntikirjausDao.save(tuntiKirjaus);
    }

    public void update(TuntiKirjaus tuntiKirjaus) {
        tuntikirjausDao.update(tuntiKirjaus);
    }

    public void delete(TuntiKirjaus tuntiKirjaus) {
        tuntikirjausDao.delete(tuntiKirjaus);
    }
}
