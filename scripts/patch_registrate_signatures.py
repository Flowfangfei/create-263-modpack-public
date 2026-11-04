#!/usr/bin/env python3
"""Adapt compile-only Registrate generic signatures to the real two-parameter 26.3 renderer API."""
from pathlib import Path
import argparse,hashlib,json,re,struct,zipfile
PATTERN=re.compile(rb'(Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderer(?:Provider)?<)([+-]?T[^;<>]+;)(>;)')
def patch_class(data):
 out=bytearray(data[:10]);offset=10;count=struct.unpack_from('>H',data,8)[0];index=1;changes=0
 sizes={3:4,4:4,5:8,6:8,7:2,8:2,9:4,10:4,11:4,12:4,15:3,16:2,17:4,18:4,19:2,20:2}
 while index<count:
  tag=data[offset];offset+=1;out.append(tag)
  if tag==1:
   size=struct.unpack_from('>H',data,offset)[0];offset+=2;value=data[offset:offset+size];offset+=size
   value,n=PATTERN.subn(lambda m:m[1]+m[2]+b'*'+m[3],value);changes+=n
   out+=struct.pack('>H',len(value))+value
  else:
   size=sizes[tag];out+=data[offset:offset+size];offset+=size
   if tag in (5,6):index+=1
  index+=1
 out+=data[offset:];return bytes(out),changes

def main():
 p=argparse.ArgumentParser(description=__doc__);p.add_argument('source',type=Path);p.add_argument('output',type=Path);a=p.parse_args()
 source_hash=hashlib.sha256(a.source.read_bytes()).hexdigest();a.output.parent.mkdir(parents=True,exist_ok=True)
 changed={}
 with zipfile.ZipFile(a.source) as src,zipfile.ZipFile(a.output,'w',zipfile.ZIP_DEFLATED) as dst:
  for entry in src.infolist():
   data=src.read(entry)
   if entry.filename.endswith('.class'):
    data,n=patch_class(data)
    if n:changed[entry.filename]=n
   dst.writestr(entry,data)
 if not changed:raise SystemExit('No old renderer generic signatures found in pinned Registrate input')
 assert hashlib.sha256(a.source.read_bytes()).hexdigest()==source_hash
 report={'source_sha256':source_hash,'output_sha256':hashlib.sha256(a.output.read_bytes()).hexdigest(),'rewritten_generic_signatures':changed,'runtime_descriptors_changed':False,'classes_added':False}
 a.output.with_suffix('.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8');print(json.dumps(report))
if __name__=='__main__':main()
