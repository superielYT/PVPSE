package com.codex.pvphud;

/** Render-only night vision strength. Does not create or remove potion effects. */
public final class FullbrightState {
    private static volatile float strength;
    private FullbrightState() {}
    public static float strength() { return strength; }
    public static void set(double value) { strength = (float) Math.clamp(value, 0, 1); }
}
