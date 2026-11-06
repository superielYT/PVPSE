package com.codex.pvphud;

import com.codex.pvphud.notification.NotificationManager;
import net.minecraft.client.Minecraft;
import java.lang.ref.WeakReference;
import java.util.*;

public final class FrameMonitor {
    private static final FrameTimes recent=new FrameTimes();
    private static long last,statsAt;
    private static WeakReference<Object> world=new WeakReference<>(null);
    private static FrameTimes.Stats cached=FrameTimes.summarize(new double[0]);
    private static final Map<String,Result> results=new LinkedHashMap<>();
    private static Session session;
    public record Scene(double x,double y,double z,float yaw,float pitch,int width,int height) {
        static Scene capture(Minecraft mc){return new Scene(mc.player.getX(),mc.player.getY(),mc.player.getZ(),mc.player.getYRot(),mc.player.getXRot(),mc.getWindow().getWidth(),mc.getWindow().getHeight());}
        boolean matches(Scene other){double angle=Math.abs(Math.IEEEremainder(yaw-other.yaw,360));return Math.pow(x-other.x,2)+Math.pow(y-other.y,2)+Math.pow(z-other.z,2)<.25 && angle<=3 && Math.abs(pitch-other.pitch)<=3 && width==other.width && height==other.height;}
    }
    public record Result(String label,FrameTimes.Stats stats,int view,int fpsCap,boolean vsync,Scene scene) {}
    private static final class Session {
        final String label;final Object world,player;final Scene scene;
        final PerformanceController.Snapshot before,applied;
        final ArrayList<Double> values=new ArrayList<>();
        long warmup=3_000_000_000L,elapsed,target=15_000_000_000L;
        Session(String label,Minecraft mc,PerformanceMode mode){
            this.label=label;world=mc.level;player=mc.player;scene=Scene.capture(mc);before=PerformanceController.Snapshot.capture(mc);
            if(mode!=null)PerformanceController.applyLimits(mc,before,mode);
            applied=PerformanceController.Snapshot.capture(mc);
            if(mode!=null)com.codex.pvphud.render.RenderBudget.configure(mode,PvpHudClient.getInstance().performanceLightHud());
        }
    }
    private FrameMonitor() {}
    public static boolean active(){return session!=null;}
    public static Collection<Result> results(){return List.copyOf(results.values());}
    public static String progress(){return session==null?"":session.warmup>0?"Benchmark: warming up":"Benchmark: "+Math.max(0,(int)Math.ceil((session.target-session.elapsed)/1e9))+"s";}
    public static double[] recent(){return recent.last(144);}
    public static FrameTimes.Stats stats(){long now=System.nanoTime();if(now-statsAt>500_000_000L){cached=FrameTimes.summarize(recent.last(600));statsAt=now;}return cached;}
    public static void start(Minecraft mc,String label,PerformanceMode mode){
        start(mc,label,mode,3_000_000_000L,15_000_000_000L);
    }
    static void start(Minecraft mc,String label,PerformanceMode mode,long warmup,long duration){
        cancel(mc);if(mc.player==null||mc.level==null)return;
        Scene scene=Scene.capture(mc);var before=PerformanceController.Snapshot.capture(mc);
        if(results.values().stream().anyMatch(r->!r.scene().matches(scene)||r.fpsCap()!=before.fpsCap||r.vsync()!=before.vsync))results.clear();
        session=new Session(label,mc,mode);session.warmup=Math.max(0,warmup);session.target=Math.max(1_000_000L,duration);last=0;mc.setScreen(null);
    }
    public static void tick(Minecraft mc){if(session!=null&&(session.world!=mc.level||session.player!=mc.player))cancel(mc);}
    public static void cancel(Minecraft mc){
        if(session==null)return;
        PerformanceController.restoreSnapshot(mc,session.before,session.applied);session=null;last=0;
        var mod=PvpHudClient.getInstance();if(mod!=null&&mod.performance()!=null)mod.performance().refreshBudget();
        mc.options.save();
    }
    public static void frame(Minecraft mc){
        long now=System.nanoTime();
        if(mc.player==null||mc.level==null){cancel(mc);last=0;return;}
        if(world.get()!=mc.level){cancel(mc);world=new WeakReference<>(mc.level);recent.clear();results.clear();last=0;}
        if(mc.screen!=null){last=0;return;}
        if(last==0){last=now;return;}
        long elapsed=now-last;last=now;
        if(elapsed<=0||elapsed>10_000_000_000L){cancel(mc);return;}
        recent.add(elapsed/1e6);
        var s=session;if(s==null)return;
        if(!s.scene.matches(Scene.capture(mc))||!PerformanceController.sameSettings(s.applied,PerformanceController.Snapshot.capture(mc))){
            cancel(mc);NotificationManager.getInstance().push("Benchmark canceled","View or settings changed; original options restored");return;
        }
        if(s.warmup>0){s.warmup-=elapsed;return;}
        s.elapsed+=elapsed;if(s.values.size()<60000)s.values.add(elapsed/1e6);
        if(s.elapsed>=s.target){
            var stats=FrameTimes.summarize(s.values.stream().mapToDouble(Double::doubleValue).toArray());
            results.put(s.label,new Result(s.label,stats,s.applied.view,s.applied.fpsCap,s.applied.vsync,s.scene));
            cancel(mc);NotificationManager.getInstance().push("Benchmark complete",s.label+": "+Math.round(stats.fps())+" FPS / "+Math.round(stats.lowOnePercent())+" 1% low. Options restored.");
        }
    }
    public static String report(){StringBuilder out=new StringBuilder("PVPSE frame benchmark (15s after 3s warmup)\n");for(var r:results.values())out.append(String.format(Locale.ROOT,"%s: %.1f FPS, %.1f 1%% low, p95 %.2fms, p99 %.2fms; view %d, cap %d, VSync %s; %dx%d\n",r.label(),r.stats().fps(),r.stats().lowOnePercent(),r.stats().p95(),r.stats().p99(),r.view(),r.fpsCap(),r.vsync(),r.scene().width(),r.scene().height()));return out.toString();}
}
