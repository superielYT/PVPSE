package com.codex.pvphud.mixin;

import com.codex.pvphud.FullbrightState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
abstract class FullbrightVisionMixin {
    @Inject(method = "getNightVisionScale", at = @At("RETURN"), cancellable = true)
    private static void pvpse$minimumVision(LivingEntity entity, float tick, CallbackInfoReturnable<Float> cir) {
        if (entity == Minecraft.getInstance().player && FullbrightState.strength() > 0)
            cir.setReturnValue(Math.max(cir.getReturnValue(), FullbrightState.strength()));
    }
    @Inject(method = "getNightVisionScale", at = @At("HEAD"), cancellable = true)
    private static void pvpse$vision(LivingEntity entity, float tick, CallbackInfoReturnable<Float> cir) {
        if (FullbrightState.strength() > 0 && entity == Minecraft.getInstance().player
                && entity.getEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION) == null)
            cir.setReturnValue(FullbrightState.strength());
    }
}
