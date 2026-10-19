// Original PVPSE vector pixel art. Draw at 4x, then resolve premultiplied coverage.
const S=4,N=32*S;
const rgba=hex=>[...Buffer.from(hex.slice(1),'hex'),255];
const mix=(a,b,t)=>a.map((v,i)=>Math.round(v+(b[i]-v)*Math.max(0,Math.min(1,t))));
const ink=rgba('#252236'),gold=rgba('#edaa58'),shine=rgba('#fff2c6');
class Canvas {
    constructor(){this.data=Buffer.alloc(N*N*4);}
    paint(test,color){
        for(let y=0;y<N;y++)for(let x=0;x<N;x++){
            const xx=(x+.5)/S,yy=(y+.5)/S;
            if(test(xx,yy))this.data.set(typeof color==='function'?color(xx,yy):color,(y*N+x)*4);
        }
    }
    poly(points,color){
        this.paint((x,y)=>{let inside=false;for(let i=0,j=points.length-1;i<points.length;j=i++){
            const [a,b]=points[i],[c,d]=points[j];
            if((b>y)!==(d>y)&&x<(c-a)*(y-b)/(d-b)+a)inside=!inside;
        }return inside;},color);
    }
    ellipse(x,y,rx,ry,color){this.paint((a,b)=>((a-x)/rx)**2+((b-y)/ry)**2<=1,color);}
    line(a,b,width,color){
        const dx=b[0]-a[0],dy=b[1]-a[1],length=dx*dx+dy*dy;
        this.paint((x,y)=>{const t=length?Math.max(0,Math.min(1,((x-a[0])*dx+(y-a[1])*dy)/length)):0;
            return (x-a[0]-t*dx)**2+(y-a[1]-t*dy)**2<=width*width/4;},color);
    }
    path(points,width,color){for(let i=1;i<points.length;i++)this.line(points[i-1],points[i],width,color);}
    resolve(size=32){
        const data=Buffer.alloc(size*size*4),step=N/size;
        for(let y=0;y<size;y++)for(let x=0;x<size;x++){
            let a=0,r=0,g=0,b=0;
            for(let yy=0;yy<step;yy++)for(let xx=0;xx<step;xx++){
                const i=((y*step+yy)*N+x*step+xx)*4,alpha=this.data[i+3]/255;
                a+=alpha;r+=this.data[i]*alpha;g+=this.data[i+1]*alpha;b+=this.data[i+2]*alpha;
            }
            const i=(y*size+x)*4;if(a){data[i]=r/a;data[i+1]=g/a;data[i+2]=b/a;data[i+3]=255*a/(step*step);}
        }
        return data;
    }
}
export function drawArt(){
    const sprites=[];
    const add=(name,label,draw)=>{const c=new Canvas();draw(c);sprites.push({name,label,canvas:c});};
    const materials=[
        ['wooden','#b88860','#ecd2a2'],['stone','#99a3b5','#e6ebf5'],
        ['copper','#dd9166','#ffe0ba'],['iron','#b5c9de','#f5fcff'],
        ['golden','#e9b44d','#fff4b5'],['diamond','#38cbbd','#d0fff1'],
        ['netherite','#72617e','#d2b7e1']
    ];
    for(const [material,base,edge]of materials)add('item/'+material+'_sword',material.toUpperCase(),c=>{
        const map=([u,v])=>[7+(u+v)*.7071,26+(u-v)*.7071];
        const poly=(p,col)=>c.poly(p.map(map),col);
        poly([[-2.9,9],[-2.9,20],[0,26],[2.9,20],[2.9,9]],ink);
        poly([[-2,10],[-2,20],[0,24.6],[2,20],[2,10]],rgba(base));
        poly([[-2,10],[-2,20],[0,24.6],[0,10]],rgba(edge));
        poly([[.7,10],[.7,20.5],[1.6,19.4],[1.6,10]],mix(rgba(base),ink,.25));
        poly([[-1.8,.7],[-1.8,8.5],[1.8,8.5],[1.8,.7]],ink);
        poly([[-.9,1.5],[-.9,7.8],[.9,7.8],[.9,1.5]],rgba('#a56849'));
        for(let v=2;v<7;v+=1.7)c.line(map([-.8,v]),map([.8,v+.6]),.55,gold);
        poly([[-5,7.5],[-5.4,10],[-3.8,10.9],[-2,9.6],[2,9.6],[3.8,10.9],[5.4,10],[5,7.5]],ink);
        poly([[-4.3,8],[-4.5,9.4],[-3.7,9.8],[-1.6,8.8],[1.6,8.8],[3.7,9.8],[4.5,9.4],[4.3,8]],gold);
        c.ellipse(...map([0,8.6]),1.2,1.2,rgba(base));
        c.ellipse(...map([0,1]),1.4,1.4,gold);
        c.ellipse(...map([-.4,1]),.5,.5,shine);
    });
    for(const enchanted of [false,true])add('item/'+(enchanted?'enchanted_golden_apple':'golden_apple'),enchanted?'ENCHANTED':'GOLD APPLE',c=>{
        const base=rgba(enchanted?'#f4b57c':'#f1bd4f'),shadow=rgba(enchanted?'#c965a5':'#cf7b37');
        c.line([16,9],[18,3],2.8,ink);c.line([16.5,8],[18,3],1.2,rgba('#92664c'));
        c.poly([[18,5],[21,2],[26,3],[23,6]],ink);c.poly([[19,5],[22,3],[25,3.7],[22,5.3]],rgba('#9eca70'));
        c.ellipse(11.5,15.4,8.1,9.2,ink);c.ellipse(20,15.5,8,9.2,ink);
        c.ellipse(15.7,22,8.6,6.2,ink);
        const fill=(x,y)=>mix(base,shadow,(x+y-21)/27);
        c.ellipse(11.7,15.6,7.2,8.3,fill);c.ellipse(19.8,15.6,7.1,8.3,fill);c.ellipse(15.7,21.8,7.7,5.2,fill);
        c.ellipse(10.4,12.7,2.3,3.2,shine);c.ellipse(9,11.4,1,1.3,rgba('#fff9df'));
        c.path([[21,22],[19,24],[16,24.7]],1,mix(base,shine,.3));
        if(enchanted){c.line([25,7],[25,12],1,shine);c.line([22.5,9.5],[27.5,9.5],1,shine);}
    });
    add('item/golden_carrot','GOLD CARROT',c=>{
        c.path([[18,10],[21,5],[25,3]],3,ink);c.path([[20,9],[26,8],[28,4]],3,ink);
        c.path([[18,10],[21,5],[25,3]],1.7,rgba('#9fd27c'));c.path([[20,9],[26,8],[28,4]],1.7,rgba('#62a46b'));
        const shape=[[21,8],[26,12],[23,18],[15,24],[5,29],[8,18],[13,11]];
        c.poly(shape,ink);c.poly([[20,9],[24.8,12.5],[22,17],[14.5,23],[6.7,27],[9,18.5],[13.6,12]],(x,y)=>mix(rgba('#ffe1a0'),rgba('#e89636'),(x+y-20)/22));
        c.path([[19,12],[14,17],[9,24]],1.1,shine);
        c.line([17,16],[21,18],1.1,rgba('#cf8335'));c.line([12,21],[15,22],1,rgba('#cf8335'));
    });
    add('item/totem_of_undying','LUCKY CAT',c=>{
        const cream=rgba('#ffe7ba'),white=rgba('#fff5db'),blush=rgba('#f3a0ab');
        c.path([[22,25],[27,23],[28,18]],3,ink);c.path([[22,25],[27,23],[28,18]],1.6,cream);
        c.ellipse(16,23,7.2,7,ink);c.ellipse(16,22.8,6.2,6.1,cream);
        c.ellipse(12.5,28.3,3,1.5,ink);c.ellipse(19.5,28.3,3,1.5,ink);
        c.ellipse(12.5,28.1,2.3,.8,cream);c.ellipse(19.5,28.1,2.3,.8,cream);
        c.poly([[6.5,12],[6.6,3],[13.6,7],[19,7],[25.4,3],[25.6,12]],ink);
        c.poly([[7.7,11],[7.8,5],[13,8.7],[19,8.7],[24.2,5],[24.4,11]],cream);
        c.poly([[8.8,9],[8.8,6.7],[11.4,9]],blush);c.poly([[20.8,9],[23.2,6.7],[23.2,9]],blush);
        c.ellipse(16,13,10,7.8,ink);c.ellipse(16,12.8,9,6.7,cream);c.ellipse(13,10,5,3,white);
        for(const x of [11.5,20.5]){c.ellipse(x,13,1.3,1.8,ink);c.ellipse(x-.35,12.4,.4,.6,white);}
        c.ellipse(9,15,1.8,.85,blush);c.ellipse(23,15,1.8,.85,blush);
        c.poly([[14.7,14.6],[17.3,14.6],[16,16]],rgba('#ac7564'));
        c.path([[14,16.2],[15,16.9],[16,16.2],[17,16.9],[18,16.2]],.6,ink);
        c.line([10.5,19],[21.5,19],1.5,rgba('#df815b'));c.ellipse(16,20,1.7,1.7,gold);
        c.ellipse(15.5,19.5,.5,.5,shine);
        c.ellipse(10,23,2.3,2.8,white);c.ellipse(22,23,2.3,2.8,white);
        c.poly([[16,26],[12.8,23.1],[13,21.5],[14.5,21],[16,22],[17.5,21],[19,21.5],[19.2,23.1]],rgba('#ed9f77'));
    });
    add('item/ender_pearl','ENDER PEARL',c=>{
        c.ellipse(16,16,12,12,ink);c.ellipse(16,16,10.8,10.8,(x,y)=>mix(rgba('#99f2de'),rgba('#247b86'),(x+y-12)/36));
        c.path([[8,16],[9,11],[12,8],[16,7]],1.3,rgba('#d5fff1'));
        c.path([[15,25],[21,23],[24,18]],1.3,rgba('#32687e'));c.ellipse(10.3,11,1.3,2.1,rgba('#edfff6'));
    });
    add('item/experience_bottle','XP BOTTLE',c=>{
        c.ellipse(16,21,9,9,ink);c.poly([[11,9],[11,17],[21,17],[21,9]],ink);
        c.ellipse(16,21,7.8,7.8,rgba('#c6edf0'));c.poly([[12,10],[12,18],[20,18],[20,10]],rgba('#c6edf0'));
        c.ellipse(16,22,6.8,5.6,(x,y)=>mix(rgba('#c3ee89'),rgba('#4aaf83'),(y-16)/12));
        c.line([12,18],[10.5,23],1.2,rgba('#f2ffed'));
        c.poly([[10,5],[22,5],[22,10],[10,10]],ink);c.poly([[11,6],[21,6],[21,9],[11,9]],gold);
        c.ellipse(17,21,1.2,1.2,rgba('#f0ffbf'));c.ellipse(14,24,.7,.7,rgba('#f0ffbf'));
    });
    add('item/end_crystal','END CRYSTAL',c=>{
        c.poly([[16,2],[28,15],[16,27],[4,15]],ink);c.poly([[16,3.5],[26.4,15],[16,25.5],[5.6,15]],rgba('#d987c5'));
        c.poly([[16,5],[16,23],[7,15]],rgba('#ffb3db'));c.poly([[16,8],[22,15],[16,21],[10,15]],rgba('#fff0fa'));
        c.poly([[16,11],[19,15],[16,18],[13,15]],rgba('#ec8bc5'));
        c.poly([[7,26],[25,26],[28,30],[4,30]],ink);c.poly([[8,26.8],[24,26.8],[26,29],[6,29]],rgba('#6b6286'));
    });
    add('block/cobweb','COBWEB',c=>{
        const center=[15.8,15.3],ends=[];
        for(let k=0;k<8;k++){const angle=k*Math.PI/4;const end=[center[0]+14*Math.cos(angle),center[1]+14*Math.sin(angle)];ends.push(end);c.line(center,end,.8,rgba('#eff5ff'));}
        for(const radius of [4.6,8.6,12.4]){
            const points=ends.map(([x,y])=>[center[0]+(x-center[0])*radius/14,center[1]+(y-center[1])*radius/14]);
            points.push(points[0]);c.path(points,.65,rgba('#c2d4e9'));
        }
        c.ellipse(...center,1,1,rgba('#f7faff'));
    });
    add('item/mace','MACE',c=>{
        c.line([9,27],[19,15],4,ink);c.line([9,27],[19,15],2.2,rgba('#9a7058'));
        for(let i=0;i<4;i++)c.line([9+i*2,26-i*2.4],[11+i*2,27-i*2.4],.8,gold);
        c.poly([[13,8],[21,3],[29,9],[29,16],[21,22],[13,16]],ink);
        c.poly([[14,9],[21,4.5],[27.6,9.5],[21,14]],rgba('#bed7df'));
        c.poly([[14,10],[20,14.8],[20,20],[14,15.4]],rgba('#648698'));
        c.poly([[21,15],[27.6,10.5],[27.6,15.5],[21,20]],rgba('#91adb8'));
        c.line([16,9],[21,6],1,shine);c.line([22,15.5],[26,13],.8,rgba('#78ddcf'));
    });
    for(let stage=-1;stage<8;stage++)add('item/'+(stage<0?'bow':'bow_pulling_'+stage),stage<0?'BOW':'DRAW '+(stage+1),c=>{
        const t=stage<0?0:stage/7,limbX=22-t*2;
        const points=[[9,3],[16,6],[limbX,12],[limbX+.5,16],[limbX,20],[16,26],[9,29]];
        c.path(points,3.1,ink);c.path(points,1.8,rgba('#b77e57'));
        const col=stage===7?rgba('#68e0a2'):mix(rgba('#f4a86b'),rgba('#ffe0a1'),Math.max(0,t));
        c.path(points.slice(0,3),.9,col);c.path(points.slice(4),.9,col);
        const pullX=stage<0?9:8-6*t;c.path([[9,3],[pullX,16],[9,29]],.65,rgba('#f4e9d6'));
        c.line([limbX,13.5],[limbX,18.5],2.7,ink);
        for(let y=14;y<19;y+=1.4)c.line([limbX-1,y],[limbX+1,y+.4],.55,gold);
        if(stage>=0){
            c.line([pullX,16],[29,16],1.3,ink);c.line([pullX,16],[28,16],.65,shine);
            c.poly([[27,13.6],[30.5,16],[27,18.4],[28,16]],col);
            c.poly([[pullX,14],[pullX+3,16],[pullX,18]],rgba('#c9b2d6'));
        }
    });
    return sprites;
}
