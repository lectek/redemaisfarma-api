const fs=require('fs');
const path='README_DEV.md';
const lines=fs.readFileSync(path,'utf8').split(/\r?\n/);
const cleaned=lines.filter(line=>{ const trimmed=line.trim(); if(trimmed==='11.'||trimmed==='12.') return false; if(trimmed.startsWith('17.') && trimmed.includes('Implementar')) return false; return true;});
fs.writeFileSync(path, cleaned.join('\n') + '\n', 'utf8');
