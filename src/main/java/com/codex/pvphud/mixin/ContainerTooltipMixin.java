package com.codex.pvphud.mixin;

import com.codex.pvphud.ContainerPreview;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ContainerTooltipMixin {
    @Inject(method = "getTooltipImage", at = @At("RETURN"), cancellable = true)
    private void pvpse$contents(CallbackInfoReturnable<Optional<TooltipComponent>> cir) {
        ContainerPreview.of((ItemStack)(Object)this).ifPresent(preview -> cir.setReturnValue(Optional.of(preview)));
    }

    @Inject(method = "addToTooltip", at = @At("HEAD"), cancellable = true)
    private void pvpse$avoidDuplicateList(DataComponentType<?> type, Item.TooltipContext context,
            TooltipDisplay display, Consumer<Component> lines, TooltipFlag flag, CallbackInfo ci) {
        if (type == DataComponents.CONTAINER && ContainerPreview.of((ItemStack)(Object)this).isPresent()) ci.cancel();
    }
}
