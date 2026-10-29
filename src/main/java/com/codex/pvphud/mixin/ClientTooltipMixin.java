package com.codex.pvphud.mixin;

import com.codex.pvphud.ContainerPreview;
import com.codex.pvphud.ContainerPreviewRenderer;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientTooltipComponent.class)
public interface ClientTooltipMixin {
    @Inject(method = "create(Lnet/minecraft/world/inventory/tooltip/TooltipComponent;)Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipComponent;",
            at = @At("HEAD"), cancellable = true)
    private static void pvpse$preview(TooltipComponent data, CallbackInfoReturnable<ClientTooltipComponent> cir) {
        if (data instanceof ContainerPreview preview) cir.setReturnValue(new ContainerPreviewRenderer(preview));
    }
}
