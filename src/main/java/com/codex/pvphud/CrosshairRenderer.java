package com.codex.pvphud;

import com.codex.pvphud.crosshair.CustomCrosshair;
import com.codex.pvphud.theme.ThemeManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.entity.player.PlayerEntity;

final class CrosshairRenderer {
    private CrosshairRenderer() {
    }

    static void draw(DrawContext context, int cx, int cy, HudConfig config, boolean useTargetColor) {
        if (!config.showCrosshair || config.crosshairStyle == HudConfig.CrosshairStyle.OFF) {
            return;
        }

        int size = Math.max(HudConfig.MIN_CROSSHAIR_SIZE, config.crosshairSize);
        int color = useTargetColor && isTargetingPlayer() ? config.dangerColor() : config.accentColor();
        int shadow = 0xAA000000;
        CustomCrosshair custom = CustomCrosshair.getInstance();
        if (custom.enabled()) {
            drawCustom(context, cx, cy, custom, useTargetColor && isTargetingPlayer() ? config.dangerColor() :
                    (custom.useThemeColor() ? ThemeManager.getInstance().getTheme().accent() : custom.color()));
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

    static void drawCentered(DrawContext context, HudConfig config) {
        int cx = context.getScaledWindowWidth() / 2;
        int cy = context.getScaledWindowHeight() / 2;
        draw(context, cx, cy, config, true);
    }

    static boolean usesCustomCrosshair(HudConfig config) {
        return config.showCrosshair && config.crosshairStyle != HudConfig.CrosshairStyle.OFF;
    }

    private static boolean isTargetingPlayer() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.crosshairTarget instanceof EntityHitResult hit && hit.getEntity() instanceof PlayerEntity;
    }

    private static void line(DrawContext context, int x1, int y1, int x2, int y2, int color) {
        context.fill(x1, y1, x2, y2, color);
    }

    private static void drawCustom(DrawContext context, int cx, int cy, CustomCrosshair custom, int color) {
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
