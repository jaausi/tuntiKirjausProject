package com.sirvja.tuntikirjaus.commandPattern.command;

import com.sirvja.tuntikirjaus.commandPattern.receiver.TuntiKirjausList;
import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;

public class AddOperation implements TuntiKirjausOperation{
    private String name;
    private TuntiKirjausList tuntiKirjausList;
    private TuntiKirjaus tuntiKirjaus;

    public AddOperation(TuntiKirjaus tuntiKirjaus) {
        this.tuntiKirjausList = TuntiKirjausList.getInstance();
        this.tuntiKirjaus = tuntiKirjaus;
        this.name = "Add tuntikirjaus";
    }

    @Override
    public void execute() {
        tuntiKirjausList.addItem(tuntiKirjaus);
    }

    @Override
    public void undo() {
        tuntiKirjausList.removeItem(tuntiKirjaus);
    }

    @Override
    public String getName() {
        return name;
    }
}
