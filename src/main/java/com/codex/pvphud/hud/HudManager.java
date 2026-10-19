package com.codex.pvphud.hud;

import com.codex.pvphud.theme.ThemeManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

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
        register(new FeatureModule(FeatureModule.Type.FULLBRIGHT, "Fullbright", 10, 344, 12));
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

    public void render(GuiGraphics context, Minecraft client, boolean editorMode) {
        if (client.player == null || client.level == null) return;
        var theme = ThemeManager.getInstance().getTheme();
        for (HudElement element : elements) {
            if (editorMode && element instanceof FeatureModule feature && feature.type() != FeatureModule.Type.WAYPOINT) continue;
            if (!editorMode) element.keepOnScreen(client);
            element.render(context, client, theme, editorMode);
        }
    }
}
