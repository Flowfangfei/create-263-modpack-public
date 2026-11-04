#!/usr/bin/env python3
"""Compare an installer-produced HMCL ZIP against the source-built runtime, entry by entry."""
from pathlib import Path
import argparse,hashlib,io,json,zipfile
ROOT=Path(__file__).resolve().parents[1]
def entries(z):return {i.filename:hashlib.sha256(z.read(i)).hexdigest() for i in z.infolist() if not i.is_dir()}
def main():
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('pack',type=Path);args=parser.parse_args();report={}
    runtime=ROOT/'artifacts/runtime/mods';jars=sorted(runtime.glob('*.jar'))
    expected_modules=json.loads((ROOT/'manifests/runtime-modules.json').read_text(encoding='utf-8'))['modules']
    if {p.name for p in jars}!={m['name'] for m in expected_modules}:raise ValueError('Runtime mod set differs from manifest')
    with zipfile.ZipFile(args.pack) as pack:
        manifest=json.loads(pack.read('manifest.json'))
        assert manifest['minecraft']=={'version':'26.3','modLoaders':[{'id':'neoforge-26.3.0.22-beta','primary':True}]}
        expected={'overrides/mods/'+jar.name for jar in jars}
        assert {n for n in pack.namelist() if n.startswith('overrides/mods/') and n.endswith('.jar')}==expected
        for jar in jars:
            with zipfile.ZipFile(jar) as tested,zipfile.ZipFile(io.BytesIO(pack.read('overrides/mods/'+jar.name))) as installed:
                a,b=entries(tested),entries(installed);changed=sorted(k for k in a.keys()|b.keys() if a.get(k)!=b.get(k))
                if changed:raise ValueError(jar.name+': changed entries '+str(changed[:10]))
                report[jar.name]={'entries':len(b),'content_matches':True}
        lock=json.loads((ROOT/'manifests/resources-lock.json').read_text());expected_asset=next(x for x in lock['resources'] if x['id']=='official-create-assets')['sha256']
        assert hashlib.sha256(pack.read('overrides/config/create-adapted/external-assets/create-1.21.1-6.0.10.jar')).hexdigest()==expected_asset
        assert not any(n.startswith('overrides/saves/') or '/world/' in n for n in pack.namelist())
    out=ROOT/'artifacts/runtime/installer-entry-verification.json';out.write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8')
    print(f'Verified all {len(jars)} jar CONTENTS, official asset hash, pinned platform, exact mod set and absence of worlds')
if __name__=='__main__':main()
