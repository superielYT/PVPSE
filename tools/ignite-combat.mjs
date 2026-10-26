import {Canvas,rgba,mix} from './ignite-art.mjs';
export function drawCombatArt(){
    const sprites=[['mace','#718ba3','#d2e3ef'],['mace_breach','#087dbf','#65edff'],
        ['mace_density','#8b303e','#ffc4ac'],['mace_wind_burst','#6752b5','#d3c5ff']].map(([id,base,edge])=>{
        const c=new Canvas(),ink=rgba('#102333'),col=rgba(base),bright=rgba(edge);
        c.line([5,28],[20,12],5,ink);c.line([5,28],[20,12],2.8,mix(col,ink,.25));
        for(let k=0;k<6;k++)c.line([5+k*1.8,26-k*1.9],[7+k*1.8,28-k*1.9],1.2,bright);
        c.poly([[10,8],[18,2],[26,5],[30,12],[25,21],[18,23],[10,17]],ink);
        c.poly([[12,8],[19,4],[25,7],[28,12],[24,19],[18,21],[12,16]],col);
        c.poly([[12,8],[19,4],[25,7],[20,12],[14,14]],bright);
        c.poly([[14,14],[20,12],[24,14],[21,20],[18,21],[12,16]],mix(col,ink,.3));
        c.poly([[24,9],[27,12],[23,18],[21,18],[22,13]],mix(col,bright,.55));
        c.line([14,8],[19,5],1.3,rgba('#eaffff'));
        c.poly([[17,7],[20,6],[22,8],[20,11],[17,11]],col);
        c.ellipse(5,28,1.7,1.7,ink);c.ellipse(5,28,.9,.9,bright);
        return {name:'item/'+id,label:id.toUpperCase().replaceAll('_',' '),canvas:c};
    });
    const c=new Canvas(),ink=rgba('#233347'),glass=rgba('#b0d5eb'),pink=rgba('#f46cde');
    c.poly([[16,2],[29,14],[17,28],[3,16]],ink);c.poly([[16,4],[27,14],[17,26],[5,16]],glass);
    c.poly([[16,8],[23,14],[17,22],[9,16]],pink);c.poly([[16,10],[21,14],[17,20],[11,16]],rgba('#fff3ff'));
    c.path([[16,4],[16,9],[25,16]],1.4,rgba('#f2ffff'));c.path([[6,15],[10,16],[17,25]],1.2,rgba('#ecffff'));
    c.poly([[15,11],[20,14],[17,18],[13,16]],pink);c.line([17,12],[16,17],1,rgba('#9f3cba'));
    c.poly([[7,27],[25,27],[29,31],[3,31]],ink);c.poly([[8,28],[24,28],[26,30],[6,30]],rgba('#615772'));
    sprites.push({name:'item/end_crystal',label:'END CRYSTAL',canvas:c});
    return sprites;
}
