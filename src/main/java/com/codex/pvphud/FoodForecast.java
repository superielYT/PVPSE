package com.codex.pvphud;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Food data comes from the actual held stack, including server-provided components. */
public record FoodForecast(int hunger, float saturation, int foodGain, float saturationGain, boolean canEat) {
    public static FoodForecast held(Player player, float currentSaturation) {
        if (player == null) return null;
        ItemStack stack = player.isUsingItem() && player.getUseItem().has(DataComponents.FOOD)
                ? player.getUseItem() : player.getMainHandItem().has(DataComponents.FOOD)
                ? player.getMainHandItem() : player.getOffhandItem();
        var food = stack.get(DataComponents.FOOD);
        if (food == null || !stack.has(DataComponents.CONSUMABLE)) return null;
        int current = player.getFoodData().getFoodLevel();
        boolean canEat = current < 20 || food.canAlwaysEat();
        int hunger = canEat ? Math.min(20, current + food.nutrition()) : current;
        float saturation = Float.isFinite(currentSaturation)
                ? (canEat ? Math.min(hunger, currentSaturation + food.saturation()) : currentSaturation) : Float.NaN;
        return new FoodForecast(hunger, saturation, hunger - current,
                Float.isFinite(saturation) ? saturation - currentSaturation : (canEat ? food.saturation() : 0), canEat);
    }
}
