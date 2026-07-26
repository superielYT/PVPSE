package com.codex.pvphud.hud;

import com.codex.pvphud.theme.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.Locale;

public final class FeatureModule extends HudElement {
    public enum Type { WAYPOINT, ZOOM, NO_PUMPKIN, FULLBRIGHT }

    private final Type type;
    private double value;
    private BlockPos waypoint;

    public FeatureModule(Type type, String name, float x, float y, double value) {
        super("feature_" + type.name().toLowerCase(Locale.ROOT), name, x, y);
        this.type = type;
        this.value = value;
    }

    protected void renderElement(DrawContext context, MinecraftClient client, Theme theme, boolean editorMode, float alpha) {
        if (type != Type.WAYPOINT && !editorMode) return;
        HudStyles.card(context, theme, width(client), height(client), alpha);
        context.drawTextWithShadow(client.textRenderer, Text.literal(nameText()), 7, 7, theme.text());
        if (type == Type.WAYPOINT) {
            String detail = waypoint == null ? "Press B to set" : distanceText(client);
            context.drawTextWithShadow(client.textRenderer, Text.literal(detail), 7, 19, theme.accent());
        }
    }

    public int width(MinecraftClient client) { return type == Type.WAYPOINT ? 122 : 104; }
    public int height(MinecraftClient client) { return type == Type.WAYPOINT ? 34 : 22; }
    public Type type() { return type; }
    public boolean adjustable() { return type == Type.ZOOM || type == Type.FULLBRIGHT; }
    public String valueName() { return type == Type.ZOOM ? "Zoom FOV" : "Brightness"; }
    public double minimum() { return type == Type.ZOOM ? 10.0 : 1.0; }
    public double maximum() { return type == Type.ZOOM ? 70.0 : 16.0; }
    public double value() { return value; }
    public void setValue(double value) { this.value = Math.clamp(value, minimum(), maximum()); }
    public BlockPos waypoint() { return waypoint; }
    public void setWaypoint(BlockPos waypoint) { this.waypoint = waypoint; }

    private String nameText() {
        return switch (type) {
            case WAYPOINT -> "WAYPOINT";
            case ZOOM -> "ZOOM";
            case NO_PUMPKIN -> "NO PUMPKIN";
            case FULLBRIGHT -> "FULLBRIGHT";
        };
    }

    private String distanceText(MinecraftClient client) {
        if (client.player == null || waypoint == null) return "--";
        double distance = Math.sqrt(client.player.squaredDistanceTo(
                waypoint.getX() + 0.5, waypoint.getY() + 0.5, waypoint.getZ() + 0.5));
        return String.format(Locale.ROOT, "%.0fm  X%d Z%d", distance, waypoint.getX(), waypoint.getZ());
    }
}
