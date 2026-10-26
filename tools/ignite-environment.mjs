// Native 26.1.2 assets; deliberately no shader replacements.
export function drawEnvironment(size){
 const out=[],image=(name,w,h,fn,meta)=>{const pixels=Buffer.alloc(w*h*4);for(let y=0;y<h;y++)for(let x=0;x<w;x++)pixels.set(fn(x,y),(y*w+x)*4);out.push({name,width:w,height:h,pixels,meta});};
 for(const soul of [false,true])for(let layer=0;layer<2;layer++){
  image('block/'+(soul?'soul_fire_':'fire_')+layer,size,size*16,(x,yy)=>{
   const y=(yy%size+.5)/size,frame=Math.floor(yy/size),u=(x+.5)/size;
   const wave=Math.sin(u*25+frame*Math.PI/4+layer*2)*.05+Math.sin(u*47-frame*Math.PI/4)*.025,top=.69+wave;
   if(y<top)return[0,0,0,0];const heat=Math.min(1,(y-top)/.28);
   return soul?[90+Math.round(150*heat),180+Math.round(65*heat),255,240]:[255,Math.round(83+168*heat),Math.round(20+125*heat),240];
  },{animation:{frametime:2,interpolate:false}});
 }
 image('environment/celestial/sun',128,128,(x,y)=>{
  const r=Math.hypot((x+.5-64)/64,(y+.5-64)/64),core=.26,halo=Math.exp(-Math.max(0,r-core)*7.2);
  return[255,Math.round(214+40*halo),Math.round(148+95*halo),r<core?255:Math.round(120*halo*Math.max(0,1-r))];
 });
 const phases=['full_moon','waning_gibbous','third_quarter','waning_crescent','new_moon','waxing_crescent','first_quarter','waxing_gibbous'];
 for(let phase=0;phase<8;phase++)image('environment/celestial/moon/'+phases[phase],128,128,(x,y)=>{
  const u=(x+.5-64)/54,v=(y+.5-64)/54,r2=u*u+v*v;if(r2>=1)return[0,0,0,0];
  const z=Math.sqrt(1-r2),angle=phase*Math.PI/4,lit=Math.max(0,z*Math.cos(angle)+u*Math.sin(angle));
  const craters=.85+.075*Math.sin(u*31+v*11)*Math.sin(v*29)+.055*Math.sin(u*69-v*37),value=Math.round((lit>0?115+130*Math.sqrt(lit):16)*craters);
  return[value,Math.min(255,value+5),Math.min(255,value+14),lit>0?255:85];
 });
 image('entity/end_crystal/end_crystal',64,32,(x,y)=>{
  if(y>=16){const n=(x*13+y*7)%11;return[42+n*2,31+n,61+n*2,255];}
  const u=x%8,v=y%8;
  if(x<32){const edge=u===0||u===7||v===0||v===7;return edge?[175+u*6,204+v*4,232,245]:[189,221,246,35];}
  const rune=u===3||u===4||v===3||(u===v&&v>1&&v<6);
  return rune?[239,88,222,255]:[255,231,255,255];
 });
 return out;
}
