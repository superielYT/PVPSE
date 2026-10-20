package com.codex.pvphud;

/** Rate-limits persistent danger and re-arms when armor recovers. */
public final class ArmorAlertGate {
    private boolean wasLow;
    private long nextTick;
    public boolean update(boolean low, long tick) {
        boolean play = low && (!wasLow || tick >= nextTick);
        wasLow = low;
        if (play) nextTick = tick + 200;
        return play;
    }
    public void reset() { wasLow = false; nextTick = 0; }
}
