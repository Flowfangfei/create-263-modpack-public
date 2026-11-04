from pathlib import Path
import re,zipfile,json,hashlib
r=Path(__file__).parent;dst=r/'src/neoforge/java';m=json.loads((r/'source-fusion-manifest.json').read_text());entries={x['path']:x for x in m['sources']}
classes=set();bybase={}
for jar in (r.parent/'steam-n-rails-263/tooling/create-adapted/libs').glob('*.jar'):
    with zipfile.ZipFile(jar) as z:
        for n in z.namelist():
            if n.endswith('.class') and '$' not in n:
                c=n[:-6].replace('/','.');classes.add(c);bybase.setdefault(c.rsplit('.',1)[-1],set()).add(c)
changes=[];rejected={}
for p in (r/'src/main/java/com/george_vi/electroenergetics').rglob('*.java'):
    if not (p.name.endswith('Block.java') or p.name.endswith('Item.java')):continue
    rel=p.relative_to(r/'src/main/java');target=dst/rel;e=entries.get(rel.as_posix())
    if not target.exists() or e is None or e['origin'].startswith('createfly'):continue
    t=p.read_text(encoding='utf-8'); mapped={};bad=False
    if any(x in t for x in ['net.fabricmc.','SpatialIntegration','team.reborn.','electroenergetics.registry.']):continue
    for cls in re.findall(r'^import (com\.zurrtum\.create\.[\w.*]+);',t,re.M):
        direct=cls.replace('com.zurrtum.create.','com.simibubi.create.')
        if direct in classes:mapped[cls]=direct;continue
        cand=bybase.get(cls.rsplit('.',1)[-1],set())
        if len(cand)==1:mapped[cls]=next(iter(cand))
        else:bad=True;rejected.setdefault(cls,[]).append(p.name)
    if bad:continue
    for cls in re.findall(r'^import (com\.george_vi\.electroenergetics\.[\w.]+);',t,re.M):
        direct=dst/(cls.replace('.','/')+'.java');outer=dst/(cls.rsplit('.',1)[0].replace('.','/')+'.java')
        if not direct.exists() and not outer.exists():bad=True
    if bad:continue
    for old,new in mapped.items():t=t.replace(old,new)
    target.write_text(t,encoding='utf-8');e['origin']='createfly-26.2-1033dcb653a3';e['snapshot_sha256']=hashlib.sha256(p.read_bytes()).hexdigest();e['verified_create_api_relocations']=mapped;changes.append(rel.as_posix())
(r/'source-fusion-manifest.json').write_text(json.dumps(m,indent=2),encoding='utf-8')
print('Highest-Minecraft block/item implementations merged:',len(changes));print('\n'.join(changes));print('Rejected API dependencies:',json.dumps(rejected))
