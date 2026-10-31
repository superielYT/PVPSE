import {Canvas,rgba,mix} from './ignite-art.mjs';
export const weaponMaterials=['wooden','stone','copper','iron','golden','diamond','netherite'];
export const swordEnchants=['fire_aspect','sharpness','smite','bane_of_arthropods'],axeEnchants=['efficiency','sharpness'];
const tintColors={fire_aspect:'#ffab59',sharpness:'#9fe9ff',smite:'#ffe698',bane_of_arthropods:'#a5ed9e',efficiency:'#b5a0ff',flame:'#ffad68',power:'#b6b5ff'};
export function drawWeaponVariants(base){
 const result=[];
 const add=(source,enchant)=>{
  const c=new Canvas();c.data=Buffer.from(source.canvas.data);const tint=rgba(tintColors[enchant]);
  for(let y=0;y<128;y++)for(let x=0;x<128;x++){const i=(y*128+x)*4;
   if(c.data[i+3]&&x>34&&y<90&&c.data[i]+c.data[i+1]+c.data[i+2]>210)c.data.set(mix([...c.data.subarray(i,i+4)],tint,.43),i);
  }
  const axe=source.name.endsWith('_axe'),bow=source.name.includes('bow'),x=axe?23:bow?21:19,y=axe?12:bow?10:16;
  if(enchant==='fire_aspect'||enchant==='flame')c.path([[x-2,y+3],[x,y-3],[x+1,y],[x+3,y-4]],.9,rgba('#fff1be'));
  else if(enchant==='smite'){c.line([x-2,y],[x+2,y],.9,tint);c.line([x,y-3],[x,y+3],.9,tint);}
  else if(enchant==='bane_of_arthropods')c.path([[x-2,y-2],[x+2,y+2],[x,y+3],[x-2,y-2],[x+2,y-2],[x-2,y+2]],.65,tint);
  else c.path([[x-2,y],[x,y-2],[x+2,y],[x,y+2],[x-2,y]],.8,tint);
  if(source.name.endsWith('bow_pulling_7'))c.poly([[27,13.6],[30.5,16],[27,18.4],[28,16]],rgba('#68e0a2'));
  result.push({name:source.name+'_'+enchant,label:enchant.toUpperCase().replaceAll('_',' '),canvas:c});
 };
 for(const material of weaponMaterials){
  for(const e of swordEnchants)add(base.find(s=>s.name==='item/'+material+'_sword'),e);
  for(const e of axeEnchants)add(base.find(s=>s.name==='item/'+material+'_axe'),e);
 }
 for(const sprite of base.filter(s=>/^item\/bow(?:_pulling_\d)?$/.test(s.name)))for(const e of ['flame','power'])add(sprite,e);
 return result;
}
export const orePalette=[['coal','#98a5b4'],['iron','#e8c4aa'],['copper','#efaa79'],['gold','#ffe181'],['redstone','#ff6b82'],['lapis','#80aaff'],['diamond','#90f3ed'],['emerald','#8de7aa']];
const facets=[[2,2,3,2],[10,3,3,2],[6,8,3,2],[12,12,2,2],[2,12,2,2]];
export function drawRadiantBlocks(){
 const textures=[],models=[],rect=(c,x,y,w,h,color)=>c.poly([[x,y],[x+w,y],[x+w,y+h],[x,y+h]],color);
 const ore=(id,base,color)=>{
  const c=new Canvas(),stone=rgba(base),crystal=rgba(color);
  c.paint(()=>true,(x,y)=>mix(stone,rgba('#15202e'),((Math.floor(x)*13+Math.floor(y)*7)%11)*.012));
  for(const [u,v,w,h]of facets){
   rect(c,u*2-1,v*2-1,w*2+2,h*2+2,mix(stone,rgba('#14202e'),.48));
   rect(c,u*2,v*2,w*2,h*2,crystal);
   rect(c,u*2,v*2,w*2,1,mix(crystal,rgba('#ffffff'),.45));
   rect(c,u*2+w*2-1,v*2+1,1,h*2-1,mix(crystal,rgba('#1d3447'),.28));
  }
  textures.push({name:'block/'+id,label:id.toUpperCase().replaceAll('_',' '),canvas:c});
  const faces=Object.fromEntries(['north','south','east','west','up','down'].map(face=>[face,{texture:'#all',cullface:face}]));
  const elements=[{from:[0,0,0],to:[16,16,16],faces}];
  for(const [u,v,w,h]of facets)for(const face of Object.keys(faces)){
   let from,to;
   if(face==='north'){from=[16-u-w,16-v-h,-.002];to=[16-u,16-v,-.002];}
   if(face==='south'){from=[u,16-v-h,16.002];to=[u+w,16-v,16.002];}
   if(face==='west'){from=[-.002,16-v-h,u];to=[-.002,16-v,u+w];}
   if(face==='east'){from=[16.002,16-v-h,16-u-w];to=[16.002,16-v,16-u];}
   if(face==='up'){from=[u,16.002,v];to=[u+w,16.002,v+h];}
   if(face==='down'){from=[u,-.002,16-v-h];to=[u+w,-.002,16-v];}
   elements.push({from,to,shade:false,light_emission:15,faces:{[face]:{uv:[u,v,u+w,v+h],texture:'#all',cullface:face}}});
  }
  models.push({name:id,json:{parent:'minecraft:block/block',ambientocclusion:true,textures:{particle:'minecraft:block/'+id,all:'minecraft:block/'+id},elements}});
 };
 for(const deep of [false,true])for(const [id,col]of orePalette)ore((deep?'deepslate_':'')+id+'_ore',deep?'#454d5d':'#7b858e',col);
 ore('nether_gold_ore','#6b3639','#ffd782');ore('nether_quartz_ore','#6b3639','#f5e8dc');
 const natural=(name,base,kind='tile')=>{
  const c=new Canvas(),col=rgba(base);
  c.paint(()=>true,(x,y)=>mix(col,rgba('#152230'),((Math.floor(x)*7+Math.floor(y)*11)%13)*.007));
  if(kind==='tile')for(let y=0;y<32;y+=8){rect(c,0,y,32,.65,mix(col,rgba('#182431'),.24));for(let x=(y/8%2)*8;x<32;x+=16)rect(c,x,y,.65,8,mix(col,rgba('#182431'),.2));}
  if(kind==='carved')for(let y=4;y<30;y+=8)c.path([[3,y],[12,y],[12,y+4],[20,y+4],[20,y],[29,y]],.7,mix(col,rgba('#182431'),.28));
  textures.push({name:'block/'+name,label:name.toUpperCase().replaceAll('_',' '),canvas:c});
 };
 for(const [id,col,kind]of [
  ['sandstone','#dbc591'],['sandstone_top','#e1cfa4','smooth'],['sandstone_bottom','#d2b981'],['cut_sandstone','#e1ce9f'],['chiseled_sandstone','#dac28e','carved'],
  ['red_sandstone','#c98252'],['red_sandstone_top','#dc9b66','smooth'],['red_sandstone_bottom','#b9754e'],['cut_red_sandstone','#d4925d'],['chiseled_red_sandstone','#c98252','carved'],
  ['quartz_block_side','#e3e4df','smooth'],['quartz_block_top','#edefe9','smooth'],['quartz_block_bottom','#d4d7d4','smooth'],['quartz_bricks','#e5e6e0'],['quartz_pillar','#e0e3df','carved'],['quartz_pillar_top','#eaede7','carved'],
  ['purpur_block','#bca0bf'],['purpur_pillar','#c2a6c5','carved'],['purpur_pillar_top','#c9afd0','carved'],['prismarine','#6caaa4'],['prismarine_bricks','#84b9ad'],['dark_prismarine','#426a68'],
  ['deepslate_tiles','#3d4757'],['polished_deepslate','#536074'],['polished_tuff','#828d84'],['tuff_bricks','#768279'],
  ['cut_copper','#c58d68'],['exposed_cut_copper','#ad9674'],['weathered_cut_copper','#74a394'],['oxidized_cut_copper','#56a698']
 ])natural(id,col,kind);
 for(const [id,col]of [['copper_grate','#c58d68'],['exposed_copper_grate','#ad9674'],['weathered_copper_grate','#74a394'],['oxidized_copper_grate','#56a698']]){
  const c=new Canvas(),color=rgba(col);
  c.paint((x,y)=>x%8<2||y%8<2,(x,y)=>mix(color,rgba('#ffffff'),x%8<1||y%8<1?.14:0));
  textures.push({name:'block/'+id,label:id.toUpperCase().replaceAll('_',' '),canvas:c});
 }
 const glass=[['','#c7e2ef'],['white_','#dce9ed'],['orange_','#f3b57b'],['magenta_','#d7a1d9'],['light_blue_','#a6daed'],['yellow_','#eedc91'],['lime_','#bddb99'],['pink_','#e8b6c7'],['gray_','#73839a'],['light_gray_','#c1cbd5'],['cyan_','#81bec8'],['purple_','#b09acb'],['blue_','#819ed4'],['brown_','#b69a86'],['green_','#96b499'],['red_','#d89ba5'],['black_','#566478']];
 for(const [prefix,color]of glass){
  const c=new Canvas(),col=rgba(color);c.paint(()=>true,[...col.slice(0,3),22]);
  for(const [x,y,w,h]of [[0,0,32,1],[0,31,32,1],[0,0,1,32],[31,0,1,32]])rect(c,x,y,w,h,[...col.slice(0,3),190]);
  c.line([5,10],[11,4],.65,[...col.slice(0,3),65]);
  textures.push({name:'block/'+(prefix?prefix+'stained_glass':'glass'),label:(prefix+'GLASS').toUpperCase(),canvas:c});
 }
 return {textures,models};
}
export function drawParticles(){
 const sprites=[],add=(name,draw)=>{const c=new Canvas();draw(c);sprites.push({name:'particle/'+name,canvas:c});};
 add('critical_hit',c=>{c.line([13,16],[19,16],1.2,rgba('#ffffff'));c.line([16,13],[16,19],1.2,rgba('#ffffff'));});
 add('enchanted_hit',c=>c.path([[16,12],[20,16],[16,20],[12,16],[16,12]],1.1,rgba('#ffffff')));
 add('damage',c=>c.path([[13,19],[16,13],[19,19]],1.2,rgba('#ffffff')));
 for(const kind of ['effect','spell','glitter'])for(let i=0;i<8;i++)add(kind+'_'+i,c=>{
  const r=2.5+i*.2;
  if(kind==='glitter'){c.line([16-r,16],[16+r,16],.9,rgba('#ffffff'));c.line([16,16-r],[16,16+r],.9,rgba('#ffffff'));}
  else c.path([[16-r,16],[16,16-r],[16+r,16],[16,16+r]],.9,rgba('#ffffff'));
 });
 return sprites;
}
