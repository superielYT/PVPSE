package com.codex.pvphud.hud;

import java.util.Set;

/** Only HUD cards change; utility switches, keybinds and themes are preserved. */
public enum PvpPreset {
    SWORD(Set.of("watermark", "fps", "ping", "cps", "armor", "target", "combo", "reach", "inventory")),
    CRYSTAL(Set.of("watermark", "fps", "ping", "armor", "coordinates", "target", "inventory")),
    MACE(Set.of("watermark", "fps", "ping", "cps", "armor", "target", "combo", "keystrokes", "inventory"));

    private final Set<String> cards;
    PvpPreset(Set<String> cards) { this.cards = cards; }
    public boolean includes(String id) { return cards.contains(id); }
    public void apply(HudManager manager) {
        manager.elements().stream().filter(e -> !(e instanceof FeatureModule))
                .forEach(e -> e.setEnabled(includes(e.id())));
    }
}
