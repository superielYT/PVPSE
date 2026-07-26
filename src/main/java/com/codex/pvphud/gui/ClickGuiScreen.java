package com.codex.pvphud.gui;

import com.codex.pvphud.animation.ScaleAnimation;
import com.codex.pvphud.hud.HudEditor;
import com.codex.pvphud.hud.HudManager;
import com.codex.pvphud.theme.Theme;
import com.codex.pvphud.theme.ThemeManager;
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
        context.getMatrices().pushMatrix();
        float scale = opening.value();
        context.getMatrices().translate(width * (1.0F - scale) / 2.0F, height * (1.0F - scale) / 2.0F);
        context.getMatrices().scale(scale, scale);
        hudPanel.render(context, client, theme, 18, 48, 220, height - 66, mouseX, mouseY,
                search == null ? "" : search.getText());
        themeSelector.render(context, client, width - 210, 48, 192, mouseX, mouseY);
        context.getMatrices().popMatrix();
        super.render(context, mouseX, mouseY, delta);
    }

    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() == 0 && click.x() >= editorButtonX && click.x() < editorButtonX + EDITOR_BUTTON_WIDTH
                && click.y() >= editorButtonY && click.y() < editorButtonY + 24) {
            if (client != null) client.setScreen(new HudEditor(hudManager, this));
            return true;
        }
        String query = search == null ? "" : search.getText();
        if (hudPanel.click(click.x(), click.y(), click.button(), query)) return true;
        if (themeSelector.click(click.x(), click.y(), click.button())) return true;
        return super.mouseClicked(click, doubled);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (hudPanel.scroll(mouseX, mouseY, verticalAmount, search == null ? "" : search.getText())) return true;
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    public boolean shouldPause() { return false; }
}
