package com.codex.pvphud.hud;

import com.codex.pvphud.theme.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import com.codex.pvphud.waypoint.WaypointManager;

import java.util.Locale;

public final class FeatureModule extends HudElement {
    public enum Type { WAYPOINT, ZOOM, NO_PUMPKIN, FULLBRIGHT, MELEE_HITBOXES }

    private final Type type;
    private double value;
    private final double defaultValue;

    public FeatureModule(Type type, String name, float x, float y, double value) {
        super("feature_" + type.name().toLowerCase(Locale.ROOT), name, x, y);
        this.type = type;
        this.value = value;
        this.defaultValue = value;
    }

    protected void renderElement(DrawContext context, MinecraftClient client, Theme theme, boolean editorMode, float alpha) {
        if (type != Type.WAYPOINT && !editorMode) return;
        HudStyles.card(context, theme, width(client), height(client), alpha);
        context.drawTextWithShadow(client.textRenderer, Text.literal(nameText()), 7, 7, theme.text());
        if (type == Type.WAYPOINT) {
            String detail = "";
            context.drawTextWithShadow(client.textRenderer, Text.literal(detail), 7, 19, theme.accent());
            com.codex.pvphud.waypoint.WaypointRadar.render(context, client, theme);
        }
    }

    public int width(MinecraftClient client) { return type == Type.WAYPOINT ? 144 : 104; }
    public int height(MinecraftClient client) { return type == Type.WAYPOINT ? 154 : 22; }
    public Type type() { return type; }
    public boolean adjustable() { return type == Type.ZOOM || type == Type.FULLBRIGHT || type == Type.MELEE_HITBOXES; }
    public String valueName() {
        return switch (type) {
            case ZOOM -> "Zoom FOV";
            case FULLBRIGHT -> "Brightness";
            case MELEE_HITBOXES -> "View range";
            default -> "Value";
        };
    }
    public double minimum() { return type == Type.ZOOM ? 10.0 : type == Type.MELEE_HITBOXES ? 2.0 : 1.0; }
    public double maximum() { return type == Type.ZOOM ? 70.0 : type == Type.MELEE_HITBOXES ? 12.0 : 16.0; }
    public double value() { return value; }
    @Override public void resetSettings() { super.resetSettings(); value = defaultValue; }
    public void setValue(double value) { if (Double.isFinite(value)) this.value = Math.clamp(value, minimum(), maximum()); }

    private String nameText() {
        return switch (type) {
            case WAYPOINT -> "WAYPOINT";
            case ZOOM -> "ZOOM";
            case NO_PUMPKIN -> "NO PUMPKIN";
            case FULLBRIGHT -> "FULLBRIGHT";
            case MELEE_HITBOXES -> "MELEE HITBOXES";
        };
    }

}
