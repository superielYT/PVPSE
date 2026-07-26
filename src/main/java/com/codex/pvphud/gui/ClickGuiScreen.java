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
    private static final int EDITOR_BUTTON_WIDTH = 100;
    private int crosshairButtonX;
    private int waypointButtonX;

    public ClickGuiScreen(HudManager hudManager) {
        super(Text.literal("PVPSE Client"));
        this.hudManager = hudManager;
        this.hudPanel = new Panel("HUD ELEMENTS", hudManager.elements());
    }

    protected void init() {
        opening.snap(0.88F);
        opening.animateTo(1.0F);
        search = addDrawableChild(new SearchBar(textRenderer, 18, 18, 170));
        editorButtonX = width - EDITOR_BUTTON_WIDTH - 18;
        crosshairButtonX = editorButtonX - EDITOR_BUTTON_WIDTH - 8;
        waypointButtonX = crosshairButtonX - EDITOR_BUTTON_WIDTH - 8;
        editorButtonY = 16;
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Theme theme = ThemeManager.getInstance().getTheme();
        context.fill(0, 0, width, height, theme.background());
        boolean editorHovered = mouseX >= editorButtonX && mouseX < editorButtonX + EDITOR_BUTTON_WIDTH
                && mouseY >= editorButtonY && mouseY < editorButtonY + 24;
        com.codex.pvphud.render.RoundedRenderer.fill(context, editorButtonX, editorButtonY,
                EDITOR_BUTTON_WIDTH, 24, 6, editorHovered ? theme.accentSecondary() : theme.accent());
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("HUD EDITOR"),
                editorButtonX + EDITOR_BUTTON_WIDTH / 2, editorButtonY + 8, 0xFF101216);
        boolean crosshairHovered = mouseX >= crosshairButtonX && mouseX < crosshairButtonX + EDITOR_BUTTON_WIDTH
                && mouseY >= editorButtonY && mouseY < editorButtonY + 24;
        com.codex.pvphud.render.RoundedRenderer.fill(context, crosshairButtonX, editorButtonY,
                EDITOR_BUTTON_WIDTH, 24, 6, crosshairHovered ? theme.accentSecondary() : theme.panelHeader());
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("CROSSHAIR"),
                crosshairButtonX + EDITOR_BUTTON_WIDTH / 2, editorButtonY + 8, theme.text());
        boolean waypointHovered = mouseX >= waypointButtonX && mouseX < waypointButtonX + EDITOR_BUTTON_WIDTH
                && mouseY >= editorButtonY && mouseY < editorButtonY + 24;
        com.codex.pvphud.render.RoundedRenderer.fill(context, waypointButtonX, editorButtonY,
                EDITOR_BUTTON_WIDTH, 24, 6, waypointHovered ? theme.accentSecondary() : theme.panelHeader());
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("WAYPOINTS"),
                waypointButtonX + EDITOR_BUTTON_WIDTH / 2, editorButtonY + 8, theme.text());
        context.getMatrices().pushMatrix();
        float scale = opening.value();
        context.getMatrices().translate(width * (1.0F - scale) / 2.0F, height * (1.0F - scale) / 2.0F);
        context.getMatrices().scale(scale, scale);
        hudPanel.render(context, client, theme, 18, 48, 220, height - 66, mouseX, mouseY,
                search == null ? "" : search.getText());
        themeSelector.render(context, client, width - 210, 48, 192, mouseX, mouseY);
        boolean themeEditHover = mouseX >= width - 210 && mouseX < width - 18 && mouseY >= 86 && mouseY < 110;
        com.codex.pvphud.render.RoundedRenderer.fill(context, width - 210, 86, 192, 24, 6,
                themeEditHover ? theme.accentSecondary() : theme.panelHeader());
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("EDIT CUSTOM THEME"),
                width - 114, 94, theme.text());
        context.getMatrices().popMatrix();
        super.render(context, mouseX, mouseY, delta);
    }

    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() == 0 && click.x() >= editorButtonX && click.x() < editorButtonX + EDITOR_BUTTON_WIDTH
                && click.y() >= editorButtonY && click.y() < editorButtonY + 24) {
            if (client != null) client.setScreen(new HudEditor(hudManager, this));
            return true;
        }
        if (click.button() == 0 && click.x() >= crosshairButtonX && click.x() < crosshairButtonX + EDITOR_BUTTON_WIDTH
                && click.y() >= editorButtonY && click.y() < editorButtonY + 24) {
            if (client != null) client.setScreen(new CrosshairDrawerScreen(this));
            return true;
        }
        if (click.button() == 0 && click.x() >= waypointButtonX && click.x() < waypointButtonX + EDITOR_BUTTON_WIDTH
                && click.y() >= editorButtonY && click.y() < editorButtonY + 24) {
            if (client != null) client.setScreen(new WaypointScreen(this));
            return true;
        }
        String query = search == null ? "" : search.getText();
        if (hudPanel.click(click.x(), click.y(), click.button(), query)) return true;
        if (themeSelector.click(click.x(), click.y(), click.button())) return true;
        if (click.button() == 0 && click.x() >= width - 210 && click.x() < width - 18
                && click.y() >= 86 && click.y() < 110) {
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

    public boolean shouldPause() { return false; }
}
