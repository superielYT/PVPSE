package com.codex.pvphud;

import com.codex.pvphud.crosshair.CustomCrosshair;
import com.codex.pvphud.theme.ThemeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;

final class CrosshairRenderer {
    private CrosshairRenderer() {
    }

    static void draw(GuiGraphicsExtractor context, int cx, int cy, HudConfig config, boolean useTargetColor) {
        if (!config.showCrosshair || config.crosshairStyle == HudConfig.CrosshairStyle.OFF) {
            return;
        }

        int size = Math.max(HudConfig.MIN_CROSSHAIR_SIZE, config.crosshairSize);
        Minecraft mc = Minecraft.getInstance();
        boolean live = useTargetColor && mc.player != null && mc.screen == null && !mc.options.hideGui;
        float readiness = live ? mc.player.getAttackStrengthScale(mc.getDeltaTracker().getGameTimeDeltaPartialTick(false)) : 1;
        int reactive = readiness < 0.95F ? 0xFFFFB454 : config.accentColor();
        int color = live && config.crosshairReactions ? reactive : config.accentColor();
        if (useTargetColor && isTargetingPlayer() && !(live && config.crosshairReactions && readiness < 0.95F)) color = config.dangerColor();
        if (live && config.cooldownRing && readiness < 0.995F)
            ring(context, cx, cy, Math.max(config.cooldownRingRadius, size + 4), readiness, color);
        int shadow = 0xAA000000;
        CustomCrosshair custom = CustomCrosshair.getInstance();
        if (custom.enabled()) {
            drawCustom(context, cx, cy, custom, live && config.crosshairReactions ? color : (useTargetColor && isTargetingPlayer() ? config.dangerColor() :
                    (custom.useThemeColor() ? ThemeManager.getInstance().getTheme().accent() : custom.color())));
            return;
        }

        switch (config.crosshairStyle) {
            case DOT -> {
                context.fill(cx - 1, cy - 1, cx + 2, cy + 2, shadow);
                context.fill(cx, cy, cx + 1, cy + 1, color);
            }
            case PLUS -> {
                line(context, cx - size, cy, cx + size + 1, cy + 1, shadow);
                line(context, cx, cy - size, cx + 1, cy + size + 1, shadow);
                line(context, cx - size + 1, cy, cx + size, cy + 1, color);
                line(context, cx, cy - size + 1, cx + 1, cy + size, color);
            }
            case GAP -> {
                int gap = 3;
                line(context, cx - size, cy, cx - gap, cy + 1, color);
                line(context, cx + gap + 1, cy, cx + size + 1, cy + 1, color);
                line(context, cx, cy - size, cx + 1, cy - gap, color);
                line(context, cx, cy + gap + 1, cx + 1, cy + size + 1, color);
            }
            case DIAMOND -> {
                for (int i = 0; i <= size; i++) {
                    context.fill(cx - i, cy - size + i, cx - i + 1, cy - size + i + 1, color);
                    context.fill(cx + i, cy - size + i, cx + i + 1, cy - size + i + 1, color);
                    context.fill(cx - i, cy + size - i, cx - i + 1, cy + size - i + 1, color);
                    context.fill(cx + i, cy + size - i, cx + i + 1, cy + size - i + 1, color);
                }
            }
            case BOX -> {
                line(context, cx - size, cy - size, cx + size + 1, cy - size + 1, color);
                line(context, cx - size, cy + size, cx + size + 1, cy + size + 1, color);
                line(context, cx - size, cy - size, cx - size + 1, cy + size + 1, color);
                line(context, cx + size, cy - size, cx + size + 1, cy + size + 1, color);
            }
            case OFF -> {
            }
        }
    }

    private static void ring(GuiGraphicsExtractor c, int cx, int cy, int radius, float progress, int tint) {
        // A one-pixel, low-opacity track with a brighter attack readiness arc.
        int segments = 96;
        int filled = Math.clamp(Math.round(progress * segments), 0, segments);
        for (int i = 0; i < segments; i++) {
            double angle = i * Math.PI * 2 / segments - Math.PI / 2;
            int x = cx + (int) Math.round(Math.cos(angle) * radius);
            int y = cy + (int) Math.round(Math.sin(angle) * radius);
            c.fill(x, y, x + 1, y + 1, i < filled ? (0xB0000000 | (tint & 0xFFFFFF)) : 0x35000000);
        }
    }

    static void drawCentered(GuiGraphicsExtractor context, HudConfig config) {
        int cx = context.guiWidth() / 2;
        int cy = context.guiHeight() / 2;
        draw(context, cx, cy, config, true);
    }

    static boolean usesCustomCrosshair(HudConfig config) {
        return config.enabled && config.showCrosshair && config.crosshairStyle != HudConfig.CrosshairStyle.OFF;
    }

    private static boolean isTargetingPlayer() {
        Minecraft client = Minecraft.getInstance();
        return client.hitResult instanceof EntityHitResult hit && hit.getEntity() instanceof Player;
    }

    private static void line(GuiGraphicsExtractor context, int x1, int y1, int x2, int y2, int color) {
        context.fill(x1, y1, x2, y2, color);
    }

    private static void drawCustom(GuiGraphicsExtractor context, int cx, int cy, CustomCrosshair custom, int color) {
        int center = CustomCrosshair.GRID_SIZE / 2;
        for (int y = 0; y < CustomCrosshair.GRID_SIZE; y++) {
            for (int x = 0; x < CustomCrosshair.GRID_SIZE; x++) {
                if (!custom.pixel(x, y)) continue;
                int px = cx + x - center;
                int py = cy + y - center;
                context.fill(px, py, px + 1, py + 1, color);
            }
        }
    }
}
