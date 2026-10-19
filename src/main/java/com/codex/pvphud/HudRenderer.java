package com.codex.pvphud;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

final class HudRenderer {
    private final List<WidgetBounds> lastBounds = new ArrayList<>();

    List<WidgetBounds> lastBounds() {
        return lastBounds;
    }

    List<WidgetBounds> render(GuiGraphics context, Minecraft client, HudConfig config, CpsTracker cps, boolean editorMode) {
        lastBounds.clear();
        if (client.player == null || client.level == null || !config.enabled) {
            return lastBounds;
        }

        Font text = client.font;
        int screenW = context.guiWidth();
        int screenH = context.guiHeight();
        Player player = client.player;

        if (config.useFreeLayout) {
            drawCombat(context, text, screenW, screenH, config, editorMode, client, cps);
            drawPlayer(context, text, screenW, screenH, config, editorMode, player);
            if (config.showCoords) {
                drawLocation(context, text, screenW, screenH, config, editorMode, player, client);
            }
            ItemStack mainHand = player.getMainHandItem();
            if (!mainHand.isEmpty() && mainHand.isDamageableItem()) {
                drawHeld(context, text, screenW, screenH, config, editorMode, mainHand);
            }
            if (config.showTarget) {
                TargetInfo target = targetInfo(client);
                if (target != null) {
                    drawTarget(context, text, screenW, screenH, config, editorMode, target);
                }
            }
            if (config.showPotions && !player.getActiveEffects().isEmpty()) {
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

    private void drawCombat(GuiGraphics context, Font text, int screenW, int screenH, HudConfig config, boolean editorMode,
                            Minecraft client, CpsTracker cps) {
        Component title = Component.translatable("hud.pvp_hud_client.widget.combat");
        List<HudPanel.Chip> chips = new ArrayList<>();
        chips.add(HudPanel.Chip.of(Component.literal("FPS "), Component.literal(String.valueOf(client.getFps()))));
        chips.add(HudPanel.Chip.of(Component.literal("Ping "), Component.literal(ping(client) + "ms")));
        if (config.showCps) {
            String cpsValue = config.showRightCps ? cps.leftCps() + "/" + cps.rightCps() : String.valueOf(cps.leftCps());
            chips.add(HudPanel.Chip.of(Component.literal("CPS "), Component.literal(cpsValue)));
        }
        placeChipWidget(context, text, screenW, screenH, config, editorMode, HudWidgetId.COMBAT, title, chips);
    }

    private void drawPlayer(GuiGraphics context, Font text, int screenW, int screenH, HudConfig config, boolean editorMode, Player player) {
        WidgetLayout layout = config.layout(HudWidgetId.PLAYER);
        if (!layout.visible && !editorMode) {
            return;
        }

        float health = player.getHealth() + player.getAbsorptionAmount();
        float maxHealth = Math.max(1.0F, player.getMaxHealth());
        float hpPercent = Mth.clamp(health / maxHealth, 0.0F, 1.0F);
        boolean lowHp = hpPercent < 0.25F;

        Component title = Component.translatable("hud.pvp_hud_client.widget.player");
        List<HudPanel.Chip> chips = List.of(
                HudPanel.Chip.of(Component.literal("HP "), Component.literal(String.format(Locale.ROOT, "%.1f", health))),
                HudPanel.Chip.of(Component.literal("Spd "), Component.literal(speed(player)))
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
            context.drawString(text, Component.literal(Math.round(hpPercent * 100) + "%"), x + width - 28, barY - 1, lowHp ? config.dangerColor() : config.mutedColor(), false);
        }
        addBounds(HudWidgetId.PLAYER, x, y, width, height, layout, editorMode);
    }

    private void drawLocation(GuiGraphics context, Font text, int screenW, int screenH, HudConfig config, boolean editorMode,
                                Player player, Minecraft client) {
        Component title = Component.translatable("hud.pvp_hud_client.widget.location");
        List<HudPanel.Chip> chips = List.of(
                HudPanel.Chip.of(Component.literal("X "), Component.literal(String.format(Locale.ROOT, "%.0f", player.getX()))),
                HudPanel.Chip.of(Component.literal("Y "), Component.literal(String.format(Locale.ROOT, "%.0f", player.getY()))),
                HudPanel.Chip.of(Component.literal("Z "), Component.literal(String.format(Locale.ROOT, "%.0f", player.getZ()))),
                HudPanel.Chip.of(Component.literal("Biome "), Component.literal(biomeName(client)))
        );
        placeChipWidget(context, text, screenW, screenH, config, editorMode, HudWidgetId.LOCATION, title, chips);
    }

    private void drawHeld(GuiGraphics context, Font text, int screenW, int screenH, HudConfig config, boolean editorMode, ItemStack stack) {
        WidgetLayout layout = config.layout(HudWidgetId.HELD);
        if (!layout.visible && !editorMode) {
            return;
        }

        boolean low = lowDurability(stack);
        int percent = durabilityPercent(stack);
        Component title = Component.translatable("hud.pvp_hud_client.widget.held");
        List<HudPanel.Line> lines = List.of(low
                ? HudPanel.Line.danger(Component.translatable("hud.pvp_hud_client.held", stack.getHoverName().getString(), durability(stack)))
                : HudPanel.Line.normal(Component.translatable("hud.pvp_hud_client.held", stack.getHoverName().getString(), durability(stack))));

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
            context.drawString(text, Component.literal(percent + "%"), x + width - 28, barY - 1, low ? config.dangerColor() : config.mutedColor(), false);
        }
        addBounds(HudWidgetId.HELD, x, y, width, height, layout, editorMode);
    }

    private void drawTarget(GuiGraphics context, Font text, int screenW, int screenH, HudConfig config, boolean editorMode, TargetInfo target) {
        placeChipWidget(context, text, screenW, screenH, config, editorMode, HudWidgetId.TARGET,
                Component.translatable("hud.pvp_hud_client.widget.target"), target.chips());
    }

    private void drawEffects(GuiGraphics context, Font text, int screenW, int screenH, HudConfig config, boolean editorMode, Player player) {
        List<HudPanel.Line> effects = player.getActiveEffects().stream()
                .sorted(Comparator.comparing(effect -> effect.getEffect().value().getDescriptionId()))
                .limit(5)
                .map(this::effectLine)
                .map(HudPanel.Line::muted)
                .toList();
        placeLineWidget(context, text, screenW, screenH, config, editorMode, HudWidgetId.EFFECTS,
                Component.translatable("hud.pvp_hud_client.widget.effects"), effects);
    }

    private void drawArmor(GuiGraphics context, Font text, int screenW, int screenH, HudConfig config, boolean editorMode, Player player) {
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
        context.drawString(text, Component.translatable("hud.pvp_hud_client.widget.armor"), x + 6, y + 4, config.accentColor(), false);
        if (editorMode) {
            HudPanel.drawCloseButton(context, x + width - 10, y + 3, false);
        }
        context.fill(x + 6, y + 14, x + width - 6, y + 15, 0x33FFFFFF);

        int slot = 0;
        for (EquipmentSlot armorSlot : List.of(EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD)) {
            ItemStack stack = player.getItemBySlot(armorSlot);
            int itemX = x + 6 + slot * slotWidth;
            if (!stack.isEmpty()) {
                context.renderItem(stack, itemX, y + 17);
                context.renderItemDecorations(text, stack, itemX, y + 17);
                if (stack.isDamageableItem()) {
                    int percent = durabilityPercent(stack);
                    boolean low = percent < 20;
                    if (config.showDurabilityBars) {
                        HudPanel.drawProgressBar(context, itemX, y + 34, 18, 3, percent / 100.0F, config, low);
                        context.drawString(text, Component.literal(percent + "%"), itemX, y + 39, low ? config.dangerColor() : config.mutedColor(), false);
                    }
                }
            }
            slot++;
        }

        addBounds(HudWidgetId.ARMOR, x, y, width, height, layout, editorMode);
    }

    private void drawStack(GuiGraphics context, Font text, int screenW, int screenH, HudConfig config, boolean editorMode,
                           Minecraft client, Player player, CpsTracker cps) {
        List<HudPanel.Line> rows = new ArrayList<>();
        rows.add(HudPanel.Line.normal(Component.translatable("hud.pvp_hud_client.stats_short",
                client.getFps(), ping(client), cps.leftCps(), cps.rightCps())));
        rows.add(HudPanel.Line.normal(Component.translatable("hud.pvp_hud_client.player_short",
                String.format(Locale.ROOT, "%.1f", player.getHealth() + player.getAbsorptionAmount()), speed(player))));
        if (config.showCoords) {
            rows.add(HudPanel.Line.muted(Component.translatable("hud.pvp_hud_client.coords",
                    player.getX(), player.getY(), player.getZ(), facing(player.getDirection()), biomeName(client))));
        }
        placeLineWidget(context, text, screenW, screenH, config, editorMode, HudWidgetId.STACK,
                Component.translatable("hud.pvp_hud_client.title"), rows);
    }

    private void placeChipWidget(GuiGraphics context, Font text, int screenW, int screenH, HudConfig config, boolean editorMode,
                                 HudWidgetId id, Component title, List<HudPanel.Chip> chips) {
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

    private void placeLineWidget(GuiGraphics context, Font text, int screenW, int screenH, HudConfig config, boolean editorMode,
                                 HudWidgetId id, Component title, List<HudPanel.Line> lines) {
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
        return Mth.clamp(Math.round(percent * screenW), 0, Math.max(0, screenW - 40));
    }

    static int toY(float percent, int screenH) {
        return Mth.clamp(Math.round(percent * screenH), 0, Math.max(0, screenH - 30));
    }

    static float toPercentX(int x, int screenW) {
        return screenW <= 0 ? 0.0F : Mth.clamp(x / (float) screenW, 0.0F, 0.95F);
    }

    static float toPercentY(int y, int screenH) {
        return screenH <= 0 ? 0.0F : Mth.clamp(y / (float) screenH, 0.0F, 0.92F);
    }

    private static int ping(Minecraft client) {
        if (client.getConnection() == null || client.player == null) {
            return 0;
        }
        PlayerInfo entry = client.getConnection().getPlayerInfo(client.player.getUUID());
        return entry == null ? 0 : entry.getLatency();
    }

    private static String speed(Player player) {
        double dx = player.getX() - player.xo;
        double dz = player.getZ() - player.zo;
        return String.format(Locale.ROOT, "%.2f b/s", Math.sqrt(dx * dx + dz * dz) * 20.0D);
    }

    private static String facing(Direction direction) {
        return switch (direction) {
            case NORTH -> "N";
            case SOUTH -> "S";
            case EAST -> "E";
            case WEST -> "W";
            default -> direction.getSerializedName().toUpperCase(Locale.ROOT);
        };
    }

    private static String biomeName(Minecraft client) {
        Holder<Biome> biome = client.level.getBiome(client.player.blockPosition());
        return biome.unwrapKey()
                .map(key -> key.identifier().getPath().replace('_', ' '))
                .orElse("unknown");
    }

    private static String durability(ItemStack stack) {
        int remaining = stack.getMaxDamage() - stack.getDamageValue();
        return remaining + "/" + stack.getMaxDamage() + " (" + durabilityPercent(stack) + "%)";
    }

    private static int durabilityPercent(ItemStack stack) {
        if (!stack.isDamageableItem()) {
            return 100;
        }
        int remaining = stack.getMaxDamage() - stack.getDamageValue();
        return Mth.floor((remaining / (float) stack.getMaxDamage()) * 100.0F);
    }

    private static boolean lowDurability(ItemStack stack) {
        return durabilityPercent(stack) < 20;
    }

    private TargetInfo targetInfo(Minecraft client) {
        HitResult hit = client.hitResult;
        if (!(hit instanceof EntityHitResult entityHit) || !(entityHit.getEntity() instanceof Player target)) {
            return null;
        }

        float health = target.getHealth() + target.getAbsorptionAmount();
        ItemStack weapon = target.getMainHandItem();
        String weaponName = weapon.isEmpty() ? "empty" : weapon.getHoverName().getString();
        return new TargetInfo(target.getName().getString(), health, client.player.distanceTo(target), weaponName);
    }

    private Component effectLine(MobEffectInstance effect) {
        String name = Component.translatable(effect.getDescriptionId()).getString();
        int amplifier = effect.getAmplifier() + 1;
        int seconds = effect.getDuration() / 20;
        return Component.literal(String.format(Locale.ROOT, "%s %d  %d:%02d", name, amplifier, seconds / 60, seconds % 60));
    }

    private record TargetInfo(String name, float health, float distance, String weapon) {
        List<HudPanel.Chip> chips() {
            return List.of(
                    HudPanel.Chip.of(Component.literal("HP "), Component.literal(String.format(Locale.ROOT, "%.1f", health))),
                    HudPanel.Chip.of(Component.literal("Dist "), Component.literal(String.format(Locale.ROOT, "%.1fm", distance))),
                    HudPanel.Chip.danger(Component.literal("Wpn "), Component.literal(weapon))
            );
        }
    }
}
