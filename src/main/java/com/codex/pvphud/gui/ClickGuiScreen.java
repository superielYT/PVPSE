package com.codex.pvphud.gui;

import com.codex.pvphud.animation.ScaleAnimation;
import com.codex.pvphud.hud.HudEditor;
import com.codex.pvphud.hud.HudManager;
import com.codex.pvphud.theme.Theme;
import com.codex.pvphud.theme.ThemeManager;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class ClickGuiScreen extends Screen {
    private final HudManager hudManager;
    private final Panel hudPanel;
    private final ThemeSelector themeSelector = new ThemeSelector();
    private final ScaleAnimation opening = new ScaleAnimation(0.88F);
    private SearchBar search;

    public ClickGuiScreen(HudManager hudManager) {
        super(Text.literal("PVPSE Client"));
        this.hudManager = hudManager;
        this.hudPanel = new Panel("HUD ELEMENTS", hudManager.elements());
    }

    protected void init() {
        opening.snap(0.88F);
        opening.animateTo(1.0F);
        search = addDrawableChild(new SearchBar(textRenderer, 18, 18, 170));
        addDrawableChild(ButtonWidget.builder(Text.literal("HUD Editor"), button -> {
            if (client != null) client.setScreen(new HudEditor(hudManager, this));
        }).dimensions(width - 112, 18, 94, 20).build());
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        Theme theme = ThemeManager.getInstance().getTheme();
        context.fill(0, 0, width, height, theme.background());
        context.getMatrices().pushMatrix();
        float scale = opening.value();
        context.getMatrices().translate(width * (1.0F - scale) / 2.0F, height * (1.0F - scale) / 2.0F);
        context.getMatrices().scale(scale, scale);
        hudPanel.render(context, client, theme, 18, 48, 210, mouseX, mouseY, search == null ? "" : search.getText());
        themeSelector.render(context, client, width - 210, 48, 192, mouseX, mouseY);
        context.getMatrices().popMatrix();
        super.render(context, mouseX, mouseY, delta);
    }

    public boolean mouseClicked(Click click, boolean doubled) {
        String query = search == null ? "" : search.getText();
        if (hudPanel.click(click.x(), click.y(), click.button(), query)) return true;
        if (themeSelector.click(click.x(), click.y(), click.button())) return true;
        return super.mouseClicked(click, doubled);
    }

    public boolean shouldPause() { return false; }
}
