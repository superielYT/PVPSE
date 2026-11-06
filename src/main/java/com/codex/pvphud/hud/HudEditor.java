package com.codex.pvphud.hud;

import com.codex.pvphud.theme.ThemeManager;
import com.codex.pvphud.render.RoundedRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import java.util.*;
import static org.lwjgl.glfw.GLFW.*;

public final class HudEditor extends Screen {
    private final HudManager manager;
    private final Screen parent;
    private final Runnable save;
    private final LinkedHashSet<HudElement> selection=new LinkedHashSet<>();
    private final LayoutHistory<Map<HudElement,Layout>> history=new LayoutHistory<>();
    private record Layout(float x,float y,float scale,boolean enabled,boolean locked) {}
    private Map<HudElement,Layout> gesture,externalBefore;
    private HudElement selected,dragging;
    private boolean resizing,controlsVisible=true,controlDown;
    private double offsetX,offsetY,resizeStartX;
    private float resizeStartScale;
    private Integer guideX,guideY;
    private String status="";
    private final List<net.minecraft.client.gui.components.Button> controls=new ArrayList<>();
    private net.minecraft.client.gui.components.Button hideButton,smallerButton,largerButton,lockButton,undoButton,redoButton,leftButton,topButton,gapXButton,gapYButton;

    public HudEditor(HudManager manager,Screen parent){this(manager,parent,()->{});}
    public HudEditor(HudManager manager,Screen parent,Runnable save){
        super(Component.literal("PVPSE HUD Editor Pro"));this.manager=manager;this.parent=parent;this.save=save;
    }
    private Map<HudElement,Layout> snapshot(){
        var result=new LinkedHashMap<HudElement,Layout>();
        for(var e:manager.layoutElements())result.put(e,new Layout(e.x(),e.y(),e.scale(),e.enabled(),e.locked()));
        return result;
    }
    private void apply(Map<HudElement,Layout> state){
        state.forEach((e,l)->{e.setPosition(l.x(),l.y());e.setScale(l.scale());e.setEnabled(l.enabled());e.setLocked(l.locked());});
        selection.removeIf(e->!e.enabled());if(selected!=null&&!selection.contains(selected))selected=selection.stream().findFirst().orElse(null);
        dragging=null;resizing=false;gesture=null;guideX=null;guideY=null;updateControls();save.run();
    }
    private void edit(Runnable action){
        var before=gesture==null?snapshot():null;action.run();
        if(before!=null){history.record(before,snapshot());save.run();}updateControls();
    }
    private void commitGesture(){
        if(gesture!=null){history.record(gesture,snapshot());gesture=null;save.run();}
        dragging=null;resizing=false;guideX=null;guideY=null;updateControls();
    }
    public void undoLayout(){commitGesture();apply(history.undo(snapshot()));}
    public void redoLayout(){commitGesture();apply(history.redo(snapshot()));}
    private List<HudElement> editable(){return selection.stream().filter(e->e.enabled()&&!e.locked()).toList();}

    @Override protected void init(){
        controls.clear();controlDown=false;
        if(externalBefore!=null){history.record(externalBefore,snapshot());externalBefore=null;}
        selection.removeIf(e->!e.enabled());if(selected!=null&&!selected.enabled())selected=null;
        int large=(Math.min(360,width-16)-82)/4,x=(width-(large*4+70))/2;
        control(x,8,large,"HUDs",()->minecraft.setScreen(new HudModulesScreen(manager,this,save)));x+=large+4;
        hideButton=control(x,8,large,"Hide",this::hideSelected);x+=large+4;
        smallerButton=control(x,8,25,"-",()->resizeSelected(-.1F,false));x+=29;
        largerButton=control(x,8,25,"+",()->resizeSelected(.1F,false));x+=29;
        control(x,8,large,"Theme",()->ThemeManager.getInstance().cycle());x+=large+4;
        control(x,8,large,"Done",this::onClose);
        int total=Math.min(420,width-16),w=(total-24)/7;x=(width-total)/2;
        lockButton=control(x,34,w,"Lock",this::toggleLock);x+=w+4;
        undoButton=control(x,34,w,"Undo",this::undoLayout);x+=w+4;
        redoButton=control(x,34,w,"Redo",this::redoLayout);x+=w+4;
        leftButton=control(x,34,w,"Left",()->align(true));x+=w+4;
        topButton=control(x,34,w,"Top",()->align(false));x+=w+4;
        gapXButton=control(x,34,w,"Gap X",()->distribute(true));x+=w+4;
        gapYButton=control(x,34,w,"Gap Y",()->distribute(false));updateControls();
    }
    private net.minecraft.client.gui.components.Button control(int x,int y,int w,String text,Runnable action){
        var button=addRenderableWidget(com.codex.pvphud.gui.PvpButton.styled(Component.literal(text),b->action.run()).bounds(x,y,w,20).build());controls.add(button);return button;
    }
    private void updateControls(){
        controls.forEach(b->b.visible=controlsVisible);if(hideButton==null)return;
        int count=editable().size();hideButton.active=!selection.isEmpty();smallerButton.active=largerButton.active=count>0;
        lockButton.active=!selection.isEmpty();lockButton.setMessage(Component.literal(selection.stream().anyMatch(e->!e.locked())?"Lock":"Unlock"));
        undoButton.active=history.canUndo();redoButton.active=history.canRedo();
        leftButton.active=topButton.active=count>1;gapXButton.active=gapYButton.active=count>2;
    }
    private void select(HudElement e,boolean toggle){
        if(toggle){if(!selection.remove(e))selection.add(e);selected=selection.contains(e)?e:selection.stream().findFirst().orElse(null);}
        else{if(!selection.contains(e)){selection.clear();selection.add(e);}selected=e;}
        status="";updateControls();
    }
    private void hideSelected(){if(selection.isEmpty())return;edit(()->{selection.forEach(e->e.setEnabled(false));selection.clear();selected=null;});}
    private void toggleLock(){if(selection.isEmpty())return;edit(()->{boolean lock=selection.stream().anyMatch(e->!e.locked());selection.forEach(e->e.setLocked(lock));});}
    private void align(boolean horizontal){
        var items=editable();if(items.size()<2)return;
        float target=items.stream().map(e->horizontal?e.x():e.y()).min(Float::compare).orElse(0F);
        edit(()->items.forEach(e->e.setPosition(horizontal?target:e.x(),horizontal?e.y():target)));
    }
    private void distribute(boolean horizontal){
        var items=new ArrayList<>(editable());if(items.size()<3)return;
        items.sort(Comparator.comparingDouble(e->horizontal?e.x():e.y()));
        float start=horizontal?items.getFirst().x():items.getFirst().y();
        float end=0;for(var e:items)end=Math.max(end,(horizontal?e.x():e.y())+(horizontal?e.width(minecraft):e.height(minecraft))*e.scale());
        float total=0;for(var e:items)total+=(horizontal?e.width(minecraft):e.height(minecraft))*e.scale();
        float gap=(end-start-total)/(items.size()-1);
        if(gap<0){status="Not enough room for non-overlapping gaps";return;}
        edit(()->{float at=start;for(var e:items){e.setPosition(horizontal?at:e.x(),horizontal?e.y():at);at+=(horizontal?e.width(minecraft):e.height(minecraft))*e.scale()+gap;}});
    }
    @Override public void extractRenderState(GuiGraphicsExtractor c,int mx,int my,float delta){
        var theme=ThemeManager.getInstance().getTheme();c.fill(0,0,width,height,theme.background());
        if(minecraft!=null){manager.render(c,minecraft,true);
            if(guideX!=null)c.fill(guideX,0,guideX+1,height,theme.accentSecondary());if(guideY!=null)c.fill(0,guideY,width,guideY+1,theme.accentSecondary());
            for(var e:manager.layoutElements())if(e.enabled())outline(c,e,theme);
        }
        if(controlsVisible){
            int w=Math.min(420,width-16);RoundedRenderer.fill(c,(width-w)/2,4,w,54,6,theme.panelHeader());
            String hint=status.isEmpty()?"Ctrl+click: multi | L: lock | Ctrl+Z / Y | Tab: toolbar":status;
            c.centeredText(font,Component.literal(font.plainSubstrByWidth(hint,width-16)),width/2,61,theme.mutedText());
        }
        super.extractRenderState(c,mx,my,delta);
    }
    @Override public boolean mouseClicked(MouseButtonEvent click,boolean doubled){
        if(super.mouseClicked(click,doubled))return true;
        if(minecraft!=null&&(click.button()==0||click.button()==1)){
            for(var e:manager.layoutElements().reversed()){
                if(!e.enabled())continue;
                boolean body=e.contains(click.x(),click.y(),minecraft);
                if(click.button()==0&&controlDown&&body){select(e,true);return true;}
                if(overCloseButton(click.x(),click.y(),e)||click.button()==1&&body){select(e,false);hideSelected();return true;}
                boolean handle=!e.locked()&&overResizeHandle(click.x(),click.y(),e);
                if(body||handle){select(e,false);if(e.locked())return true;
                    gesture=snapshot();dragging=e;resizing=handle;offsetX=click.x()-e.x();offsetY=click.y()-e.y();resizeStartX=click.x();resizeStartScale=e.scale();return true;
                }
            }
            if(!controlDown){selection.clear();selected=null;updateControls();}
        }
        return super.mouseClicked(click,doubled);
    }
    @Override public boolean mouseDragged(MouseButtonEvent click,double dx,double dy){
        if(dragging==null)return super.mouseDragged(click,dx,dy);
        if(resizing)resizeSelected(resizeStartScale+(float)((click.x()-resizeStartX)/Math.max(40,dragging.width(minecraft))),true);
        else moveWithSnapping((float)(click.x()-offsetX),(float)(click.y()-offsetY));return true;
    }
    @Override public boolean mouseScrolled(double x,double y,double horizontal,double vertical){
        for(var e:manager.layoutElements().reversed())if(e.enabled()&&e.contains(x,y,minecraft)){
            select(e,false);if(!e.locked())resizeSelected((float)vertical*.05F,false);return true;
        }
        return super.mouseScrolled(x,y,horizontal,vertical);
    }
    private void resizeSelected(float value,boolean absolute){
        var items=editable();if(items.isEmpty())return;
        var base=selected!=null&&!selected.locked()?selected:items.getFirst();float difference=absolute?value-base.scale():value;
        edit(()->{for(var e:items){float maximum=Math.min(2.5F,Math.min(width/(float)Math.max(1,e.width(minecraft)),height/(float)Math.max(1,e.height(minecraft))));e.setScale(Math.min(e.scale()+difference,Math.max(.5F,maximum)));e.keepOnScreen(minecraft);}});
    }
    private void moveWithSnapping(float px,float py){
        var items=editable();if(items.isEmpty()||gesture==null)return;
        var anchor=gesture.get(dragging);float ew=dragging.width(minecraft)*anchor.scale(),eh=dragging.height(minecraft)*anchor.scale();
        var xs=new ArrayList<Float>(List.of(0F,width/2F,(float)width));var ys=new ArrayList<Float>(List.of(0F,height/2F,(float)height));
        for(var e:manager.layoutElements())if(e.enabled()&&!selection.contains(e)){float w=e.width(minecraft)*e.scale(),h=e.height(minecraft)*e.scale();xs.add(e.x());xs.add(e.x()+w/2);xs.add(e.x()+w);ys.add(e.y());ys.add(e.y()+h/2);ys.add(e.y()+h);}
        Snap sx=snap(px,ew,xs),sy=snap(py,eh,ys);float dx=sx.position()-anchor.x(),dy=sy.position()-anchor.y();
        float minX=Float.MAX_VALUE,minY=Float.MAX_VALUE,maxX=0,maxY=0;
        for(var e:items){var l=gesture.get(e);minX=Math.min(minX,l.x());minY=Math.min(minY,l.y());maxX=Math.max(maxX,l.x()+e.width(minecraft)*l.scale());maxY=Math.max(maxY,l.y()+e.height(minecraft)*l.scale());}
        dx=Math.clamp(dx,-minX,Math.max(-minX,width-maxX));dy=Math.clamp(dy,-minY,Math.max(-minY,height-maxY));
        for(var e:items){var l=gesture.get(e);e.setPosition(l.x()+dx,l.y()+dy);}guideX=sx.guide();guideY=sy.guide();
    }
    @Override public boolean mouseReleased(MouseButtonEvent click){commitGesture();return super.mouseReleased(click);}
    @Override public boolean keyPressed(KeyEvent input){
        int key=input.key();boolean ctrl=(input.modifiers()&GLFW_MOD_CONTROL)!=0;
        controlDown=ctrl||key==GLFW_KEY_LEFT_CONTROL||key==GLFW_KEY_RIGHT_CONTROL;
        if(key==GLFW_KEY_TAB){controlsVisible=!controlsVisible;setFocused(null);updateControls();return true;}
        if(ctrl&&key==GLFW_KEY_Z){if((input.modifiers()&GLFW_MOD_SHIFT)!=0)redoLayout();else undoLayout();return true;}
        if(ctrl&&key==GLFW_KEY_Y){redoLayout();return true;}
        if(ctrl&&key==GLFW_KEY_A){selection.clear();manager.layoutElements().stream().filter(HudElement::enabled).forEach(selection::add);selected=selection.stream().findFirst().orElse(null);updateControls();return true;}
        if(key==GLFW_KEY_L){toggleLock();return true;}
        if(!selection.isEmpty()){
            if(key==GLFW_KEY_DELETE||key==GLFW_KEY_BACKSPACE){hideSelected();return true;}
            if(key==GLFW_KEY_EQUAL||key==GLFW_KEY_KP_ADD){resizeSelected(.1F,false);return true;}
            if(key==GLFW_KEY_MINUS||key==GLFW_KEY_KP_SUBTRACT){resizeSelected(-.1F,false);return true;}
            if(key==GLFW_KEY_R){edit(()->editable().forEach(e->{e.resetLayout();e.keepOnScreen(minecraft);}));return true;}
            int step=(input.modifiers()&GLFW_MOD_SHIFT)!=0?10:1,dx=key==GLFW_KEY_LEFT?-step:key==GLFW_KEY_RIGHT?step:0,dy=key==GLFW_KEY_UP?-step:key==GLFW_KEY_DOWN?step:0;
            if(dx!=0||dy!=0){edit(()->nudge(dx,dy));return true;}
        }
        return super.keyPressed(input);
    }
    private void nudge(float dx,float dy){
        var items=editable();if(items.isEmpty())return;
        float minX=Float.MAX_VALUE,minY=Float.MAX_VALUE,maxX=0,maxY=0;
        for(var e:items){minX=Math.min(minX,e.x());minY=Math.min(minY,e.y());maxX=Math.max(maxX,e.x()+e.width(minecraft)*e.scale());maxY=Math.max(maxY,e.y()+e.height(minecraft)*e.scale());}
        dx=Math.clamp(dx,-minX,Math.max(-minX,width-maxX));dy=Math.clamp(dy,-minY,Math.max(-minY,height-maxY));
        for(var e:items)e.setPosition(e.x()+dx,e.y()+dy);
    }
    @Override public boolean keyReleased(KeyEvent input){if(input.key()==GLFW_KEY_LEFT_CONTROL||input.key()==GLFW_KEY_RIGHT_CONTROL)controlDown=false;return super.keyReleased(input);}
    @Override public void onClose(){commitGesture();if(minecraft!=null)minecraft.setScreen(parent);}
    @Override public void removed(){commitGesture();externalBefore=snapshot();save.run();super.removed();}
    @Override public boolean isPauseScreen(){return false;}
    private void outline(GuiGraphicsExtractor c,HudElement e,com.codex.pvphud.theme.Theme t){
        int x=Math.round(e.x()),y=Math.round(e.y()),w=Math.round(e.width(minecraft)*e.scale()),h=Math.round(e.height(minecraft)*e.scale());
        int color=e.locked()?t.mutedText():selection.contains(e)?t.accentSecondary():t.accent();
        c.fill(x,y,x+w,y+1,color);c.fill(x,y+h-1,x+w,y+h,color);c.fill(x,y,x+1,y+h,color);c.fill(x+w-1,y,x+w,y+h,color);
        if(!e.locked())RoundedRenderer.fill(c,x+w-5,y+h-5,9,9,3,color);
        RoundedRenderer.fill(c,x+w-12,y+2,10,10,2,t.panelHeader());c.centeredText(font,Component.literal("x"),x+w-7,y+3,t.text());
        if(e.locked())c.text(font,"L",x+2,y+2,t.mutedText(),true);
        if(selection.contains(e)){String text=String.format(Locale.ROOT,"%.2fx",e.scale());c.text(font,text,Math.clamp(x+w-font.width(text)-3,0,Math.max(0,width-font.width(text))),Math.max(0,y+h-12),t.text(),false);}
    }
    private boolean overCloseButton(double x,double y,HudElement e){int right=Math.round(e.x()+e.width(minecraft)*e.scale());return inside(x,y,right-12,Math.round(e.y())+2,10,10);}
    private boolean overResizeHandle(double x,double y,HudElement e){int right=Math.round(e.x()+e.width(minecraft)*e.scale()),bottom=Math.round(e.y()+e.height(minecraft)*e.scale());return inside(x,y,right-7,bottom-7,14,14);}
    private static boolean inside(double x,double y,int left,int top,int width,int height){return x>=left&&y>=top&&x<left+width&&y<top+height;}
    private static Snap snap(float position,float size,List<Float> targets){float best=position,distance=6.1F;Integer guide=null;for(float target:targets)for(float anchor:new float[]{0,size/2,size}){float next=target-anchor,delta=Math.abs(next-position);if(delta<distance){distance=delta;best=next;guide=Math.round(target);}}return new Snap(best,guide);}
    private record Snap(float position,Integer guide){}
}
