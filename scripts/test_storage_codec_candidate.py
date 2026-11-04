#!/usr/bin/env python3
"""Validate a proposed storage codec in a fresh, exclusively owned baseline runtime."""
from pathlib import Path
import argparse,datetime,hashlib,json,os,shutil,socket,subprocess,time
from build import ROOT,config
from test_three_addons import owned,write_json,MARKER

def digest(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def main():
 p=argparse.ArgumentParser(description=__doc__);p.add_argument('--sha256',required=True);a=p.parse_args()
 proposal=ROOT/'modules/create-storage-adapted-263/tooling/persistence-proposal/storage-codec-probe.jar'
 if len(a.sha256)!=64 or digest(proposal)!=a.sha256:raise ValueError('Candidate SHA-256 mismatch')
 run=owned(ROOT/'runtime'/('storage-codec-'+datetime.datetime.now().strftime('%Y%m%d-%H%M%S-%f')))
 server=run/'server';server.mkdir(parents=True,exist_ok=False)
 (server/MARKER).write_text('create263-pack-owned-disposable-runtime\n',encoding='utf-8')
 local=json.loads((ROOT/'.local/config.json').read_text(encoding='utf-8'))
 template=Path(local['server_launch_template'])
 (server/'server-launch.args').write_text(template.read_text(encoding='utf-8').replace(template.parent.as_posix(),server.as_posix()),encoding='utf-8')
 (server/'eula.txt').write_text('eula=true\n',encoding='utf-8')
 (server/'server.properties').write_text('server-ip=127.0.0.1\nserver-port=25590\nonline-mode=false\nlevel-name=world\nlevel-type=minecraft:flat\ngenerator-settings={"biome":"minecraft:plains","layers":[{"block":"minecraft:bedrock","height":1},{"block":"minecraft:dirt","height":2},{"block":"minecraft:grass_block","height":1}]}\nview-distance=2\nsimulation-distance=2\nmax-players=1\n',encoding='utf-8')
 manifest=json.loads((ROOT/'artifacts/runtime/package-manifest.json').read_text(encoding='utf-8'))
 baseline=json.loads((ROOT/'manifests/runtime-modules.json').read_text(encoding='utf-8'))['modules'][:12]
 mods=server/'mods';mods.mkdir()
 copied={}
 for row in baseline:
  name=row['name'];src=ROOT/'artifacts/runtime/mods'/name
  if name not in manifest or digest(src)!=manifest[name]['runtime_sha256']:raise ValueError('Unverified baseline '+name)
  shutil.copy2(src,mods/name);copied[name]=digest(src)
 shutil.copy2(proposal,mods/'storage-codec-probe.jar')
 env,_=config();java=Path(env['JAVA_HOME'])/'bin'/('java.exe' if os.name=='nt' else 'java')
 with socket.socket() as port:port.bind(('127.0.0.1',25590))
 cmd=[str(java),'-Xms512M','-Xmx3G','--enable-native-access=ALL-UNNAMED','--enable-native-access=sable','-Dcreate263.probeRoot='+str(server),'@server-launch.args','nogui']
 log=run/'server.log';report=server/'storage-codec-probe-result.json';ready=False;time_checked=False;probe={}
 with log.open('wb') as stream:
  child=subprocess.Popen(cmd,cwd=server,env=env,stdin=subprocess.PIPE,stdout=stream,stderr=subprocess.STDOUT)
  print('Fresh owned codec runtime '+str(run)+'; PID '+str(child.pid),flush=True)
  deadline=time.monotonic()+180
  try:
   while child.poll() is None and time.monotonic()<deadline:
    text=log.read_text(encoding='utf-8',errors='replace')
    if not ready and 'Done (' in text:
     ready=True;child.stdin.write(b'time query gametime\n');child.stdin.flush()
    time_checked='The game time is ' in text
    if report.is_file():
     try:probe=json.loads(report.read_text(encoding='utf-8'))
     except json.JSONDecodeError:pass
    if ready and time_checked and probe:break
    time.sleep(1)
  finally:
   if child.poll() is None:
    try:child.stdin.write(b'stop\n');child.stdin.flush()
    except BrokenPipeError:pass
    try:child.wait(timeout=45)
    except subprocess.TimeoutExpired:child.kill();child.wait()
 summary={'startup_ready':ready,'vanilla_time_success':time_checked,'exit_code':child.returncode,'candidate_sha256':a.sha256,'baseline_jars':copied,'candidate':probe,'original_saves_accessed':False,'live_storage_entity_sources_changed':False,'log':log.relative_to(ROOT).as_posix()}
 summary['passed']=ready and time_checked and child.returncode==0 and probe.get('status')=='passed'
 write_json(run/'result.json',summary);write_json(ROOT/'.local/last-storage-codec-runtime.json',{'root':str(run)})
 print(json.dumps(summary,ensure_ascii=False,indent=2),flush=True)
 return 0 if summary['passed'] else 1
if __name__=='__main__':raise SystemExit(main())
