package com.codex.pvphud.hud;

import com.codex.pvphud.gui.PvpButton;
import com.codex.pvphud.theme.ThemeManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

/** Explicitly choose HUD cards; hidden cards never have to occupy the canvas. */
public final class HudModulesScreen extends Screen {
    private final HudManager manager;
    private final Screen parent;
    private final Runnable save;
    private enum Filter { ALL, SHOWN, HIDDEN }
    private Filter filter = Filter.ALL;
    private int page;
    private String query = "";
    private EditBox search;
    private boolean restoreFocus;

    public HudModulesScreen(HudManager manager, Screen parent, Runnable save) {
        super(Component.literal("Choose HUDs")); this.manager = manager; this.parent = parent; this.save = save;
    }

    @Override protected void init() {
        int w = Math.min(360, width - 24), x = (width - w) / 2;
        boolean typing = restoreFocus || (search != null && search.isFocused()); restoreFocus = false;
        search = addRenderableWidget(new EditBox(font, x(), 31, w, 20, Component.literal("Search HUDs")));
        search.setMaxLength(64); search.setHint(Component.literal("Find a HUD: armor, held, packs...")); search.setValue(query);
        search.setResponder(value -> {
            if (query.equals(value)) return;
            query = value; page = 0; restoreFocus = true; rebuildWidgets();
        });
        if (typing) setFocused(search);
        int fw = (w - 8) / 3;
        for (Filter next : Filter.values()) {
            String label = switch (next) { case ALL -> "All HUDs"; case SHOWN -> "Shown"; case HIDDEN -> "Hidden"; };
            button(x + next.ordinal() * (fw + 4), 57, fw, label, () -> { filter = next; page = 0; rebuildWidgets(); }).active = next != filter;
        }
        var cards = matches();
        int perPage = Math.max(1, (height - 138) / 24), pages = Math.max(1, (cards.size() + perPage - 1) / perPage);
        page = Math.clamp(page, 0, pages - 1);
        for (int i = page * perPage; i < Math.min(cards.size(), (page + 1) * perPage); i++) {
            HudElement card = cards.get(i);
            button(x, 85 + (i % perPage) * 24, w, card.name() + ": " + (card.enabled() ? "ON" : "OFF"), () -> {
                boolean firstPlacement = !card.enabled() && card.needsInitialPlacement();
                card.setEnabled(!card.enabled());
                if (firstPlacement && minecraft.player != null && !manager.placeNewCards(minecraft, java.util.List.of(card.id()))) {
                    card.setEnabled(false);
                    minecraft.setScreen(new net.minecraft.client.gui.screens.ConfirmScreen(yes -> {
                        if (yes) { card.setEnabled(true); if (!manager.tryArrange(minecraft)) card.setEnabled(false); }
                        save.run(); minecraft.setScreen(this);
                    }, Component.literal("Make room for " + card.name() + "?"),
                            Component.literal("No free space. Yes: arrange shown cards to fit. No: keep this card off. Hidden layouts are kept.")));
                    return;
                }
                save.run(); rebuildWidgets();
            });
        }
        int third = (w - 8) / 3;
        button(x, height - 28, third, "Previous", () -> { page--; rebuildWidgets(); }).active = page > 0;
        button(x + third + 4, height - 28, third, "Next", () -> { page++; rebuildWidgets(); }).active = page + 1 < pages;
        button(x + (third + 4) * 2, height - 28, third, "Done", this::onClose);
    }

    private int x() { return (width - Math.min(360, width - 24)) / 2; }

    private java.util.List<HudElement> matches() {
        String[] words = query.strip().toLowerCase(java.util.Locale.ROOT).split("\\s+");
        return manager.layoutElements().stream()
                .filter(e -> filter == Filter.ALL || (filter == Filter.SHOWN) == e.enabled())
                .filter(e -> {
                    String text = (e.name() + " " + e.id().replace('_', ' ')).toLowerCase(java.util.Locale.ROOT);
                    for (String word : words) if (!text.contains(word)) return false;
                    return true;
                }).toList();
    }

    private net.minecraft.client.gui.components.Button button(int x, int y, int w, String label, Runnable action) {
        return addRenderableWidget(PvpButton.styled(Component.literal(label), b -> action.run()).bounds(x, y, w, 20).build());
    }

    @Override public void extractRenderState(GuiGraphicsExtractor c, int mx, int my, float delta) {
        var theme = ThemeManager.getInstance().getTheme();
        c.fill(0, 0, width, height, theme.background());
        long shown = manager.layoutElements().stream().filter(HudElement::enabled).count();
        c.centeredText(font, Component.literal("Choose HUDs  |  " + shown + "/" + manager.layoutElements().size() + " shown"), width / 2, 12, theme.accent());
        if (matches().isEmpty()) c.centeredText(font, Component.literal("No matching HUDs"), width / 2, 91, theme.mutedText());
        c.centeredText(font, Component.literal(font.plainSubstrByWidth("ON: visible | OFF: hidden | Saves immediately", width - 24)), width / 2, height - 43, theme.mutedText());
        super.extractRenderState(c, mx, my, delta);
    }

    @Override public void onClose() { minecraft.setScreen(parent); }
    @Override public void removed() { save.run(); super.removed(); }
    @Override public boolean isPauseScreen() { return false; }
}
