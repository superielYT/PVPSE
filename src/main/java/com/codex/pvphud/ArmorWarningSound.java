package com.codex.pvphud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import com.codex.pvphud.hud.ArmorHUD;

final class ArmorWarningSound {
    private final ArmorAlertGate gate = new ArmorAlertGate();
    private Object player;
    void tick(Minecraft client, HudConfig config) {
        if (player != client.player) { gate.reset(); player = client.player; }
        if (client.player == null || !client.player.isAlive() || client.player.isSpectator()
                || !config.enabled || !config.showArmorWarning || !config.armorWarningSound
                || client.options.hideGui
                || (config.hideOnDebug && client.getDebugOverlay().showDebugScreen())) {
            gate.reset(); return;
        }
        if (client.screen != null) return;
        int lowest = 101;
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            var stack = client.player.getItemBySlot(slot);
            if (!stack.isEmpty() && stack.isDamageableItem() && stack.getMaxDamage() > 0)
                lowest = Math.min(lowest, ArmorHUD.durabilityPercent(stack.getDamageValue(), stack.getMaxDamage()));
        }
        if (gate.update(lowest <= config.armorWarningPercent, client.player.tickCount)) {
            client.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_PLING.value(),
                    1.25F, config.armorSoundVolume / 100.0F));
        }
    }
}
