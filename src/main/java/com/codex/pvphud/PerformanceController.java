package com.codex.pvphud;

import com.codex.pvphud.render.RenderBudget;
import java.util.Objects;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;

/** Explicit, persistent presets. Restore only fields still equal to the value we applied. */
final class PerformanceController {
    static final class Snapshot {
        int view, simulation;
        boolean shadows;
        CloudStatus clouds;
        ParticleStatus particles;
        static Snapshot capture(Minecraft mc) {
            var s = new Snapshot();
            s.view = mc.options.renderDistance().get(); s.simulation = mc.options.simulationDistance().get();
            s.shadows = mc.options.entityShadows().get(); s.clouds = mc.options.cloudStatus().get();
            s.particles = mc.options.particles().get(); return s;
        }
    }

    private final HudConfig config;
    private boolean initialized;
    PerformanceController(HudConfig config) { this.config = config; }

    void initialize(Minecraft mc) {
        if (initialized) return;
        initialized = true;
        if (config.performanceMode != PerformanceMode.OFF
                && (config.performanceBefore == null || config.performanceApplied == null)) {
            apply(mc, config.performanceMode);
        }
        refreshBudget();
    }

    void setMode(Minecraft mc, PerformanceMode mode) {
        initialize(mc);
        if (mode == config.performanceMode) return;
        restoreManaged(mc);
        config.performanceMode = mode;
        if (mode != PerformanceMode.OFF) apply(mc, mode);
        refreshBudget(); mc.options.save(); config.save();
    }

    private void apply(Minecraft mc, PerformanceMode mode) {
        config.performanceBefore = Snapshot.capture(mc);
        mc.options.renderDistance().set(Math.min(config.performanceBefore.view, mode.viewCap));
        mc.options.simulationDistance().set(Math.min(config.performanceBefore.simulation, mode.simulationCap));
        mc.options.entityShadows().set(false);
        mc.options.cloudStatus().set(CloudStatus.OFF);
        mc.options.particles().set(mode == PerformanceMode.MAX ? ParticleStatus.MINIMAL
                : config.performanceBefore.particles == ParticleStatus.ALL ? ParticleStatus.DECREASED : config.performanceBefore.particles);
        config.performanceApplied = Snapshot.capture(mc);
        mc.options.save(); config.save();
    }

    private void restoreManaged(Minecraft mc) {
        var before = config.performanceBefore; var applied = config.performanceApplied;
        if (before != null && applied != null) {
            if (Objects.equals(mc.options.renderDistance().get(), applied.view)) mc.options.renderDistance().set(before.view);
            if (Objects.equals(mc.options.simulationDistance().get(), applied.simulation)) mc.options.simulationDistance().set(before.simulation);
            if (Objects.equals(mc.options.entityShadows().get(), applied.shadows)) mc.options.entityShadows().set(before.shadows);
            if (before.clouds != null && mc.options.cloudStatus().get() == applied.clouds) mc.options.cloudStatus().set(before.clouds);
            if (before.particles != null && mc.options.particles().get() == applied.particles) mc.options.particles().set(before.particles);
        }
        config.performanceBefore = null; config.performanceApplied = null;
    }

    void refreshBudget() { RenderBudget.configure(config.performanceMode, config.performanceLightHud); }
}
