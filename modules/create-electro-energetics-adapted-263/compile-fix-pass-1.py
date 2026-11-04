from pathlib import Path
import re,json,hashlib
r=Path(__file__).parent;s=r/'src/neoforge/java';u=r/'upstream-neoforge-reference/src/main/java';m=json.loads((r/'source-fusion-manifest.json').read_text());entries={e['path']:e for e in m['sources']}
# Config leaf classes inherit the real NeoForge-compatible Catnip ConfigBase.
for p in (u/'com/george_vi/electroenergetics/config').glob('*.java'):
    if p.name=='CEEConfigs.java':continue
    rel=p.relative_to(u);t=p.read_text(encoding='utf-8').replace('net.createmod.catnip.config.ConfigBase','net.createmod.catnip.api.config.ConfigBase')
    (s/rel).write_text(t,encoding='utf-8')
    entries[rel.as_posix()]['origin']='neoforge-dev-4828c3487a9f';entries[rel.as_posix()]['snapshot_sha256']=hashlib.sha256(p.read_bytes()).hexdigest()
# Unsupported optional mods keep complete original sources, with no substitute implementation.
for name in ['CreateElectroEnergetics','content/accumulator/AccumulatorBlockEntity','content/gauge/ElectricGaugeBlockEntity','content/energy_meter/EnergyMeterBlockEntity']:
    p=s/('com/george_vi/electroenergetics/'+name+'.java');t=p.read_text(encoding='utf-8')
    t=re.sub(r'^import .*([Cc]omputercraft|PeripheralCapability).*;\n','',t,flags=re.M)
    t=re.sub(r'^    public AbstractComputerBehaviour computerBehaviour;\n','',t,flags=re.M)
    t=re.sub(r'^        behaviours.add\(computerBehaviour = CCProxy.behaviour\(this\)\);\n','',t,flags=re.M)
    t=re.sub(r'^        CCProxy.register\(\);\n','',t,flags=re.M)
    pat=re.search(r'    public static void registerCapabilities\(RegisterCapabilitiesEvent event\) \{',t)
    if pat:
        start=pat.start();i=pat.end();d=1
        while d:d+=(t[i]=='{')-(t[i]=='}');i+=1
        t=t[:start]+t[i:]
    p.write_text(t,encoding='utf-8')
p=s/'com/george_vi/electroenergetics/events/ModEvents.java';t=p.read_text(encoding='utf-8');t=re.sub(r'^        (ElectricGauge|Accumulator|EnergyMeter)BlockEntity.registerCapabilities\(event\);\n','',t,flags=re.M);p.write_text(t,encoding='utf-8')
for name in ['CEEWireAttachments','CEEWireInteractionBehaviours']:
    p=s/('com/george_vi/electroenergetics/'+name+'.java');t=p.read_text(encoding='utf-8');t=re.sub(r'^.*Bunting.*\n','',t,flags=re.M);p.write_text(t,encoding='utf-8')
# Adopt typed defaults matching the old NBT API for all literal-key legacy reads.
defaults={'Byte':'(byte) 0','Short':'(short) 0','Int':'0','Long':'0L','Float':'0f','Double':'0d','Boolean':'false','String':'""'}
for p in s.rglob('*.java'):
    t=p.read_text(encoding='utf-8');old=t
    for typ,default in defaults.items():
        t=re.sub(r'\.get'+typ+r'\(("(?:[^"\\]|\\.)*")\)(?!\s*\.\s*(?:orElse|map|isPresent|isEmpty|ifPresent))',lambda a:'.get'+typ+'Or('+a[1]+', '+default+')',t)
    t=re.sub(r'\.getCompound\(("(?:[^"\\]|\\.)*")\)(?!\s*\.\s*(?:orElse|map|isPresent|isEmpty|ifPresent))',r'.getCompoundOrEmpty(\1)',t)
    t=re.sub(r'\.getList\(("(?:[^"\\]|\\.)*"),\s*[^)]+\)',r'.getListOrEmpty(\1)',t)
    t=t.replace('net.createmod.catnip.config.ui.BaseConfigScreen','net.createmod.catnip.api.client.config.BaseConfigScreen')
    t=re.sub(r'\.random\b(?!\s*\()', '.getRandom()',t)
    t=t.replace('import org.lwjgl.glfw.GLFW;\n','').replace('GLFW.GLFW_MOUSE_BUTTON_LEFT','0')
    if t!=old:p.write_text(t,encoding='utf-8')
# The packaged generated assets are authoritative; do not compile legacy offline data callbacks.
for name in ['CEEBlocks','CEEItems']:
    p=s/('com/george_vi/electroenergetics/'+name+'.java');t=p.read_text(encoding='utf-8')
    for method in ['loot','blockstate','model']:
        while True:
            match=re.search(r'\.'+method+r'\(',t)
            if not match:break
            a=match.start();i=match.end();d=1;quote=None;escape=False
            while d:
                c=t[i]
                if quote:
                    if escape:escape=False
                    elif c=='\\':escape=True
                    elif c==quote:quote=None
                elif c in ['"',"'"]:quote=c
                elif c=='(':d+=1
                elif c==')':d-=1
                i+=1
            t=t[:a]+t[i:]
    for cls in re.findall(r'^import ([\w.]+);',t,re.M):
        simple=cls.rsplit('.',1)[-1]
        body=t[t.find('public class'):]
        if simple not in body:t=t.replace('import '+cls+';\n','')
    p.write_text(t,encoding='utf-8')
(r/'source-fusion-manifest.json').write_text(json.dumps(m,indent=2),encoding='utf-8')
p=r/'build.gradle';t=p.read_text(encoding='utf-8');t += "\nsourceSets.main.java.exclude 'com/george_vi/electroenergetics/compat/computercraft/**'\nsourceSets.main.java.exclude 'com/george_vi/electroenergetics/content/wire/attachments/Bunting*'\ntasks.withType(JavaCompile).configureEach { options.compilerArgs += ['-Xmaxerrs', '2000'] }\n";p.write_text(t,encoding='utf-8')
