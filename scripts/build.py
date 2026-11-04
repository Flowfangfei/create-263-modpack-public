#!/usr/bin/env python3
"""Build licensed source modules in dependency order. No publishing or game launch."""
from pathlib import Path
import argparse,json,os,subprocess,sys,datetime
from gradle_lock import gradle_writer
ROOT=Path(__file__).resolve().parents[1]
MODULES=ROOT/'modules'

def config():
    path=ROOT/'.local/config.json'
    local=json.loads(path.read_text(encoding='utf-8')) if path.is_file() else {}
    env=os.environ.copy()
    java_home=env.get('JAVA_HOME') or local.get('java_home')
    if not java_home:raise SystemExit('Set JAVA_HOME to a Java 25 JDK.')
    env['JAVA_HOME']=java_home
    env['PATH']=str(Path(java_home)/'bin')+os.pathsep+env.get('PATH','')
    env['GRADLE_USER_HOME']=env.get('GRADLE_USER_HOME') or local.get('gradle_user_home') or str(ROOT/'tooling/gradle-user-home')
    gradle=env.get('GRADLE_CMD') or local.get('gradle_cmd') or str(MODULES/'create-adapted-263'/('gradlew.bat' if os.name=='nt' else 'gradlew'))
    return env,gradle

def run_gradle(module,tasks):
    env,gradle=config();stamp=datetime.datetime.now().astimezone().strftime('%Y%m%d-%H%M%S')
    output=ROOT/'artifacts/build'/f'{module.replace(chr(47),chr(95))}-{stamp}.log';output.parent.mkdir(parents=True,exist_ok=True)
    module_path=ROOT/'tests/runtime-probe' if module=='runtime-probe' else MODULES/module
    command=[gradle,'-Ppython='+sys.executable,'--no-daemon','--no-parallel','--max-workers=1','--console=plain',*tasks]
    if env.get('CREATE263_GRADLE_OFFLINE') == 'true': command.insert(1,'--offline')
    proxy=env.get('CREATE263_HTTPS_PROXY')
    if proxy:
        from urllib.parse import urlparse
        parsed=urlparse(proxy);port=parsed.port or 8080
        command[1:1]=[f'-Dhttps.proxyHost={parsed.hostname}',f'-Dhttps.proxyPort={port}',f'-Dhttp.proxyHost={parsed.hostname}',f'-Dhttp.proxyPort={port}']
    print(f'Building {module}: {" ".join(tasks)} -> {output.relative_to(ROOT)}',flush=True)
    with gradle_writer(), output.open('w',encoding='utf-8') as log:
        result=subprocess.run(command,cwd=module_path,env=env,stdout=log,stderr=subprocess.STDOUT)
    tail=output.read_text(encoding='utf-8',errors='replace').splitlines()[-18:]
    print('\n'.join(tail),flush=True)
    if result.returncode:raise SystemExit(result.returncode)

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--only',choices=['create','aeronautics','railways','connected','diesel','liquid-fuel','ore-excavation','transmission-linkage','electro','storage','dragons-plus','enchantment-industry','additional-logistics','fluid-logistics','five-addons','all'],default='all')
    args=parser.parse_args()
    if args.only in ('create','all'):
        run_gradle('create-adapted-263',['buildRecoveredRelease','downstreamCompatibilityJar'])
    if args.only=='all':
        subprocess.run([sys.executable,'-X','utf8','-B',str(MODULES/'steam-n-rails-263/tools/stage-create-adapted-deps.py')],check=True)
    if args.only in ('aeronautics','all'):
        run_gradle('create-aeronautics-adapted-263',['jar'])
    if args.only in ('railways','all'):
        run_gradle('steam-n-rails-263',['jar','verifyPublishedNamespaces'])
    if args.only in ('connected','all'):
        run_gradle('create-connected-adapted-263',['jar','simulatedJar'])
    if args.only in ('diesel','all'):
        run_gradle('create-diesel-generators-adapted-263',['jar','auditDedicatedServerClasses'])
    if args.only in ('liquid-fuel','all'):
        run_gradle('create-liquid-fuel-adapted-263',['build'])
    if args.only in ('ore-excavation','all'):
        run_gradle('create-ore-excavation-adapted-263',['build'])
    if args.only in ('transmission-linkage','all'):
        run_gradle('transmission-linkage-adapted-263',['build'])
    # Highest-source addon ports share the same platform and are built in dependency order.
    addon_order = [
        ('dragons-plus','create-dragons-plus-adapted-263'),
        ('enchantment-industry','create-enchantment-industry-adapted-263'),
        ('electro','create-electro-energetics-adapted-263'),
        ('additional-logistics','create-additional-logistics-adapted-263'),
        ('storage','create-storage-adapted-263'),
        ('fluid-logistics','create-fluid-logistics-adapted-263'),
    ]
    for key,module in addon_order:
        if args.only in (key,'five-addons','all'): run_gradle(module,['build'])
    return 0
if __name__=='__main__':raise SystemExit(main())
