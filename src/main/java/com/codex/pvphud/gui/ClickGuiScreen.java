package com.codex.pvphud.gui;

import com.codex.pvphud.crosshair.CrosshairDrawerScreen;
import com.codex.pvphud.hud.HudEditor;
import com.codex.pvphud.hud.HudManager;
import com.codex.pvphud.hud.FeatureModule;
import com.codex.pvphud.theme.ThemeManager;
import com.codex.pvphud.theme.ThemeEditorScreen;
import com.codex.pvphud.waypoint.WaypointScreen;
import java.util.function.Function;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class ClickGuiScreen extends Screen {
    private enum Filter { ALL, HUD, UTILITIES, ENABLED }
    private final HudManager hudManager;
    private final Panel hudPanel;
    private final Runnable save;
    private final Function<Screen, Screen> settings;
    private final Screen parent;
    private final java.util.function.BooleanSupplier hudEnabled;
    private final Runnable toggleHud;
    private final Runnable restoreHud;
    private SearchBar search;
    private String query = "";
    private Filter filter = Filter.ALL;

    public ClickGuiScreen(HudManager hudManager, Runnable save, Function<Screen, Screen> settings,
                          Screen parent, java.util.function.BooleanSupplier hudEnabled, Runnable toggleHud, Runnable restoreHud) {
        super(Component.literal("PVPSE")); this.hudManager = hudManager; this.save = save; this.settings = settings;
        this.parent = parent; this.hudEnabled = hudEnabled; this.toggleHud = toggleHud; this.restoreHud = restoreHud;
        hudPanel = new Panel("MODULES", hudManager.elements());
    }
    private int sidebarWidth() { return Math.min(148, Math.max(100, width / 3)); }
    private int contentX() { return sidebarWidth() + 24; }
    private int contentWidth() { return width - contentX() - 12; }

    protected void init() {
        search = addRenderableWidget(new SearchBar(font, contentX(), 34, contentWidth()));
        search.setValue(query); search.setResponder(value -> query = value);
        int tabWidth = (contentWidth() - 9) / 4;
        for (Filter next : Filter.values()) {
            var button = addRenderableWidget(com.codex.pvphud.gui.PvpButton.styled(Component.literal(switch (next) { case ALL -> "ALL"; case HUD -> "HUD"; case UTILITIES -> "TOOLS"; case ENABLED -> "ON"; }), b -> {
                filter = next; applyFilter(); rebuildWidgets();
            }).bounds(contentX() + next.ordinal() * (tabWidth + 3), 59, tabWidth, 20).build());
            button.active = next != filter;
        }
        applyFilter();
        int step = Math.min(24, Math.max(16, (height - 90) / 8));
        var editor = action(38, "HUD editor", () -> minecraft.setScreen(new HudEditor(hudManager, this, save)));
        editor.active = minecraft.player != null;
        action(38 + step, "Crosshair studio", () -> minecraft.setScreen(new CrosshairDrawerScreen(this)));
        action(38 + step * 2, "Waypoints", () -> minecraft.setScreen(new WaypointScreen(this)));
        action(38 + step * 3, "Settings & profiles", () -> minecraft.setScreen(settings.apply(this)));
        action(38 + step * 4, "Theme: " + ThemeManager.getInstance().getTheme().displayName(), () -> {
            ThemeManager.getInstance().cycle(); save.run(); rebuildWidgets();
        });
        action(38 + step * 5, "Custom theme", () -> minecraft.setScreen(new ThemeEditorScreen(this)));
        action(38 + step * 6, "HUD: " + (hudEnabled.getAsBoolean() ? "ON" : "OFF"), () -> {
            toggleHud.run(); rebuildWidgets();
        });
        action(38 + step * 7, "RESTORE HUD", () -> minecraft.setScreen(new net.minecraft.client.gui.screens.ConfirmScreen(yes -> {
            if (yes) restoreHud.run();
            minecraft.setScreen(this);
        }, Component.literal("Restore HUD visibility?"), Component.literal("Turns the HUD on and repositions cards. Hidden cards stay hidden; choose them in HUD editor > HUDs."))));
        action(height - 28, "Done", this::onClose);
    }
    private void applyFilter() {
        hudPanel.setCategory(element -> switch (filter) {
            case ALL -> true;
            case HUD -> !(element instanceof FeatureModule) || ((FeatureModule) element).type() == FeatureModule.Type.WAYPOINT;
            case UTILITIES -> element instanceof FeatureModule;
            case ENABLED -> element.enabled();
        });
    }
    private Button action(int y, String label, Runnable action) {
        return addRenderableWidget(com.codex.pvphud.gui.PvpButton.styled(Component.literal(font.plainSubstrByWidth(label, sidebarWidth() - 10)), b -> action.run())
                .bounds(12, y, sidebarWidth(), Math.min(20, Math.max(15, (height - 90) / 8 - 1))).build());
    }
    public void extractRenderState(GuiGraphicsExtractor c, int mx, int my, float delta) {
        var theme = ThemeManager.getInstance().getTheme();
        c.fill(0, 0, width, height, theme.background());
        c.fill(0, 0, sidebarWidth() + 24, height, theme.panel());
        c.fill(sidebarWidth() + 23, 0, sidebarWidth() + 24, height, theme.panelHeader());
        c.text(font, Component.literal("PVPSE"), 16, 15, theme.accent());
        c.text(font, Component.literal(font.plainSubstrByWidth("CLIENT / HUD / PVP", contentWidth())), contentX(), 16, theme.mutedText());
        hudPanel.render(c, minecraft, theme, contentX(), 85, contentWidth(), height - 116, mx, my, query);
        if (height > 225) {
            String hint = minecraft.player == null ? "Join a world to see and edit the HUD" : "Click: toggle   |   Right-click / +: settings";
            c.text(font, Component.literal(font.plainSubstrByWidth(hint, contentWidth())), contentX(), height - 22, theme.mutedText());
        }
        if (height > 320) {
            long enabled = hudManager.elements().stream().filter(e -> e.enabled()).count();
            c.text(font, Component.literal(enabled + " modules enabled"), 16, height - 55, theme.mutedText());
        }
        super.extractRenderState(c, mx, my, delta);
    }
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (super.mouseClicked(click, doubled)) return true;
        return hudPanel.click(click.x(), click.y(), click.button(), query);
    }
    public boolean mouseScrolled(double x, double y, double horizontal, double vertical) { return hudPanel.scroll(x, y, vertical, query) || super.mouseScrolled(x, y, horizontal, vertical); }
    public boolean mouseDragged(MouseButtonEvent click, double dx, double dy) { return hudPanel.drag(click.x(), click.y(), query) || super.mouseDragged(click, dx, dy); }
    public boolean mouseReleased(MouseButtonEvent click) { hudPanel.release(); return super.mouseReleased(click); }
    @Override public boolean keyPressed(net.minecraft.client.input.KeyEvent input) {
        return com.codex.pvphud.FeatureKeybinds.capture(input, minecraft) || super.keyPressed(input);
    }
    public void removed() { com.codex.pvphud.FeatureKeybinds.cancel(); hudPanel.release(); save.run(); super.removed(); }
    public void onClose() { if (minecraft != null) minecraft.setScreen(parent); }
    public boolean isPauseScreen() { return false; }
}
