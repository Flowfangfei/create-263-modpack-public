#!/usr/bin/env python3
"""Reproduce and regress vent meshing using only new project-owned client worlds."""
from pathlib import Path
import argparse, datetime, json, os, shutil, subprocess, time
from build import ROOT, config
from test_three_addons import stage_server, stage_client, owned, write_json, WORLD, MARKER, digest

def seed():
    run=stage_server(); server=owned(run/'server'); env,_=config()
    java=Path(env['JAVA_HOME'])/'bin/java.exe'; out=run/'seed-server.log'
    ready=False
    with out.open('wb') as stream:
        child=subprocess.Popen([str(java),'-Xms512M','-Xmx2G','@server-launch.args','nogui'],cwd=server,env=env,stdin=subprocess.PIPE,stdout=stream,stderr=subprocess.STDOUT)
        try:
            deadline=time.monotonic()+150
            while child.poll() is None and time.monotonic()<deadline:
                if 'Done (' in out.read_text(encoding='utf-8',errors='replace'):
                    ready=True; break
                time.sleep(1)
        finally:
            if child.poll() is None:
                child.stdin.write(b'save-all flush\nstop\n');child.stdin.flush()
                try:child.wait(timeout=45)
                except subprocess.TimeoutExpired:child.kill();child.wait()
    if not ready or child.returncode!=0:raise RuntimeError('New-world seed server failed; inspect '+str(out))
    write_json(ROOT/'.local/last-vent-runtime.json',{'root':str(run)})
    return run

def run_phase(run,phase,candidate=None,probe=None,storage_candidate=None):
    run=owned(run)
    if phase=='reload':
        source=owned(Path(json.loads((run/'candidate.json').read_text(encoding='utf-8'))['client_directory']))
        reload_run=owned(run/('reload-seed-'+datetime.datetime.now().strftime('%H%M%S-%f')));(reload_run/'server').mkdir(parents=True,exist_ok=False)
        shutil.copytree(source/'saves'/WORLD,reload_run/'server/world',ignore=shutil.ignore_patterns('session.lock'))
        dest=stage_client(reload_run)
    else:dest=stage_client(run)
    if probe is not None:
        probe=owned(probe)
        snapshot=json.loads((probe.parent/'probe-snapshot.json').read_text(encoding='utf-8'))
        if digest(probe)!=snapshot['probe_jar_sha256']:raise ValueError('Probe snapshot hash mismatch')
        shutil.copy2(probe,dest/'mods/create263-runtime-probe.jar')
    for artifact in [candidate,storage_candidate]:
        if artifact is None:continue
        artifact=artifact.resolve()
        if not artifact.is_relative_to((ROOT/'runtime').resolve()):raise ValueError('Candidate outside isolated runtime packaging output')
        if artifact.name not in {'railways-adapted.jar','storage-adapted.jar'}:raise ValueError('Unexpected hotfix module')
        manifest=json.loads((artifact.parent.parent/'package-manifest.json').read_text(encoding='utf-8'))
        if digest(artifact)!=manifest[artifact.name]['runtime_sha256']:raise ValueError('Packaged candidate hash mismatch')
        shutil.copy2(artifact,dest/'mods'/artifact.name)
    options=dest/'options.txt'
    lines=options.read_text(encoding='utf-8').splitlines()
    settings={'renderDistance':'4','simulationDistance':'3','guiScale':'2','pauseOnLostFocus':'false'}
    options.write_text('\n'.join(k+':'+settings[k] if (k:=line.partition(':')[0]) in settings else line for line in lines)+'\n',encoding='utf-8')
    env,_=config();java=Path(env['JAVA_HOME'])/'bin/java.exe';out=run/(phase+'-client.log')
    cmd=[str(java),'-Xms512M','-Xmx3G','-Dcreate263.clientVentRegression=true','-Dcreate263.clientProbeRoot='+str(dest),'@launch-client.args','--quickPlaySingleplayer',WORLD]
    if phase=='reload':cmd.insert(1,'-Dcreate263.ventReload=true')
    timed_out=False
    with out.open('wb') as stream:
        child=subprocess.Popen(cmd,cwd=dest,env=env,stdout=stream,stderr=subprocess.STDOUT)
        print(f'Owned {phase} client PID {child.pid}; log {out.relative_to(ROOT)}',flush=True)
        try:child.wait(timeout=220)
        except subprocess.TimeoutExpired:
            timed_out=True; child.terminate()
            try:child.wait(timeout=15)
            except subprocess.TimeoutExpired:child.kill();child.wait()
    text=out.read_text(encoding='utf-8',errors='replace');probe_file=dest/'vent-client-regression.json'
    report=json.loads(probe_file.read_text(encoding='utf-8')) if probe_file.is_file() else {}
    original='VentBlock.hidesNeighborFace' in text and 'AssertionError' in text
    result={'phase':phase,'timed_out':timed_out,'exit_code':child.returncode,'client_directory':str(dest),'railways_sha256':digest(dest/'mods/railways-adapted.jar'),'probe_sha256':digest(dest/'mods/create263-runtime-probe.jar'),'storage_sha256':digest(dest/'mods/storage-adapted.jar'),'probe':report,'original_assertion_reproduced':original,'log':out.relative_to(ROOT).as_posix()}
    result['passed']=original and (child.returncode!=0 or report.get('passed') is False) if phase=='baseline' else child.returncode==0 and report.get('passed') is True and not original
    result['passed']=result['passed'] and not timed_out
    write_json(run/(phase+'.json'),result)
    print(json.dumps(result,ensure_ascii=False),flush=True)
    if not result['passed']:print('\n'.join(text.splitlines()[-35:]),flush=True)
    return result['passed']

def main():
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('phase',choices=['baseline','candidate','reload']);parser.add_argument('--root',type=Path);parser.add_argument('--candidate',type=Path);parser.add_argument('--probe',type=Path);parser.add_argument('--storage-candidate',type=Path);args=parser.parse_args()
    run=args.root or (seed() if args.phase=='baseline' else Path(json.loads((ROOT/'.local/last-vent-runtime.json').read_text(encoding='utf-8'))['root']))
    return 0 if run_phase(run,args.phase,args.candidate,args.probe,args.storage_candidate) else 1
if __name__=='__main__':raise SystemExit(main())
