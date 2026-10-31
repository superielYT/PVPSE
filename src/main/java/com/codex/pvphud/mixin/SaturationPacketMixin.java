package com.codex.pvphud.mixin;

import com.codex.pvphud.SaturationState;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPacketListener.class)
public abstract class SaturationPacketMixin {
    @Redirect(method="handleSetHealth",at=@At(value="INVOKE",target="Lnet/minecraft/world/food/FoodData;setSaturation(F)V"))
    private void pvpse$serverSnapshot(FoodData food,float value){
        SaturationState.applyingVanilla=true;
        try{food.setSaturation(value);}finally{SaturationState.applyingVanilla=false;}
        SaturationState.accept(value,false);
    }
}
