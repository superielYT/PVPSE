package com.codex.pvphud.hud;

import com.codex.pvphud.theme.ThemeManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class HudManager {
    private final List<HudElement> elements = new ArrayList<>();

    public HudManager() {
        register(new WatermarkHUD());
        register(new ArrayListHUD());
        register(new FPSHUD());
        register(new PingHUD());
        register(new CPSHUD());
        register(new KeystrokesHUD());
        register(new ArmorHUD());
        register(new CoordinatesHUD());
        register(new TargetHUD());
        register(new SessionHUD());
        register(new PotionHUD());
    }

    public void register(HudElement element) {
        elements.add(element);
    }

    public List<HudElement> elements() {
        return Collections.unmodifiableList(elements);
    }

    public Optional<HudElement> find(String id) {
        return elements.stream().filter(element -> element.id().equals(id)).findFirst();
    }

    public void render(DrawContext context, MinecraftClient client, boolean editorMode) {
        if (client.player == null || client.world == null) return;
        var theme = ThemeManager.getInstance().getTheme();
        for (HudElement element : elements) {
            element.render(context, client, theme, editorMode);
        }
    }
}
