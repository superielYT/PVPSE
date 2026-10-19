package com.codex.pvphud.hud;

import com.codex.pvphud.theme.ThemeManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

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
        register(new ComboHUD());
        register(new ReachHUD());
        register(new FeatureModule(FeatureModule.Type.WAYPOINT, "Waypoint", 198, 250, 0));
        register(new FeatureModule(FeatureModule.Type.ZOOM, "Zoom", 10, 292, 30));
        register(new FeatureModule(FeatureModule.Type.NO_PUMPKIN, "No Pumpkin", 10, 318, 0));
        register(new FeatureModule(FeatureModule.Type.FULLBRIGHT, "Brightness", 10, 344, 1));
        register(new FeatureModule(FeatureModule.Type.MELEE_HITBOXES, "Melee Hitboxes", 10, 370, 5));
    }

    public void register(HudElement element) {
        elements.add(element);
    }

    public List<HudElement> elements() {
        return Collections.unmodifiableList(elements);
    }

    public List<HudElement> layoutElements() {
        return elements.stream().filter(e -> !(e instanceof FeatureModule f) || f.type() == FeatureModule.Type.WAYPOINT).toList();
    }

    public Optional<HudElement> find(String id) {
        return elements.stream().filter(element -> element.id().equals(id)).findFirst();
    }

    public Optional<FeatureModule> feature(FeatureModule.Type type) {
        return elements.stream().filter(FeatureModule.class::isInstance).map(FeatureModule.class::cast)
                .filter(feature -> feature.type() == type).findFirst();
    }

    public void render(GuiGraphicsExtractor context, Minecraft client, boolean editorMode) {
        if (client.player == null || client.level == null) return;
        var theme = ThemeManager.getInstance().getTheme();
        for (HudElement element : elements) {
            if (editorMode && element instanceof FeatureModule feature && feature.type() != FeatureModule.Type.WAYPOINT) continue;
            if (!editorMode) element.keepOnScreen(client);
            element.render(context, client, theme, editorMode);
        }
    }

    /** Pack restored widgets into columns, reserving room for the hotbar/editor tools. */
    public void arrange(Minecraft client) {
        var widgets = new ArrayList<>(layoutElements());
        if (widgets.isEmpty()) return;
        float viewportWidth = client.getWindow().getGuiScaledWidth();
        float bottom = client.getWindow().getGuiScaledHeight() - 48;
        for (float scale : new float[]{1, 0.9F, 0.8F, 0.7F, 0.6F, 0.5F}) {
            float cell = widgets.stream().mapToInt(e -> e.width(client)).max().orElse(144) * scale + 6;
            int columns = Math.max(1, (int) ((viewportWidth - 10) / cell));
            float[] used = new float[columns];
            java.util.Arrays.fill(used, 8);
            var sorted = new ArrayList<>(widgets);
            sorted.sort(java.util.Comparator.comparingInt((HudElement e) -> layoutHeight(e, client)).reversed());
            HudElement watermark = find("watermark").orElse(null);
            if (watermark != null) { sorted.remove(watermark); sorted.addFirst(watermark); }
            boolean fits = true;
            for (HudElement element : sorted) {
                int column = 0;
                for (int i = 1; i < columns; i++) if (used[i] < used[column]) column = i;
                element.setScale(scale);
                element.setPosition(8 + column * cell, used[column]);
                used[column] += layoutHeight(element, client) * scale + 6;
                if (used[column] - 6 > bottom) fits = false;
            }
            if (fits) return;
        }
    }

    private static int layoutHeight(HudElement element, Minecraft client) {
        return element instanceof PotionHUD ? 72 : element.height(client);
    }
}
