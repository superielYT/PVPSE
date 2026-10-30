package com.codex.pvphud.mixin;

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
    @Inject(method = "getNameTag", at = @At("RETURN"), cancellable = true)
    private void pvpse$healthAndPops(Entity entity, CallbackInfoReturnable<Component> cir) {
        Minecraft mc = Minecraft.getInstance();
        if (!(entity instanceof Player player) || mc.player == null || player == mc.player) return;
        float health = player.getHealth() + player.getAbsorptionAmount();
        int pops = PvpStats.getInstance().totemPops(player.getUUID());
        var label = cir.getReturnValue().copy()
                .append(Component.literal("  "))
                .append(Component.literal(String.format(java.util.Locale.ROOT, "%.1f❤", health))
                        .withStyle(ChatFormatting.RED));
        if (pops > 0) label.append(Component.literal("  ×" + pops).withStyle(ChatFormatting.GOLD));
        cir.setReturnValue(label);
    }
}
