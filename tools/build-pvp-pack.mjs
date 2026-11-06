// Reproducible 32x Smooth and 16x Lite Minecraft 26.1.2 packs. Node built-ins only.
import {mkdirSync,writeFileSync,readFileSync,existsSync} from 'node:fs';
import {join,dirname,resolve} from 'node:path';
import {fileURLToPath} from 'node:url';
import {deflateSync,inflateSync} from 'node:zlib';
import assert from 'node:assert/strict';
import {execFileSync} from 'node:child_process';
import {drawArt} from './ignite-art.mjs';
import {drawGui} from './ignite-gui.mjs';
import {drawBooks,enchantments} from './ignite-books-reforged.mjs';
import {drawBlocks} from './ignite-blocks.mjs';
import {drawCombatArt} from './ignite-combat.mjs';
import {drawEnvironment} from './ignite-environment.mjs';
import {decodePng,cubemap} from './sky-cubemap.mjs';
import {drawWeaponVariants,drawRadiantBlocks,drawParticles,weaponMaterials,swordEnchants,axeEnchants} from './ignite-radiant.mjs';
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
if(['totem','pearl','break'].some(name=>!existsSync(join(root,'assets/sounds',name+'.ogg.b64'))))execFileSync(process.execPath,[join(root,'tools/make-ignite-sounds.mjs')],{stdio:'inherit'});
const blocks=drawBlocks();
const baseArt=[...drawArt().filter(a=>!['item/mace','item/end_crystal'].includes(a.name)),...drawCombatArt(),...drawBooks(),...blocks];
assert.equal(baseArt.length,232);
const radiant=drawRadiantBlocks(),variants=drawWeaponVariants(baseArt);
const art=[...new Map([...baseArt,...radiant.textures,...variants].map(sprite=>[sprite.name,sprite])).values()];
assert.equal(new Set(art.map(a=>a.name)).size,art.length);
const blockCount=art.filter(a=>a.name.startsWith('block/')).length;
console.log('Radiant: '+blockCount+' blocks, '+variants.length+' weapon variants, '+radiant.models.length+' glowing ores, '+drawParticles().length+' particle sprites');
const sky=cubemap(decodePng(Buffer.from(readFileSync(join(root,'assets/sky-day.png.b64'),'utf8').trim(),'base64')));
const skyPng=png(sky.width,sky.height,sky.pixels);
const gui=drawGui();
assert.equal(gui.length,18);
const icons=art.find(a=>a.name==='item/totem_of_undying').canvas;
mkdirSync(join(root,'dist'),{recursive:true});
for(const size of [32,16]){
    const edition=size===32?'Smooth':'Lite',id='PVPSE-Ignite-'+size+'x';
    const files=new Map(),asset=(name,value)=>files.set(name,Buffer.isBuffer(value)?value:Buffer.from(value));
    const json=(name,value)=>asset(name,JSON.stringify(value,null,2)+'\n');
    for(const sprite of art){
        const pixels=sprite.canvas.resolve(size);
        assert.ok(pixels.some((v,i)=>i%4===3&&v>0),'Empty '+sprite.name);
        if(sprite.name.endsWith('copper_grate'))assert.ok(pixels.some((v,i)=>i%4===3&&v===0),'Grate openings became opaque');
        asset('assets/minecraft/textures/'+sprite.name+'.png',png(size,size,pixels));
    }
    for(const sprite of drawParticles())asset('assets/minecraft/textures/'+sprite.name+'.png',png(size,size,sprite.canvas.resolve(size)));
    for(const ore of radiant.models)json('assets/minecraft/models/block/'+ore.name+'.json',ore.json);
    for(const name of ['totem','pearl','break']){
        const data=Buffer.from(readFileSync(join(root,'assets/sounds',name+'.ogg.b64'),'utf8').trim(),'base64');
        assert.equal(data.subarray(0,4).toString(),'OggS');assert.ok(data.includes(Buffer.from('vorbis')));
        asset('assets/pvpse/sounds/combat/'+name+'.ogg',data);
    }
    json('assets/minecraft/sounds.json',Object.fromEntries([
        ['item.totem.use','totem',.8],['entity.ender_pearl.throw','pearl',.85],['entity.item.break','break',.8]
    ].map(([event,name,volume])=>[event,{replace:true,sounds:[{name:'pvpse:combat/'+name,volume}]}])));
    asset('pack.png',png(64,64,icons.resolve(64)));
    for(const sprite of gui){
        asset('assets/minecraft/textures/'+sprite.name+'.png',png(sprite.width,sprite.height,sprite.pixels));
        if(sprite.meta)json('assets/minecraft/textures/'+sprite.name+'.png.mcmeta',sprite.meta);
    }
    for(const sprite of drawEnvironment(size)){
        asset('assets/minecraft/textures/'+sprite.name+'.png',png(sprite.width,sprite.height,sprite.pixels));
        if(sprite.meta)json('assets/minecraft/textures/'+sprite.name+'.png.mcmeta',sprite.meta);
        if(sprite.name.includes('fire_')){
            for(let frame=0;frame<16;frame++)for(let y=0;y<Math.floor(size*.35);y++)for(let x=0;x<size;x++)
                assert.equal(sprite.pixels[(frame*size*size+y*size+x)*4+3],0,'Fire blocks the upper view');
        }
    }
    asset('assets/minecraft/optifine/sky/world0/sky1.png',skyPng);
    asset('assets/minecraft/optifine/sky/world0/sky1.properties','source=minecraft:optifine/sky/world0/sky1.png\nblend=alpha\nrotate=false\nstartFadeIn=05:30\nendFadeIn=07:00\nstartFadeOut=17:00\nendFadeOut=18:30\nweather=clear\n');
    asset('SKY.txt','Realistic daytime clouds require an OptiFine-format custom-sky loader. Fabric 26.1.2: install a matching Skyboxify release from https://modrinth.com/mod/skyboxify. Vanilla safely ignores the custom sky and uses the upgraded native sun and eight moon textures. No sky mod is bundled. Native clouds can be disabled separately to reveal the photographic cloud layer.\n');
    json('pack.mcmeta',{pack:{description:'PVPSE Ignite Radiant '+size+'x '+edition+' | Glowing ores, enchanted gear, clean blocks & dark GUI',min_format:[84,0],max_format:[84,0]}});
    json('assets/minecraft/models/item/enchanted_golden_apple.json',{parent:'minecraft:item/generated',textures:{layer0:'minecraft:item/enchanted_golden_apple'}});
    for(let i=3;i<8;i++)json('assets/minecraft/models/item/bow_pulling_'+i+'.json',{parent:'minecraft:item/bow',textures:{layer0:'minecraft:item/bow_pulling_'+i}});
    const model=name=>({type:'minecraft:model',model:'minecraft:item/'+name});
    let book=model('enchanted_book');
    for(const [id]of [...enchantments].reverse()){
        json('assets/minecraft/models/item/books/'+id+'.json',{parent:'minecraft:item/generated',textures:{layer0:'minecraft:item/books/'+id}});
        book={type:'minecraft:condition',property:'minecraft:component',predicate:'minecraft:stored_enchantments',
            value:[{enchantments:'minecraft:'+id,levels:{min:1}}],on_true:model('books/'+id),on_false:book};
    }
    json('assets/minecraft/items/enchanted_book.json',{model:book});
    let mace=model('mace');
    for(const enchantment of ['wind_burst','density','breach']){
        json('assets/minecraft/models/item/mace_'+enchantment+'.json',{parent:'minecraft:item/handheld_mace',textures:{layer0:'minecraft:item/mace_'+enchantment}});
        mace={type:'minecraft:condition',property:'minecraft:component',predicate:'minecraft:enchantments',
            value:[{enchantments:'minecraft:'+enchantment,levels:{min:1}}],on_true:model('mace_'+enchantment),on_false:mace};
    }
    json('assets/minecraft/items/mace.json',{model:mace});
    const enchanted=(id,enchantments)=>{
        let node=model(id);
        for(const enchantment of [...enchantments].reverse()){
            const name=id+'_'+enchantment;
            json('assets/minecraft/models/item/'+name+'.json',{parent:id.startsWith('bow')?'minecraft:item/bow':'minecraft:item/handheld',textures:{layer0:'minecraft:item/'+name}});
            node={type:'minecraft:condition',property:'minecraft:component',predicate:'minecraft:enchantments',value:[{enchantments:'minecraft:'+enchantment,levels:{min:1}}],on_true:model(name),on_false:node};
        }
        return node;
    };
    for(const material of weaponMaterials)for(const [tool,enchantments]of [['sword',swordEnchants],['axe',axeEnchants]]){
        const id=material+'_'+tool;json('assets/minecraft/items/'+id+'.json',{model:enchanted(id,enchantments)});
    }
    const thresholds=[.15,.30,.45,.60,.75,.90,1];
    json('assets/minecraft/items/bow.json',{model:{
        type:'minecraft:condition',property:'minecraft:using_item',on_false:enchanted('bow',['flame','power']),
        on_true:{type:'minecraft:range_dispatch',property:'minecraft:use_duration',scale:.05,fallback:enchanted('bow_pulling_0',['flame','power']),
            entries:thresholds.map((threshold,i)=>({threshold,model:enchanted('bow_pulling_'+(i+1),['flame','power'])}))}
    }});
    for(const blocking of [false,true]){
        const name='shield'+(blocking?'_blocking':'');
        json('assets/pvpse/models/item/low_'+name+'.json',{parent:'minecraft:item/'+name,display:{
            firstperson_righthand:{rotation:[0,180,blocking?-5:5],translation:blocking?[-15,-2.5,-11]:[-10,-4,-10],scale:[1.1,1.1,1.1]},
            firstperson_lefthand:{rotation:[0,180,blocking?-5:5],translation:blocking?[5,-2.5,-11]:[10,-4,-10],scale:[1.1,1.1,1.1]}
        }});
    }
    const shield=blocking=>({type:'minecraft:special',base:'pvpse:item/low_shield'+(blocking?'_blocking':''),model:{type:'minecraft:shield'}});
    json('assets/minecraft/items/shield.json',{model:{type:'minecraft:condition',property:'minecraft:using_item',on_true:shield(true),on_false:shield(false),
        transformation:{left_rotation:[0,0,0,1],right_rotation:[0,0,0,1],scale:[1,-1,-1],translation:[0,0,0]}}});
    asset('LICENSE',readFileSync(join(root,'LICENSE')));
    asset('README.txt','PVPSE Ignite Reforged '+size+'x '+edition+' - Minecraft Java 26.1.2\n\nOriginal forged short swords and axes, lucky-cat totem, moonstone pearl, cute apples and PvP essentials. Dark hotbar, buttons, inventory, chest and crafting GUI. '+blockCount+' coordinated block textures with 18 native glowing ore models, 60 enchantment weapon variants, 27 quieter particle textures and three original sound cues, illustrated enchantment books, enchantment-specific maces, animated low fire and soul fire, detailed native sun/moons and a photographic custom sky (see SKY.txt for loader requirements). Unique covers for all 43 vanilla enchantments, including Lunge. Native component predicates work at any level, including combined books (first matching enchantment in BOOKS.txt wins). No CIT, OptiFine or renaming required.\n\nEight native bow charge stages: green at 20 ticks. Low shields preserve vanilla banners.\n\nInstall the ZIP in resourcepacks and enable ONE Ignite edition above other packs. Smooth 32x has richer detail; Lite 16x uses smaller textures. GUI stays at its native dimensions. No reach, damage or attack timing changes.\n\nCopyright: see LICENSE.\n');
    asset('RADIANT.txt','PVPSE Ignite Radiant 3.6\n'+blockCount+' block textures, 60 weapon variants and 18 ore models with native light_emission=15 mineral facets. Stone remains normally shaded. No shader/OptiFine/mod is needed for ore glow; it neither casts light nor reveals covered ores. The photographic sky still needs Skyboxify (SKY.txt).\n\nSword priority: Fire Aspect > Sharpness > Smite > Bane. Axe: Efficiency > Sharpness. Bow: Flame > Power, all charge stages retained. Mace: Breach > Density > Wind Burst. Enchantment components work at every level without renaming/CIT.\n\nThree original quiet sound cues replace totem use, pearl throw and item break (tools and armor). Smaller crit, enchanted-hit, damage, potion and totem particles preserve native colors and timing. Dark GUI includes inventory, chest, crafting, shulkers and furnace family.\n');
    asset('BOOKS.txt','Book cover priority for combined books (top first):\n'+enchantments.map(([id,code])=>code+' - '+id).join('\n')+'\nUnknown/modded enchantments use the generic cover. Levels and tooltips remain vanilla.\n');
    assert.ok(files.size>320);
    asset('REFORGED.txt','PVPSE Ignite Reforged 3.1\n'+blocks.length+' upgraded block textures, 43 illustrated enchantment covers, native enchanted mace variants, blue diamond gear, lower banner-compatible shield, animated low fire and soul fire, round sun and eight detailed moons. Original generated cloud sky: see SKY.txt for the separate loader requirement. Reference images guide the style; no third-party pack assets are redistributed.\n');
    for(const [name,data]of files){
        assert.ok(!name.includes('..')&&!name.includes('\\'));
        if(name.endsWith('.json')||name.endsWith('.mcmeta'))JSON.parse(data.toString());
        if(name.endsWith('.png'))validatePNG(data);
        const target=join(root,'resourcepacks',id,name);mkdirSync(dirname(target),{recursive:true});writeFileSync(target,data);
    }
    const bow=JSON.parse(files.get('assets/minecraft/items/bow.json'));
    // Exercise the generated predicate chain with single, combined and unknown books.
    const cover=stored=>{let node=JSON.parse(files.get('assets/minecraft/items/enchanted_book.json')).model;
        while(node.type==='minecraft:condition'){
            assert.equal(node.predicate,'minecraft:stored_enchantments');
            const condition=node.value[0];node=(stored[condition.enchantments]>=condition.levels.min)?node.on_true:node.on_false;
        }return node.model;};
    for(const [id]of enchantments)for(const level of [1,5,10])assert.equal(cover({['minecraft:'+id]:level}),'minecraft:item/books/'+id);
    assert.equal(cover({'minecraft:mending':1,'minecraft:sharpness':5}),'minecraft:item/books/sharpness');
    assert.equal(cover({'example:unknown':1}),'minecraft:item/enchanted_book');
    assert.equal(new Set(enchantments.map(e=>e[1])).size,43,'Duplicate book identifiers');
    const maceCover=stored=>{let node=JSON.parse(files.get('assets/minecraft/items/mace.json')0,0,0];}
  const rune=u===3||u===4||v===3||(u===v&&v>1&&v<6);
  return rune?[239,88,222,255]:[255,231,255,255];
 });
 return out;
}
