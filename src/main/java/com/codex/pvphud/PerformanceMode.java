package com.codex.pvphud;

public enum PerformanceMode {
    OFF("OFF", 32, 32, 1.0, 5), BALANCED("Balanced", 12, 8, 0.75, 2), MAX("Max FPS", 6, 5, 0.5, 0);
    public final String label;
    public final int viewCap, simulationCap, biomeBlend;
    public final double entityDistance;
    PerformanceMode(String label, int viewCap, int simulationCap, double entityDistance, int biomeBlend) {
        this.label = label; this.viewCap = viewCap; this.simulationCap = simulationCap;
        this.entityDistance = entityDistance; this.biomeBlend = biomeBlend;
    }
    public PerformanceMode next() { return values()[(ordinal() + 1) % values().length]; }
}
