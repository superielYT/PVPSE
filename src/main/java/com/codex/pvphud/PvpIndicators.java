package com.codex.pvphud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.text.Text;

/** Local player information only; no target scanning or server actions. */
final class PvpIndicators {
    private static final EquipmentSlot[] ARMOR = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    static void render(DrawContext c, MinecraftClient client, HudConfig config) {
        if (client.player == null || client.currentScreen != null || client.player.isSpectator()) return;
        int cx = c.getScaledWindowWidth() / 2;
        int cy = c.getScaledWindowHeight() / 2;
        if (config.showAttackCooldown) {
            float cooldown = Math.clamp(client.player.getAttackCooldownProgress(0), 0, 1);
            // Only show while recharging, avoiding a permanent extra crosshair.
            if (cooldown < 1) {
                c.fill(cx - 12, cy + 14, cx + 12, cy + 17, 0xB0181D26);
                c.fill(cx - 12, cy + 14, cx - 12 + Math.round(24 * cooldown), cy + 17, config.accentColor());
            }
        }
        int y = Math.max(8, c.getScaledWindowHeight() / 5);
        if (config.showLowHealthWarning && client.player.isAlive()
                && client.player.getHealth() <= config.lowHealthThreshold) {
            warning(c, client, "LOW HEALTH", cx, y, config.dangerColor());
            y += 19;
        }
        if (config.showArmorWarning) {
            int lowest = 101;
            for (EquipmentSlot slot : ARMOR) {
                var stack = client.player.getEquippedStack(slot);
                if (!stack.isEmpty() && stack.isDamageable() && stack.getMaxDamage() > 0) {
                    lowest = Math.min(lowest, Math.round(100.0F * (stack.getMaxDamage() - stack.getDamage()) / stack.getMaxDamage()));
                }
            }
            if (lowest <= config.armorWarningPercent) {
                warning(c, client, "ARMOR LOW  " + lowest + "%", cx, y, 0xFFFFB454);
            }
        }
    }

    private static void warning(DrawContext c, MinecraftClient client, String message, int cx, int y, int color) {
        int half = client.textRenderer.getWidth(message) / 2 + 9;
        c.fill(cx - half, y - 4, cx + half, y + 13, 0xDE11161E);
        c.fill(cx - half, y - 4, cx - half + 2, y + 13, color);
        c.drawCenteredTextWithShadow(client.textRenderer, Text.literal(message), cx, y, color);
    }
}
