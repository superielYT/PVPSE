package com.codex.pvphud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;

/** Local player information only; no target scanning or server actions. */
final class PvpIndicators {
    private static final EquipmentSlot[] ARMOR = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    static void render(GuiGraphicsExtractor c, Minecraft client, HudConfig config) {
        if (client.player == null || client.screen != null || client.player.isSpectator()) return;
        int cx = c.guiWidth() / 2;
        int cy = c.guiHeight() / 2;
        if (config.showAttackCooldown) {
            float cooldown = Math.clamp(client.player.getAttackStrengthScale(0), 0, 1);
            // Only show while recharging, avoiding a permanent extra crosshair.
            if (cooldown < 1) {
                c.fill(cx - 12, cy + 14, cx + 12, cy + 17, 0xB0181D26);
                c.fill(cx - 12, cy + 14, cx - 12 + Math.round(24 * cooldown), cy + 17, config.accentColor());
            }
        }
        int y = Math.max(8, c.guiHeight() / 5);
        if (config.showLowHealthWarning && client.player.isAlive()
                && client.player.getHealth() <= config.lowHealthThreshold) {
            warning(c, client, "LOW HEALTH", cx, y, config.dangerColor());
            y += 19;
        }
        if (config.showArmorWarning) {
            int lowest = 101;
            for (EquipmentSlot slot : ARMOR) {
                var stack = client.player.getItemBySlot(slot);
                if (!stack.isEmpty() && stack.isDamageableItem() && stack.getMaxDamage() > 0) {
                    lowest = Math.min(lowest, Math.round(100.0F * (stack.getMaxDamage() - stack.getDamageValue()) / stack.getMaxDamage()));
                }
            }
            if (lowest <= config.armorWarningPercent) {
                warning(c, client, "ARMOR LOW  " + lowest + "%", cx, y, 0xFFFFB454);
            }
        }
    }

    private static void warning(GuiGraphicsExtractor c, Minecraft client, String message, int cx, int y, int color) {
        int half = client.font.width(message) / 2 + 9;
        c.fill(cx - half, y - 4, cx + half, y + 13, 0xDE11161E);
        c.fill(cx - half, y - 4, cx - half + 2, y + 13, color);
        c.centeredText(client.font, Component.literal(message), cx, y, color);
    }
}
