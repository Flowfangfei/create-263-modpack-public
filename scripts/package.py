#!/usr/bin/env python3
"""Assemble source-built runtime jars and a restricted-asset-free release payload."""
from pathlib import Path
import hashlib,json,os,shutil,subprocess,zipfile,tomllib
from build import ROOT,MODULES,config
from publication_policy import restricted_payload_entries,sanitize_editor_metadata

def digest(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def one(module,pattern):
    files=list((MODULES/module).glob(pattern))
    if len(files)!=1:raise ValueError(f'Expected one build artifact: {module}/{pattern}: {len(files)}')
    return files[0]
def assemble(runtime_modules=None, output_root=None):
    """Assemble all release modules, or an explicitly isolated diagnostic subset."""
    output_root=ROOT/'artifacts' if output_root is None else Path(output_root).resolve()
    if output_root!=(ROOT/'artifacts').resolve() and not output_root.is_relative_to((ROOT/'runtime').resolve()):
        raise ValueError('Diagnostic output must stay in this project/runtime')
    if runtime_modules is None:
        runtime_modules=json.loads((ROOT/'manifests/runtime-modules.json').read_text(encoding='utf-8'))['modules']
    if not runtime_modules:raise ValueError('No runtime modules selected')
    env,_=config();java=str(Path(env['JAVA_HOME'])/'bin'/('java.exe' if os.name=='nt' else 'java'))
    cache=Path(env['GRADLE_USER_HOME'])/'caches/modules-2/files-2.1/org.ow2.asm'
    asm=[one_cache(cache/a/'9.10.1','*.jar') for a in ['asm','asm-commons']]
    cp=os.pathsep.join(map(str,[MODULES/'create-adapted-263/build/flywheel-isolation-tool',*asm]))
    runtime=output_root/'runtime/mods';runtime.mkdir(parents=True,exist_ok=True)
    inputs={row['name']:one(row['module'],row['artifact']) for row in runtime_modules}
    restricted_entries=restricted_payload_entries(json.loads((ROOT/'manifests/resources-lock.json').read_text(encoding='utf-8')))
    package_owners={}
    report={};payload=output_root/'payload/mods';payload.mkdir(parents=True,exist_ok=True)
    for name,source in inputs.items():
        out=runtime/name
        if name.startswith('create-'):shutil.copy2(source,out)
        else:subprocess.run([java,'-cp',cp,'IsolateFlywheel',str(source),str(out)],check=True,env=env,stdout=subprocess.DEVNULL)
        with zipfile.ZipFile(out) as jar:
            names=jar.namelist()
            metadata=tomllib.loads(jar.read('META-INF/neoforge.mods.toml').decode('utf-8'))
            for dependencies in metadata.get('dependencies',{}).values():
                if any(d.get('versionRange')=='' for d in dependencies):raise ValueError('Empty dependency version range: '+name)
            if 'META-INF/neoforge.mods.toml' not in names:raise ValueError('No NeoForge metadata: '+name)
            if 'fabric.mod.json' in names:raise ValueError('Unexpected Fabric entry point: '+name)
            classes=[n for n in names if n.endswith('.class')]
            for package in {n.rsplit('/',1)[0] for n in classes if '/' in n}:
                if package in package_owners:raise ValueError('Split Java package across mod files: '+package+' in '+package_owners[package]+' and '+name)
                package_owners[package]=name
            if not name.startswith('create-'):
                foreign=[n for n in classes if n.startswith(('net/minecraft/','net/neoforged/','net/fabricmc/','com/simibubi/create/','com/zurrtum/create/','com/tterrag/','dev/engine_room/'))]
                if foreign:raise ValueError('Foreign classes in addon: '+str(foreign[:5]))
                if any(b'dev/engine_room/flywheel' in jar.read(n) for n in classes):raise ValueError('Unisolated Flywheel: '+name)
            stripped=[];sanitized=[]
            with zipfile.ZipFile(payload/name,'w',zipfile.ZIP_DEFLATED,compresslevel=9) as stripped_jar:
                for entry in jar.infolist():
                    restricted=entry.filename in restricted_entries.get(name,set()) or (name in ('aeronautics-adapted.jar','simulated-adapted.jar','offroad-adapted.jar') and entry.filename.startswith(('assets/aeronautics/','assets/simulated/','assets/offroad/')))
                    if restricted:stripped.append(entry.filename);continue
                    original=jar.read(entry);contents=sanitize_editor_metadata(entry.filename,original)
                    if contents!=original:sanitized.append(entry.filename)
                    stripped_jar.writestr(entry,contents)
        report[name]={'source':source.relative_to(ROOT).as_posix(),'input_sha256':digest(source),'runtime_sha256':digest(out),'payload_sha256':digest(payload/name),'excluded_resource_entries':len(stripped),'sanitized_editor_metadata':sanitized,'classes':len(classes)}
    (output_root/'runtime/package-manifest.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8')
    return report

def main():
    print(json.dumps(assemble(),indent=2))
def one_cache(root,pattern):
    files=list(root.glob('*/'+pattern))
    if len(files)!=1:raise ValueError('Missing or ambiguous ASM dependency: '+str(root))
    return files[0]
if __name__=='__main__':main()
