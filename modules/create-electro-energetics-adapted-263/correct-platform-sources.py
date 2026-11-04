from pathlib import Path
import re,json,hashlib
r=Path(__file__).parent; out=r/'src/neoforge/java';m=json.loads((r/'source-fusion-manifest.json').read_text())
restored=[]
for e in m['sources']:
    if not e['origin'].startswith('createfly'): continue
    p=out/e['path']; txt=p.read_text(encoding='utf-8')
    if 'electroenergetics.registry.' in txt or p.name.endswith('Packet.java') or p.name=='ModEvents.java':
        upstream=r/'upstream-neoforge-reference/src/main/java'/e['path']
        if p.name=='ModEvents.java':continue
        t=upstream.read_text(encoding='utf-8')
        t=re.sub(r'\bResourceLocation\b','Identifier',t)
        t=re.sub(r'\.isClientSide\b(?!\s*\()', '.isClientSide()',t)
        p.write_text(t,encoding='utf-8'); e['origin']='neoforge-dev-4828c3487a9f';e['snapshot_sha256']=hashlib.sha256(upstream.read_bytes()).hexdigest();restored.append(e['path'])
(r/'source-fusion-manifest.json').write_text(json.dumps(m,indent=2),encoding='utf-8')
print('Restored platform-bound files',len(restored))
print('\n'.join(restored))
