package com.sirvja.tuntikirjaus.commandPattern.command;

import com.sirvja.tuntikirjaus.commandPattern.receiver.TuntiKirjausList;
import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;

public class RemoveOperation implements TuntiKirjausOperation{
    private String name;
    private TuntiKirjausList tuntiKirjausList;
    private TuntiKirjaus tuntiKirjaus;

    public RemoveOperation(TuntiKirjaus tuntiKirjaus) {
        this.tuntiKirjausList = TuntiKirjausList.getInstance();
        this.tuntiKirjaus = tuntiKirjaus;
        this.name = "Remove tuntikirjaus";
    }

    @Override
    public void execute() {
        tuntiKirjausList.removeItem(tuntiKirjaus);
    }

    @Override
    public void undo() {
        tuntiKirjausList.addItem(tuntiKirjaus);
    }

    @Override
    public String getName() {
        return name;
    }
}
