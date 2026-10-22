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
        sprites.push({name:'block/'+name,label:name.toUpperCase().replaceAll('_',' '),canvas:c});
    };
    for(const [id,col,kind,glow]of [
        ['stone','#858b90'],['cobblestone','#767e87','brick'],['stone_bricks','#899098','brick'],
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
    return sprites;
}
