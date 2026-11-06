package com.codex.pvphud.mixin;

import com.codex.pvphud.PvpHudClient;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adjust only vanilla's local retry delay. No extra packets or item switching. */
@Mixin(Minecraft.class)
abstract class FastXpMixin {
    @Shadow private int rightClickDelay;
    @Inject(method="startUseItem",at=@At("RETURN"))
    private void pvpse$xp(CallbackInfo ci){
        var mc=(Minecraft)(Object)this;var mod=PvpHudClient.getInstance();
        if(mod==null || !mod.fastXpEnabled() || mc.player==null || mc.screen!=null || !mc.options.keyUse.isDown() || mc.player.isSpectator())return;
        if(mc.player.getMainHandItem().is(Items.EXPERIENCE_BOTTLE) || mc.player.getOffhandItem().is(Items.EXPERIENCE_BOTTLE))
            rightClickDelay=Math.min(rightClickDelay,mod.xpUseInterval());
    }
}
