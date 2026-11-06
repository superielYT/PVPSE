package com.codex.pvphud.mixin;

import com.codex.pvphud.EnemyInfo;
import com.codex.pvphud.PvpHudClient;
import com.codex.pvphud.PvpStats;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
abstract class PlayerNameHealthMixin {
    @Inject(method="getNameTag",at=@At("RETURN"),cancellable=true)
    private void pvpse$health(Entity entity,CallbackInfoReturnable<Component> cir){
        var mc=Minecraft.getInstance(); var mod=PvpHudClient.getInstance();
        if(mod==null || !(entity instanceof Player player) || player==mc.player || mc.player==null || cir.getReturnValue()==null)return;
        if(!mod.enemyHealthEnabled() && !mod.enemyPopsEnabled())return;
        var label=cir.getReturnValue().copy();
        if(mod.enemyHealthEnabled()) {
            label.append(Component.literal(String.format(java.util.Locale.ROOT,"  %.1f❤",EnemyInfo.hearts(player.getHealth()))).withStyle(ChatFormatting.RED));
            if(player.getAbsorptionAmount()>0)label.append(Component.literal(String.format(java.util.Locale.ROOT," +%.1f",EnemyInfo.hearts(player.getAbsorptionAmount()))).withStyle(ChatFormatting.GOLD));
        }
        int pops=PvpStats.getInstance().totemPops(player.getUUID());
        if(mod.enemyPopsEnabled() && pops>0)label.append(Component.literal("  ×"+pops).withStyle(ChatFormatting.GOLD));
        cir.setReturnValue(label);
    }
}
