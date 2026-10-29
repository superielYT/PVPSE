package com.codex.pvphud;

import java.util.List;
import java.util.Locale;

/** Search destinations jump to the real controls, with Dutch and English aliases. */
public final class SettingsSearch {
    public record Result(String label, String tab, String aliases) {}
    private static final List<Result> ENTRIES = List.of(
            new Result("Clean hotbar", "ITEMS", "hotbar slots cooldown durability aantallen"),
            new Result("Any held item", "HITBOX", "hitbox altijd unarmed hand algemeen vasthouden"),
            new Result("Background opacity %", "HITBOX", "hitbox zichtbaar visibility andere spelers"),
            new Result("Container previews", "ITEMS", "shulker bundle bundel inhoud preview voorvertoning"),
            new Result("Held food preview", "ITEMS", "eten food hunger saturatie vasthouden voorvertoning"),
            new Result("MIDNIGHT EMBER", "ITEMS", "dark donker midnight thema theme"),
            new Result("Potion timers", "GENERAL", "potion potions effect timer tijd vanilla drankjes"),
            new Result("Yellow saturation", "GENERAL", "saturatie saturation eten food hunger honger geel vanilla"),
            new Result("Cooldown circle", "AIM", "cooldown cirkel aanval crosshair"),
            new Result("Crosshair reactions", "AIM", "reacties vizier klaar opgeladen"),
            new Result("Circle radius", "AIM", "cirkel grootte"),
            new Result("Health indicator", "AIM", "health hp levens hartjes"),
            new Result("HUD", "GENERAL", "zichtbaar visibility overlay aan uit"),
            new Result("Notifications", "GENERAL", "meldingen"),
            new Result("Hide on F3", "GENERAL", "debug verbergen"),
            new Result("No damage tilt", "GENERAL", "camera schade"),
            new Result("Custom crosshair", "GENERAL", "vizier kruis"),
            new Result("Block Overlay", "GENERAL", "blok blokken block outline omtrek aankijken muis inspect target"),
            new Result("FPS mode", "FPS", "performance boost fps verbeteren sneller lag soepel smooth snelheid"),
            new Result("Balanced", "FPS", "performance fps gebalanceerd"),
            new Result("Max FPS", "FPS", "performance boost fps snel"),
            new Result("Restore previous settings", "FPS", "performance fps herstel terugzetten"),
            new Result("Light HUD", "FPS", "performance fps licht schaduw"),
            new Result("Sword preset", "GENERAL", "zwaard melee profiel hud"),
            new Result("Crystal preset", "GENERAL", "kristal profiel hud"),
            new Result("Mace preset", "GENERAL", "profiel hud"),
            new Result("Attack cooldown bar", "COMBAT", "aanval balk"),
            new Result("Low health alert", "COMBAT", "hp levens waarschuwing"),
            new Result("HP threshold", "COMBAT", "health drempel"),
            new Result("Armor alert", "COMBAT", "armor armour uitrusting waarschuwing"),
            new Result("Durability %", "COMBAT", "armor armour durability duurzaamheid drempel"),
            new Result("Armor sound", "COMBAT", "armor geluid audio waarschuwing"),
            new Result("Sound volume %", "COMBAT", "armor geluid audio volume"),
            new Result("Melee hitboxes", "HITBOX", "hitbox zwaard mace"),
            new Result("Swords", "HITBOX", "zwaard hitbox"),
            new Result("Mace", "HITBOX", "hitbox"),
            new Result("Target only", "HITBOX", "doelwit hitbox"),
            new Result("View range", "HITBOX", "afstand distance bereik reach 12 32 blokken hitbox"),
            new Result("Eye-level ring", "HITBOX", "ooghoogte hitbox"),
            new Result("Line width", "HITBOX", "lijndikte hitbox"),
            new Result("State colors", "HITBOX", "kleur hitbox"),
            new Result("Hit marker", "EFFECTS", "hitmarker raken"),
            new Result("Marker size", "EFFECTS", "grootte hitmarker"),
            new Result("Damage flash", "EFFECTS", "flits schade"),
            new Result("Flash %", "EFFECTS", "sterkte flits schade"),
            new Result("Kill particles", "EFFECTS", "deeltjes effect"),
            new Result("Particles", "EFFECTS", "aantal deeltjes"),
            new Result("MINIMAL EFFECTS", "EFFECTS", "rustig effecten"),
            new Result("Theme", "STYLE", "thema glass doorzichtig"),
            new Result("HUD opacity %", "STYLE", "transparantie doorzichtig alpha"),
            new Result("Accent lines", "STYLE", "accent kleur lijnen"),
            new Result("Panel shadows", "STYLE", "paneel schaduw"),
            new Result("Text shadow", "STYLE", "tekst schaduw"),
            new Result("MINIMAL HUD", "STYLE", "rustig preset"),
            new Result("RESET STYLE", "STYLE", "herstel stijl"),
            new Result("Profile", "PROFILES", "profiel wisselen"),
            new Result("COPY CURRENT", "PROFILES", "profiel kopie opslaan"),
            new Result("Reset HUD positions & scales", "PROFILES", "herstel positie grootte"),
            new Result("AUTO ARRANGE", "PROFILES", "ordenen indeling overlap verplaatsen"));

    public static List<Result> find(String query) {
        if (query == null || query.isBlank()) return List.of();
        String[] words = query.strip().toLowerCase(Locale.ROOT).split("\\s+");
        return ENTRIES.stream().filter(e -> {
            String haystack = (e.label + " " + e.tab + " " + e.aliases).toLowerCase(Locale.ROOT);
            for (String word : words) if (!haystack.contains(word)) return false;
            return true;
        }).toList();
    }
}
