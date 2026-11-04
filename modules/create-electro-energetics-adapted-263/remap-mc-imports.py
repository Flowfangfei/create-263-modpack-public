from pathlib import Path
import zipfile,re,json
r=Path(__file__).parent;src=r/'src/neoforge/java'
jar=r.parent/'create-aeronautics-adapted-263/aeronautics/build/moddev/artifacts/minecraft-patched-26.3.0.22-beta.jar'
classes=set(); bybase={}
with zipfile.ZipFile(jar) as z:
    for n in z.namelist():
        if n.endswith('.class') and '$' not in n:
            c=n[:-6].replace('/','.');classes.add(c);bybase.setdefault(c.rsplit('.',1)[-1],set()).add(c)
mapping={};missing={}
for p in src.rglob('*.java'):
    t=p.read_text(encoding='utf-8'); orig=t
    for cls in re.findall(r'^import (net\.minecraft\.[\w.]+);',t,re.M):
        if cls in classes:continue
        cand=bybase.get(cls.rsplit('.',1)[-1],set())
        if len(cand)==1:
            new=next(iter(cand));mapping[cls]=new;t=t.replace(cls,new)
        else: missing[cls]=list(cand)
    t=t.replace('net.createmod.catnip.lang.LangNumberFormat','net.createmod.catnip.api.lang.LangNumberFormat')
    if t!=orig:p.write_text(t,encoding='utf-8')
print(json.dumps({'actual_mc_relocations':mapping,'missing_or_ambiguous':missing},indent=2))
(r/'mc-api-import-audit.json').write_text(json.dumps({'actual_mc_relocations':mapping,'missing_or_ambiguous':missing},indent=2),encoding='utf-8')
