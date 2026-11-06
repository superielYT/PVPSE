package com.codex.pvphud.hud;

import java.util.ArrayDeque;
import java.util.Objects;

/** Bounded transaction history; unchanged edits do not consume a step. */
public final class LayoutHistory<T> {
    private final ArrayDeque<T> undo=new ArrayDeque<>(),redo=new ArrayDeque<>();
    public void record(T before,T after){
        if(Objects.equals(before,after))return;
        undo.addLast(before);while(undo.size()>100)undo.removeFirst();redo.clear();
    }
    public T undo(T current){if(undo.isEmpty())return current;redo.addLast(current);return undo.removeLast();}
    public T redo(T current){if(redo.isEmpty())return current;undo.addLast(current);return redo.removeLast();}
    public boolean canUndo(){return !undo.isEmpty();}
    public boolean canRedo(){return !redo.isEmpty();}
}
