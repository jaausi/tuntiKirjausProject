package com.sirvja.tuntikirjaus.domain;

import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Paiva {
    private LocalDate paivamaara;
    private List<TuntiKirjaus> kirjaukset;

    public Paiva() {
        this.paivamaara = LocalDate.now();
        this.kirjaukset = new ArrayList<>();
    }

    public Paiva(LocalDate date) {
        this.paivamaara = date;
        this.kirjaukset = new ArrayList<>();
    }

    public void lisaaKirjaus(TuntiKirjaus kirjaus){
        kirjaukset.add(kirjaus);
    }

    public LocalDate getPaivamaara() {
        return paivamaara;
    }

    public List<TuntiKirjaus> getKirjaukset() {
        return kirjaukset;
    }

    public void setPaivamaara(LocalDate paivamaara) {
        this.paivamaara = paivamaara;
    }

    public void setKirjaukset(List<TuntiKirjaus> kirjaukset) {
        this.kirjaukset = kirjaukset;
    }

    public String toString(){
        return paivamaara.toString();
    }
}
