package com.codex.pvphud.notification;

import com.codex.pvphud.render.RoundedRenderer;
import com.codex.pvphud.theme.Theme;
import com.codex.pvphud.theme.ThemeManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.concurrent.ConcurrentLinkedDeque;

public final class NotificationManager {
    private static final NotificationManager INSTANCE = new NotificationManager();
    private static final long LIFETIME_MS = 3_500L;
    private final ConcurrentLinkedDeque<Notice> notices = new ConcurrentLinkedDeque<>();

    private NotificationManager() {}

    public static NotificationManager getInstance() {
        return INSTANCE;
    }

    public void push(String title, String message) {
        notices.addFirst(new Notice(title, message, System.currentTimeMillis()));
        while (notices.size() > 4) notices.pollLast();
    }

    public void render(DrawContext context, MinecraftClient client) {
        long now = System.currentTimeMillis();
        notices.removeIf(notice -> now - notice.createdAt() >= LIFETIME_MS);
        Theme theme = ThemeManager.getInstance().getTheme();
        int index = 0;
        for (Notice notice : notices) {
            long age = now - notice.createdAt();
            float enter = Math.min(1.0F, age / 240.0F);
            float leave = Math.min(1.0F, (LIFETIME_MS - age) / 300.0F);
            float progress = easeOut(Math.min(enter, leave));
            int boxWidth = 190;
            int x = Math.round(context.getScaledWindowWidth() - 12 - boxWidth * progress);
            int y = context.getScaledWindowHeight() - 48 - index++ * 48;
            RoundedRenderer.fill(context, x, y, boxWidth, 40, 7, theme.panel());
            context.fill(x, y, x + 3, y + 40, theme.accent());
            context.drawTextWithShadow(client.textRenderer, Text.literal(notice.title()), x + 10, y + 7, theme.accent());
            context.drawTextWithShadow(client.textRenderer, Text.literal(notice.message()), x + 10, y + 22, theme.mutedText());
        }
    }

    private static float easeOut(float value) {
        float inverse = 1.0F - value;
        return 1.0F - inverse * inverse * inverse;
    }

    private record Notice(String title, String message, long createdAt) {}
}
