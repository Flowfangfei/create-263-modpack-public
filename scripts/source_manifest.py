#!/usr/bin/env python3
"""Record SHA-256 of staged source blobs, using Git's canonical text representation."""
from pathlib import Path
import hashlib,json,subprocess
ROOT=Path(__file__).resolve().parents[1]
def main():
    records=[]
    for row in subprocess.check_output(['git','ls-files','--stage','-z'],cwd=ROOT).split(b'\0'):
        if not row:continue
        meta,path=row.split(b'\t',1);mode,sha,stage=meta.split();path=path.decode('utf-8')
        if path.startswith('modules/'):records.append((sha,path))
    result=subprocess.run(['git','cat-file','--batch'],input=b''.join(sha+b'\n' for sha,_ in records),cwd=ROOT,stdout=subprocess.PIPE,check=True).stdout
    cursor=0;modules={}
    for sha,path in records:
        end=result.index(b'\n',cursor);header=result[cursor:end].split();assert header[:2]==[sha,b'blob'];size=int(header[2]);cursor=end+1;data=result[cursor:cursor+size];cursor+=size+1
        module=path.split('/')[1];modules.setdefault(module,{})[path]={'sha256':hashlib.sha256(data).hexdigest(),'bytes':len(data)}
    out=ROOT/'manifests/source-sha256';out.mkdir(parents=True,exist_ok=True)
    provenance=json.loads((ROOT/'manifests/sources.json').read_text(encoding='utf-8'))
    for item in provenance['modules']:
        name=item['module'];p=out/(name+'.json');content={'schema':1,'representation':'Git index blobs (canonical line endings)','module':name,'files':modules[name]};p.write_text(json.dumps(content,indent=2)+'\n',encoding='utf-8',newline='\n')
        item['source_sha256_manifest']=p.relative_to(ROOT).as_posix();item['source_manifest_sha256']=hashlib.sha256(p.read_bytes()).hexdigest();item['file_count']=len(modules[name])
    (ROOT/'manifests/sources.json').write_text(json.dumps(provenance,indent=2)+'\n',encoding='utf-8',newline='\n')
    print('Recorded',sum(map(len,modules.values())),'source files in',len(modules),'modules')
if __name__=='__main__':main()
