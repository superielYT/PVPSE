// Original combat cues; ffmpeg required only when rebuilding the audio.
import {mkdirSync,writeFileSync,readFileSync} from 'node:fs';
import {spawnSync} from 'node:child_process';
import {join,resolve,dirname} from 'node:path';
import {fileURLToPath} from 'node:url';
const root=resolve(dirname(fileURLToPath(import.meta.url)),'..'),rate=48000,target=join(root,'build/audio-source');
mkdirSync(target,{recursive:true});mkdirSync(join(root,'assets/sounds'),{recursive:true});
let seed=984321;const noise=()=>{seed^=seed<<13;seed^=seed>>>17;seed^=seed<<5;return(seed>>>0)/2147483648-1;};
const tone=(t,start,freq,decay)=>{const dt=t-start;return dt<0?0:Math.sin(2*Math.PI*freq*dt)*Math.exp(-dt*decay)*(1-Math.exp(-dt*160));};
for(const [name,length,sample]of [
 ['totem',.62,t=>(tone(t,0,659,10)+tone(t,.075,880,10)+tone(t,.15,1108,9))*.25+noise()*.018*Math.exp(-t*10)],
 ['pearl',.23,t=>(noise()*.18+Math.sin(2*Math.PI*(510*t-620*t*t))*.2)*Math.sin(Math.PI*t/.23)**2*Math.exp(-t*7)],
 ['break',.29,t=>tone(t,0,1480,35)*.28+tone(t,.025,930,32)*.25+tone(t,.05,610,28)*.2+noise()*.13*Math.exp(-t*45)]
]){
 const count=Math.round(rate*length),data=Buffer.alloc(count*2),head=Buffer.alloc(44);let peak=0;
 for(let i=0;i<count;i++){const t=i/rate,edge=Math.min(1,i/192,(count-i)/384),v=Math.max(-.8,Math.min(.8,sample(t)*edge));peak=Math.max(peak,Math.abs(v));data.writeInt16LE(Math.round(v*32767),i*2);}
 head.write('RIFF');head.writeUInt32LE(36+data.length,4);head.write('WAVEfmt ',8);head.writeUInt32LE(16,16);head.writeUInt16LE(1,20);head.writeUInt16LE(1,22);head.writeUInt32LE(rate,24);head.writeUInt32LE(rate*2,28);head.writeUInt16LE(2,32);head.writeUInt16LE(16,34);head.write('data',36);head.writeUInt32LE(data.length,40);
 const wav=join(target,name+'.wav'),ogg=join(target,name+'.ogg');writeFileSync(wav,Buffer.concat([head,data]));
 const r=spawnSync('ffmpeg',['-hide_banner','-loglevel','error','-y','-i',wav,'-c:a','libvorbis','-q:a','4','-map_metadata','-1',ogg],{stdio:'inherit'});
 if(r.status!==0)throw Error('ffmpeg failed: '+name);
 writeFileSync(join(root,'assets/sounds',name+'.ogg.b64'),readFileSync(ogg).toString('base64')+'\n');
 console.log(name+': '+length+'s, peak '+peak.toFixed(3));
}
