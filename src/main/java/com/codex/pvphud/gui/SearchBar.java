package com.codex.pvphud.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public final class SearchBar extends EditBox {
    public SearchBar(Font renderer, int x, int y, int width) {
        super(renderer, x, y, width, 20, Component.literal("Search HUD"));
        setHint(Component.literal("Search HUD elements..."));
        setMaxLength(32);
    }
}
