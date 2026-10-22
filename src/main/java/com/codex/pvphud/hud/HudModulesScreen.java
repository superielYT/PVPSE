package com.codex.pvphud.hud;

import com.codex.pvphud.gui.PvpButton;
import com.codex.pvphud.theme.ThemeManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Explicitly choose HUD cards; hidden cards never have to occupy the canvas. */
public final class HudModulesScreen extends Screen {
    private final HudManager manager;
    private final Screen parent;
    private final Runnable save;
    private boolean hiddenOnly;
    private int page;

    public HudModulesScreen(HudManager manager, Screen parent, Runnable save) {
        super(Component.literal("Choose HUDs")); this.manager = manager; this.parent = parent; this.save = save;
    }

    @Override protected void init() {
        int w = Math.min(360, width - 24), x = (width - w) / 2;
        var all = button(x, 32, (w - 4) / 2, "All HUDs", () -> { hiddenOnly = false; page = 0; rebuildWidgets(); });
        var hidden = button(x + (w + 4) / 2, 32, (w - 4) / 2, "Hidden", () -> { hiddenOnly = true; page = 0; rebuildWidgets(); });
        all.active = hiddenOnly; hidden.active = !hiddenOnly;
        var cards = manager.layoutElements().stream().filter(e -> !hiddenOnly || !e.enabled()).toList();
        int perPage = Math.max(1, (height - 114) / 24), pages = Math.max(1, (cards.size() + perPage - 1) / perPage);
        page = Math.clamp(page, 0, pages - 1);
        for (int i = page * perPage; i < Math.min(cards.size(), (page + 1) * perPage); i++) {
            HudElement card = cards.get(i);
            button(x, 62 + (i % perPage) * 24, w, card.name() + ": " + (card.enabled() ? "ON" : "OFF"), () -> {
                card.setEnabled(!card.enabled()); save.run(); rebuildWidgets();
            });
        }
        int third = (w - 8) / 3;
        button(x, height - 28, third, "Previous", () -> { page--; rebuildWidgets(); }).active = page > 0;
        button(x + third + 4, height - 28, third, "Next", () -> { page++; rebuildWidgets(); }).active = page + 1 < pages;
        button(x + (third + 4) * 2, height - 28, third, "Done", this::onClose);
    }

    private net.minecraft.client.gui.components.Button button(int x, int y, int w, String label, Runnable action) {
        return addRenderableWidget(PvpButton.styled(Component.literal(label), b -> action.run()).bounds(x, y, w, 20).build());
    }

    @Override public void extractRenderState(GuiGraphicsExtractor c, int mx, int my, float delta) {
        var theme = ThemeManager.getInstance().getTheme();
        c.fill(0, 0, width, height, theme.background());
        c.centeredText(font, title, width / 2, 12, theme.accent());
        if (hiddenOnly && manager.layoutElements().stream().allMatch(HudElement::enabled))
            c.centeredText(font, Component.literal("No hidden HUDs"), width / 2, 68, theme.mutedText());
        c.centeredText(font, Component.literal("ON: visible  |  OFF: hidden  |  Changes save immediately"), width / 2, height - 43, theme.mutedText());
        super.extractRenderState(c, mx, my, delta);
    }

    @Override public void onClose() { minecraft.setScreen(parent); }
    @Override public void removed() { save.run(); super.removed(); }
    @Override public boolean isPauseScreen() { return false; }
}
