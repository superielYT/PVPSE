package com.codex.pvphud.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/** Uses Minecraft's actual crosshair target; never scans nearby or hidden blocks. */
public final class BlockOverlayHUD {
    public record Target(BlockPos position, BlockState state, double distance) {}

    private BlockOverlayHUD() {}

    public static Target target(Minecraft mc) {
        if (mc.level == null || mc.player == null || !(mc.hitResult instanceof BlockHitResult hit)
                || hit.getType() != HitResult.Type.BLOCK) return null;
        BlockState state = mc.level.getBlockState(hit.getBlockPos());
        if (state.isAir()) return null;
        return new Target(hit.getBlockPos().immutable(), state, mc.player.getEyePosition().distanceTo(hit.getLocation()));
    }

}
