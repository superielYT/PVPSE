package com.codex.pvphud.hud;
import com.codex.pvphud.theme.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import java.util.Locale;
public final class FoodHUD extends HudElement {
 private volatile Sample sample;
 private int lastTick = -1;
 private record Sample(java.lang.ref.WeakReference<Object> server, java.util.UUID player, float saturation) {}
 public FoodHUD() { super("food_saturation", "Food & Saturation", 150, 224, false); }
 protected void renderElement(GuiGraphicsExtractor c, Minecraft mc, Theme t, boolean editor, float alpha) {
  if(mc.player==null)return;
  var server=mc.getSingleplayerServer(); var id=mc.player.getUUID();
  if(lastTick!=mc.player.tickCount){lastTick=mc.player.tickCount;
   if(server!=null)server.execute(()->{var p=server.getPlayerList().getPlayer(id);if(p!=null)sample=new Sample(new java.lang.ref.WeakReference<>(server),id,p.getFoodData().getSaturationLevel());});
   else sample=null;
  }
  HudStyles.card(c,t,width(mc),height(mc),alpha);
  int food=mc.player.getFoodData().getFoodLevel();
  HudStyles.label(c,mc,Component.literal("FOOD  "+food+" / 20"),7,6,t.text());
  c.fill(7,20,139,24,0x55000000);c.fill(7,20,7+Math.round(132*food/20F),24,t.accent());
  Sample s=sample;
  String value=s!=null&&s.server().get()==server&&s.player().equals(id)?String.format(Locale.ROOT,"Saturation: %.1f",s.saturation()):"Saturation: unknown";
  HudStyles.label(c,mc,Component.literal(value),7,31,t.accentSecondary());
  if(server==null)HudStyles.label(c,mc,Component.literal("Not shared by this server"),7,44,t.mutedText());
 }
 public int width(Minecraft mc){return 150;}
 public int height(Minecraft mc){return 59;}
}
