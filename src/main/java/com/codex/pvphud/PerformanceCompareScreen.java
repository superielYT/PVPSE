package com.codex.pvphud;

import com.codex.pvphud.gui.PvpButton;
import com.codex.pvphud.theme.ThemeManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.Locale;

public final class PerformanceCompareScreen extends Screen {
    private final Screen parent;
    public PerformanceCompareScreen(Screen parent){super(Component.literal("PERFORMANCE COMPARISON"));this.parent=parent;}
    @Override protected void init(){
        int w=Math.min(420,width-24),left=(width-w)/2,third=(w-8)/3;
        String[] names={"Current","Balanced","Max"};PerformanceMode[] modes={null,PerformanceMode.BALANCED,PerformanceMode.MAX};
        for(int i=0;i<3;i++){final int index=i;var button=addRenderableWidget(PvpButton.styled(Component.literal("Measure "+names[i]),b->FrameMonitor.start(minecraft,names[index],modes[index])).bounds(left+i*(third+4),55,third,20).build());button.active=minecraft.player!=null&&!FrameMonitor.active();}
        addRenderableWidget(PvpButton.styled(Component.literal("Cancel measurement"),b->{FrameMonitor.cancel(minecraft);rebuildWidgets();}).bounds(left,height-52,(w-4)/2,20).build());
        addRenderableWidget(PvpButton.styled(Component.literal("Copy report"),b->minecraft.keyboardHandler.setClipboard(FrameMonitor.report())).bounds(left+(w+4)/2,height-52,(w-4)/2,20).build());
        addRenderableWidget(PvpButton.styled(Component.literal("Done"),b->onClose()).bounds(left,height-28,w,20).build());
    }
    @Override public void extractRenderState(GuiGraphicsExtractor c,int mx,int my,float delta){
        var theme=ThemeManager.getInstance().getTheme();com.codex.pvphud.gui.MenuBackdrop.draw(c,width,height,theme);
        int left=Math.max(12,(width-420)/2);c.text(font,title,left,12,theme.accent());
        c.text(font,"Stay in the same spot and keep your view still.",left,29,theme.text());
        c.text(font,"3s warmup + 15s gameplay. Options restore afterward.",left,41,theme.mutedText());
        int y=86;
        for(var r:FrameMonitor.results()){
            String line=String.format(Locale.ROOT,"%s: %.0f FPS | %.0f 1%% low",r.label(),r.stats().fps(),r.stats().lowOnePercent());
            c.text(font,line,left,y,theme.text());c.text(font,String.format(Locale.ROOT,"p95 %.1fms | p99 %.1fms | view %d",r.stats().p95(),r.stats().p99(),r.view()),left,y+11,theme.mutedText());y+=28;
        }
        if(FrameMonitor.results().isEmpty())c.text(font,"No measurements yet. Real results appear here.",left,91,theme.mutedText());
        if(FrameMonitor.active())c.text(font,FrameMonitor.progress()+" (menus pause)",left,Math.min(Math.max(y,110),height-65),theme.accent());
        super.extractRenderState(c,mx,my,delta);
    }
    @Override public void onClose(){minecraft.setScreen(parent);}
}
