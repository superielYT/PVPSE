import {Canvas,rgba,mix} from './ignite-art.mjs';
// Ordered priority for combined books: rare/movement/combat first, utility last.
// Native component predicates match any level and do not require CIT or renaming.
export const enchantments=[
 ['wind_burst','WB','wind','#89d8e7'],['breach','BR','sword','#d794c5'],['density','DE','mace','#9da8d4'],
 ['lunge','LU','sword','#efb574'],['riptide','RI','wave','#5bcbd3'],['channeling','CH','bolt','#e6d37e'],
 ['frost_walker','FW','snow','#a9e2f7'],['soul_speed','SO','wing','#66d3cf'],['swift_sneak','SS','wing','#8dbbb0'],
 ['sharpness','SH','sword','#ee898d'],['smite','SM','sword','#f2d09b'],['bane_of_arthropods','BA','sword','#b8b46f'],
 ['fire_aspect','FA','flame','#f0a56d'],['knockback','KN','wind','#b29bdb'],['sweeping_edge','SE','sword','#c9a3d8'],
 ['protection','PR','shield','#7fafe4'],['blast_protection','BP','shield','#edb27c'],
 ['fire_protection','FP','shield','#ec8f79'],['projectile_protection','PP','shield','#a8d5a0'],
 ['feather_falling','FF','wing','#e2d8c4'],['thorns','TH','thorn','#a5c884'],['respiration','RE','wave','#7fbcd7'],
 ['aqua_affinity','AA','wave','#7ad5c6'],['depth_strider','DS','wave','#76a6d5'],
 ['power','PO','bow','#e9a3ba'],['punch','PU','bow','#c2a0d7'],['flame','FL','flame','#edac68'],
 ['infinity','IN','infinity','#b29fe7'],['multishot','MU','bow','#a1c38d'],['piercing','PI','bow','#99bbd5'],
 ['quick_charge','QC','bolt','#ebd48b'],['impaling','IM','sword','#7ac7c4'],['loyalty','LO','heart','#9fabe6'],
 ['binding_curse','BC','lock','#e57f9f'],['vanishing_curse','VC','ghost','#b28ecf'],
 ['mending','ME','heart','#86d6a3'],['unbreaking','UN','shield','#8da8cf'],['efficiency','EF','pick','#6dd0be'],
 ['fortune','FO','gem','#e9cc78'],['silk_touch','ST','gem','#d7b6e4'],['looting','LT','gem','#b3dc8c'],
 ['luck_of_the_sea','LS','wave','#81ccca'],['lure','LR','hook','#a3c9e3']
];
const glyph={A:['010','101','111','101','101'],B:['110','101','110','101','110'],C:['011','100','100','100','011'],
D:['110','101','101','101','110'],E:['111','100','110','100','111'],F:['111','100','110','100','100'],
G:['011','100','101','101','011'],H:['101','101','111','101','101'],I:['111','010','010','010','111'],
J:['001','001','001','101','010'],K:['101','101','110','101','101'],L:['100','100','100','100','111'],
M:['101','111','111','101','101'],N:['101','111','111','111','101'],O:['010','101','101','101','010'],
P:['110','101','110','100','100'],Q:['010','101','101','111','011'],R:['110','101','110','101','101'],
S:['011','100','010','001','110'],T:['111','010','010','010','010'],U:['101','101','101','101','111'],
V:['101','101','101','101','010'],W:['101','101','111','111','101'],Y:['101','101','010','010','010']};
export function drawBooks(){
    return [['enchanted_book','??','gem','#b39ce1'],...enchantments].map(([id,letters,symbol,hex])=>{
        const c=new Canvas(),color=rgba(hex),ink=rgba('#15202e'),light=mix(color,rgba('#ffffff'),.65);
        c.poly([[6,3],[23,3],[28,8],[28,28],[9,30],[4,25],[4,7]],ink);
        c.poly([[7,5],[22,5],[26,9],[26,26],[9,28],[6,24],[6,7]],mix(color,ink,.48));
        c.poly([[9,5],[22,5],[25,8],[25,24],[9,26]],mix(color,ink,.25));
        c.poly([[7,24],[10,26],[26,24],[26,27],[10,29],[7,26]],rgba('#e5ddcb'));
        c.line([11,27.2],[24.5,25.7],.5,rgba('#a59ca3'));
        c.line([8,6],[8,23],1.1,color);c.line([11,6],[22,6],.6,light);
        c.poly([[23,7],[25,9],[25,12],[22,9]],color);
        // Badge pictograms are shared by family; unique initials identify the enchantment.
        if(symbol==='sword'||symbol==='pick'||symbol==='mace'){
            c.line([14,17],[21,9],1.6,light);c.line([13,13],[18,17],1.4,color);
            if(symbol==='pick')c.path([[14,9],[19,8],[23,12]],1.5,light);
            if(symbol==='mace')c.poly([[18,8],[23,11],[20,15],[16,12]],light);
        }else if(symbol==='shield'||symbol==='lock'){
            c.poly([[13,10],[17,8],[22,10],[21,15],[17,18],[14,15]],light);
            c.poly([[15,11],[17,10],[20,11],[19,14],[17,16],[16,14]],mix(color,ink,.4));
        }else if(symbol==='heart'){
            c.poly([[17,11],[15,9],[12,10],[12,13],[17,18],[22,13],[22,10],[19,9]],light);
        }else if(symbol==='wave'||symbol==='wind'||symbol==='infinity'||symbol==='hook'){
            c.path([[12,12],[15,10],[20,10],[22,12],[20,14],[15,14],[13,16],[17,18],[21,16]],1.3,light);
        }else if(symbol==='bolt'||symbol==='flame'){
            c.poly([[18,8],[13,14],[17,14],[16,19],[22,12],[18,12],[20,8]],light);
        }else if(symbol==='wing'){
            c.path([[12,17],[21,9],[22,12],[19,16],[15,18]],1.5,light);
            c.line([16,12],[18,15],.7,color);
        }else if(symbol==='bow'){
            c.path([[14,9],[20,11],[22,14],[20,17],[14,18]],1.3,light);c.line([14,9],[14,18],.65,color);
        }else if(symbol==='snow'||symbol==='thorn'){
            for(let k=0;k<3;k++){const a=k*Math.PI/3;c.line([17-5*Math.cos(a),13-5*Math.sin(a)],[17+5*Math.cos(a),13+5*Math.sin(a)],1,light);}
        }else if(symbol==='ghost'){
            c.ellipse(17,12,4.5,4.5,light);c.poly([[12.5,12],[12.5,18],[15,16],[17,18],[19,16],[21.5,18],[21.5,12]],light);
            c.ellipse(15.5,12,.65,1,ink);c.ellipse(18.5,12,.65,1,ink);
        }else{
            c.poly([[17,8],[22,12],[17,18],[12,12]],light);c.poly([[17,10],[19,12],[17,15],[15,12]],color);
        }
        for(let letter=0;letter<letters.length;letter++)for(let y=0;y<5;y++)for(let x=0;x<3;x++){
            if(glyph[letters[letter]]?.[y][x]==='1')c.poly([[13+letter*4+x,20+y],[14+letter*4+x,20+y],[14+letter*4+x,21+y],[13+letter*4+x,21+y]],light);
        }
        return {name:id==='enchanted_book'?'item/enchanted_book':'item/books/'+id,label:id.toUpperCase().replaceAll('_',' '),canvas:c};
    });
}
