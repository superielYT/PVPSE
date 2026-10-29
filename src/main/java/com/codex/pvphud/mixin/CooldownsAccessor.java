package com.codex.pvphud.mixin;

import java.util.Map;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemCooldowns;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemCooldowns.class)
public interface CooldownsAccessor {
    @Accessor("tickCount") int pvpse$tickCount();
    @Accessor("cooldowns") Map<Identifier, ?> pvpse$cooldowns();
}
