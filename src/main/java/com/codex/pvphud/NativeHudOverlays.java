package com.codex.pvphud;

import java.util.Comparator;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

/** Small additions anchored to vanilla HUD elements, without extra HUD cards. */
public final class NativeHudOverlays {
    private static final String[] GOLD_OUTLINE = {
            "  ###    ", " #   #   ", "#     #  ", "#     #  ",
            " #   #   ", "  ### #  ", "     # # ", "      #  ", "         "
    };

    private NativeHudOverlays() {}

    public static void food(GuiGraphicsExtractor graphics, Player player, int y, int right) {
        Minecraft mc = Minecraft.getInstance();
        var mod = PvpHudClient.getInstance();
        if (mod == null || !mod.nativeSaturationEnabled() || player != mc.player) return;
        float saturation = SaturationState.value(mc);
        if (!Float.isFinite(saturation) || saturation <= 0) return;
        int halves = (int) Math.ceil(saturation);
        for (int i = 0; i < 10 && i * 2 < halves; i++) {
            int x = right - i * 8 - 9;
            int columns = i * 2 + 1 < halves ? 9 : 4;
            for (int row = 0; row < GOLD_OUTLINE.length; row++) {
                for (int col = 0; col < columns; col++) {
                    if (GOLD_OUTLINE[row].charAt(col) == '#')
                        graphics.fill(x + col, y + row, x + col + 1, y + row + 1, 0xFFFFD34A);
                }
            }
        }
    }

    public static void potions(GuiGraphicsExtractor graphics) {
        Minecraft mc = Minecraft.getInstance();
        var mod = PvpHudClient.getInstance();
        if (mod == null || !mod.nativePotionTimersEnabled() || mc.player == null
                || (mc.screen != null && mc.screen.showsActiveEffects())) return;
        int good = 0, bad = 0;
        // Match vanilla's ordering, separate rows, demo offset and icon visibility.
        for (var effect : mc.player.getActiveEffects().stream().sorted(Comparator.reverseOrder()).toList()) {
            if (!effect.showIcon()) continue;
            boolean beneficial = effect.getEffect().value().isBeneficial();
            int x = graphics.guiWidth() - 25 * (beneficial ? ++good : ++bad);
            int y = 1 + (mc.isDemo() ? 15 : 0) + (beneficial ? 0 : 26);
            String text = duration(effect);
            int color = effect.isInfiniteDuration() || effect.getDuration() > 200 ? 0xFFFFFFFF
                    : effect.getDuration() > 100 ? 0xFFFFD45A : 0xFFFF7878;
            graphics.pose().pushMatrix();
            try {
                graphics.pose().translate(x + 12F, y + 17F);
                float scale = Math.min(0.65F, 22F / Math.max(1, mc.font.width(text)));
                graphics.pose().scale(scale, scale);
                graphics.text(mc.font, text, -mc.font.width(text) / 2, 0, color, true);
            } finally {
                graphics.pose().popMatrix();
            }
        }
    }

    static String duration(MobEffectInstance effect) {
        if (effect.isInfiniteDuration()) return "\u221e";
        int seconds = Math.max(0, (int) Math.ceil(effect.getDuration() / 20.0));
        if (seconds >= 3600) return seconds / 3600 + "h" + String.format(Locale.ROOT, "%02d", seconds / 60 % 60);
        return seconds / 60 + ":" + String.format(Locale.ROOT, "%02d", seconds % 60);
    }
}
