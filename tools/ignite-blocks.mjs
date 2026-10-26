import {Canvas,rgba,mix} from './ignite-art.mjs';
// Small, tileable value changes keep terrain readable instead of adding noisy outlines.
export function drawBlocks(){
    const sprites=[];
    const add=(name,base,kind='stone',highlight)=>{
        const c=new Canvas(),color=rgba(base),shade=mix(color,rgba('#19202d'),.18);
        c.paint(()=>true,color);
        const box=(x,y,w,h,col)=>c.poly([[x,y],[x+w,y],[x+w,y+h],[x,y+h]],col);
        if(kind==='plank'){
            for(let y=0;y<32;y+=8){
                box(0,y,32,1,shade);box(0,y+1,32,1,mix(color,rgba('#fff1d5'),.12));
                const seam=(y/8%2)*16+7;box(seam,y+1,1,7,shade);
                for(let row=0;row<3;row++){
                    const x=(row*11+y*3+3)%32,len=4+(row+y)%7;
                    for(let xx=0;xx<len;xx++)box((x+xx)%32,y+3+row,1,.6,mix(color,shade,.35));
                }
            }
        }else if(kind==='wool'){
            for(let y=0;y<32;y+=4)for(let x=0;x<32;x+=4){
                box(x,y,3,1,mix(color,rgba('#ffffff'),.05));box(x+3,y+1,1,3,mix(color,shade,.45));
            }
        }else if(kind==='brick'){
            for(let y=0;y<32;y+=8){box(0,y,32,1,shade);box(0,y+1,32,1,mix(color,rgba('#ffffff'),.1));
                for(let x=(y/8%2)*8;x<32;x+=16)box(x,y,1,8,shade);}
        }else{
            for(let y=0;y<32;y+=4)for(let x=0;x<32;x+=4){
                const n=(x*13+y*7+x*y*3)%17;
                box(x,y,4,4,mix(color,n<8?shade:rgba('#e2e5df'),n<8?.12+n*.035:.025+(n-8)*.006));
                if(n===3||n===12)box(x+1,y+1,2,1,mix(color,shade,.7));
            }
            if(kind==='obsidian')for(const [x,y]of [[2,3],[19,2],[9,15],[23,22]]){
                c.path([[x,y],[x+6,y+1],[x+8,y+6],[x+3,y+9]],1.2,rgba(highlight||'#675779'));
                c.line([x+1,y+1],[x+5,y+2],.6,rgba(highlight?'#e4a9f3':'#8d78a0'));
            }
            if(kind==='ore')for(const [x,y]of [[4,5],[19,4],[12,15],[24,23],[3,25]]){
                c.poly([[x,y],[x+4,y-1],[x+6,y+2],[x+4,y+5],[x,y+4],[x-1,y+1]],shade);
                c.poly([[x+.7,y+.6],[x+3.5,y],[x+4.8,y+2],[x+3.5,y+3.8],[x+.5,y+3]],rgba(highlight));
                c.line([x+1,y+1],[x+3,y+.6],.65,mix(rgba(highlight),rgba('#ffffff'),.5));
            }
        }
        const pixels=c.data;
        for(let y=0;y<128;y++)for(let x=0;x<128;x++){
            const at=(y*128+x)*4,ix=Math.floor(x/4),iy=Math.floor(y/4);
            const hash=((ix*374761393+iy*668265263)^(ix*iy*1274126177))>>>0;
            const n=((hash^(hash>>>13))%13)-6,detail=kind==='plank'?n*.55:kind==='wool'?n*.35:n*.8;
            for(let k=0;k<3;k++)pixels[at+k]=Math.max(0,Math.min(255,pixels[at+k]+detail));
        }
        sprites.push({name:'block/'+name,label:name.toUpperCase().replaceAll('_',' '),canvas:c});
    };
    for(const [id,col,kind,glow]of [
        ['stone','#858b90'],['cobblestone','#767e87','brick'],['stone_bricks','#899098','brick'],
        ['andesite','#929498'],['polished_andesite','#9b9da0','brick'],['granite','#a47765'],
        ['polished_granite','#ad8777','brick'],['diorite','#c4c5bd'],['polished_diorite','#d1d0c7','brick'],
        ['tuff','#74796e'],['calcite','#e2dfd4'],['blackstone','#34313d'],['polished_blackstone','#413c4d','brick'],
        ['polished_blackstone_bricks','#3b3745','brick'],['netherrack','#78413e'],['nether_bricks','#452c3a','brick'],
        ['red_nether_bricks','#743537','brick'],['bricks','#ad6b54','brick'],['mud','#514c50'],
        ['packed_mud','#8d6b51'],['mud_bricks','#967557','brick'],['clay','#a2a9b2'],
        ['snow','#eef5fa'],['powder_snow','#eef5fa'],['red_sand','#c88950'],['soul_sand','#5e4c40'],['soul_soil','#594538'],
        ['deepslate','#4c515e'],['cobbled_deepslate','#505561','brick'],['deepslate_bricks','#454b59','brick'],
        ['obsidian','#252238','obsidian'],['crying_obsidian','#302440','obsidian','#b875dd'],
        ['end_stone','#d9d6a1'],['dirt','#94705a'],['sand','#e3d4aa'],['gravel','#969092']
    ])add(id,col,kind,glow);
    for(const [id,col]of [['oak','#ba9763'],['spruce','#805e41'],['birch','#d9c58d'],['jungle','#b88c6e'],
        ['acacia','#b96d4f'],['dark_oak','#624936'],['mangrove','#82474c'],['cherry','#e4b5ba'],
        ['pale_oak','#dfd5c8'],['crimson','#793b58'],['warped','#3d8b83']])add(id+'_planks',col,'plank');
    for(const [id,col]of [['white','#e4e8e8'],['orange','#e6a064'],['magenta','#bd7fbc'],['light_blue','#8bc7df'],
        ['yellow','#efd37a'],['lime','#a8cb7f'],['pink','#e6acbf'],['gray','#555c6a'],['light_gray','#b1b5bd'],
        ['cyan','#589ba7'],['purple','#9676b6'],['blue','#667fae'],['brown','#92715b'],['green','#7e9560'],
        ['red','#c66c78'],['black','#303746']])add(id+'_wool',col,'wool');
    for(const deep of [false,true])for(const [ore,col]of [['coal','#353b46'],['iron','#d6b6a0'],['copper','#cf936d'],
        ['gold','#e3bf65'],['redstone','#d46d82'],['lapis','#7094ce'],['diamond','#7bd4ce'],['emerald','#8fcf9b']]){
        add((deep?'deepslate_':'')+ore+'_ore',deep?'#505663':'#858b90','ore',col);
    }
    const colors=[['white','#e4e8e8'],['orange','#e6a064'],['magenta','#bd7fbc'],['light_blue','#8bc7df'],
        ['yellow','#efd37a'],['lime','#a8cb7f'],['pink','#e6acbf'],['gray','#555c6a'],['light_gray','#b1b5bd'],
        ['cyan','#589ba7'],['purple','#9676b6'],['blue','#667fae'],['brown','#92715b'],['green','#7e9560'],['red','#c66c78'],['black','#303746']];
    for(const [id,col]of colors){add(id+'_concrete',col);add(id+'_terracotta',col);}
    for(const [id,col]of [['diamond','#48cddd'],['emerald','#40b279'],['gold','#e8c05a'],['iron','#d5dbe0'],
        ['copper','#c17c5c'],['netherite','#514954'],['lapis','#3f69a5'],['redstone','#b43a44'],['coal','#343741']])add(id+'_block',col,'brick');
    const natural=(name,draw)=>{const c=new Canvas();draw(c);sprites.push({name:'block/'+name,label:name.toUpperCase().replaceAll('_',' '),canvas:c});};
    const noise=(x,y)=>{let n=(Math.floor(x)*374761393+Math.floor(y)*668265263)>>>0;n=((n^(n>>>13))*1274126177)>>>0;return(n%1024)/1023;};
    natural('grass_block_top',c=>c.paint(()=>true,(x,y)=>mix(rgba('#89947c'),rgba('#d2d6bd'),noise(x,y)*.7)));
    natural('grass_block_side',c=>{c.paint(()=>true,(x,y)=>mix(rgba('#80604a'),rgba('#b9926b'),noise(x,y)*.7));c.paint((x,y)=>y<5+noise(x,0)*3,(x,y)=>mix(rgba('#72865a'),rgba('#aab87a'),noise(x,y)*.7));});
    natural('grass_block_side_overlay',c=>c.paint((x,y)=>y<5+noise(x,0)*3,(x,y)=>mix(rgba('#89947c'),rgba('#d2d6bd'),noise(x,y)*.7)));
    for(const [wood,col]of [['oak','#8e7450'],['spruce','#62503c'],['birch','#d0cabb'],['jungle','#99795d'],
        ['acacia','#7f7669'],['dark_oak','#4d4030'],['mangrove','#655149'],['cherry','#5e3c47'],['pale_oak','#c9c5b6']]){
        natural(wood+'_log',c=>{const color=rgba(col);c.paint(()=>true,(x,y)=>{const grain=Math.sin(x*1.1+Math.sin(y*.32)*.7);return mix(color,grain>0?rgba('#ead0a0'):rgba('#292725'),Math.abs(grain)*.2+noise(x,y)*.09);});});
        natural(wood+'_log_top',c=>{const color=mix(rgba(col),rgba('#eacb96'),.5);c.paint(()=>true,(x,y)=>{const r=Math.hypot(x-15.5,y-15.5),edge=Math.max(Math.abs(x-16),Math.abs(y-16));return edge>13?mix(rgba(col),rgba('#292725'),noise(x,y)*.2):mix(color,rgba('#75523c'),(.5+.5*Math.sin(r*2.1))*.24+noise(x,y)*.08);});});
    }
    for(const type of ['oak','spruce','birch','jungle','acacia','dark_oak','mangrove','pale_oak'])natural(type+'_leaves',c=>{
        c.paint((x,y)=>noise(x,y)>.16,(x,y)=>mix(rgba('#626e53'),rgba('#bac5a2'),noise(x+8,y+11)*.8));
    });
    return sprites;
}
