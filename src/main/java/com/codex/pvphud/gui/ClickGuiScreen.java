package com.codex.pvphud.gui;

import com.codex.pvphud.animation.ScaleAnimation;
import com.codex.pvphud.crosshair.CrosshairDrawerScreen;
import com.codex.pvphud.hud.HudEditor;
import com.codex.pvphud.hud.HudManager;
import com.codex.pvphud.theme.Theme;
import com.codex.pvphud.theme.ThemeManager;
import com.codex.pvphud.theme.ThemeEditorScreen;
import com.codex.pvphud.waypoint.WaypointScreen;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public final class ClickGuiScreen extends Screen {
    private final HudManager hudManager;
    private final Panel hudPanel;
    private final ThemeSelector themeSelector = new ThemeSelector();
    private final ScaleAnimation opening = new ScaleAnimation(0.88F);
    private SearchBar search;
    private int editorButtonX;
    private int editorButtonY;
    private int editorButtonWidth = 100;
    private final Runnable save;
    private int crosshairButtonX;
    private int waypointButtonX;

    public ClickGuiScreen(HudManager hudManager, Runnable save) {
        super(Text.literal("PVPSE Client"));
        this.hudManager = hudManager;
        this.save = save;
        this.hudPanel = new Panel("HUD ELEMENTS", hudManager.elements());
    }

    protected void init() {
        opening.snap(0.88F);
        opening.animateTo(1.0F);
        search = addDrawableChild(new SearchBar(textRenderer, 18, 18, Math.min(170, width - 36)));
        editorButtonWidth = Math.min(100, Math.max(40, (width - 52) / 3));
        editorButtonX = width - editorButtonWidth - 18;
        crosshairButtonX = editorButtonX - editorButtonWidth - 8;
        waypointButtonX = crosshairButtonX - editorButtonWidth - 8;
        editorButtonY = 44;
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Theme theme = ThemeManager.getInstance().getTheme();
        context.fill(0, 0, width, height, theme.background());
        boolean editorHovered = mouseX >= editorButtonX && mouseX < editorButtonX + editorButtonWidth
                && mouseY >= editorButtonY && mouseY < editorButtonY + 24;
        com.codex.pvphud.render.RoundedRenderer.fill(context, editorButtonX, editorButtonY,
                editorButtonWidth, 24, 6, editorHovered ? theme.accentSecondary() : theme.accent());
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("HUD EDITOR"),
                editorButtonX + editorButtonWidth / 2, editorButtonY + 8, 0xFF101216);
        boolean crosshairHovered = mouseX >= crosshairButtonX && mouseX < crosshairButtonX + editorButtonWidth
                && mouseY >= editorButtonY && mouseY < editorButtonY + 24;
        com.codex.pvphud.render.RoundedRenderer.fill(context, crosshairButtonX, editorButtonY,
                editorButtonWidth, 24, 6, crosshairHovered ? theme.accentSecondary() : theme.panelHeader());
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("CROSSHAIR"),
                crosshairButtonX + editorButtonWidth / 2, editorButtonY + 8, theme.text());
        boolean waypointHovered = mouseX >= waypointButtonX && mouseX < waypointButtonX + editorButtonWidth
                && mouseY >= editorButtonY && mouseY < editorButtonY + 24;
        com.codex.pvphud.render.RoundedRenderer.fill(context, waypointButtonX, editorButtonY,
                editorButtonWidth, 24, 6, waypointHovered ? theme.accentSecondary() : theme.panelHeader());
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("WAYPOINTS"),
                waypointButtonX + editorButtonWidth / 2, editorButtonY + 8, theme.text());
        context.getMatrices().pushMatrix();
        float scale = opening.value();

        // Keep controls aligned with their mouse hitboxes during opening.
        hudPanel.render(context, client, theme, 18, 76, panelWidth(), height - 94, mouseX, mouseY,
                search == null ? "" : search.getText());
        themeSelector.render(context, client, rightPanelX(), 76, panelWidth(), mouseX, mouseY);
        boolean themeEditHover = mouseX >= rightPanelX() && mouseX < width - 18 && mouseY >= 114 && mouseY < 138;
        com.codex.pvphud.render.RoundedRenderer.fill(context, rightPanelX(), 114, panelWidth(), 24, 6,
                themeEditHover ? theme.accentSecondary() : theme.panelHeader());
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("EDIT CUSTOM THEME"),
                rightPanelX() + panelWidth() / 2, 122, theme.text());
        context.getMatrices().popMatrix();
        super.render(context, mouseX, mouseY, delta);
    }

    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() == 0 && click.x() >= editorButtonX && click.x() < editorButtonX + editorButtonWidth
                && click.y() >= editorButtonY && click.y() < editorButtonY + 24) {
            if (client != null) client.setScreen(new HudEditor(hudManager, this));
            return true;
        }
        if (click.button() == 0 && click.x() >= crosshairButtonX && click.x() < crosshairButtonX + editorButtonWidth
                && click.y() >= editorButtonY && click.y() < editorButtonY + 24) {
            if (client != null) client.setScreen(new CrosshairDrawerScreen(this));
            return true;
        }
        if (click.button() == 0 && click.x() >= waypointButtonX && click.x() < waypointButtonX + editorButtonWidth
                && click.y() >= editorButtonY && click.y() < editorButtonY + 24) {
            if (client != null) client.setScreen(new WaypointScreen(this));
            return true;
        }
        String query = search == null ? "" : search.getText();
        if (hudPanel.click(click.x(), click.y(), click.button(), query)) return true;
        if (themeSelector.click(click.x(), click.y(), click.button())) return true;
        if (click.button() == 0 && click.x() >= rightPanelX() && click.x() < width - 18
                && click.y() >= 114 && click.y() < 138) {
            if (client != null) client.setScreen(new ThemeEditorScreen(this));
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (hudPanel.scroll(mouseX, mouseY, verticalAmount, search == null ? "" : search.getText())) return true;
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        if (hudPanel.drag(click.x(), click.y(), search == null ? "" : search.getText())) return true;
        return super.mouseDragged(click, deltaX, deltaY);
    }

    public boolean mouseReleased(Click click) {
        hudPanel.release();
        return super.mouseReleased(click);
    }

    private int panelWidth() { return Math.max(100, (width - 54) / 2); }
    private int rightPanelX() { return width - 18 - panelWidth(); }
    @Override public void removed() { save.run(); super.removed(); }

    public boolean shouldPause() { return false; }
}
