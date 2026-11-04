#!/usr/bin/env python3
"""Run a fresh isolated smoke world, using explicitly configured existing game libraries."""
from pathlib import Path
import argparse,datetime,json,os,shlex,shutil,socket,subprocess,time
from build import ROOT,config
WORLD='aeronautics-probe-copy'
def settings():return json.loads((ROOT/'.local/config.json').read_text(encoding='utf-8'))
def owned(path):
    p=path.resolve()
    if not p.is_relative_to(ROOT/'runtime') or p!=path.absolute():raise ValueError('Runtime output escapes owned directory')
    return p
def mods(dest):
    owned(dest);dest.mkdir(parents=True,exist_ok=True)
    expected=json.loads((ROOT/'artifacts/runtime/package-manifest.json').read_text())
    extras={p.name for p in dest.glob('*.jar')}-set(expected)
    if extras:raise ValueError('Unexpected mods in test directory: '+str(extras))
    from package import digest
    for name,item in expected.items():
        src=ROOT/'artifacts/runtime/mods'/name
        if digest(src)!=item['runtime_sha256']:raise ValueError('Runtime JAR hash mismatch')
        shutil.copy2(src,dest/name)
def stage_server():
    server=owned(ROOT/'runtime/smoke-server');server.mkdir(parents=True,exist_ok=True)
    template=Path(settings()['server_launch_template'])
    text=template.read_text(encoding='utf-8').replace(template.parent.as_posix(),server.as_posix())
    (server/'server-launch.args').write_text(text,encoding='utf-8')
    (server/'eula.txt').write_text('eula=true\n',encoding='utf-8')
    if not (server/'server.properties').exists():(server/'server.properties').write_text('server-ip=127.0.0.1\nserver-port=25589\nonline-mode=false\nlevel-name=world\nlevel-type=minecraft:flat\ngenerator-settings={"biome":"minecraft:plains","layers":[{"block":"minecraft:bedrock","height":1},{"block":"minecraft:dirt","height":2},{"block":"minecraft:grass_block","height":1}]}\nspawn-protection=0\nview-distance=3\nsimulation-distance=3\nmax-players=1\nallow-flight=true\n',encoding='utf-8')
    mods(server/'mods')
def run_server(timeout=300):
    server=owned(ROOT/'runtime/smoke-server')
    world=owned(server/'world')
    if (world/'level.dat').exists() and not (world/'data/minecraft/world_gen_settings.dat').exists():
        retained=owned(server/('incomplete-world-'+datetime.datetime.now().strftime('%Y%m%d-%H%M%S')))
        if retained.exists():raise FileExistsError(retained)
        world.rename(retained)
        for name in ['aero-probe-expected.txt','aero-probe-last-result.json']:
            if (server/name).exists():(server/name).rename(retained/name)
        print('Retained incomplete OWNED test world: '+retained.relative_to(ROOT).as_posix(),flush=True)
    env,_=config();java=Path(env['JAVA_HOME'])/'bin'/('java.exe' if os.name=='nt' else 'java')
    with socket.socket() as probe:probe.bind(('127.0.0.1',25589))
    logs=ROOT/'artifacts/runtime';stamp=datetime.datetime.now().astimezone().strftime('%Y%m%d-%H%M%S');out=logs/('server-'+stamp+'.log')
    result=server/'aero-probe-last-result.json';result.unlink(missing_ok=True)
    cmd=[str(java),'-Xms512M','-Xmx3G','--enable-native-access=ALL-UNNAMED','--enable-native-access=sable','-Daeronautics.adapted.runtimeChecks=true','-Dcreate263.probeRoot='+str(server),'@server-launch.args','nogui']
    ready=False
    with out.open('wb') as stream:
        child=subprocess.Popen(cmd,cwd=server,env=env,stdin=subprocess.PIPE,stdout=stream,stderr=subprocess.STDOUT)
        print('Owned server started; '+out.relative_to(ROOT).as_posix(),flush=True);deadline=time.monotonic()+timeout;ready_at=None
        while child.poll() is None and time.monotonic()<deadline:
            text=out.read_text(encoding='utf-8',errors='replace')
            if not ready and 'Done (' in text:
                ready=True;ready_at=time.monotonic()
                child.stdin.write(b'time query gametime\nforceload add 0 0\nsetblock 8 -60 8 create_connected:parallel_gearbox\nsetblock 10 -60 8 createdieselgenerators:diesel_engine\ndata get block 8 -60 8\ndata get block 10 -60 8\nexecute if block 8 -60 8 create_connected:parallel_gearbox run say CREATE263_CONNECTED_BLOCK_OK\nexecute if block 10 -60 8 createdieselgenerators:diesel_engine run say CREATE263_DIESEL_BLOCK_OK\n');child.stdin.flush()
            if ready and time.monotonic()-ready_at>25 and result.exists():break
            time.sleep(1)
        if child.poll() is None:
            child.stdin.write(b'stop\n');child.stdin.flush()
            try:child.wait(timeout=60)
            except subprocess.TimeoutExpired:child.kill();child.wait()
    text=out.read_text(encoding='utf-8',errors='replace');physics=json.loads(result.read_text()) if result.exists() else None
    report={'startup_ready':ready,'exit_code':child.returncode,'vanilla_time_success':'The game time is ' in text,'physics_probe':physics,'connected_block_present':'[Server] CREATE263_CONNECTED_BLOCK_OK' in text,'diesel_block_present':'[Server] CREATE263_DIESEL_BLOCK_OK' in text,'log':out.relative_to(ROOT).as_posix()}
    (out.with_suffix('.json')).write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8');print(json.dumps(report),flush=True)
    return 0 if ready and child.returncode==0 and report['vanilla_time_success'] and physics and physics.get('passed') and report['connected_block_present'] and report['diesel_block_present'] else 1

def stage_client():
    dest=owned(ROOT/'runtime/smoke-client');dest.mkdir(parents=True,exist_ok=True);src=Path(settings()['client_template_directory'])
    text=(src/'launch-client.args').read_text(encoding='utf-8').replace(src.as_posix(),dest.as_posix()).replace('SablePortTest','Create263Test').replace('AeroPortTest','Create263Test')
    tokens=shlex.split(text,posix=True)
    if tokens.count('--gameDir')!=1 or tokens[tokens.index('--gameDir')+1]!=dest.as_posix():raise ValueError('Incorrect client game directory')
    for name in ['natives','config','resourcepacks']:
        if (src/name).is_dir():shutil.copytree(src/name,dest/name,dirs_exist_ok=True)
    options=(src/'options.txt').read_text().replace('pauseOnLostFocus:true','pauseOnLostFocus:false');(dest/'options.txt').write_text(options,encoding='utf-8')
    (dest/'launch-client.args').write_text(text,encoding='utf-8');mods(dest/'mods')
    source=owned(ROOT/'runtime/smoke-server/world');target=owned(dest/'saves'/WORLD)
    if target.exists():raise FileExistsError('Test world copy already exists')
    if not (source/'level.dat').is_file():raise FileNotFoundError('Run and stop the fresh test server first')
    shutil.copytree(source,target,ignore=shutil.ignore_patterns('session.lock'))
    import hashlib
    hashes=lambda d:{p.relative_to(d).as_posix():hashlib.sha256(p.read_bytes()).hexdigest() for p in d.rglob('*') if p.is_file() and p.name!='session.lock'}
    if hashes(source)!=hashes(target):raise ValueError('Test world copy hash mismatch')
    print('Staged client with a verified copy of the NEW smoke world')
def run_client(timeout=300):
    dest=owned(ROOT/'runtime/smoke-client');env,_=config();java=Path(env['JAVA_HOME'])/'bin'/('java.exe' if os.name=='nt' else 'java');stamp=datetime.datetime.now().strftime('%Y%m%d-%H%M%S');out=ROOT/'artifacts/runtime'/('client-'+stamp+'.log')
    cmd=[str(java),'-Dsimulated.clientSmokeTest=true','-Dcreate263.clientProbeRoot='+str(dest),'-Dsimulated.clientSmokeWorld='+WORLD,'@launch-client.args','--quickPlaySingleplayer',WORLD]
    with out.open('wb') as stream:
        child=subprocess.Popen(cmd,cwd=dest,env=env,stdout=stream,stderr=subprocess.STDOUT);print('Owned client started; '+out.relative_to(ROOT).as_posix(),flush=True)
        try:child.wait(timeout=timeout)
        except subprocess.TimeoutExpired:child.terminate();child.wait(timeout=20)
    text=out.read_text(encoding='utf-8',errors='replace');report={'exit_code':child.returncode,'title_ready':'Aeronautics client smoke reached title screen after resource reload' in text,'world_loaded':'Aeronautics client world smoke loaded at ' in text,'world_saved':'Aeronautics client world smoke saved and returned to title screen' in text,'log':out.relative_to(ROOT).as_posix()}
    out.with_suffix('.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8');print(json.dumps(report),flush=True)
    return 0 if child.returncode==0 and all(report[k] for k in ['title_ready','world_loaded','world_saved']) else 1
if __name__=='__main__':
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('action',choices=['stage-server','server','stage-client','client']);a=parser.parse_args().action
    if a=='stage-server':stage_server()
    elif a=='stage-client':stage_client()
    else:raise SystemExit(run_server() if a=='server' else run_client())
