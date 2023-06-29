package com.sirvja.tuntikirjaus.commandPattern.command;

import com.sirvja.tuntikirjaus.commandPattern.receiver.TuntiKirjausList;
import com.sirvja.tuntikirjaus.domain.TuntiKirjaus;

public class EditOperation implements TuntiKirjausOperation{
    private String name;
    private TuntiKirjausList tuntiKirjausList;
    private TuntiKirjaus tuntiKirjaus;
    private TuntiKirjaus originalTuntikirjaus;

    public EditOperation(TuntiKirjaus tuntiKirjaus) {
        this.tuntiKirjausList = TuntiKirjausList.getInstance();
        this.tuntiKirjaus = tuntiKirjaus;
        this.originalTuntikirjaus = null;
        this.name = "Edit tuntikirjaus";
    }

    @Override
    public void execute() {
        this.originalTuntikirjaus = tuntiKirjausList.getItem(tuntiKirjaus.getId())
                .orElseThrow(() -> new RuntimeException("Couldn't load original tuntikirjaus"));
        tuntiKirjausList.editItem(tuntiKirjaus);
    }

    @Override
    public void undo() {
        if(originalTuntikirjaus != null) {
            tuntiKirjausList.editItem(originalTuntikirjaus);
            this.originalTuntikirjaus = null;
        } else {
            throw new RuntimeException("Not able to undo edit operation, because original object not saved.");
        }
    }

    @Override
    public String getName() {
        return name;
    }
}
