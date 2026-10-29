package com.codex.pvphud.waypoint;

import com.codex.pvphud.theme.ThemeManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class WaypointScreen extends Screen {
    private final Screen parent;
    private final WaypointManager manager = WaypointManager.getInstance();
    private int scroll, selected = -1;
    private EditBox nameField;
    private String draft = "";
    public WaypointScreen(Screen parent) { super(Component.literal("WAYPOINTS & RADAR")); this.parent = parent; }
    private int panelWidth() { return Math.min(420, width - 16); }
    private int left() { return (width - panelWidth()) / 2; }
    protected void init() {
        nameField = addRenderableWidget(new EditBox(font, left(), 34, panelWidth(), 20, Component.literal("Waypoint name")));
        nameField.setMaxLength(24);
        nameField.setValue(draft);
        nameField.setHint(Component.literal("Name your waypoint..."));
        nameField.setResponder(value -> draft = value);
        row(60, new String[]{"ADD HERE", "RENAME", "DELETE", "TRACK"}, new Runnable[]{
            () -> { var point = manager.addCurrent(minecraft, nameField.getValue()); if (point != null) { selected = manager.waypoints().size() - 1; nameField.setValue(""); } },
            () -> manager.rename(selected, nameField.getValue()),
            () -> { manager.remove(selected); selected = -1; clamp(); },
            () -> { if (valid()) manager.track(selected); }});
        row(84, new String[]{"SHOW / HIDE", "COLOR", "COPY XYZ", "CLEAR ALL"}, new Runnable[]{
            () -> manager.toggleHidden(selected), () -> manager.cycleColor(selected),
            () -> { if (valid()) { var p = manager.waypoints().get(selected); minecraft.keyboardHandler.setClipboard(p.x() + " " + p.y() + " " + p.z()); } },
            () -> minecraft.setScreen(new ConfirmScreen(yes -> { if (yes) { manager.clear(); selected = -1; } minecraft.setScreen(this); }, Component.literal("Clear all waypoints?"), Component.literal("This removes saved waypoints from every world.")))});
        row(108, new String[]{"RANGE " + manager.mapRange() + "m", manager.northUp() ? "NORTH UP" : "ROTATING", manager.labels() ? "LABELS ON" : "LABELS OFF", manager.deathMarkers() ? "DEATH ON" : "DEATH OFF"}, new Runnable[]{
            () -> { manager.cycleRange(); rebuildWidgets(); }, () -> { manager.toggleNorth(); rebuildWidgets(); },
            () -> { manager.toggleLabels(); rebuildWidgets(); }, () -> { manager.toggleDeaths(); rebuildWidgets(); }});
        addRenderableWidget(com.codex.pvphud.gui.PvpButton.styled(Component.literal("Done"), b -> onClose()).bounds(left(), height - 26, panelWidth(), 20).build());
        clamp();
    }
    private boolean valid() { return selected >= 0 && selected < manager.waypoints().size(); }
    private void row(int y, String[] labels, Runnable[] actions) {
        int w = (panelWidth() - 12) / 4;
        for (int i = 0; i < labels.length; i++) { final int index = i;
            addRenderableWidget(com.codex.pvphud.gui.PvpButton.styled(Component.literal(labels[i]), b -> actions[index].run()).bounds(left() + i * (w + 4), y, w, 20).build());
        }
    }
    private void clamp() { scroll = Math.clamp(scroll, 0, Math.max(0, manager.waypoints().size() * 40 - Math.max(0, height - 174))); }
    public void extractRenderState(GuiGraphicsExtractor c, int mx, int my, float delta) {
        var t = ThemeManager.getInstance().getTheme();
        c.fill(0, 0, width, height, t.background());
        c.centeredText(font, Component.literal("WAYPOINTS / Shift+B: temporary look target (5 min)"), width / 2, 15, t.accent());
        clamp();
        if (height > 174) {
            c.enableScissor(left(), 138, left() + panelWidth(), height - 36);
            var points = manager.waypoints();
            for (int i = 0; i < points.size(); i++) {
                var p = points.get(i); int y = 140 + i * 40 - scroll;
                c.fill(left(), y, left() + panelWidth() - 5, y + 36, selected == i ? t.panelHeader() : t.panel());
                c.fill(left(), y, left() + 3, y + 36, p.tint());
                String name = (manager.tracked(p) ? "> " : "") + p.name() + (p.hidden() ? " [hidden]" : "");
                c.text(font, Component.literal(font.plainSubstrByWidth(name, panelWidth() - 20)), left() + 9, y + 5, p.tint());
                String detail = p.x() + ", " + p.y() + ", " + p.z() + " | " + p.dimension().replace("minecraft:", "") + (p.world() == null ? " | legacy" : " | " + (p.world().startsWith("server:") ? p.world().substring(7) : "singleplayer"));
                c.text(font, Component.literal(font.plainSubstrByWidth(detail, panelWidth() - 20)), left() + 9, y + 21, t.mutedText());
            }
            if (points.isEmpty()) c.centeredText(font, Component.literal("Press ADD HERE to save your location"), width / 2, 148, t.mutedText());
            c.disableScissor();
            int available = height - 174, total = points.size() * 40;
            if (total > available) { int thumb = Math.max(12, available * available / total); int y = 138 + (available - thumb) * scroll / Math.max(1, total - available); c.fill(left() + panelWidth() - 3, y, left() + panelWidth(), y + thumb, t.accent()); }
        }
        super.extractRenderState(c, mx, my, delta);
    }
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (super.mouseClicked(click, doubled)) return true;
        if (click.button() == 0 && click.x() >= left() && click.x() < left() + panelWidth() && click.y() >= 140 && click.y() < height - 36) {
            int index = (int) (click.y() - 140 + scroll) / 40;
            if (index >= 0 && index < manager.waypoints().size() && (click.y() - 140 + scroll) % 40 < 36) { selected = index; nameField.setValue(manager.waypoints().get(index).name()); return true; }
        }
        return false;
    }
    public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
        if (x < left() || x >= left() + panelWidth() || y < 138 || y >= height - 36) return super.mouseScrolled(x, y, horizontal, vertical);
        scroll -= (int) Math.round(vertical * 28); clamp(); return true;
    }
    public void onClose() { if (minecraft != null) minecraft.setScreen(parent); }
    public boolean isPauseScreen() { return false; }
}
