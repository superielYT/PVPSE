package com.codex.pvphud.mixin;

import com.codex.pvphud.FullbrightState;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
abstract class FullbrightEffectMixin {
    @Inject(method = "hasEffect", at = @At("HEAD"), cancellable = true)
    private void pvpse$nightVision(Holder<MobEffect> effect, CallbackInfoReturnable<Boolean> cir) {
        if (FullbrightState.strength() > 0 && effect.equals(MobEffects.NIGHT_VISION)
                && (Object) this == Minecraft.getInstance().player) cir.setReturnValue(true);
    }
}
