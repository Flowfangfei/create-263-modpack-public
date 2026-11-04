from pathlib import Path
import re,json,hashlib
r=Path(__file__).parent;src=r/'src/neoforge/java'; m=json.loads((r/'source-fusion-manifest.json').read_text()); entries={e['path']:e for e in m['sources']}
restored=[]
for p in src.rglob('*.java'):
    rel=p.relative_to(src);key=rel.as_posix(); e=entries.get(key)
    if e is None or not e['origin'].startswith('createfly'):continue
    t=p.read_text(encoding='utf-8'); missing=False
    for cls in re.findall(r'^import (com\.george_vi\.electroenergetics\.[\w.]+);',t,re.M):
        direct=src/(cls.replace('.','/')+'.java');outer=src/(cls.rsplit('.',1)[0].replace('.','/')+'.java')
        if not direct.exists() and not outer.exists():missing=True
    if not missing:continue
    q=r/'upstream-neoforge-reference/src/main/java'/rel
    t=q.read_text(encoding='utf-8');t=re.sub(r'\bResourceLocation\b','Identifier',t);t=re.sub(r'\.isClientSide\b(?!\s*\()', '.isClientSide()',t)
    p.write_text(t,encoding='utf-8');e['origin']='neoforge-dev-4828c3487a9f';e['snapshot_sha256']=hashlib.sha256(q.read_bytes()).hexdigest();restored.append(key)
(r/'source-fusion-manifest.json').write_text(json.dumps(m,indent=2),encoding='utf-8')
print('Restored incompatible local facades',len(restored))
