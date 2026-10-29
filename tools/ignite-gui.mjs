// Original native-size GUI sprites. Geometry follows Minecraft 26.1.2 atlas slots.
export function drawGui() {
    const result=[];
    const panel=[15,19,27,235],edge=[51,63,78,235],accent=[255,157,87,255];
    function sprite(name,w,h,draw,meta) {
        const pixels=Buffer.alloc(w*h*4);
        const rect=(x,y,rw,rh,color)=>{
            for(let yy=Math.max(0,y);yy<Math.min(h,y+rh);yy++)
                for(let xx=Math.max(0,x);xx<Math.min(w,x+rw);xx++)pixels.set(color,(yy*w+xx)*4);
        };
        const frame=(x,y,rw,rh,color)=>{
            rect(x+1,y,rw-2,1,color);rect(x+1,y+rh-1,rw-2,1,color);
            rect(x,y+1,1,rh-2,color);rect(x+rw-1,y+1,1,rh-2,color);
        };
        draw(rect,frame);result.push({name:name.startsWith('../container/')?'gui/container/'+name.slice(13):'gui/sprites/'+name,width:w,height:h,pixels,meta});
    }
    sprite('hud/hotbar',182,22,(r,f)=>{
        r(1,1,180,20,panel);f(0,0,182,22,edge);
        r(2,1,178,1,[116,131,145,130]);
        for(let slot=1;slot<9;slot++)r(slot*20+1,3,1,16,[83,98,111,105]);
    });
    sprite('hud/hotbar_selection',24,23,(r,f)=>{
        f(0,0,24,23,[40,29,20,240]);f(1,1,22,21,accent);
        r(3,1,18,1,[255,230,177,255]);r(9,21,6,1,accent);
    });
    for(const side of ['left','right'])sprite('hud/hotbar_offhand_'+side,29,24,(r,f)=>{
        const x=side==='left'?0:7;r(x+1,2,20,20,panel);f(x,1,22,22,edge);
        r(x+3,2,16,1,[116,131,145,130]);
    });
    for(const progress of [false,true])sprite('hud/experience_bar_'+(progress?'progress':'background'),182,5,(r,f)=>{
        if(!progress){r(1,1,180,3,[12,21,28,220]);f(0,0,182,5,edge);}
        else{r(1,1,180,3,[68,183,156,255]);r(1,1,180,1,[166,242,204,255]);r(1,3,180,1,[39,119,114,255]);}
    });
    for(const state of ['button','button_highlighted','button_disabled'])sprite('widget/'+state,200,20,(r,f)=>{
        const hover=state.endsWith('highlighted'),disabled=state.endsWith('disabled');
        r(1,1,198,18,disabled?[26,32,40,230]:hover?[48,49,49,250]:[29,39,50,245]);
        f(0,0,200,20,disabled?[49,58,67,255]:hover?accent:edge);
        r(3,1,194,1,disabled?[44,51,59,255]:hover?[255,221,157,255]:[97,115,133,180]);
        r(3,18,194,1,[10,17,24,170]);
    },{gui:{scaling:{type:'nine_slice',width:200,height:20,border:3}}});
    // Keep vanilla UV positions and slot centers. Pale title strips retain black-label contrast.
    for(const name of ['inventory','generic_54','crafting_table','shulker_box','furnace','blast_furnace','smoker']){
        const chest=name==='generic_54',h=chest?222:166;
        const background=(r,f)=>{
            r(1,1,174,h-2,[18,23,32,255]);f(0,0,176,h,[8,12,19,255]);
            const furnace=['furnace','blast_furnace','smoker'].includes(name);
            if(name!=='inventory')r(furnace?44:6,4,furnace?88:82,11,[158,169,185,255]);r(6,2,22,1,accent);
            const slot=(x,y)=>{r(x-1,y-1,18,18,[10,15,23,255]);f(x-1,y-1,18,18,[53,67,84,255]);r(x,y,16,1,[6,10,16,255]);};
            const main=chest?140:84;
            if(name!=='inventory')r(6,main-16,60,11,[158,169,185,255]);
            for(let row=0;row<3;row++)for(let col=0;col<9;col++)slot(8+col*18,main+row*18);
            for(let col=0;col<9;col++)slot(8+col*18,main+58);
            if(chest)for(let row=0;row<6;row++)for(let col=0;col<9;col++)slot(8+col*18,18+row*18);
            else if(name==='inventory'){
                r(26,18,52,52,[14,23,33,255]);f(25,17,54,54,[72,87,102,255]);
                for(let row=0;row<4;row++)slot(8,8+row*18);
                slot(77,62);
                r(95,5,70,12,[158,169,185,255]);
                for(let row=0;row<2;row++)for(let col=0;col<2;col++)slot(98+col*18,26+row*18);
                slot(154,36);r(136,35,9,2,accent);r(141,32,2,8,accent);
            }else if(name==='shulker_box'){
                for(let row=0;row<3;row++)for(let col=0;col<9;col++)slot(8+col*18,18+row*18);
            }else if(furnace){
                slot(56,17);slot(56,53);slot(116,35);r(80,34,22,16,[35,43,55,255]);r(57,37,12,12,[35,43,55,255]);
            }else{
                for(let row=0;row<3;row++)for(let col=0;col<3;col++)slot(30+col*18,17+row*18);
                slot(124,35);r(91,34,19,3,accent);r(105,31,3,9,accent);
            }
        };
        sprite('../container/'+name,256,256,background);
    }
    return result;
}
