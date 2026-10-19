package com.codex.pvphud.hud;

import com.codex.pvphud.theme.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class CPSHUD extends HudElement {
    private boolean lastLeft;
    private boolean lastRight;
    private int left;
    private int right;
    private long windowStarted = System.currentTimeMillis();
    public CPSHUD() { super("cps", "CPS", 154, 84); }
    protected void renderElement(GuiGraphicsExtractor c, Minecraft mc, Theme t, boolean editor, float alpha) {
        update(mc);
        HudStyles.card(c, t, width(mc), height(mc), alpha);
        HudStyles.label(c, mc, Component.literal("CPS "), 6, 6, t.mutedText());
        HudStyles.label(c, mc, Component.literal(left + " | " + right), 29, 6, t.accent());
    }
    private void update(Minecraft mc) {
        boolean l = mc.options.keyAttack.isDown();
        boolean r = mc.options.keyUse.isDown();
        if (l && !lastLeft) left++;
        if (r && !lastRight) right++;
        lastLeft = l; lastRight = r;
        if (System.currentTimeMillis() - windowStarted >= 1_000L) {
            left = right = 0;
            windowStarted = System.currentTimeMillis();
        }
    }
    public int width(Minecraft mc) { return 75; }
    public int height(Minecraft mc) { return 21; }
}
