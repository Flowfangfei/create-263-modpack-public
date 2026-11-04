#!/usr/bin/env python3
"""Compile a module against Gradle-exported real APIs without writing Gradle caches."""
from pathlib import Path
import argparse,datetime,fnmatch,json,os,subprocess,sys
ROOT=Path(__file__).resolve().parents[1]
def quoted(value):
 return '"'+str(value).replace('\\','/').replace('"','\\"')+'"'
def main():
 p=argparse.ArgumentParser(description=__doc__);p.add_argument('module');p.add_argument('--allow-missing',action='store_true');a=p.parse_args()
 module=(ROOT/'modules'/a.module).resolve()
 if not module.is_relative_to((ROOT/'modules').resolve()):raise ValueError('Invalid module')
 inputs=module/'build/port-compile-inputs.json'
 if not inputs.is_file():raise SystemExit('Primary agent must run exportPortCompileInputs first.')
 data=json.loads(inputs.read_text(encoding='utf-8'))
 config=json.loads((ROOT/'.local/config.json').read_text(encoding='utf-8'))
 javac=Path(os.environ.get('JAVA_HOME') or config['java_home'])/'bin'/('javac.exe' if os.name=='nt' else 'javac')
 stamp=datetime.datetime.now().strftime('%Y%m%d-%H%M%S-%f')
 dest=module/'build/port-javac'/stamp;dest.mkdir(parents=True,exist_ok=False)
 sources=[Path(x) for x in data['sources'] if Path(x).is_file()]
 if data.get('sourceRoots'):
  # Re-evaluate only the Gradle-exported source trees and exact include/exclude patterns.
  # Classpath/AT/dependency changes still require a fresh export and Gradle remains authoritative.
  discovered=set()
  for source_root in map(Path,data['sourceRoots']):
   if not source_root.is_dir():continue
   for source_file in source_root.rglob('*.java'):
    relative=source_file.relative_to(source_root).as_posix()
    includes=data.get('sourceIncludes',[])
    if includes and not any(fnmatch.fnmatchcase(relative,x) for x in includes):continue
    if any(fnmatch.fnmatchcase(relative,x) for x in data.get('sourceExcludes',[])):continue
    discovered.add(source_file)
  sources=sorted(discovered,key=str)
 missing=[x for x in data['classpath'] if not Path(x).exists()]
 if missing and not a.allow_missing:raise SystemExit('Missing real classpath entries: '+repr(missing))
 if missing:print('Pending dependencies remain unresolved; javac will report their missing types: '+repr(missing),flush=True)
 args=['-proc:none','-encoding','UTF-8','--release','25','-Xmaxerrs','2000','-d',str(dest/'classes'),'-classpath',os.pathsep.join(data['classpath'])]
 argfile=dest/'javac.args';argfile.write_text('\n'.join(quoted(x) for x in args+[str(f) for f in sources])+'\n',encoding='utf-8')
 log=dest/'javac.log'
 print('Diagnostic javac against exported Gradle classpath: '+str(log),flush=True)
 with log.open('wb') as stream:
  result=subprocess.run([str(javac),'-J-Duser.language=en','-J-Duser.country=US','@'+str(argfile)],cwd=module,stdout=stream,stderr=subprocess.STDOUT)
 print('\n'.join(log.read_text(encoding='utf-8',errors='replace').splitlines()[-18:]))
 (module/'build/last-port-javac.json').write_text(json.dumps({'log':str(log),'exit_code':result.returncode,'sources':len(sources),'missing_classpath_entries':missing,'inputs':str(inputs)},indent=2)+'\n',encoding='utf-8')
 return result.returncode
if __name__=='__main__':raise SystemExit(main())
