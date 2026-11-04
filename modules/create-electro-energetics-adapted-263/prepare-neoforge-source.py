from pathlib import Path
import shutil, re, json, zipfile, hashlib
r=Path(__file__).parent
out=r/'src/neoforge/java'
manifest=[]
for p in (r/'upstream-neoforge-reference/src/main/java').rglob('*.java'):
    rel=p.relative_to(r/'upstream-neoforge-reference/src/main/java')
    source=p; origin='neoforge-dev-4828c3487a9f'
    newer=r/'src/main/java'/rel
    # Merge the highest-Minecraft snapshot where it has no platform-specific imports.
    if newer.exists():
        txt=newer.read_text(encoding='utf-8')
        if not any(x in txt for x in ['com.zurrtum.', 'net.fabricmc.', 'SpatialIntegration', 'team.reborn.']):
            source=newer; origin='createfly-26.2-1033dcb653a3'
    dest=out/rel; dest.parent.mkdir(parents=True,exist_ok=True)
    txt=source.read_text(encoding='utf-8').replace('ResourceLocation','Identifier')
    txt=re.sub(r'\.isClientSide\b(?!\s*\()', '.isClientSide()',txt)
    dest.write_text(txt,encoding='utf-8')
    manifest.append({'path':str(rel).replace('\\','/'),'origin':origin,'snapshot_sha256':hashlib.sha256(source.read_bytes()).hexdigest()})
# Use class entries from the real built dependencies to map relocated Catnip/Flywheel API classes.
libs=r.parent/'steam-n-rails-263/tooling/create-adapted/libs'
jars=list(libs.glob('*.jar'))+[r.parent/'sable-adapted-263/build/libs/sable-neoforge-26.3-3.0.0-adapted.1.jar']
classes=set(); bybase={}
for jar in jars:
    with zipfile.ZipFile(jar) as z:
        for name in z.namelist():
            if name.endswith('.class') and '$' not in name:
                cls=name[:-6].replace('/','.')
                classes.add(cls); bybase.setdefault(cls.rsplit('.',1)[-1],set()).add(cls)
relocations={}
for p in out.rglob('*.java'):
    txt=p.read_text(encoding='utf-8')
    for cls in re.findall(r'^import ([\w.]+);',txt,re.M):
        if not cls.startswith(('net.createmod.catnip.','dev.engine_room.flywheel.')) or cls in classes: continue
        candidates=bybase.get(cls.rsplit('.',1)[-1],set())
        if len(candidates)==1:
            new=next(iter(candidates)); relocations[cls]=new; txt=txt.replace(cls,new)
    p.write_text(txt,encoding='utf-8')
(r/'source-fusion-manifest.json').write_text(json.dumps({'sources':manifest,'verified_api_relocations':relocations},indent=2),encoding='utf-8')
print('Source files',len(manifest),'highest-Minecraft files',sum(x['origin'].startswith('createfly') for x in manifest),'actual API relocations',len(relocations))
