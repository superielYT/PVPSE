package com.codex.pvphud.gui;
import com.codex.pvphud.render.ColorUtil;
import com.codex.pvphud.render.RoundedRenderer;
import com.codex.pvphud.theme.Theme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
public final class MenuBackdrop {
 private MenuBackdrop() {}
 public static void draw(GuiGraphicsExtractor c,int width,int height,Theme theme){
  for(int y=0;y<height;y+=4)c.fill(0,y,width,Math.min(height,y+4),ColorUtil.lerp(theme.background(),0xFF070B12,.35F*y/Math.max(1,height)));
  RoundedRenderer.fill(c,width-170,-100,240,240,110,ColorUtil.withAlpha(theme.accent(),8));
  RoundedRenderer.fill(c,width-140,-80,180,180,85,ColorUtil.withAlpha(theme.accentSecondary(),6));
  c.fill(0,0,width,1,ColorUtil.withAlpha(theme.accent(),45));
 }
}
