from pathlib import Path
import json,fnmatch,datetime,subprocess,os,re
module=Path(__file__).resolve().parents[1];root=module.parents[1]
data=json.loads((module/'build/port-compile-inputs.json').read_text(encoding='utf8'))
excluded=re.findall(r"['\"](\*\*/[^'\"]+)['\"]",(module/'build.gradle').read_text(encoding='utf8'))
sources=[str(f) for base in ['src/main/java','src/integration/sable/java'] for f in (module/base).rglob('*.java') if not any(fnmatch.fnmatch(f.as_posix(),pat) for pat in excluded)]
dest=module/'build/port-javac'/datetime.datetime.now().strftime('%Y%m%d-%H%M%S-%f');dest.mkdir(parents=True)
config=json.loads((root/'.local/config.json').read_text(encoding='utf8'));javac=Path(config['java_home'])/'bin/javac.exe'
args=['-proc:none','-encoding','UTF-8','--release','25','-Xmaxerrs','2000','-Xmaxwarns','2','-d',str(dest/'classes'),'-classpath',os.pathsep.join(data['classpath'])]+sources
quote=lambda s:'"'+str(s).replace('\\','/')+'"'
arg=dest/'javac.args';arg.write_text('\n'.join(map(quote,args)),encoding='utf8')
with (dest/'javac.log').open('wb') as log:r=subprocess.run([str(javac),'-J-Duser.language=en','-J-Duser.country=US','@'+str(arg)],stdout=log,stderr=subprocess.STDOUT)
print(dest/'javac.log',r.returncode,len(sources));print('\n'.join((dest/'javac.log').read_text(encoding='utf8').splitlines()[-18:]))
(module/'build/last-port-javac.json').write_text(json.dumps({'log':str(dest/'javac.log'),'exit_code':r.returncode,'sources':len(sources)},indent=2),encoding='utf8')
raise SystemExit(r.returncode)
