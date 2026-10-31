package com.codex.pvphud;

import com.codex.pvphud.mixin.CooldownsAccessor;
import com.codex.pvphud.mixin.CooldownEntryAccessor;
import com.codex.pvphud.render.RenderUtil;
import com.codex.pvphud.render.RoundedRenderer;
import com.codex.pvphud.theme.ThemeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class CleanHotbar {
    private CleanHotbar() {}
    public static boolean background(GuiGraphicsExtractor c, Identifier sprite, int x,int y,int width,int height) {
        var mod=PvpHudClient.getInstance();
        if(mod==null || !mod.cleanHotbarEnabled() || !sprite.getNamespace().equals("minecraft")) return false;
        String path=sprite.getPath();
        var theme=ThemeManager.getInstance().getTheme();
        var style=ThemeManager.getInstance().appearance();
        if(path.equals("hud/hotbar_selection")) {
            RoundedRenderer.fill(c,x+1,y+1,width-2,height-1,3,theme.accent());
            RoundedRenderer.fill(c,x+2,y+2,width-4,height-3,2,theme.panel());
            return true;
        }
        if(!path.equals("hud/hotbar") && !path.startsWith("hud/hotbar_offhand_")) return false;
        RenderUtil.panel(c,x,y,width,height,theme,style.panelColor(theme.panel(),1),style.accents,false);
        if(path.equals("hud/hotbar")) for(int i=1;i<9;i++) c.fill(x+1+i*20,y+4,x+2+i*20,y+height-4,0x1CFFFFFF);
        return true;
    }

    public static int remainingTicks(Player player, ItemStack stack) {
        var cooldowns=player.getCooldowns();
        var access=(CooldownsAccessor)cooldowns;
        Object entry=access.pvpse$cooldowns().get(cooldowns.getCooldownGroup(stack));
        return entry instanceof CooldownEntryAccessor data ? Math.max(0,data.pvpse$endTime()-access.pvpse$tickCount()) : 0;
    }

    public static void details(GuiGraphicsExtractor c, int x,int y,Player player,ItemStack stack) {
        var mod=PvpHudClient.getInstance();
        if(mod==null || !mod.cleanHotbarEnabled() || stack.isEmpty()) return;
        var mc=Minecraft.getInstance();
        var theme=ThemeManager.getInstance().getTheme();
        if(stack.getCount()>1) {
            String count=Integer.toString(stack.getCount()); int left=x+17-mc.font.width(count);
            c.fill(left-1,y+8,x+18,y+18,0xB0080A10);
            c.text(mc.font,count,left,y+9,0xFFFFFFFF,true);
        }
        if(stack.isDamageableItem() && stack.getCount()==1) {
            int percent=com.codex.pvphud.hud.ArmorHUD.durabilityPercent(stack.getDamageValue(),stack.getMaxDamage());
            c.fill(x,y+16,x+16,y+18,0xCC07090D);
            c.fill(x,y+16,x+(stack.getDamageValue()<stack.getMaxDamage()?Math.max(1,Math.round(16*percent/100F)):0),y+18,
                    percent<=mod.armorWarningThreshold()?theme.danger():percent<=30?0xFFFFC56B:theme.accentSecondary());
        }
        int ticks=remainingTicks(player,stack);
        if(ticks>0) {
            String time=(int)Math.ceil(ticks/20.0)+"s";
            c.pose().pushMatrix();
            try {
                c.pose().translate(x+8F,y-1F); c.pose().scale(.7F,.7F);
                int left=-mc.font.width(time)/2;
                c.fill(left-1,-1,left+mc.font.width(time)+1,9,0xD0080A10);
                c.text(mc.font,time,left,0,0xFFFFCF85,true);
            } finally {c.pose().popMatrix();}
        }
    }
}
