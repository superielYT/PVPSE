package com.codex.pvphud.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets="net.minecraft.world.item.ItemCooldowns$CooldownInstance")
public interface CooldownEntryAccessor {
    @Accessor("endTime") int pvpse$endTime();
}
