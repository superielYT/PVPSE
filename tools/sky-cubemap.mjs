import {inflateSync} from 'node:zlib';
import assert from 'node:assert/strict';
// RGB/RGBA PNG decoder for deterministic panorama-to-cubemap format conversion.
export function decodePng(bytes){
 assert.equal(bytes.subarray(0,8).toString('hex'),'89504e470d0a1a0a');
 const width=bytes.readUInt32BE(16),height=bytes.readUInt32BE(20),type=bytes[25],bpp=type===2?3:4;
 assert.equal(bytes[24],8);assert.ok(type===2||type===6);assert.equal(bytes[28],0);
 const parts=[];for(let off=8;off<bytes.length;){const n=bytes.readUInt32BE(off);if(bytes.subarray(off+4,off+8).toString()==='IDAT')parts.push(bytes.subarray(off+8,off+8+n));off+=n+12;}
 const raw=inflateSync(Buffer.concat(parts)),stride=width*bpp,out=Buffer.alloc(width*height*bpp);
 const paeth=(a,b,c)=>{const p=a+b-c,pa=Math.abs(p-a),pb=Math.abs(p-b),pc=Math.abs(p-c);return pa<=pb&&pa<=pc?a:pb<=pc?b:c;};
 for(let y=0;y<height;y++){const filter=raw[y*(stride+1)];assert.ok(filter<=4);
  for(let x=0;x<stride;x++){const a=x>=bpp?out[y*stride+x-bpp]:0,b=y?out[(y-1)*stride+x]:0,c=y&&x>=bpp?out[(y-1)*stride+x-bpp]:0;
   const predict=[0,a,b,Math.floor((a+b)/2),paeth(a,b,c)][filter];out[y*stride+x]=(raw[y*(stride+1)+1+x]+predict)&255;}}
 return{width,height,bpp,pixels:out};
}
export function cubemap(source,size=512){
 // OptiFine/Skyboxify atlas: bottom, top, south / west, north, east.
 const faces=[(u,v)=>[u,-1,v],(u,v)=>[u,1,-v],(u,v)=>[-u,-v,1],(u,v)=>[-1,-v,-u],(u,v)=>[u,-v,-1],(u,v)=>[1,-v,u]];
 const width=size*3,height=size*2,pixels=Buffer.alloc(width*height*4);
 for(let face=0;face<6;face++)for(let y=0;y<size;y++)for(let x=0;x<size;x++){
  const [dx,dy,dz]=faces[face]((x+.5)/size*2-1,(y+.5)/size*2-1),angle=Math.atan2(dz,dx),elevation=Math.atan2(dy,Math.hypot(dx,dz));
  const fx=((angle/(Math.PI*2)+.5)*source.width)%source.width,fy=Math.max(0,Math.min(source.height-1,(.5-elevation/Math.PI)*source.height));
  const ix=Math.floor(fx),iy=Math.floor(fy),tx=fx-ix,ty=fy-iy,dest=(((Math.floor(face/3)*size+y)*width)+(face%3)*size+x)*4;
  for(let k=0;k<3;k++){let value=0;for(let yy=0;yy<2;yy++)for(let xx=0;xx<2;xx++){
   const at=(Math.min(source.height-1,iy+yy)*source.width+(ix+xx)%source.width)*source.bpp+k;
   value+=source.pixels[at]*(xx?tx:1-tx)*(yy?ty:1-ty);
  }pixels[dest+k]=Math.round(value);}pixels[dest+3]=255;
 }
 return{width,height,pixels};
}
