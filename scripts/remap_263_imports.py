#!/usr/bin/env python3
"""Map removed import names only when a unique class exists in pinned real Create APIs."""
from pathlib import Path
import argparse,json,re,zipfile,collections
ROOT=Path(__file__).resolve().parents[1]
def main():
 p=argparse.ArgumentParser(description=__doc__)
 p.add_argument('module')
 p.add_argument('--source',action='append',default=[])
 a=p.parse_args();module=(ROOT/'modules'/a.module).resolve()
 if not module.is_relative_to((ROOT/'modules').resolve()):raise ValueError('Module escapes workspace')
 jars=ROOT/'modules/steam-n-rails-263/tooling/create-adapted/libs'
 classes=set()
 for jar in jars.glob('*.jar'):
  with zipfile.ZipFile(jar) as z:
   classes.update(n[:-6].replace('/','.') for n in z.namelist() if n.endswith('.class') and '$' not in n)
 by_name=collections.defaultdict(list)
 for name in classes:by_name[name.rsplit('.',1)[-1]].append(name)
 source_dirs=[module/s for s in (a.source or ['src/main/java'])]
 changed=[];maps={};ambiguous={}
 for source in source_dirs:
  if not source.resolve().is_relative_to(module):raise ValueError('Source escapes module')
  for f in source.rglob('*.java'):
   s=f.read_text(encoding='utf-8');out=s
   for name in set(re.findall(r'(?:import\s+(?:static\s+)?|(?<![\w.]))(net\.createmod\.[A-Za-z_][\w.]*)',s)):
    parts=name.split('.')
    # An import may name a nested class or static member. Resolve the longest class prefix.
    for i in range(len(parts),2,-1):
     old='.'.join(parts[:i]);short=parts[i-1]
     if not short[:1].isupper():continue
     if old in classes:break
     candidates=[n for n in by_name.get(short,[]) if n.startswith('net.createmod.')]
     if len(candidates)==1:
      new=candidates[0];out=re.sub(r'(?<![\w.])'+re.escape(old)+r'(?![\w])',new,out);maps[old]=new;break
     if len(candidates)>1:ambiguous[old]=candidates
   substitutions={
    'net.neoforged.neoforge.items.':'com.simibubi.create.compat.neoforge263.items.',
    'net.neoforged.neoforge.fluids.capability.':'com.simibubi.create.compat.neoforge263.fluids.capability.',
    'net.neoforged.neoforge.fluids.FluidUtil':'com.simibubi.create.compat.neoforge263.fluids.FluidUtil',
    'net.minecraft.Util':'net.minecraft.util.Util',
   }
   for old,new in substitutions.items():out=out.replace(old,new)
   out=re.sub(r'\bResourceLocation\b','Identifier',out)
   if out!=s:f.write_text(out,encoding='utf-8');changed.append(f.relative_to(module).as_posix())
 print(json.dumps({'module':a.module,'changed_files':len(changed),'class_maps':maps,'ambiguous':ambiguous},ensure_ascii=False,indent=2))
if __name__=='__main__':main()
