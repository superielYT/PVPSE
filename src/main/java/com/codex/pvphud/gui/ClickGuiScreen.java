package com.codex.pvphud.gui;

import com.codex.pvphud.crosshair.CrosshairDrawerScreen;
import com.codex.pvphud.hud.HudEditor;
import com.codex.pvphud.hud.HudManager;
import com.codex.pvphud.hud.FeatureModule;
import com.codex.pvphud.theme.ThemeManager;
import com.codex.pvphud.theme.ThemeEditorScreen;
import com.codex.pvphud.waypoint.WaypointScreen;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import java.util.function.Function;

public final class ClickGuiScreen extends Screen {
    private enum Filter { ALL, HUD, UTILITIES, ENABLED }
    private final HudManager hudManager;
    private final Panel hudPanel;
    private final Runnable save;
    private final Function<Screen, Screen> settings;
    private SearchBar search;
    private String query = "";
    private Filter filter = Filter.ALL;

    public ClickGuiScreen(HudManager hudManager, Runnable save, Function<Screen, Screen> settings) {
        super(Text.literal("PVPSE")); this.hudManager = hudManager; this.save = save; this.settings = settings;
        hudPanel = new Panel("MODULES", hudManager.elements());
    }
    private int sidebarWidth() { return Math.min(148, Math.max(100, width / 3)); }
    private int contentX() { return sidebarWidth() + 24; }
    private int contentWidth() { return width - contentX() - 12; }

    protected void init() {
        search = addDrawableChild(new SearchBar(textRenderer, contentX(), 34, contentWidth()));
        search.setText(query); search.setChangedListener(value -> query = value);
        int tabWidth = (contentWidth() - 9) / 4;
        for (Filter next : Filter.values()) {
            var button = addDrawableChild(ButtonWidget.builder(Text.literal(switch (next) { case ALL -> "ALL"; case HUD -> "HUD"; case UTILITIES -> "TOOLS"; case ENABLED -> "ON"; }), b -> {
                filter = next; applyFilter(); clearAndInit();
            }).dimensions(contentX() + next.ordinal() * (tabWidth + 3), 59, tabWidth, 20).build());
            button.active = next != filter;
        }
        applyFilter();
        action(42, "HUD editor", () -> client.setScreen(new HudEditor(hudManager, this)));
        action(66, "Crosshair studio", () -> client.setScreen(new CrosshairDrawerScreen(this)));
        action(90, "Waypoints", () -> client.setScreen(new WaypointScreen(this)));
        action(122, "Settings & profiles", () -> client.setScreen(settings.apply(this)));
        action(146, "Theme: " + ThemeManager.getInstance().getTheme().displayName(), () -> {
            ThemeManager.getInstance().cycle(); save.run(); clearAndInit();
        });
        action(170, "Custom theme", () -> client.setScreen(new ThemeEditorScreen(this)));
        action(height - 28, "Done", this::close);
    }
    private void applyFilter() {
        hudPanel.setCategory(element -> switch (filter) {
            case ALL -> true;
            case HUD -> !(element instanceof FeatureModule) || ((FeatureModule) element).type() == FeatureModule.Type.WAYPOINT;
            case UTILITIES -> element instanceof FeatureModule;
            case ENABLED -> element.enabled();
        });
    }
    private void action(int y, String label, Runnable action) {
        addDrawableChild(ButtonWidget.builder(Text.literal(textRenderer.trimToWidth(label, sidebarWidth() - 10)), b -> action.run())
                .dimensions(12, y, sidebarWidth(), 20).build());
    }
    public void render(DrawContext c, int mx, int my, float delta) {
        var theme = ThemeManager.getInstance().getTheme();
        c.fill(0, 0, width, height, theme.background());
        c.fill(0, 0, sidebarWidth() + 24, height, theme.panel());
        c.fill(sidebarWidth() + 23, 0, sidebarWidth() + 24, height, theme.panelHeader());
        c.drawTextWithShadow(textRenderer, Text.literal("PVPSE"), 16, 15, theme.accent());
        c.drawTextWithShadow(textRenderer, Text.literal(textRenderer.trimToWidth("CUSTOMIZE YOUR CLIENT", contentWidth())), contentX(), 16, theme.mutedText());
        hudPanel.render(c, client, theme, contentX(), 85, contentWidth(), height - 116, mx, my, query);
        if (height > 225) {
            String hint = "Click: toggle   |   Right-click / +: settings";
            c.drawTextWithShadow(textRenderer, Text.literal(textRenderer.trimToWidth(hint, contentWidth())), contentX(), height - 22, theme.mutedText());
        }
        if (height > 280) {
            long enabled = hudManager.elements().stream().filter(e -> e.enabled()).count();
            c.drawTextWithShadow(textRenderer, Text.literal(enabled + " modules enabled"), 16, 206, theme.mutedText());
        }
        super.render(c, mx, my, delta);
    }
    public boolean mouseClicked(Click click, boolean doubled) {
        if (super.mouseClicked(click, doubled)) return true;
        return hudPanel.click(click.x(), click.y(), click.button(), query);
    }
    public boolean mouseScrolled(double x, double y, double horizontal, double vertical) { return hudPanel.scroll(x, y, vertical, query) || super.mouseScrolled(x, y, horizontal, vertical); }
    public boolean mouseDragged(Click click, double dx, double dy) { return hudPanel.drag(click.x(), click.y(), query) || super.mouseDragged(click, dx, dy); }
    public boolean mouseReleased(Click click) { hudPanel.release(); return super.mouseReleased(click); }
    @Override public boolean keyPressed(net.minecraft.client.input.KeyInput input) {
        return com.codex.pvphud.FeatureKeybinds.capture(input, client) || super.keyPressed(input);
    }
    public void removed() { com.codex.pvphud.FeatureKeybinds.cancel(); hudPanel.release(); save.run(); super.removed(); }
    public boolean shouldPause() { return false; }
}
