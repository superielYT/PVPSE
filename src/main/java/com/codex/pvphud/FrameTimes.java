package com.codex.pvphud;

import java.util.Arrays;

/** Statistics use frame intervals, not sampled FPS counters. */
public final class FrameTimes {
    private final double[] samples=new double[600];
    private int head,size;
    public void add(double milliseconds){if(!Double.isFinite(milliseconds)||milliseconds<=0)return;samples[head]=milliseconds;head=(head+1)%samples.length;size=Math.min(size+1,samples.length);}
    public void clear(){head=0;size=0;}
    public int size(){return size;}
    public double[] last(int maximum){int count=Math.min(size,Math.max(0,maximum));double[] result=new double[count];for(int i=0;i<count;i++)result[i]=samples[(head-count+i+samples.length)%samples.length];return result;}
    public record Stats(int count,double fps,double lowOnePercent,double p95,double p99,double averageMs) {}
    public static Stats summarize(double[] input){
        double[] values=Arrays.stream(input).filter(v->Double.isFinite(v)&&v>0).sorted().toArray();
        if(values.length==0)return new Stats(0,0,0,0,0,0);
        double average=Arrays.stream(values).average().orElse(0),slow=0;int tail=Math.max(1,(int)Math.ceil(values.length*.01));
        for(int i=values.length-tail;i<values.length;i++)slow+=values[i];
        return new Stats(values.length,1000/average,1000/(slow/tail),values[Math.max(0,(int)Math.ceil(values.length*.95)-1)],values[Math.max(0,(int)Math.ceil(values.length*.99)-1)],average);
    }
}
