package com.codex.pvphud.hud;

import com.codex.pvphud.theme.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/** Uses Minecraft's actual crosshair target; never scans nearby or hidden blocks. */
public final class BlockOverlayHUD extends HudElement {
    public record Target(BlockPos position, BlockState state, double distance) {}

    public BlockOverlayHUD() { super("block_overlay", "Block Overlay", 250, 90); }

    public static Target target(Minecraft mc) {
        if (mc.level == null || mc.player == null || !(mc.hitResult instanceof BlockHitResult hit)
                || hit.getType() != HitResult.Type.BLOCK) return null;
        BlockState state = mc.level.getBlockState(hit.getBlockPos());
        if (state.isAir()) return null;
        return new Target(hit.getBlockPos().immutable(), state, mc.player.getEyePosition().distanceTo(hit.getLocation()));
    }

    @Override protected void renderElement(GuiGraphicsExtractor c, Minecraft mc, Theme theme, boolean editor, float alpha) {
        if (!editor && mc.screen != null) return;
        Target target = target(mc);
        if (target == null && !editor) return;
        BlockState state = target == null ? Blocks.STONE.defaultBlockState() : target.state();
        HudStyles.card(c, theme, width(mc), height(mc), alpha);
        HudStyles.label(c, mc, Component.literal("LOOKING AT"), 6, 5, theme.mutedText());
        ItemStack icon = new ItemStack(state.getBlock().asItem());
        if (!icon.isEmpty()) c.item(icon, 6, 20);
        else HudStyles.label(c, mc, Component.literal("?"), 11, 24, theme.accent());
        HudStyles.label(c, mc, Component.literal(mc.font.plainSubstrByWidth(state.getBlock().getName().getString(), 142)),
                28, 19, theme.text());
        String id = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
        HudStyles.label(c, mc, Component.literal(mc.font.plainSubstrByWidth(id, 142)), 28, 31, theme.mutedText());
        String detail = target == null ? "Aim at a block to inspect it" : String.format(java.util.Locale.ROOT,
                "%d %d %d  |  %.1fm", target.position().getX(), target.position().getY(), target.position().getZ(), target.distance());
        HudStyles.label(c, mc, Component.literal(mc.font.plainSubstrByWidth(detail, 164)), 6, 47, theme.accentSecondary());
    }

    public int width(Minecraft mc) { return 176; }
    public int height(Minecraft mc) { return 63; }
}
