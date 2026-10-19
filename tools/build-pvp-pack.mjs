// Reproducible 32x Smooth and 16x Lite Minecraft 26.1.2 packs. Node built-ins only.
import {mkdirSync,writeFileSync,readFileSync} from 'node:fs';
import {join,dirname,resolve} from 'node:path';
import {fileURLToPath} from 'node:url';
import {deflateSync,inflateSync} from 'node:zlib';
import assert from 'node:assert/strict';
import {drawArt} from './ignite-art.mjs';
const root=resolve(dirname(fileURLToPath(import.meta.url)),'..');
function crc32(bytes) {
    let crc = 0xffffffff;
    for (const byte of bytes) {
        crc ^= byte;
        for (let i = 0; i < 8; i++) crc = (crc >>> 1) ^ (0xedb88320 & -(crc & 1));
    }
    return (crc ^ 0xffffffff) >>> 0;
}
function chunk(type, data) {
    const name = Buffer.from(type), header = Buffer.alloc(4), crc = Buffer.alloc(4);
    header.writeUInt32BE(data.length); crc.writeUInt32BE(crc32(Buffer.concat([name, data])));
    return Buffer.concat([header, name, data, crc]);
}
function png(w, h, rgba) {
    const header = Buffer.alloc(13); header.writeUInt32BE(w); header.writeUInt32BE(h, 4);
    header[8] = 8; header[9] = 6;
    const scan = Buffer.alloc(h * (w * 4 + 1));
    for (let y = 0; y < h; y++) rgba.copy(scan, y * (w * 4 + 1) + 1, y * w * 4, (y + 1) * w * 4);
    return Buffer.concat([Buffer.from('89504e470d0a1a0a', 'hex'), chunk('IHDR', header), chunk('IDAT', deflateSync(scan)), chunk('IEND', Buffer.alloc(0))]);
}

function zip(files){
    const entries=[],central=[];let offset=0;
    for(const [name,data]of [...files].sort(([a],[b])=>a.localeCompare(b))){
        const filename=Buffer.from(name),crc=crc32(data);
        const h=Buffer.alloc(30);h.writeUInt32LE(0x04034b50);h.writeUInt16LE(20,4);h.writeUInt16LE(0x800,6);h.writeUInt16LE(0x21,12);
        h.writeUInt32LE(crc,14);h.writeUInt32LE(data.length,18);h.writeUInt32LE(data.length,22);h.writeUInt16LE(filename.length,26);
        const c=Buffer.alloc(46);c.writeUInt32LE(0x02014b50);c.writeUInt16LE(20,4);c.writeUInt16LE(20,6);c.writeUInt16LE(0x800,8);c.writeUInt16LE(0x21,14);
        c.writeUInt32LE(crc,16);c.writeUInt32LE(data.length,20);c.writeUInt32LE(data.length,24);c.writeUInt16LE(filename.length,28);c.writeUInt32LE(offset,42);
        entries.push(h,filename,data);central.push(c,filename);offset+=30+filename.length+data.length;
    }
    const dir=Buffer.concat(central),end=Buffer.alloc(22);end.writeUInt32LE(0x06054b50);end.writeUInt16LE(files.size,8);end.writeUInt16LE(files.size,10);
    end.writeUInt32LE(dir.length,12);end.writeUInt32LE(offset,16);
    return Buffer.concat([...entries,dir,end]);
}
function validatePNG(data){
    assert.equal(data.subarray(0,8).toString('hex'),'89504e470d0a1a0a');
    let offset=8;const parts=[];
    while(offset<data.length){
        const length=data.readUInt32BE(offset),kind=data.subarray(offset+4,offset+8).toString();
        const chunkData=data.subarray(offset+8,offset+8+length);
        assert.equal(crc32(data.subarray(offset+4,offset+8+length)),data.readUInt32BE(offset+8+length));
        if(kind==='IDAT')parts.push(chunkData);offset+=length+12;
    }
    assert.equal(offset,data.length);
    const w=data.readUInt32BE(16),h=data.readUInt32BE(20);
    assert.equal(inflateSync(Buffer.concat(parts)).length,h*(w*4+1));
}
const art=drawArt();
assert.equal(art.length,28);
const icons=art.find(a=>a.name==='item/totem_of_undying').canvas;
mkdirSync(join(root,'dist'),{recursive:true});
for(const size of [32,16]){
    const edition=size===32?'Smooth':'Lite',id='PVPSE-Ignite-'+size+'x';
    const files=new Map(),asset=(name,value)=>files.set(name,Buffer.isBuffer(value)?value:Buffer.from(value));
    const json=(name,value)=>asset(name,JSON.stringify(value,null,2)+'\n');
    for(const sprite of art){
        const pixels=sprite.canvas.resolve(size);
        assert.ok(pixels.some((v,i)=>i%4===3&&v>0),'Empty '+sprite.name);
        asset('assets/minecraft/textures/'+sprite.name+'.png',png(size,size,pixels));
    }
    asset('pack.png',png(64,64,icons.resolve(64)));
    json('pack.mcmeta',{pack:{description:'PVPSE Ignite '+size+'x '+edition+' | Smooth short swords & PvP essentials',min_format:[84,0],max_format:[84,0]}});
    json('assets/minecraft/models/item/enchanted_golden_apple.json',{parent:'minecraft:item/generated',textures:{layer0:'minecraft:item/enchanted_golden_apple'}});
    for(let i=3;i<8;i++)json('assets/minecraft/models/item/bow_pulling_'+i+'.json',{parent:'minecraft:item/bow',textures:{layer0:'minecraft:item/bow_pulling_'+i}});
    const model=name=>({type:'minecraft:model',model:'minecraft:item/'+name});
    const thresholds=[.15,.30,.45,.60,.75,.90,1];
    json('assets/minecraft/items/bow.json',{model:{
        type:'minecraft:condition',property:'minecraft:using_item',on_false:model('bow'),
        on_true:{type:'minecraft:range_dispatch',property:'minecraft:use_duration',scale:.05,fallback:model('bow_pulling_0'),
            entries:thresholds.map((threshold,i)=>({threshold,model:model('bow_pulling_'+(i+1))}))}
    }});
    for(const blocking of [false,true]){
        const name='shield'+(blocking?'_blocking':'');
        json('assets/pvpse/models/item/low_'+name+'.json',{parent:'minecraft:item/'+name,display:{
            firstperson_righthand:{rotation:[0,180,blocking?-5:5],translation:blocking?[-15,-1,-11]:[-10,-5,-10],scale:[1.05,1.05,1.05]},
            firstperson_lefthand:{rotation:[0,180,blocking?-5:5],translation:blocking?[5,-1,-11]:[10,-7,-10],scale:[1.05,1.05,1.05]}
        }});
    }
    const shield=blocking=>({type:'minecraft:special',base:'pvpse:item/low_shield'+(blocking?'_blocking':''),model:{type:'minecraft:shield'}});
    json('assets/minecraft/items/shield.json',{model:{type:'minecraft:condition',property:'minecraft:using_item',on_true:shield(true),on_false:shield(false),
        transformation:{left_rotation:[0,0,0,1],right_rotation:[0,0,0,1],scale:[1,-1,-1],translation:[0,0,0]}}});
    asset('LICENSE',readFileSync(join(root,'LICENSE')));
    asset('README.txt','PVPSE Ignite '+size+'x '+edition+' — Minecraft Java 26.1.2\n\nOriginal 32x art with shaded short swords, a lucky-cat totem, glossy PvP food, pearls, XP, crystals, mace and open cobwebs. Eight native bow charge stages: green at 20 ticks. Low shields preserve vanilla banners.\n\nInstall the ZIP in resourcepacks and enable ONE Ignite edition above other packs. Use Smooth 32x for richer detail or Lite 16x for smaller textures. No reach, damage or attack timing changes.\n\nCopyright: see LICENSE.\n');
    assert.equal(files.size,42);
    for(const [name,data]of files){
        assert.ok(!name.includes('..')&&!name.includes('\\'));
        if(name.endsWith('.json')||name.endsWith('.mcmeta'))JSON.parse(data.toString());
        if(name.endsWith('.png'))validatePNG(data);
        const target=join(root,'resourcepacks',id,name);mkdirSync(dirname(target),{recursive:true});writeFileSync(target,data);
    }
    const bow=JSON.parse(files.get('assets/minecraft/items/bow.json'));
    assert.equal(bow.model.on_true.entries.at(-1).threshold,1);
    for(let i=3;i<8;i++)assert.ok(files.has('assets/minecraft/models/item/bow_pulling_'+i+'.json'));
    const bytes=zip(files),filename=id+'-26.1.2.zip';
    assert.ok(bytes.equals(zip(files)),'Non-deterministic ZIP');
    writeFileSync(join(root,'dist',filename),bytes);
    console.log('Validated '+id+' '+edition+': '+files.size+' entries, '+bytes.length+' bytes');
}
const glyphs={
 A:'0e11111f111111',B:'1e11111e11111e',C:'0e11101010110e',D:'1e11111111111e',E:'1f10101e10101f',F:'1f10101e101010',
 G:'0e11101711110f',H:'1111111f111111',I:'0e04040404040e',J:'0702020212120c',K:'11121418141211',L:'1010101010101f',
 M:'111b1515111111',N:'11191513111111',O:'0e11111111110e',P:'1e11111e101010',Q:'0e11111115120d',R:'1e11111e141211',
 S:'0f10100e01011e',T:'1f040404040404',U:'1111111111110e',V:'11111111110a04',W:'11111115151b11',X:'11110a040a1111',
 Y:'11110a04040404',Z:'1f01020408101f','1':'040c040404040e','2':'0e11010204081f','3':'1e01010e01011e','4':'02060a121f0202',
 '5':'1f10101e01011e','6':'0e10101e11110e','7':'1f010204080808','8':'0e11110e11110e','0':'0e11131519110e',' ':'00000000000000'
};
const visible=[...art.slice(0,16),art[16],art[17],art[20],art[24],...art.slice(25)];
const cols=5,cellW=120,cellH=124,width=cols*cellW,height=Math.ceil(visible.length/cols)*cellH;
const sheet=Buffer.alloc(width*height*4);
for(let i=0;i<sheet.length;i+=4)sheet.set([18,23,33,255],i);
for(let index=0;index<visible.length;index++){
    const sprite=visible[index],pixels=sprite.canvas.resolve(32),left=index%cols*cellW+12,top=Math.floor(index/cols)*cellH+8;
    for(let y=0;y<32;y++)for(let x=0;x<32;x++){
        const at=(y*32+x)*4,alpha=pixels[at+3]/255;
        for(let yy=0;yy<3;yy++)for(let xx=0;xx<3;xx++){
            const dst=((top+y*3+yy)*width+left+x*3+xx)*4;
            for(let c=0;c<3;c++)sheet[dst+c]=Math.round(sheet[dst+c]*(1-alpha)+pixels[at+c]*alpha);
        }
    }
    const label=sprite.label,start=index%cols*cellW+Math.round((cellW-label.length*6)/2),baseline=top+104;
    for(let letter=0;letter<label.length;letter++){
        const glyph=glyphs[label[letter]]||glyphs[' '];
        for(let y=0;y<7;y++){const row=parseInt(glyph.slice(y*2,y*2+2),16);for(let x=0;x<5;x++)if(row&(1<<(4-x))){
            const dst=((baseline+y)*width+start+letter*6+x)*4;sheet.set([210,217,230,255],dst);
        }}
    }
}
writeFileSync(join(root,'dist/PVPSE-Ignite-preview.png'),png(width,height,sheet));
