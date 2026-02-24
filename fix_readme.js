const fs=require('fs');
const path='README_DEV.md';
const lines=fs.readFileSync(path,'utf8').split(/\r?\n/);
const start=lines.findIndex(line=>line.startsWith('## Pr'));
if(start<0){throw new Error('start header not found');}
let section=[];
let end=start+1;
while(end<lines.length && lines[end].trim()){section.push(lines[end]); end++;}
const pieces=section.map(line=>{ const idx=line.indexOf('.'); return idx>=0 ? line.slice(idx+1).trim() : line.trim();});
const newPieces=pieces.filter(text=>!text.includes('Implementar valida'));
const newSection=newPieces.map((text,idx)=> (11+idx) + '. ' + text);
const updated=lines.slice(0,start).concat(newSection, lines.slice(end));
fs.writeFileSync(path, updated.join('\n') + '\n', 'utf8');
