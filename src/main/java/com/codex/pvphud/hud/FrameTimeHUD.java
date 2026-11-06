package com.codex.pvphud.hud;

import com.codex.pvphud.FrameMonitor;
import com.codex.pvphud.theme.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class FrameTimeHUD extends HudElement {
    public FrameTimeHUD(){super("frame_time","Frametime Graph",8,240,false);}
    public int width(Minecraft mc){return 160;}
    public int height(Minecraft mc){return 65;}
    @Override protected void renderElement(GuiGraphicsExtractor c,Minecraft mc,Theme theme,boolean editor,float alpha){
        HudStyles.card(c,theme,width(mc),height(mc),alpha);var stats=FrameMonitor.stats();
        HudStyles.label(c,mc,Component.literal(String.format(java.util.Locale.ROOT,"%.0f FPS | p95 %.1fms",stats.fps(),stats.p95())),6,5,theme.text());
        c.fill(7,20,153,55,0x44000000);
        c.fill(7,49,153,50,0x4446E0A0);c.fill(7,43,153,44,0x44FFB454);
        var samples=FrameMonitor.recent();
        for(int i=0;i<samples.length;i++){int h=Math.clamp((int)Math.ceil(samples[i]/100*34),1,34),color=samples[i]<=16.7?0xFF46E0A0:samples[i]<=33.4?0xFFFFB454:theme.danger();c.fill(8+i,55-h,9+i,55,color);}
        HudStyles.label(c,mc,Component.literal("16.7 / 33.3ms targets"),6,57,theme.mutedText());
    }
}
