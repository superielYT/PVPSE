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
        register(new InventoryHUD());
        register(new HeldItemHUD());
        register(new PackDisplayHUD());
        register(new CoordinatesHUD());
        register(new TargetHUD());
        register(new HealthIndicatorHUD());
        register(new SessionHUD());
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
            if (!editorMode && element.enabled()) element.keepOnScreen(client);
            element.render(context, client, theme, editorMode);
        }
    }

    /** Pack restored widgets without overlap, reserving room for hotbar/editor tools. */
    public void arrange(Minecraft client) {
        tryArrange(client);
    }

    /** Commit a complete layout only; failed attempts leave user positions intact. */
    public boolean tryArrange(Minecraft client) {
        var widgets = new ArrayList<>(layoutElements().stream().filter(HudElement::enabled).toList());
        if (widgets.isEmpty()) return true;
        float viewportWidth = client.getWindow().getGuiScaledWidth();
        float bottom = client.getWindow().getGuiScaledHeight() - 48;
        for (float scale : new float[]{1, 0.9F, 0.8F, 0.7F, 0.6F, 0.5F}) {
            var placed = new ArrayList<Placement>();
            var sorted = new ArrayList<>(widgets);
            sorted.sort(java.util.Comparator.comparingInt((HudElement e) -> layoutHeight(e, client)).reversed());
            HudElement watermark = find("watermark").orElse(null);
            if (watermark != null && widgets.contains(watermark)) { sorted.remove(watermark); sorted.addFirst(watermark); }
            boolean fits = true;
            for (HudElement element : sorted) {
                var xs = new java.util.TreeSet<Float>();
                var ys = new java.util.TreeSet<Float>();
                xs.add(8F); ys.add(8F);
                placed.forEach(p -> { xs.add(p.x + p.width + 6); ys.add(p.y + p.height + 6); });
                Placement selected = null;
                search: for (float y : ys) for (float x : xs) {
                    var candidate = new Placement(x, y, element.width(client) * scale, layoutHeight(element, client) * scale);
                    if (x + candidate.width > viewportWidth - 8 || y + candidate.height > bottom) continue;
                    if (placed.stream().noneMatch(candidate::overlaps)) { selected = candidate; break search; }
                }
                if (selected == null) { fits = false; break; }
                placed.add(selected);
            }
            if (fits) {
                for (int i = 0; i < sorted.size(); i++) {
                    var element = sorted.get(i); var placement = placed.get(i);
                    element.setScale(scale); element.setPosition(placement.x, placement.y);
                }
                return true;
            }
        }
        return false;
    }

    private record Placement(float x, float y, float width, float height) {
        boolean overlaps(Placement other) {
            return x < other.x + other.width + 5 && x + width + 5 > other.x
                    && y < other.y + other.height + 5 && y + height + 5 > other.y;
        }
    }

    /** Place newly added modules without moving an existing user's cards. */
    public boolean placeNewCards(Minecraft client, List<String> ids) {
        boolean allPlaced = true;
        var occupied = new ArrayList<Placement>();
        layoutElements().stream().filter(e -> e.enabled() && !ids.contains(e.id())).forEach(e -> occupied.add(
                new Placement(e.x(), e.y(), e.width(client) * e.scale(), layoutHeight(e, client) * e.scale())));
        for (String id : ids) {
            HudElement element = find(id).orElse(null);
            if (element == null || !element.enabled() || !layoutElements().contains(element)) continue;
            boolean placed = false;
            search: for (float scale : new float[]{1, 0.8F, 0.6F, 0.5F}) {
                for (float y = 8; y + layoutHeight(element, client) * scale <= client.getWindow().getGuiScaledHeight() - 58; y += 8)
                    for (float x = 8; x + element.width(client) * scale <= client.getWindow().getGuiScaledWidth() - 8; x += 8) {
                        var candidate = new Placement(x, y, element.width(client) * scale, layoutHeight(element, client) * scale);
                        if (occupied.stream().anyMatch(candidate::overlaps)) continue;
                        element.setPosition(x, y); element.setScale(scale); occupied.add(candidate); placed = true; break search;
                    }
            }
            if (!placed) allPlaced = false;
        }
        return allPlaced;
    }

    private static int layoutHeight(HudElement element, Minecraft client) {
        return element.height(client);
    }
}
