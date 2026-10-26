package com.codex.pvphud.render;

import com.codex.pvphud.PerformanceMode;

/** Cosmetic budgets do not change HUD visibility, themes or combat information. */
public final class RenderBudget {
    private static volatile PerformanceMode mode = PerformanceMode.OFF;
    private static volatile boolean lightHud = true;
    private RenderBudget() {}
    public static void configure(PerformanceMode next, boolean light) { mode = next; lightHud = light; }
    public static boolean lightweight() { return lightHud && mode != PerformanceMode.OFF; }
    public static boolean maximum() { return mode == PerformanceMode.MAX; }
    public static int particles(int requested) {
        return mode == PerformanceMode.MAX ? 0 : Math.min(mode == PerformanceMode.BALANCED ? 8 : 48, Math.max(0, requested));
    }
}
