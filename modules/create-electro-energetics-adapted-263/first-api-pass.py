from pathlib import Path
import re,zipfile,json
r=Path(__file__).parent; out=r/'src/neoforge/java'
# Event registration belongs to the NeoForge platform, even when a facade imports no loader API.
p=out/'com/george_vi/electroenergetics/events/ModEvents.java'
p.write_text((r/'upstream-neoforge-reference/src/main/java/com/george_vi/electroenergetics/events/ModEvents.java').read_text(encoding='utf-8'),encoding='utf-8')
# Map changed imports only where the actual compiled dependencies expose exactly one matching class.
jars=list((r.parent/'steam-n-rails-263/tooling/create-adapted/libs').glob('*.jar'))
jars += list((r.parent/'create-liquid-fuel-adapted-263/build/moddev/artifacts').glob('minecraft-patched-26.3.0.22-beta.jar'))
classes=set(); bybase={}
for jar in jars:
    with zipfile.ZipFile(jar) as z:
        for n in z.namelist():
            if n.endswith('.class') and '$' not in n:
                c=n[:-6].replace('/','.');classes.add(c);bybase.setdefault(c.rsplit('.',1)[-1],set()).add(c)
for p in out.rglob('*.java'):
    t=p.read_text(encoding='utf-8').replace('ResourceLocation','Identifier')
    for cls in re.findall(r'^import ([\w.]+);',t,re.M):
        if not cls.startswith(('net.createmod.catnip.','dev.engine_room.flywheel.','net.minecraft.','net.neoforged.')) or cls in classes:continue
        cand=bybase.get(cls.rsplit('.',1)[-1],set())
        if len(cand)==1:t=t.replace(cls,next(iter(cand)))
    t=t.replace('import com.george_vi.electroenergetics.compat.strut_your_stuff.StrutYourStuffRegistryEntries;\n','')
    t=re.sub(r'\s*if \(ModList.get\(\).isLoaded\("struts"\)\)\s*StrutYourStuffRegistryEntries\.(register\(\)|fillCreativeTab\(output\));','',t)
    p.write_text(t,encoding='utf-8')
# Exclude legacy datagen entrypoints from the live runtime class. Generated JSON is retained in resources.
p=out/'com/george_vi/electroenergetics/events/ModEvents.java';t=p.read_text(encoding='utf-8')
for method in ['gatherDataHighPriority','gatherData']:
    pat=re.search(r'    @SubscribeEvent[^\n]*\n    public static void '+method+r'\([^)]*\) \{',t)
    if pat:
        i=pat.end();depth=1
        while depth:
            depth += (t[i]=='{')-(t[i]=='}');i+=1
        t=t[:pat.start()]+t[i:]
t=re.sub(r'^import .*events.datagen.\*;\n','',t,flags=re.M)
t=re.sub(r'^import .*(GatherDataEvent|DataGenerator|PackOutput|ProviderType);\n','',t,flags=re.M)
p.write_text(t,encoding='utf-8')
