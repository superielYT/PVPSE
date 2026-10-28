package com.codex.pvphud.mixin;

import com.codex.pvphud.PvpStats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
abstract class TotemPopMixin {
    @Inject(method = "handleEntityEvent", at = @At("HEAD"))
    private void pvpse$totemPop(byte status, CallbackInfo ci) {
        if (status == 35) PvpStats.getInstance().recordTotemPop((Entity) (Object) this);
    }
}
