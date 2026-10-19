// Original PVPSE pixel assets. Node built-ins only; run: node tools/build-pvp-pack.mjs
import { mkdirSync, writeFileSync, readFileSync, readdirSync } from 'node:fs';
import { join, dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import { deflateSync } from 'node:zlib';
import assert from 'node:assert/strict';
const root = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const out = join(root, 'resourcepacks/PVPSE-Ignite-16x');
const files = new Map();
const previews = [];
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
function asset(name, data) { files.set(name, Buffer.isBuffer(data) ? data : Buffer.from(data)); }
const ink = '#232734', orange = '#ff9446', light = '#ffe0aa', handle = '#825039';
function rgba(hex) { return [...Buffer.from(hex.slice(1), 'hex'), 255]; }
function sprite(name, rows, palette) {
    assert.equal(rows.length, 16);
    const pixels = Buffer.alloc(16 * 16 * 4);
    for (let y = 0; y < 16; y++) {
        assert.equal(rows[y].length, 16, name);
        for (let x = 0; x < 16; x++) {
            const key = rows[y][x];
            if (key !== '.') { assert.ok(palette[key], key); pixels.set(rgba(palette[key]), (y * 16 + x) * 4); }
        }
    }
    asset('assets/minecraft/textures/' + name + '.png', png(16, 16, pixels));
    previews.push({name, pixels});
    return pixels;
}
const sword = [
'................','................','................','.........OO.....',
'........OLBO....','.......OLBBO....','......OLBBO.....','.....OLBBO......',
'....OOLBO.......','.....OGO........','....OGGOO.......','...OHO..........',
'..OHO...........','..OO............','................','................'
];
for (const [material, blade, shine] of [
    ['wooden','#b48055','#e3bd8f'], ['stone','#969eaa','#dce2eb'],
    ['copper','#d87d50','#ffc39e'], ['iron','#bfcbd7','#f5fbff'],
    ['golden','#f2b53f','#fff3a2'], ['diamond','#36cdbd','#b5fff0'],
    ['netherite','#645a76','#b7a2cc']
]) sprite('item/' + material + '_sword', sword, { O:ink, L:shine, B:blade, G:orange, H:handle });
sprite('item/ender_pearl', [
'................','................','.....OOOOOO.....','...OOTTCCCCOO...',
'...OTTTCCCCCO...','..OTTTCCCCCCCO..','..OTTCCCCDDDDO..','..OTCCCCDDDDDO..',
'..OCCCCDDDDDDO..','..OCCCDDDDDDDO..','...OCCDDDDDDO...','...OOCDDDDOOO...',
'.....OOOOOO.....','................','................','................'
], { O:ink, T:'#c4fff3', C:'#35c8b5', D:'#16786e' });
sprite('item/golden_apple', [
'................','.......OO.......','.......OH.OO....','.......OHOGO....',
'....OOOOOOOO....','...OGGLLLGGGO...','..OGGLLLGGGGGO..','..OGLLLGGGGGGO..',
'..OGLLGGGGGGGO..','..OGGGGGGDDDGO..','..OGGGGGDDDDGO..','...OGGGDDDDGO...',
'....OGGDDDGO....','.....OOOOOO.....','................','................'
], { O:ink, H:handle, G:'#ffbe42', L:'#fff2ab', D:'#d98528' });
// A tiny lucky cat: ears, bright eyes, pink cheeks and a gold collar.
sprite('item/totem_of_undying', [
'................','....OO....OO....','...OLLO..OLLO...','...OLLLLLLLLO...',
'...OLBLLBL L O..'.replaceAll(' ','' ).padEnd(16,'.'),
'...OLBLLBL L O..'.replaceAll(' ','' ).padEnd(16,'.'),
'...OPLLHLLPLO...','....OLLLLLLO....','.....OGGGO......','...OOOLLOOOO....',
'..OLLLLLLLLL O..'.replaceAll(' ','').padEnd(16,'.'),
'..OOOLLLLL OOO..'.replaceAll(' ','').padEnd(16,'.'),
'.....OLLLLO.....','.....OL..LO.....','.....OO..OO.....','................'
], { O:ink, L:'#ffe9bb', B:'#302734', H:'#de9878', P:'#ff8fa3', G:orange });
sprite('item/golden_carrot', [
'................','..........OO....','........OO G O..'.replaceAll(' ','').padEnd(16,'.'),
'.......OGGGO....','......OOOGOO....','.....O L G G O....'.replaceAll(' ','').padEnd(16,'.'),
'....O L L G G O....'.replaceAll(' ','').padEnd(16,'.'),
'...O L L G G D O....'.replaceAll(' ','').padEnd(16,'.'),
'...O L G G D O.....'.replaceAll(' ','').padEnd(16,'.'),
'..O L G G D O......'.replaceAll(' ','').padEnd(16,'.'),
'..O G G D O.......'.replaceAll(' ','').padEnd(16,'.'),
'.O G G D O........'.replaceAll(' ','').padEnd(16,'.'),
'.O G D O.........'.replaceAll(' ','').padEnd(16,'.'),
'.O D O..........'.replaceAll(' ','').padEnd(16,'.'),
'..OO............','................'
], { O:ink, G:'#ffbc43', L:'#fff1a2', D:'#cf772d' });
sprite('item/enchanted_golden_apple', [
'................','.......OO.......','.......OH.OO....','.......OHOGO....',
'....OOOOOOOO....','...OGGLLLGGGO...','..OGGLLLGGGGGO..','..OGLLLGGGGGGO..',
'..OGLLGGGGGGGO..','..OGGGGGGDDDGO..','..OGGGGGDDDDGO..','...OGGGDDDDGO...',
'....OGGDDDGO....','.....OOOOOO.....','................','................'
], { O:'#43253e', H:handle, G:'#ffbc6c', L:'#fff4d2', D:'#e773a5' });
sprite('item/experience_bottle', [
'................','......OOOO......','......OHHO......','......OOOO......',
'......OLLO......','.....OLLLLO.....','....OLLLLLLO....','...OLLLTTLLLO...',
'...OLLTTTT L O...'.replaceAll(' ','').padEnd(16,'.'),
'...OLTTTTTTLO...','...OLTTTCCLLO...','...OLTTCCC L O...'.replaceAll(' ','').padEnd(16,'.'),
'....OLCCC L O....'.replaceAll(' ','').padEnd(16,'.'),
'.....OOOOOO.....','................','................'
], { O:ink, H:handle, L:'#c6f5ef', T:'#92e87a', C:'#47ad70' });
sprite('item/end_crystal', [
'................','.......OO.......','......OLLO......','.....OLPPLO.....',
'....OLPPPPL O...'.replaceAll(' ','').padEnd(16,'.'),
'...OLPPWWPPLO...','..OLPPWWWWPPLO..','...OLPPWWPPLO...',
'....OLPPPPL O...'.replaceAll(' ','').padEnd(16,'.'),
'.....OLPPLO.....','......OLLO......','.......OO.......',
'....OOOOOOOO....','...ODDDDDDDDO...','...OOOOOOOOOO...','................'
], { O:ink, L:'#ffb2e0', P:'#e975b8', W:'#fff1fc', D:'#777b91' });
// Open, high contrast web with visible radial strands and rings.
const web = Buffer.alloc(16 * 16 * 4);
for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) {
    const radius = Math.max(Math.abs(x - 7), Math.abs(y - 7));
    const spoke = x === 7 || y === 7 || x === y || x + y === 14;
    if (spoke || radius === 3 || radius === 6) web.set(rgba(spoke ? '#f0f5ff' : '#aebdd0'), (y * 16 + x) * 4);
}
asset('assets/minecraft/textures/block/cobweb.png', png(16,16,web));
previews.push({name:'block/cobweb', pixels:web});
// Inherit vanilla special-renderer models, preserving banners and other views.
for (const blocking of [false, true]) {
    const id = blocking ? 'shield_blocking' : 'shield';
    asset('assets/pvpse/models/item/low_' + id + '.json', JSON.stringify({
        parent: 'minecraft:item/' + id,
        display: {
            firstperson_righthand: { rotation: [0,180,blocking ? -5 : 5],
                translation: blocking ? [-15,-1,-11] : [-10,-5,-10], scale: [1.05,1.05,1.05] },
            firstperson_lefthand: { rotation: [0,180,blocking ? -5 : 5],
                translation: blocking ? [5,-1,-11] : [10,-7,-10], scale: [1.05,1.05,1.05] }
        }
    },null,2));
}
const specialShield = blocking => ({
    type:'minecraft:special', base:'pvpse:item/low_shield' + (blocking ? '_blocking' : ''),
    model:{type:'minecraft:shield'}
});
asset('assets/minecraft/items/shield.json', JSON.stringify({
    model:{type:'minecraft:condition', property:'minecraft:using_item',
        on_true:specialShield(true), on_false:specialShield(false)}
},null,2));
// Vanilla enchanted apples share the ordinary apple texture; point this model
// to the dedicated pink/gold sprite so the two items remain distinguishable.
asset('assets/minecraft/models/item/enchanted_golden_apple.json', JSON.stringify({
    parent:'minecraft:item/generated', textures:{layer0:'minecraft:item/enchanted_golden_apple'}
},null,2));
// Green means a full 20-tick draw, rather than vanilla's early final-frame switch.
const bowModel = name => ({type:'minecraft:model',model:'minecraft:item/'+name});
asset('assets/minecraft/items/bow.json',JSON.stringify({
    model:{type:'minecraft:condition',property:'minecraft:using_item',
        on_false:bowModel('bow'),
        on_true:{type:'minecraft:range_dispatch',property:'minecraft:use_duration',scale:0.05,
            fallback:bowModel('bow_pulling_0'),entries:[
                {threshold:0.65,model:bowModel('bow_pulling_1')},
                {threshold:1.0,model:bowModel('bow_pulling_2')}
            ]}
    }
},null,2));
// Four native bow sprites: idle plus Minecraft's three pulling stages.
for (const stage of [-1, 0, 1, 2]) {
    const pixels = Buffer.alloc(16 * 16 * 4);
    const put = (x, y, color) => {
        if (x >= 0 && x < 16 && y >= 0 && y < 16) pixels.set(rgba(color), (y * 16 + x) * 4);
    };
    const line = (x1,y1,x2,y2,color,radius=0) => {
        const steps=Math.max(Math.abs(x2-x1),Math.abs(y2-y1));
        for(let i=0;i<=steps;i++){
            const t=steps ? i/steps : 0, x=Math.round(x1+(x2-x1)*t), y=Math.round(y1+(y2-y1)*t);
            for(let dy=-radius;dy<=radius;dy++)for(let dx=-radius;dx<=radius;dx++)put(x+dx,y+dy,color);
        }
    };
    const points=[[5,2],[8,3],[10,5],[11,7],[10,10],[8,12],[5,13]];
    const color=stage===2 ? '#58e6a1' : stage===1 ? '#ffda66' : orange;
    // Dark silhouette with warm wood and a colored charge strip.
    for(let i=1;i<points.length;i++)line(...points[i-1],...points[i],ink,1);
    for(let i=1;i<points.length;i++)line(...points[i-1],...points[i],color);
    const pullX=stage < 0 ? 5 : 3-stage;
    line(5,2,pullX,7,'#f3e8cf');line(pullX,7,5,13,'#f3e8cf');
    put(10,6,handle);put(11,7,handle);put(11,8,handle);
    if(stage>=0){
        line(pullX,7,14,7,'#e5eaf4');
        put(13,6,color);put(14,7,color);put(13,8,color);
        put(pullX,6,'#b4a0d7');put(pullX,8,'#b4a0d7');
    }
    const name=stage<0 ? 'bow' : 'bow_pulling_'+stage;
    asset('assets/minecraft/textures/item/'+name+'.png',png(16,16,pixels));
    previews.push({name:'item/'+name,pixels});
}
const icon = Buffer.alloc(64 * 64 * 4);
for (let y = 0; y < 64; y++) for (let x = 0; x < 64; x++) {
    const edge = x < 3 || y < 3 || x > 60 || y > 60;
    const mark = (x >= 18 && x <= 25 && y >= 16 && y <= 48)
        || (x >= 25 && x <= 44 && y >= 16 && y <= 23)
        || (x >= 37 && x <= 44 && y >= 23 && y <= 33)
        || (x >= 25 && x <= 37 && y >= 31 && y <= 38);
    icon.set(rgba(edge || mark ? orange : '#141923'), (y * 64 + x) * 4);
}
asset('pack.png', png(64, 64, icon));
asset('pack.mcmeta', JSON.stringify({ pack: {
    description: 'PVPSE Ignite 16x | Short swords & clear PvP essentials',
    min_format: [75, 0], max_format: [75, 0]
}}, null, 2) + '\n');
asset('README.txt', 'PVPSE Ignite 16x — Minecraft Java 1.21.11\n\nOriginal 16x art: seven short swords, lucky-cat totem, golden carrot, golden and enchanted apples, ender pearl, XP bottle, end crystal and cobweb. Bow stages: orange (early), gold (middle), green (fully drawn).\nLowered shield in both hands, resting and blocking; vanilla banner rendering is preserved.\nOrange guards, dark outlines, distinct material colors. Other textures stay vanilla.\n\nInstall the ZIP in .minecraft/resourcepacks, then enable it above other packs.\nThis changes appearance only. It does not change reach, damage or attack timing.\nDisable the client custom crosshair if you prefer the vanilla crosshair.\n\nCopyright and permissions: see the PVPSE repository LICENSE.\n');
asset('LICENSE', readFileSync(join(root, 'LICENSE')));
for (const [name, data] of files) { const target = join(out, name); mkdirSync(dirname(target), { recursive: true }); writeFileSync(target, data); }
// Deterministic ZIP (stored entries); Minecraft accepts standard ZIP containers.
const entries = [], central = []; let offset = 0;
for (const [name, data] of [...files].sort(([a], [b]) => a.localeCompare(b))) {
    const filename = Buffer.from(name), crc = crc32(data);
    const h = Buffer.alloc(30); h.writeUInt32LE(0x04034b50); h.writeUInt16LE(20, 4);
    h.writeUInt16LE(0x800, 6); h.writeUInt16LE(0x21, 12); h.writeUInt32LE(crc, 14);
    h.writeUInt32LE(data.length, 18); h.writeUInt32LE(data.length, 22); h.writeUInt16LE(filename.length, 26);
    const c = Buffer.alloc(46); c.writeUInt32LE(0x02014b50); c.writeUInt16LE(20, 4); c.writeUInt16LE(20, 6);
    c.writeUInt16LE(0x800, 8); c.writeUInt16LE(0x21, 14); c.writeUInt32LE(crc, 16);
    c.writeUInt32LE(data.length, 20); c.writeUInt32LE(data.length, 24); c.writeUInt16LE(filename.length, 28);
    c.writeUInt32LE(offset, 42);
    entries.push(h, filename, data); central.push(c, filename); offset += h.length + filename.length + data.length;
}
const directory = Buffer.concat(central), end = Buffer.alloc(22);
end.writeUInt32LE(0x06054b50); end.writeUInt16LE(files.size, 8); end.writeUInt16LE(files.size, 10);
end.writeUInt32LE(directory.length, 12); end.writeUInt32LE(offset, 16);
mkdirSync(join(root, 'dist'), { recursive: true });
const zip = Buffer.concat([...entries, directory, end]);
writeFileSync(join(root, 'dist/PVPSE-Ignite-16x-1.21.11.zip'), zip);
assert.equal(files.size, 28);
for (const [name, data] of files) {
    if (name.endsWith('.json') || name.endsWith('.mcmeta')) JSON.parse(data.toString());
    if (name.endsWith('.png')) assert.equal(data.subarray(0,8).toString('hex'),'89504e470d0a1a0a');
}
const columns = 5, cell = 80, rows = Math.ceil(previews.length / columns);
const sheet = Buffer.alloc(columns * cell * rows * cell * 4);
for (let i = 0; i < sheet.length; i += 4) sheet.set([20,25,35,255],i);
previews.forEach(({pixels}, index) => {
    for (let y = 0; y < 16; y++) for (let x = 0; x < 16; x++) {
        const pixel = pixels.subarray((y*16+x)*4,(y*16+x)*4+4);
        if (!pixel[3]) continue;
        for (let yy=0; yy<4; yy++) for (let xx=0; xx<4; xx++) {
            const dx=(index%columns)*cell+8+x*4+xx, dy=Math.floor(index/columns)*cell+8+y*4+yy;
            sheet.set(pixel,(dy*columns*cell+dx)*4);
        }
    }
});
writeFileSync(join(root,'dist/PVPSE-Ignite-preview.png'),png(columns*cell,rows*cell,sheet));
assert.equal(JSON.parse(files.get('pack.mcmeta')).pack.min_format[0], 75);
console.log('Validated ' + files.size + ' pack entries; ZIP: ' + zip.length + ' bytes');
