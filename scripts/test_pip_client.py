#!/usr/bin/env python3
"""Regress hot-air Ponder previews in fresh project-owned runtimes, without user worlds."""
from pathlib import Path
import argparse,datetime,json,shutil,subprocess,os
from build import ROOT,config
from test_three_addons import stage_client,owned,write_json,digest,settings

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('phase',choices=['baseline','candidate'])
    parser.add_argument('--aero',type=Path)
    args=parser.parse_args()
    run=owned(ROOT/'runtime'/('pip-'+args.phase+'-'+datetime.datetime.now().strftime('%Y%m%d-%H%M%S-%f')))
    (run/'server').mkdir(parents=True,exist_ok=False)
    # This stopped test seed was generated inside this project; no HMCL save is accessed.
    seed=owned(ROOT/'runtime/three-addons-20261029-005603-489197/server/world')
    shutil.copytree(seed,run/'server/world',ignore=shutil.ignore_patterns('session.lock'))
    dest=stage_client(run)
    installed=Path(settings().get('ponder_reference_mods_directory', ROOT/'artifacts/runtime/mods'))
    production=list(installed.glob('*.jar'))
    if len(production)!=18:raise ValueError('Expected current18-mod instance')
    for jar in production:shutil.copy2(jar,dest/'mods'/jar.name)
    if any(digest(jar)!=digest(dest/'mods'/jar.name) for jar in production):raise ValueError('Instance JAR copy mismatch')
    if args.aero:
        aero=owned(args.aero)
        with_path=aero.parent.parent/'package-manifest.json'
        manifest=json.loads(with_path.read_text(encoding='utf-8'))
        if digest(aero)!=manifest['aeronautics-adapted.jar']['runtime_sha256']:raise ValueError('Candidate checksum mismatch')
        shutil.copy2(aero,dest/'mods/aeronautics-adapted.jar')
    options=dest/'options.txt';settings={'renderDistance':'3','simulationDistance':'3','guiScale':'2','pauseOnLostFocus':'false','maxFps':'60'}
    lines=options.read_text(encoding='utf-8').splitlines();options.write_text('\n'.join(k+':'+settings[k] if (k:=line.partition(':')[0]) in settings else line for line in lines)+'\n',encoding='utf-8')
    checks={p.name:digest(p) for p in (dest/'mods').glob('*.jar')}
    write_json(run/'inputs.json',{'phase':args.phase,'jar_sha256':checks,'source_test_seed':str(seed),'client_directory':str(dest),'original_saves_accessed':False})
    env,_=config();java=Path(env['JAVA_HOME'])/'bin/java.exe';out=run/'client.log'
    cmd=[str(java),'-Xms512M','-Xmx3G','-Dcreate263.clientPipRegression=true','-Dcreate263.clientProbeRoot='+str(dest),'@launch-client.args','--quickPlaySingleplayer','three-addon-test-copy']
    timed_out=False
    with out.open('wb') as stream:
        child=subprocess.Popen(cmd,cwd=dest,env=env,stdout=stream,stderr=subprocess.STDOUT)
        print(f'Owned PiP {args.phase} client PID {child.pid}; log {out}',flush=True)
        try:child.wait(timeout=300)
        except subprocess.TimeoutExpired:
            timed_out=True;child.terminate()
            try:child.wait(timeout=20)
            except subprocess.TimeoutExpired:child.kill();child.wait()
    text=out.read_text(encoding='utf-8',errors='replace')
    file=dest/'pip-client-regression.json';probe=json.loads(file.read_text(encoding='utf-8')) if file.is_file() else {}
    reproduced='Render pass color attachment count must match pipeline color target state count.' in text
    result={'phase':args.phase,'exit_code':child.returncode,'timed_out':timed_out,'reproduced_reported_error':reproduced,'probe':probe,'jar_sha256':checks,'client_directory':str(dest),'log':str(out)}
    if args.phase=='baseline':result['passed']=reproduced and child.returncode!=0 and not timed_out
    else:result['passed']=child.returncode==0 and not timed_out and not reproduced and probe.get('passed') is True and probe.get('completed_scenes')==3 and probe.get('burner_color_target_counts')=={'burnerFlame':1,'irisCompositeBurnerFlame':1}
    write_json(run/'result.json',result)
    print(json.dumps({'passed':result['passed'],'reproduced':reproduced,'exit_code':child.returncode,'timed_out':timed_out,'report':str(run/'result.json'),'probe':{k:v for k,v in probe.items() if k!='samples'}},ensure_ascii=False,indent=2),flush=True)
    if not result['passed']:print('\n'.join(text.splitlines()[-25:]),flush=True)
    return 0 if result['passed'] else 1
if __name__=='__main__':raise SystemExit(main())
