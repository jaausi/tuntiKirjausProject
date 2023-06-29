package com.sirvja.tuntikirjaus.commandPattern.command;

public interface TuntiKirjausOperation {
    void execute();
    void undo();
    String getName();
}
