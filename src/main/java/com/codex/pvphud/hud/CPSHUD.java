package com.codex.pvphud.hud;

import com.codex.pvphud.theme.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class CPSHUD extends HudElement {
    private boolean lastLeft;
    private boolean lastRight;
    private int left;
    private int right;
    private long windowStarted = System.currentTimeMillis();
    public CPSHUD() { super("cps", "CPS", 154, 84); }
    protected void renderElement(DrawContext c, MinecraftClient mc, Theme t, boolean editor, float alpha) {
        update(mc);
        HudStyles.card(c, t, width(mc), height(mc), alpha);
        HudStyles.label(c, mc, Text.literal("CPS "), 6, 6, t.mutedText());
        HudStyles.label(c, mc, Text.literal(left + " | " + right), 29, 6, t.accent());
    }
    private void update(MinecraftClient mc) {
        boolean l = mc.options.attackKey.isPressed();
        boolean r = mc.options.useKey.isPressed();
        if (l && !lastLeft) left++;
        if (r && !lastRight) right++;
        lastLeft = l; lastRight = r;
        if (System.currentTimeMillis() - windowStarted >= 1_000L) {
            left = right = 0;
            windowStarted = System.currentTimeMillis();
        }
    }
    public int width(MinecraftClient mc) { return 75; }
    public int height(MinecraftClient mc) { return 21; }
}
