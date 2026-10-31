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
        if (mod == null || player != mc.player) return;
        float saturation = SaturationState.value(mc);
        boolean approximate=SaturationState.approximate(mc);
        var forecast = mod.foodPreviewEnabled() ? FoodForecast.held(player, saturation) : null;
        if (forecast != null) {
            for (int half = player.getFoodData().getFoodLevel(); half < forecast.hunger(); half++)
                outlineHalf(graphics, right, y, half, 0xFF80E5FF, false);
            if (mod.nativeSaturationEnabled() && Float.isFinite(forecast.saturation())) {
                for (int half = (int)Math.ceil(saturation); half < (int)Math.ceil(forecast.saturation()); half++)
                    outlineHalf(graphics, right, y, half, 0xFFFFE699, true);
            }
            String text = !forecast.canEat() ? "Full - cannot eat yet" : "+" + forecast.foodGain() + " food  "
                    + (Float.isFinite(saturation) ? (approximate ? "~+" : "+") : "up to ")
                    + String.format(Locale.ROOT, "%.1f", forecast.saturationGain()) + " sat";
            graphics.pose().pushMatrix();
            try {
                float scale = Math.min(.9F, 88F / Math.max(1, mc.font.width(text)));
                graphics.pose().translate(right - 1F, y - 10F);
                graphics.pose().scale(scale, scale);
                graphics.text(mc.font, text, -mc.font.width(text), 0, 0xFFFFE699, true);
            } finally { graphics.pose().popMatrix(); }
        }
        if (mod.nativeSaturationEnabled() && Float.isFinite(saturation))
            for (int half = 0; half < (int)Math.ceil(saturation); half++)
                outlineHalf(graphics, right, y, half, approximate?0xBBFFD34A:0xFFFFD34A, false);
        if(mod.nativeSaturationEnabled() && approximate)
            graphics.text(mc.font,"~",right+3,y,0xFFFFD34A,true);
    }

    private static void outlineHalf(GuiGraphicsExtractor c, int right, int y, int half, int color, boolean dashed) {
        int x = right - (half / 2) * 8 - 9;
        int from = half % 2 == 0 ? 0 : 4, to = half % 2 == 0 ? 4 : 9;
        for (int row = 0; row < GOLD_OUTLINE.length; row++) for (int col = from; col < to; col++)
            if (GOLD_OUTLINE[row].charAt(col) == '#' && (!dashed || (row + col) % 2 == 0))
                c.fill(x + col, y + row, x + col + 1, y + row + 1, color);
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
