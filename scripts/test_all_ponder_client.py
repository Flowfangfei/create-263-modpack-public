#!/usr/bin/env python3
"""Compile the live Ponder registry and render bounded batches in owned test copies."""
from pathlib import Path
import argparse,datetime,json,shutil,subprocess,re
from build import ROOT,config
from test_three_addons import stage_client,owned,write_json,digest,settings

def classify_internal_errors(text):
    internal_errors=[]
    events=re.findall(r'<log4j:Event\b([^>]*)>(.*?)</log4j:Event>',text,re.S)
    for attributes,body in events:
        attrs=dict(re.findall(r'(\w+)="([^"]*)"',attributes))
        if attrs.get('level')!='ERROR':continue
        logger=attrs.get('logger','');match=re.search(r'<log4j:Message><!\[CDATA\[(.*?)\]\]></log4j:Message>',body,re.S)
        message=match.group(1) if match else body
        environment_error=(logger,message) in {
            ('oshi.driver.windows.registry.HkeyPerformanceDataUtil','Unable to locate English counter names in registry Perflib 009. Counters may need to be rebuilt: '),
            ('net.minecraft.client.Minecraft','Failed to fetch user properties'),
            ('com.mojang.realmsclient.client.RealmsClient','Failed to fetch Realms feature flags'),
            ('com.mojang.realmsclient.RealmsAvailability',"Couldn't connect to realms"),
            ('Railways','Failed to fetch dev cape data')}
        if not environment_error:
            row={'logger':logger,'message':message}
            if row not in internal_errors:internal_errors.append(row)
    if not events:internal_errors.append({'logger':'audit','message':'Expected XML log events missing; cannot validate swallowed errors'})
    return internal_errors

def main():
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('mode',choices=['scan','render']);parser.add_argument('--storage',type=Path,required=True);parser.add_argument('--scan',type=Path);parser.add_argument('--start',type=int,default=0);parser.add_argument('--limit',type=int,default=40);parser.add_argument('--candidate-mod',type=Path,action='append',default=[]);parser.add_argument('--reload-resources',action='store_true');args=parser.parse_args()
    run=owned(ROOT/'runtime'/('all-ponder-'+args.mode+'-'+datetime.datetime.now().strftime('%Y%m%d-%H%M%S-%f')));(run/'server').mkdir(parents=True,exist_ok=False)
    seed=owned(ROOT/'runtime/three-addons-20261029-005603-489197/server/world');shutil.copytree(seed,run/'server/world',ignore=shutil.ignore_patterns('session.lock'));dest=stage_client(run)
    installed=Path(settings().get('ponder_reference_mods_directory', ROOT/'artifacts/runtime/mods'));jars=list(installed.glob('*.jar'))
    if len(jars)!=18:raise ValueError('Expected18 production modules')
    for jar in jars:shutil.copy2(jar,dest/'mods'/jar.name)
    for source in [args.storage,*args.candidate_mod]:
        source=owned(source);manifest=json.loads((source.parent.parent/'package-manifest.json').read_text(encoding='utf-8'))
        if source.name not in manifest or digest(source)!=manifest[source.name]['runtime_sha256']:raise ValueError('Candidate hash mismatch')
        shutil.copy2(source,dest/'mods'/source.name)
    selected=[]
    if args.mode=='render':
        if not args.scan:raise ValueError('Need scan report')
        scan=owned(args.scan);data=json.loads(scan.read_text(encoding='utf-8'));selected=data.get('probe',data)['render_jobs'][args.start:args.start+args.limit]
        if not selected:raise ValueError('Empty render slice')
        write_json(dest/'ponder-render-jobs.json',selected)
    options=dest/'options.txt';settings={'renderDistance':'2','simulationDistance':'5','guiScale':'1','pauseOnLostFocus':'false','maxFps':'60'};lines=options.read_text(encoding='utf-8').splitlines();options.write_text('\n'.join(k+':'+settings[k] if (k:=line.partition(':')[0]) in settings else line for line in lines)+'\n',encoding='utf-8')
    inputs={p.name:digest(p) for p in (dest/'mods').glob('*.jar')};write_json(run/'inputs.json',{'mode':args.mode,'jar_sha256':inputs,'start':args.start,'limit':args.limit,'selected_jobs':selected,'reload_resources':args.reload_resources,'original_saves_accessed':False})
    env,_=config();java=Path(env['JAVA_HOME'])/'bin/java.exe';out=run/'client.log';cmd=[str(java),'-Xms512M','-Xmx4G','-Dcreate263.clientAllPonderAudit=true','-Dcreate263.allPonderMode='+args.mode,'-Dcreate263.clientProbeRoot='+str(dest),'@launch-client.args','--quickPlaySingleplayer','three-addon-test-copy']
    if args.reload_resources:cmd.insert(1,'-Dcreate263.ponderReloadResources=true')
    timed_out=False
    with out.open('wb') as stream:
        child=subprocess.Popen(cmd,cwd=dest,env=env,stdout=stream,stderr=subprocess.STDOUT);print(f'Owned all-Ponder {args.mode} PID{child.pid}; log {out}',flush=True)
        try:child.wait(timeout=900)
        except subprocess.TimeoutExpired:
            timed_out=True;child.terminate()
            try:child.wait(timeout=20)
            except subprocess.TimeoutExpired:child.kill();child.wait()
    report=dest/'all-ponder-client-audit.json';probe=json.loads(report.read_text(encoding='utf-8')) if report.exists() else {}
    text=out.read_text(encoding='utf-8',errors='replace');internal_errors=classify_internal_errors(text)
    result={'mode':args.mode,'passed':child.returncode==0 and not timed_out and probe.get('passed') is True and not internal_errors,'exit_code':child.returncode,'timed_out':timed_out,'jar_sha256':inputs,'start':args.start,'limit':args.limit,'probe':probe,'internal_errors':internal_errors,'client_directory':str(dest),'log':str(out)}
    write_json(run/'result.json',result);write_json(ROOT/('.local/last-all-ponder-'+args.mode+'.json'),{'result':str(run/'result.json')})
    print(json.dumps({'report':str(run/'result.json'),'passed':result['passed'],'exit_code':child.returncode,'timed_out':timed_out,'summary':{k:v for k,v in probe.items() if k not in ['checks','render_jobs','rendered','loaded_mods','content_checks']}},ensure_ascii=False,indent=2),flush=True)
    return 0 if result['passed'] else 1
if __name__=='__main__':raise SystemExit(main())
