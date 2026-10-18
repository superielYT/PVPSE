package com.codex.pvphud.gui;

import com.codex.pvphud.crosshair.CrosshairDrawerScreen;
import com.codex.pvphud.hud.HudEditor;
import com.codex.pvphud.hud.HudManager;
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
    private final HudManager hudManager;
    private final Panel hudPanel;
    private final Runnable save;
    private final Function<Screen, Screen> settings;
    private SearchBar search;
    private String query = "";
    public ClickGuiScreen(HudManager hudManager, Runnable save, Function<Screen, Screen> settings) {
        super(Text.literal("PVPSE")); this.hudManager = hudManager; this.save = save; this.settings = settings;
        hudPanel = new Panel("MODULES", hudManager.elements());
    }
    private int panelWidth() { return (width - 36) / 2; }
    private int rightX() { return width - 12 - panelWidth(); }
    protected void init() {
        search = addDrawableChild(new SearchBar(textRenderer, 12, 12, panelWidth()));
        search.setText(query); search.setChangedListener(value -> query = value);
        int buttonWidth = (width - 36) / 3;
        action(12, 40, buttonWidth, "HUD EDITOR", () -> client.setScreen(new HudEditor(hudManager, this)));
        action(18 + buttonWidth, 40, buttonWidth, "CROSSHAIR", () -> client.setScreen(new CrosshairDrawerScreen(this)));
        action(24 + buttonWidth * 2, 40, buttonWidth, "WAYPOINTS", () -> client.setScreen(new WaypointScreen(this)));
        action(rightX(), 72, panelWidth(), "Theme: " + ThemeManager.getInstance().getTheme().displayName(), () -> { ThemeManager.getInstance().cycle(); save.run(); clearAndInit(); });
        action(rightX(), 98, panelWidth(), "EDIT CUSTOM THEME", () -> client.setScreen(new ThemeEditorScreen(this)));
        action(rightX(), 124, panelWidth(), "SETTINGS & PROFILES", () -> client.setScreen(settings.apply(this)));
        action(rightX(), height - 28, panelWidth(), "DONE", this::close);
    }
    private void action(int x, int y, int w, String label, Runnable action) {
        addDrawableChild(ButtonWidget.builder(Text.literal(label), b -> action.run()).dimensions(x, y, w, 20).build());
    }
    public void render(DrawContext c, int mx, int my, float delta) {
        var theme = ThemeManager.getInstance().getTheme(); c.fill(0, 0, width, height, theme.background());
        c.drawTextWithShadow(textRenderer, Text.literal("PVPSE  1.8.0"), rightX(), 18, theme.accent());
        hudPanel.render(c, client, theme, 12, 72, panelWidth(), height - 84, mx, my, query);
        if (height >= 230) {
            c.drawTextWithShadow(textRenderer, Text.literal("Left-click: toggle"), rightX(), 160, theme.mutedText());
            c.drawTextWithShadow(textRenderer, Text.literal("Right-click: settings"), rightX(), 174, theme.mutedText());
            long enabled = hudManager.elements().stream().filter(e -> e.enabled()).count();
            c.drawTextWithShadow(textRenderer, Text.literal(enabled + " / " + hudManager.elements().size() + " enabled"), rightX(), 194, theme.accent());
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
