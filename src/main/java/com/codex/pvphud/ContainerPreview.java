package com.codex.pvphud;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.ShulkerBoxBlock;

/** Snapshot only the contents actually supplied in the hovered item's components. */
public record ContainerPreview(List<ItemStack> items, boolean bundle, float fullness, int selected)
        implements TooltipComponent {
    public static Optional<ContainerPreview> of(ItemStack stack) {
        var mod = PvpHudClient.getInstance();
        if (mod == null || !mod.containerPreviewsEnabled() || stack.isEmpty()) return Optional.empty();
        var display = stack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);
        if (display.hideTooltip()) return Optional.empty();
        var contents = stack.get(DataComponents.BUNDLE_CONTENTS);
        if (contents != null && display.shows(DataComponents.BUNDLE_CONTENTS)) {
            return Optional.of(new ContainerPreview(contents.itemCopyStream().limit(64).toList(), true,
                    contents.weight().result().map(Number::floatValue).orElse(0F), contents.getSelectedItemIndex()));
        }
        if (stack.getItem() instanceof BlockItem block && block.getBlock() instanceof ShulkerBoxBlock
                && display.shows(DataComponents.CONTAINER)) {
            var slots = NonNullList.withSize(27, ItemStack.EMPTY);
            stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(slots);
            return Optional.of(new ContainerPreview(List.copyOf(slots), false, 0, -1));
        }
        return Optional.empty();
    }
}
