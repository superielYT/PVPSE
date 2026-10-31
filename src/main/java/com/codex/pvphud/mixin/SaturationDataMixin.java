package com.codex.pvphud.mixin;

import com.codex.pvphud.SaturationState;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public abstract class SaturationDataMixin {
    @Inject(method="setSaturation",at=@At("RETURN"))
    private void pvpse$existingAppleSkin(float value,CallbackInfo ci){
        if(SaturationState.applyingVanilla || !FabricLoader.getInstance().isModLoaded("appleskin"))return;
        var mc=Minecraft.getInstance();
        if(mc.isSameThread() && mc.player!=null && mc.player.getFoodData()==(Object)this)SaturationState.accept(value,true);
    }
}
