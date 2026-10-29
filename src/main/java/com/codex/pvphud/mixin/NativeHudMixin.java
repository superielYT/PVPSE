package com.codex.pvphud.mixin;

import com.codex.pvphud.NativeHudOverlays;
import com.codex.pvphud.CleanHotbar;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class NativeHudMixin {
    @Redirect(method="extractItemHotbar", at=@At(value="INVOKE",
            target="Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V"))
    private void pvpse$hotbarBackground(GuiGraphicsExtractor graphics, RenderPipeline pipeline,
            Identifier sprite, int x,int y,int width,int height) {
        if(!CleanHotbar.background(graphics,sprite,x,y,width,height)) graphics.blitSprite(pipeline,sprite,x,y,width,height);
    }

    @Inject(method="extractSlot",at=@At("RETURN"))
    private void pvpse$hotbarDetails(GuiGraphicsExtractor graphics,int x,int y,DeltaTracker delta,
            Player player,ItemStack stack,int seed,CallbackInfo ci) {
        CleanHotbar.details(graphics,x,y,player,stack);
    }
    @Inject(method = "extractEffects", at = @At("RETURN"))
    private void pvpse$potionTimers(GuiGraphicsExtractor graphics, DeltaTracker delta, CallbackInfo ci) {
        NativeHudOverlays.potions(graphics);
    }

    @Inject(method = "extractFood", at = @At("RETURN"))
    private void pvpse$saturation(GuiGraphicsExtractor graphics, Player player, int y, int right, CallbackInfo ci) {
        NativeHudOverlays.food(graphics, player, y, right);
    }
}
