package com.codex.pvphud.theme;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public final class ThemeManager {
    private static final ThemeManager INSTANCE = new ThemeManager();
    private final CustomTheme customTheme = CustomTheme.load();
    private final List<Theme> themes = List.of(
            new IgniteTheme(),
            new MidnightTheme(),
            new NeonTheme(),
            new CyberpunkTheme(),
            new FrostTheme(),
            new SakuraTheme(),
            customTheme
    );
    private final AtomicReference<Theme> current = new AtomicReference<>(themes.getFirst());

    private ThemeManager() {}

    public static ThemeManager getInstance() {
        return INSTANCE;
    }

    public Theme getTheme() {
        return current.get();
    }

    public List<Theme> themes() {
        return themes;
    }

    public void setTheme(Theme theme) {
        current.set(Objects.requireNonNull(theme, "theme"));
    }

    public boolean setTheme(String id) {
        return themes.stream().filter(theme -> theme.id().equalsIgnoreCase(id)).findFirst()
                .map(theme -> {
                    setTheme(theme);
                    return true;
                }).orElse(false);
    }

    public Theme cycle() {
        Theme active = current.get();
        int next = (themes.indexOf(active) + 1) % themes.size();
        Theme selected = themes.get(next);
        current.set(selected);
        return selected;
    }

    public CustomTheme customTheme() {
        return customTheme;
    }
}
