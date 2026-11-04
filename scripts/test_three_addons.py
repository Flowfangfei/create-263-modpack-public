#!/usr/bin/env python3
"""Run the three add-ons together in an exclusively owned new world, then reload it."""
from pathlib import Path
import argparse,datetime,hashlib,json,os,shlex,shutil,socket,subprocess,time
from build import ROOT,config
WORLD='three-addon-test-copy'
MARKER='.create263-disposable-runtime'

def owned(path):
    p=path.resolve()
    if not p.is_relative_to((ROOT/'runtime').resolve()):raise ValueError('Test path escapes the project runtime directory')
    return p

def settings():return json.loads((ROOT/'.local/config.json').read_text(encoding='utf-8'))
def digest(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def checks():return json.loads((ROOT/'manifests/runtime-validation.json').read_text(encoding='utf-8'))['checks']
def write_json(p,v):p.write_text(json.dumps(v,indent=2,ensure_ascii=False)+'\n',encoding='utf-8')
def runtime_jars(dest):
    dest.mkdir(parents=True,exist_ok=True)
    manifest=json.loads((ROOT/'artifacts/runtime/package-manifest.json').read_text(encoding='utf-8'))
    required={x['name'] for x in json.loads((ROOT/'manifests/runtime-modules.json').read_text(encoding='utf-8'))['modules']}
    if set(manifest)!=required:raise ValueError('Package all runtime modules before testing')
    for name,row in manifest.items():
        src=ROOT/'artifacts/runtime/mods'/name
        if digest(src)!=row['runtime_sha256']:raise ValueError('Runtime hash mismatch: '+name)
        shutil.copy2(src,dest/name)

def test_pack(server):
    data=server/'world/datapacks/create263-probe'
    (data/'data/createliquidfuel/data_maps/fluid').mkdir(parents=True,exist_ok=True)
    (data/'data/create263_probe/compat').mkdir(parents=True,exist_ok=True)
    write_json(data/'pack.mcmeta',{'pack':{'description':'Disposable Create263 verification fuels','min_format':[121,0],'max_format':[121,0]}})
    write_json(data/'data/createliquidfuel/data_maps/fluid/liquid_fuel.json',{'values':{'minecraft:water':{'burnTime':32,'superHeat':True,'amountConsumedPerTick':10}}})
    write_json(data/'data/create263_probe/compat/legacy.json',{'fluid':'minecraft:flowing_water','burnTime':7,'amountConsumedPerTick':2})

def stage_server():
    stamp=datetime.datetime.now().strftime('%Y%m%d-%H%M%S-%f')
    run=owned(ROOT/'runtime'/('three-addons-'+stamp));run.mkdir(parents=True,exist_ok=False)
    server=owned(run/'server');server.mkdir()
    (server/MARKER).write_text('create263-pack-owned-disposable-runtime\n',encoding='utf-8')
    template=Path(settings()['server_launch_template'])
    launch=template.read_text(encoding='utf-8').replace(template.parent.as_posix(),server.as_posix())
    (server/'server-launch.args').write_text(launch,encoding='utf-8')
    (server/'eula.txt').write_text('eula=true\n',encoding='utf-8')
    (server/'server.properties').write_text('server-ip=127.0.0.1\nserver-port=25590\nonline-mode=false\nlevel-name=world\nlevel-type=minecraft:flat\ngenerator-settings={"biome":"minecraft:plains","layers":[{"block":"minecraft:bedrock","height":1},{"block":"minecraft:dirt","height":2},{"block":"minecraft:grass_block","height":1}]}\nspawn-protection=0\nview-distance=3\nsimulation-distance=3\nmax-players=1\nallow-flight=true\n',encoding='utf-8')
    runtime_jars(server/'mods')
    jars=list((ROOT/'tests/runtime-probe/build/libs').glob('create263_runtime_probe-*.jar'))
    jars=[p for p in jars if not p.name.endswith('-sources.jar')]
    if len(jars)!=1:raise ValueError('Build the runtime probe JAR first')
    shutil.copy2(jars[0],server/'mods/create263-runtime-probe.jar')
    test_pack(server)
    write_json(ROOT/'.local/last-three-addon-runtime.json',{'root':str(run)})
    print('Created NEW owned runtime: '+run.relative_to(ROOT).as_posix(),flush=True)
    return run

def run_server(run,phase,timeout=300):
    server=owned(run/'server')
    if not (server/MARKER).is_file():raise ValueError('Missing test marker')
    expected=checks()
    expected=[dict(row, result=row.get('reload_result',row['result'])) if phase=='reload' else row for row in expected]
    for row in expected:(server/row['result']).unlink(missing_ok=True)
    env,_=config();java=Path(env['JAVA_HOME'])/'bin'/('java.exe' if os.name=='nt' else 'java')
    with socket.socket() as s:s.bind(('127.0.0.1',25590))
    out=run/(phase+'-server.log')
    cmd=[str(java),'-Xms512M','-Xmx3G','--enable-native-access=ALL-UNNAMED','--enable-native-access=sable','-Dcreate263.probeRoot='+str(server)]
    cmd += ['-D'+row['flag']+'=true' for row in expected]
    if phase=='reload':
        for row in expected:
            for key,value in row.get('reload_properties',{}).items():cmd.append('-D'+key+'='+value)
    cmd += ['@server-launch.args','nogui']
    ready=False;reload_sent=False;reports={};completed=False
    with out.open('wb') as stream:
        child=subprocess.Popen(cmd,cwd=server,env=env,stdin=subprocess.PIPE,stdout=stream,stderr=subprocess.STDOUT)
        print(f'Owned {phase} server PID {child.pid}; log {out.relative_to(ROOT)}',flush=True)
        deadline=time.monotonic()+timeout
        try:
            while child.poll() is None and time.monotonic()<deadline:
                log=out.read_text(encoding='utf-8',errors='replace')
                if not ready and 'Done (' in log:
                    ready=True;child.stdin.write(b'time query gametime\n');child.stdin.flush()
                for row in expected:
                    path=server/row['result']
                    if path.is_file():
                        try:reports[row['id']]=json.loads(path.read_text(encoding='utf-8'))
                        except json.JSONDecodeError:pass
                if any(report.get('passed') is False or report.get('status')=='failed' for report in reports.values()):break
                liquid=reports.get('liquid-fuel',{})
                if liquid.get('phase')=='ready-for-data-reload' and not reload_sent:
                    data=server/'world/datapacks/create263-probe/data'
                    write_json(data/'createliquidfuel/data_maps/fluid/liquid_fuel.json',{'values':{'minecraft:water':{'burnTime':64,'superHeat':True,'amountConsumedPerTick':10}}})
                    owned(data/'create263_probe/compat/legacy.json').unlink()
                    child.stdin.write(b'reload\n');child.stdin.flush();reload_sent=True
                    print('Requested actual datapack reload in the disposable world',flush=True)
                if len(reports)==len(expected) and all(row.get('passed') is True or row.get('status')=='passed' for row in reports.values()):completed=True;break
                time.sleep(1)
        finally:
            if child.poll() is None:
                try:child.stdin.write(b'save-all flush\nstop\n');child.stdin.flush()
                except BrokenPipeError:pass
                try:child.wait(timeout=45)
                except subprocess.TimeoutExpired:child.kill();child.wait()
    text=out.read_text(encoding='utf-8',errors='replace')
    summary={'phase':phase,'startup_ready':ready,'exit_code':child.returncode,'vanilla_time_success':'The game time is ' in text,'completed':completed,'datapack_reload_requested':reload_sent,'reports':reports,'log':out.relative_to(ROOT).as_posix()}
    summary['passed']=ready and completed and child.returncode==0 and summary['vanilla_time_success']
    write_json(run/(phase+'-server.json'),summary)
    print(json.dumps(summary,ensure_ascii=False),flush=True)
    if not summary['passed']:print('\n'.join(text.splitlines()[-22:]),flush=True)
    return summary['passed']

def stage_client(run):
    src=Path(settings()['client_template_directory'])
    name='client' if not (run/'client').exists() else 'client-'+datetime.datetime.now().strftime('%H%M%S-%f')
    dest=owned(run/name);dest.mkdir(exist_ok=False)
    (dest/MARKER).write_text('create263-pack-owned-disposable-runtime\n',encoding='utf-8')
    text=(src/'launch-client.args').read_text(encoding='utf-8').replace(src.as_posix(),dest.as_posix()).replace('SablePortTest','Create263Test').replace('AeroPortTest','Create263Test')
    tokens=shlex.split(text,posix=True)
    if tokens.count('--gameDir')!=1 or tokens[tokens.index('--gameDir')+1]!=dest.as_posix():raise ValueError('Client game directory mismatch')
    (dest/'launch-client.args').write_text(text,encoding='utf-8')
    for name in ['natives','config','resourcepacks']:
        if (src/name).is_dir():shutil.copytree(src/name,dest/name)
    options=(src/'options.txt').read_text(encoding='utf-8').replace('pauseOnLostFocus:true','pauseOnLostFocus:false')
    (dest/'options.txt').write_text(options,encoding='utf-8');runtime_jars(dest/'mods')
    probe_jars=[p for p in (ROOT/'tests/runtime-probe/build/libs').glob('create263_runtime_probe-*.jar') if not p.name.endswith('-sources.jar')]
    if len(probe_jars)!=1:raise ValueError('Build exactly one current client runtime probe first')
    shutil.copy2(probe_jars[0],dest/'mods/create263-runtime-probe.jar')
    source=owned(run/'server/world');target=owned(dest/'saves'/WORLD)
    if not (source/'level.dat').is_file():raise ValueError('No stopped server world exists')
    shutil.copytree(source,target,ignore=shutil.ignore_patterns('session.lock'))
    def hashes(p):return {f.relative_to(p).as_posix():digest(f) for f in p.rglob('*') if f.is_file() and f.name!='session.lock'}
    if hashes(source)!=hashes(target):raise ValueError('Copied test-world hashes differ')
    five=owned(run/'server/five-addons-probe-result.json')
    if five.is_file():shutil.copy2(five,dest/'server-five-addons-probe-result.json')
    print('Prepared a verified client COPY of the new test world',flush=True)
    return dest

def run_client(run):
    dest=stage_client(run);env,_=config();java=Path(env['JAVA_HOME'])/'bin'/('java.exe' if os.name=='nt' else 'java');out=run/(dest.name+'.log')
    cmd=[str(java),'-Dcreate263.clientSmoke=true','-Dcreate263.clientFiveAddons=true','-Dcreate263.clientProbeRoot='+str(dest),'@launch-client.args','--quickPlaySingleplayer',WORLD]
    with out.open('wb') as stream:
        child=subprocess.Popen(cmd,cwd=dest,env=env,stdout=stream,stderr=subprocess.STDOUT);print('Owned client PID '+str(child.pid),flush=True)
        try:child.wait(timeout=300)
        except subprocess.TimeoutExpired:
            child.terminate()
            try:child.wait(timeout=20)
            except subprocess.TimeoutExpired:child.kill();child.wait()
    text=out.read_text(encoding='utf-8',errors='replace')
    probe_path=dest/'joint-client-probe.json'
    probe=json.loads(probe_path.read_text(encoding='utf-8')) if probe_path.exists() else {}
    summary={'exit_code':child.returncode,'probe':probe,'client_directory':dest.relative_to(ROOT).as_posix(),'log':out.relative_to(ROOT).as_posix()}
    summary['passed']=child.returncode==0 and probe.get('passed') is True
    write_json(run/'client.json',summary);print(json.dumps(summary),flush=True)
    if not summary['passed']:print('\n'.join(text.splitlines()[-70:]),flush=True)
    return summary['passed']

def main():
    p=argparse.ArgumentParser(description=__doc__);p.add_argument('action',choices=['server','client']);p.add_argument('--root',type=Path);a=p.parse_args()
    if a.action=='server':
        run=stage_server()
        if not run_server(run,'create'):return 1
        return 0 if run_server(run,'reload') else 1
    run=owned(a.root) if a.root else owned(Path(json.loads((ROOT/'.local/last-three-addon-runtime.json').read_text(encoding='utf-8'))['root']))
    return 0 if run_client(run) else 1
if __name__=='__main__':raise SystemExit(main())
