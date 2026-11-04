#!/usr/bin/env python3
"""Restore pinned upstream resources locally; never republish restricted assets."""
from pathlib import Path
import argparse, hashlib, io, json, urllib.request, zipfile
ROOT=Path(__file__).resolve().parents[1]
def digest(data): return hashlib.sha256(data).hexdigest()
def safe(path):
    p=(ROOT/path).resolve()
    if not p.is_relative_to(ROOT): raise ValueError('Resource path escapes the repository')
    return p
def bootstrap(check=False):
    lock=json.loads((ROOT/'manifests/resources-lock.json').read_text(encoding='utf-8'))
    for resource in lock['resources']:
        targets=resource['targets']
        missing=[t for t in targets if not safe(t['path']).is_file() or digest(safe(t['path']).read_bytes())!=t['sha256']]
        if not missing: print(resource['id']+': verified'); continue
        if check: raise ValueError(resource['id']+': missing or modified local resources')
        cache=ROOT/'tooling/downloads'/resource['id'];cache.parent.mkdir(parents=True,exist_ok=True)
        if cache.is_file() and digest(cache.read_bytes())==resource['sha256']: data=cache.read_bytes()
        else:
            request=urllib.request.Request(resource['url'],headers={'User-Agent':'Create263Pack/0.1'})
            with urllib.request.urlopen(request,timeout=120) as response:data=response.read()
            if digest(data)!=resource['sha256']:raise ValueError('Download hash mismatch: '+resource['id'])
            cache.write_bytes(data)
        archive=zipfile.ZipFile(io.BytesIO(data)) if resource['type']=='zip' else None
        for target in missing:
            content=archive.read(target['entry']) if archive else data
            if digest(content)!=target['sha256']:raise ValueError('Resource entry hash mismatch')
            output=safe(target['path'])
            if output.exists(): raise FileExistsError('Modified resource will not be overwritten: '+target['path'])
            output.parent.mkdir(parents=True,exist_ok=True);output.write_bytes(content)
        if archive:archive.close()
        print(resource['id']+': restored '+str(len(missing)))
if __name__=='__main__':
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--check',action='store_true');bootstrap(parser.parse_args().check)
