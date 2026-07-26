package com.codex.pvphud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

final class HudRenderer {
    private final List<WidgetBounds> lastBounds = new ArrayList<>();

    List<WidgetBounds> lastBounds() {
        return lastBounds;
    }

    List<WidgetBounds> render(DrawContext context, MinecraftClient client, HudConfig config, CpsTracker cps, boolean editorMode) {
        lastBounds.clear();
        if (client.player == null || client.world == null || !config.enabled) {
            return lastBounds;
        }

        TextRenderer text = client.textRenderer;
        int screenW = context.getScaledWindowWidth();
        int screenH = context.getScaledWindowHeight();
        PlayerEntity player = client.player;

        if (config.useFreeLayout) {
            drawCombat(context, text, screenW, screenH, config, editorMode, client, cps);
            drawPlayer(context, text, screenW, screenH, config, editorMode, player);
            if (config.showCoords) {
                drawLocation(context, text, screenW, screenH, config, editorMode, player, client);
            }
            ItemStack mainHand = player.getMainHandStack();
            if (!mainHand.isEmpty() && mainHand.isDamageable()) {
                drawHeld(context, text, screenW, screenH, config, editorMode, mainHand);
            }
            if (config.showTarget) {
                TargetInfo target = targetInfo(client);
                if (target != null) {
                    drawTarget(context, text, screenW, screenH, config, editorMode, target);
                }
            }
            if (config.showPotions && !player.getStatusEffects().isEmpty()) {
                drawEffects(context, text, screenW, screenH, config, editorMode, player);
            }
            if (config.showArmor) {
                drawArmor(context, text, screenW, screenH, config, editorMode, player);
            }
        } else {
            drawStack(context, text, screenW, screenH, config, editorMode, client, player, cps);
        }

        return lastBounds;
    }

    private void drawCombat(DrawContext context, TextRenderer text, int screenW, int screenH, HudConfig config, boolean editorMode,
                            MinecraftClient client, CpsTracker cps) {
        Text title = Text.translatable("hud.pvp_hud_client.widget.combat");
        List<HudPanel.Chip> chips = new ArrayList<>();
        chips.add(HudPanel.Chip.of(Text.literal("FPS "), Text.literal(String.valueOf(client.getCurrentFps()))));
        chips.add(HudPanel.Chip.of(Text.literal("Ping "), Text.literal(ping(client) + "ms")));
        if (config.showCps) {
            String cpsValue = config.showRightCps ? cps.leftCps() + "/" + cps.rightCps() : String.valueOf(cps.leftCps());
            chips.add(HudPanel.Chip.of(Text.literal("CPS "), Text.literal(cpsValue)));
        }
        placeChipWidget(context, text, screenW, screenH, config, editorMode, HudWidgetId.COMBAT, title, chips);
    }

    private void drawPlayer(DrawContext context, TextRenderer text, int screenW, int screenH, HudConfig config, boolean editorMode, PlayerEntity player) {
        WidgetLayout layout = config.layout(HudWidgetId.PLAYER);
        if (!layout.visible && !editorMode) {
            return;
        }

        float health = player.getHealth() + player.getAbsorptionAmount();
        float maxHealth = Math.max(1.0F, player.getMaxHealth());
        float hpPercent = MathHelper.clamp(health / maxHealth, 0.0F, 1.0F);
        boolean lowHp = hpPercent < 0.25F;

        Text title = Text.translatable("hud.pvp_hud_client.widget.player");
        List<HudPanel.Chip> chips = List.of(
                HudPanel.Chip.of(Text.literal("HP "), Text.literal(String.format(Locale.ROOT, "%.1f", health))),
                HudPanel.Chip.of(Text.literal("Spd "), Text.literal(speed(player)))
        );

        int x = toX(layout.x, screenW);
        int y = toY(layout.y, screenH);
        int width = HudPanel.chipPanelWidth(text, title, chips, config.compact);
        int height = HudPanel.chipPanelHeight(text, title, chips, config.compact);
        if (config.showHealthBar) {
            height += 8;
        }

        HudPanel.drawChips(context, text, x, y, title, chips, config, editorMode);
        if (config.showHealthBar) {
            int barY = y + height - 10;
            HudPanel.drawProgressBar(context, x + 6, barY, width - 12, 4, hpPercent, config, lowHp);
            context.drawText(text, Text.literal(Math.round(hpPercent * 100) + "%"), x + width - 28, barY - 1, lowHp ? config.dangerColor() : config.mutedColor(), false);
        }
        addBounds(HudWidgetId.PLAYER, x, y, width, height, layout, editorMode);
    }

    private void drawLocation(DrawContext context, TextRenderer text, int screenW, int screenH, HudConfig config, boolean editorMode,
                                PlayerEntity player, MinecraftClient client) {
        Text title = Text.translatable("hud.pvp_hud_client.widget.location");
        List<HudPanel.Chip> chips = List.of(
                HudPanel.Chip.of(Text.literal("X "), Text.literal(String.format(Locale.ROOT, "%.0f", player.getX()))),
                HudPanel.Chip.of(Text.literal("Y "), Text.literal(String.format(Locale.ROOT, "%.0f", player.getY()))),
                HudPanel.Chip.of(Text.literal("Z "), Text.literal(String.format(Locale.ROOT, "%.0f", player.getZ()))),
                HudPanel.Chip.of(Text.literal("Biome "), Text.literal(biomeName(client)))
        );
        placeChipWidget(context, text, screenW, screenH, config, editorMode, HudWidgetId.LOCATION, title, chips);
    }

    private void drawHeld(DrawContext context, TextRenderer text, int screenW, int screenH, HudConfig config, boolean editorMode, ItemStack stack) {
        WidgetLayout layout = config.layout(HudWidgetId.HELD);
        if (!layout.visible && !editorMode) {
            return;
        }

        boolean low = lowDurability(stack);
        int percent = durabilityPercent(stack);
        Text title = Text.translatable("hud.pvp_hud_client.widget.held");
        List<HudPanel.Line> lines = List.of(low
                ? HudPanel.Line.danger(Text.translatable("hud.pvp_hud_client.held", stack.getName().getString(), durability(stack)))
                : HudPanel.Line.normal(Text.translatable("hud.pvp_hud_client.held", stack.getName().getString(), durability(stack))));

        int x = toX(layout.x, screenW);
        int y = toY(layout.y, screenH);
        int width = HudPanel.panelWidth(text, title, lines, config.compact);
        int height = HudPanel.panelHeight(text, title, lines, config.compact);
        if (config.showDurabilityBars) {
            height += 8;
        }

        HudPanel.draw(context, text, x, y, title, lines, config, editorMode);
        if (config.showDurabilityBars) {
            int barY = y + height - 10;
            HudPanel.drawProgressBar(context, x + 6, barY, width - 12, 4, percent / 100.0F, config, low);
            context.drawText(text, Text.literal(percent + "%"), x + width - 28, barY - 1, low ? config.dangerColor() : config.mutedColor(), false);
        }
        addBounds(HudWidgetId.HELD, x, y, width, height, layout, editorMode);
    }

    private void drawTarget(DrawContext context, TextRenderer text, int screenW, int screenH, HudConfig config, boolean editorMode, TargetInfo target) {
        placeChipWidget(context, text, screenW, screenH, config, editorMode, HudWidgetId.TARGET,
                Text.translatable("hud.pvp_hud_client.widget.target"), target.chips());
    }

    private void drawEffects(DrawContext context, TextRenderer text, int screenW, int screenH, HudConfig config, boolean editorMode, PlayerEntity player) {
        List<HudPanel.Line> effects = player.getStatusEffects().stream()
                .sorted(Comparator.comparing(effect -> effect.getEffectType().value().getTranslationKey()))
                .limit(5)
                .map(this::effectLine)
                .map(HudPanel.Line::muted)
                .toList();
        placeLineWidget(context, text, screenW, screenH, config, editorMode, HudWidgetId.EFFECTS,
                Text.translatable("hud.pvp_hud_client.widget.effects"), effects);
    }

    private void drawArmor(DrawContext context, TextRenderer text, int screenW, int screenH, HudConfig config, boolean editorMode, PlayerEntity player) {
        WidgetLayout layout = config.layout(HudWidgetId.ARMOR);
        if (!layout.visible && !editorMode) {
            return;
        }

        int slotWidth = 30;
        int width = 6 + slotWidth * 4 + 6;
        int height = config.showDurabilityBars ? 52 : 36;
        int x = toX(layout.x, screenW);
        int y = toY(layout.y, screenH);

        HudPanel.drawWidgetFrame(context, x, y, width, height, config, editorMode);
        context.drawText(text, Text.translatable("hud.pvp_hud_client.widget.armor"), x + 6, y + 4, config.accentColor(), false);
        if (editorMode) {
            HudPanel.drawCloseButton(context, x + width - 10, y + 3, false);
        }
        context.fill(x + 6, y + 14, x + width - 6, y + 15, 0x33FFFFFF);

        int slot = 0;
        for (EquipmentSlot armorSlot : List.of(EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD)) {
            ItemStack stack = player.getEquippedStack(armorSlot);
            int itemX = x + 6 + slot * slotWidth;
            if (!stack.isEmpty()) {
                context.drawItem(stack, itemX, y + 17);
                context.drawStackOverlay(text, stack, itemX, y + 17);
                if (stack.isDamageable()) {
                    int percent = durabilityPercent(stack);
                    boolean low = percent < 20;
                    if (config.showDurabilityBars) {
                        HudPanel.drawProgressBar(context, itemX, y + 34, 18, 3, percent / 100.0F, config, low);
                        context.drawText(text, Text.literal(percent + "%"), itemX, y + 39, low ? config.dangerColor() : config.mutedColor(), false);
                    }
                }
            }
            slot++;
        }

        addBounds(HudWidgetId.ARMOR, x, y, width, height, layout, editorMode);
    }

    private void drawStack(DrawContext context, TextRenderer text, int screenW, int screenH, HudConfig config, boolean editorMode,
                           MinecraftClient client, PlayerEntity player, CpsTracker cps) {
        List<HudPanel.Line> rows = new ArrayList<>();
        rows.add(HudPanel.Line.normal(Text.translatable("hud.pvp_hud_client.stats_short",
                client.getCurrentFps(), ping(client), cps.leftCps(), cps.rightCps())));
        rows.add(HudPanel.Line.normal(Text.translatable("hud.pvp_hud_client.player_short",
                String.format(Locale.ROOT, "%.1f", player.getHealth() + player.getAbsorptionAmount()), speed(player))));
        if (config.showCoords) {
            rows.add(HudPanel.Line.muted(Text.translatable("hud.pvp_hud_client.coords",
                    player.getX(), player.getY(), player.getZ(), facing(player.getHorizontalFacing()), biomeName(client))));
        }
        placeLineWidget(context, text, screenW, screenH, config, editorMode, HudWidgetId.STACK,
                Text.translatable("hud.pvp_hud_client.title"), rows);
    }

    private void placeChipWidget(DrawContext context, TextRenderer text, int screenW, int screenH, HudConfig config, boolean editorMode,
                                 HudWidgetId id, Text title, List<HudPanel.Chip> chips) {
        WidgetLayout layout = config.layout(id);
        if (!layout.visible && !editorMode) {
            return;
        }

        int x = toX(layout.x, screenW);
        int y = toY(layout.y, screenH);
        int width = HudPanel.chipPanelWidth(text, title, chips, config.compact);
        int height = HudPanel.chipPanelHeight(text, title, chips, config.compact);
        HudPanel.drawChips(context, text, x, y, title, chips, config, editorMode);
        addBounds(id, x, y, width, height, layout, editorMode);
    }

    private void placeLineWidget(DrawContext context, TextRenderer text, int screenW, int screenH, HudConfig config, boolean editorMode,
                                 HudWidgetId id, Text title, List<HudPanel.Line> lines) {
        WidgetLayout layout = config.layout(id);
        if (!layout.visible && !editorMode) {
            return;
        }

        int x = toX(layout.x, screenW);
        int y = toY(layout.y, screenH);
        int width = HudPanel.panelWidth(text, title, lines, config.compact);
        int height = HudPanel.panelHeight(text, title, lines, config.compact);
        HudPanel.draw(context, text, x, y, title, lines, config, editorMode);
        addBounds(id, x, y, width, height, layout, editorMode);
    }

    private void addBounds(HudWidgetId id, int x, int y, int width, int height, WidgetLayout layout, boolean editorMode) {
        if (layout.visible || editorMode) {
            lastBounds.add(new WidgetBounds(id, x, y, width, height));
        }
    }

    static int toX(float percent, int screenW) {
        return MathHelper.clamp(Math.round(percent * screenW), 0, Math.max(0, screenW - 40));
    }

    static int toY(float percent, int screenH) {
        return MathHelper.clamp(Math.round(percent * screenH), 0, Math.max(0, screenH - 30));
    }

    static float toPercentX(int x, int screenW) {
        return screenW <= 0 ? 0.0F : MathHelper.clamp(x / (float) screenW, 0.0F, 0.95F);
    }

    static float toPercentY(int y, int screenH) {
        return screenH <= 0 ? 0.0F : MathHelper.clamp(y / (float) screenH, 0.0F, 0.92F);
    }

    private static int ping(MinecraftClient client) {
        if (client.getNetworkHandler() == null || client.player == null) {
            return 0;
        }
        PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
        return entry == null ? 0 : entry.getLatency();
    }

    private static String speed(PlayerEntity player) {
        double dx = player.getX() - player.lastX;
        double dz = player.getZ() - player.lastZ;
        return String.format(Locale.ROOT, "%.2f b/s", Math.sqrt(dx * dx + dz * dz) * 20.0D);
    }

    private static String facing(Direction direction) {
        return switch (direction) {
            case NORTH -> "N";
            case SOUTH -> "S";
            case EAST -> "E";
            case WEST -> "W";
            default -> direction.asString().toUpperCase(Locale.ROOT);
        };
    }

    private static String biomeName(MinecraftClient client) {
        RegistryEntry<Biome> biome = client.world.getBiome(client.player.getBlockPos());
        return biome.getKey()
                .map(key -> key.getValue().getPath().replace('_', ' '))
                .orElse("unknown");
    }

    private static String durability(ItemStack stack) {
        int remaining = stack.getMaxDamage() - stack.getDamage();
        return remaining + "/" + stack.getMaxDamage() + " (" + durabilityPercent(stack) + "%)";
    }

    private static int durabilityPercent(ItemStack stack) {
        if (!stack.isDamageable()) {
            return 100;
        }
        int remaining = stack.getMaxDamage() - stack.getDamage();
        return MathHelper.floor((remaining / (float) stack.getMaxDamage()) * 100.0F);
    }

    private static boolean lowDurability(ItemStack stack) {
        return durabilityPercent(stack) < 20;
    }

    private TargetInfo targetInfo(MinecraftClient client) {
        HitResult hit = client.crosshairTarget;
        if (!(hit instanceof EntityHitResult entityHit) || !(entityHit.getEntity() instanceof PlayerEntity target)) {
            return null;
        }

        float health = target.getHealth() + target.getAbsorptionAmount();
        ItemStack weapon = target.getMainHandStack();
        String weaponName = weapon.isEmpty() ? "empty" : weapon.getName().getString();
        return new TargetInfo(target.getName().getString(), health, client.player.distanceTo(target), weaponName);
    }

    private Text effectLine(StatusEffectInstance effect) {
        String name = Text.translatable(effect.getTranslationKey()).getString();
        int amplifier = effect.getAmplifier() + 1;
        int seconds = effect.getDuration() / 20;
        return Text.literal(String.format(Locale.ROOT, "%s %d  %d:%02d", name, amplifier, seconds / 60, seconds % 60));
    }

    private record TargetInfo(String name, float health, float distance, String weapon) {
        List<HudPanel.Chip> chips() {
            return List.of(
                    HudPanel.Chip.of(Text.literal("HP "), Text.literal(String.format(Locale.ROOT, "%.1f", health))),
                    HudPanel.Chip.of(Text.literal("Dist "), Text.literal(String.format(Locale.ROOT, "%.1fm", distance))),
                    HudPanel.Chip.danger(Text.literal("Wpn "), Text.literal(weapon))
            );
        }
    }
}
