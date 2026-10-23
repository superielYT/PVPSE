package com.codex.pvphud.hud;

import com.codex.pvphud.theme.Theme;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/** User-selected file packs only; vanilla and Fabric's internal packs do not fill the card. */
public final class PackDisplayHUD extends HudElement {
    public PackDisplayHUD() { super("resource_packs", "Resource Packs", 8, 204, false); }

    public static List<String> activeNames(Minecraft mc) {
        return mc.getResourcePackRepository().getSelectedPacks().stream()
                .filter(pack -> pack.getId().startsWith("file/"))
                .map(pack -> pack.getTitle().getString()).toList().reversed();
    }

    @Override protected void renderElement(GuiGraphicsExtractor c, Minecraft mc, Theme theme, boolean editor, float alpha) {
        var names = activeNames(mc);
        HudStyles.card(c, theme, width(mc), height(mc), alpha);
        HudStyles.label(c, mc, Component.literal("RESOURCE PACKS"), 6, 5, theme.mutedText());
        if (names.isEmpty()) {
            HudStyles.label(c, mc, Component.literal("Vanilla textures"), 6, 20, theme.text());
            HudStyles.label(c, mc, Component.literal("No file packs enabled"), 6, 35, theme.mutedText());
        } else {
            for (int i = 0; i < Math.min(2, names.size()); i++)
                HudStyles.label(c, mc, Component.literal(mc.font.plainSubstrByWidth(names.get(i), 172)), 6, 20 + i * 13,
                        i == 0 ? theme.text() : theme.mutedText());
            String footer = names.size() > 2 ? "+" + (names.size() - 2) + " more  |  highest priority first" : "Highest priority first";
            HudStyles.label(c, mc, Component.literal(mc.font.plainSubstrByWidth(footer, 172)), 6, 48, theme.accentSecondary());
        }
    }

    public int width(Minecraft mc) { return 184; }
    public int height(Minecraft mc) { return 63; }
}
