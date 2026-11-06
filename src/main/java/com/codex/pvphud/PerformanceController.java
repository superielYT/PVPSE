package com.codex.pvphud;

import com.codex.pvphud.render.RenderBudget;
import java.util.Objects;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ParticleStatus;

/** Explicit, persistent presets. Restore only fields still equal to the value we applied. */
final class PerformanceController {
    static final class Snapshot {
        int view, simulation;
        Integer biomeBlend;
        Double entityDistance;
        Integer fpsCap;
        Boolean vsync;
        boolean shadows;
        CloudStatus clouds;
        ParticleStatus particles;
        static Snapshot capture(Minecraft mc) {
            var s = new Snapshot();
            s.view = mc.options.renderDistance().get(); s.simulation = mc.options.simulationDistance().get();
            s.shadows = mc.options.entityShadows().get(); s.clouds = mc.options.cloudStatus().get();
            s.particles = mc.options.particles().get();
            s.entityDistance = mc.options.entityDistanceScaling().get();
            s.biomeBlend = mc.options.biomeBlendRadius().get();
            s.fpsCap=mc.options.framerateLimit().get(); s.vsync=mc.options.enableVsync().get();
            return s;
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
        FrameMonitor.cancel(mc);
        initialize(mc);
        if (mode == config.performanceMode) return;
        restoreManaged(mc);
        config.performanceMode = mode;
        if (mode != PerformanceMode.OFF) apply(mc, mode);
        refreshBudget(); mc.options.save(); config.save();
    }

    private void apply(Minecraft mc, PerformanceMode mode) {
        config.performanceBefore = Snapshot.capture(mc);
        applyLimits(mc, config.performanceBefore, mode);
        config.performanceApplied = Snapshot.capture(mc);
        mc.options.save(); config.save();
    }

    static void applyLimits(Minecraft mc, Snapshot before, PerformanceMode mode) {
        mc.options.renderDistance().set(Math.min(before.view, mode.viewCap));
        mc.options.simulationDistance().set(Math.min(before.simulation, mode.simulationCap));
        mc.options.entityShadows().set(false);
        mc.options.cloudStatus().set(CloudStatus.OFF);
        mc.options.particles().set(mode == PerformanceMode.MAX ? ParticleStatus.MINIMAL
                : before.particles == ParticleStatus.ALL ? ParticleStatus.DECREASED : before.particles);
        mc.options.entityDistanceScaling().set(Math.min(before.entityDistance, mode.entityDistance));
        mc.options.biomeBlendRadius().set(Math.min(before.biomeBlend, mode.biomeBlend));
    }
    static void restoreSnapshot(Minecraft mc, Snapshot before, Snapshot applied) {
        if(before==null || applied==null)return;
        if(Objects.equals(mc.options.renderDistance().get(),applied.view))mc.options.renderDistance().set(before.view);
        if(Objects.equals(mc.options.simulationDistance().get(),applied.simulation))mc.options.simulationDistance().set(before.simulation);
        if(Objects.equals(mc.options.entityShadows().get(),applied.shadows))mc.options.entityShadows().set(before.shadows);
        if(before.clouds!=null && mc.options.cloudStatus().get()==applied.clouds)mc.options.cloudStatus().set(before.clouds);
        if(before.particles!=null && mc.options.particles().get()==applied.particles)mc.options.particles().set(before.particles);
        if(before.entityDistance!=null && applied.entityDistance!=null && Objects.equals(mc.options.entityDistanceScaling().get(),applied.entityDistance))mc.options.entityDistanceScaling().set(before.entityDistance);
        if(before.biomeBlend!=null && applied.biomeBlend!=null && Objects.equals(mc.options.biomeBlendRadius().get(),applied.biomeBlend))mc.options.biomeBlendRadius().set(before.biomeBlend);
    }
    static boolean sameSettings(Snapshot a, Snapshot b) {
        return a.view==b.view && a.simulation==b.simulation && a.shadows==b.shadows && a.clouds==b.clouds && a.particles==b.particles
                && Objects.equals(a.entityDistance,b.entityDistance) && Objects.equals(a.biomeBlend,b.biomeBlend)
                && Objects.equals(a.fpsCap,b.fpsCap) && Objects.equals(a.vsync,b.vsync);
    }
    private void restoreManaged(Minecraft mc) {
        restoreSnapshot(mc,config.performanceBefore,config.performanceApplied);
        config.performanceBefore=null;config.performanceApplied=null;
    }

    void refreshBudget() { RenderBudget.configure(config.performanceMode, config.performanceLightHud); }
}
