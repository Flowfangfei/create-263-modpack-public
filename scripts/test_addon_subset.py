#!/usr/bin/env python3
"""Run an explicitly incomplete diagnostic subset without replacing any release package."""
from pathlib import Path
import argparse,datetime,importlib,json,shutil
from build import ROOT
import test_three_addons as test

def main():
 p=argparse.ArgumentParser(description=__doc__);p.add_argument('--exclude',action='append',required=True);a=p.parse_args()
 rows=json.loads((ROOT/'manifests/runtime-modules.json').read_text(encoding='utf-8'))['modules']
 names={r['name'] for r in rows};excluded=set(a.exclude)
 if not excluded or not excluded<names:raise ValueError('Specify an explicit nonempty proper subset of module names to exclude')
 selected=[r for r in rows if r['name'] not in excluded]
 stamp=datetime.datetime.now().strftime('%Y%m%d-%H%M%S-%f')
 assembly=test.owned(ROOT/'runtime'/('subset-assembly-'+stamp))
 report=importlib.import_module('package').assemble(selected,assembly)
 def copy_jars(dest):
  dest.mkdir(parents=True,exist_ok=True)
  for name,row in report.items():
   src=assembly/'runtime/mods'/name
   if test.digest(src)!=row['runtime_sha256']:raise ValueError('Subset runtime hash mismatch '+name)
   shutil.copy2(src,dest/name)
 test.runtime_jars=copy_jars
 baseline_checks=[r for r in test.checks() if r['id']!='five-addons']
 test.checks=lambda:baseline_checks
 run=test.stage_server()
 test.write_json(ROOT/'.local/last-subset-runtime.json',{'root':str(run),'assembly':str(assembly)})
 passed=test.run_server(run,'create')
 test.write_json(run/'subset-diagnostic.json',{'passed_baseline_checks':passed,'selected_modules':[r['mod_id'] for r in selected],'excluded_runtime_names':sorted(excluded),'full_pack_acceptance':False,'new_addon_gameplay_verified':False,'assembly_directory':assembly.relative_to(ROOT).as_posix()})
 return 0 if passed else 1
if __name__=='__main__':raise SystemExit(main())
