package com.codex.pvphud;

public enum PerformanceMode {
    OFF("OFF", 32, 32), BALANCED("Balanced", 12, 8), MAX("Max FPS", 8, 5);
    public final String label;
    public final int viewCap, simulationCap;
    PerformanceMode(String label, int viewCap, int simulationCap) {
        this.label = label; this.viewCap = viewCap; this.simulationCap = simulationCap;
    }
    public PerformanceMode next() { return values()[(ordinal() + 1) % values().length]; }
}
