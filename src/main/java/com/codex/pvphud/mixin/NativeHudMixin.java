package com.codex.pvphud.mixin;

import com.codex.pvphud.NativeHudOverlays;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class NativeHudMixin {
    @Inject(method = "extractEffects", at = @At("RETURN"))
    private void pvpse$potionTimers(GuiGraphicsExtractor graphics, DeltaTracker delta, CallbackInfo ci) {
        NativeHudOverlays.potions(graphics);
    }

    @Inject(method = "extractFood", at = @At("RETURN"))
    private void pvpse$saturation(GuiGraphicsExtractor graphics, Player player, int y, int right, CallbackInfo ci) {
        NativeHudOverlays.food(graphics, player, y, right);
    }
}
