package com.sirvja.tuntikirjaus.commandPattern.invoker;

import com.sirvja.tuntikirjaus.commandPattern.command.TuntiKirjausOperation;

import java.util.*;

public class TuntiKirjausOperationExecutor {
    private static TuntiKirjausOperationExecutor instance = null;
    private QueueStack<List<TuntiKirjausOperation>> queueStack;
    private QueueStack<List<TuntiKirjausOperation>> queueStackReverse;
    private List<String> actionHistory;

    public static TuntiKirjausOperationExecutor getInstance(){
        if(instance != null) {
            return instance;
        }
        return new TuntiKirjausOperationExecutor();
    }

    private TuntiKirjausOperationExecutor() {
        queueStack = new QueueStack<>();
        queueStackReverse = new QueueStack<>();
        actionHistory = new ArrayList<>();
    }

    public void executeOperations(List<TuntiKirjausOperation> operationList){
        operationList.forEach(TuntiKirjausOperation::execute);
        queueStack.push(operationList);
        operationList.forEach(o -> actionHistory.add(o.getName()));
    }

    public void undo() {
        Optional<List<TuntiKirjausOperation>> optionalOperations = queueStack.pop();
        optionalOperations.ifPresent(oList -> {
            oList.forEach(TuntiKirjausOperation::undo);
            queueStackReverse.push(oList);
            oList.forEach(o -> actionHistory.add(o.getName() + " - undo"));
        });
    }

    public void redo() {
        Optional<List<TuntiKirjausOperation>> optionalOperations = queueStackReverse.pop();
        optionalOperations.ifPresent(oList -> {
            oList.forEach(TuntiKirjausOperation::execute);
            queueStack.push(oList);
            oList.forEach(o -> actionHistory.add(o.getName() + " - redo"));
        });
    }

    public boolean canUndo() {
        return !queueStack.isEmpty();
    }
    public boolean canRedo() {
        return !queueStackReverse.isEmpty();
    }

    public void clear() {
        queueStack.clear();
    }

    void clearReverse() {
        queueStackReverse.clear();
    }

    List<String> getActionHistory() {
        return actionHistory;
    }
}
