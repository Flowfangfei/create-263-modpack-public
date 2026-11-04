#!/usr/bin/env python3
"""Export canonical Git blobs, then verify every module source SHA-256 in the ZIP."""
from pathlib import Path
import argparse,datetime,hashlib,json,subprocess,zipfile
from publication_policy import restricted_source
ROOT=Path(__file__).resolve().parents[1]

def git(*args):return subprocess.check_output(['git',*args],cwd=ROOT)

def verify(path):
    count=0
    with zipfile.ZipFile(path) as z:
        names=z.namelist()
        if any(n.startswith(('.local/','runtime/','artifacts/','docs/ai-collaboration/')) for n in names):raise ValueError('Private/generated files in source archive')
        forbidden=[n for n in names if restricted_source(n)]
        if forbidden:raise ValueError('Restricted assets or vendored dependency binaries in source archive: '+str(forbidden[:10]))
        provenance=json.loads(z.read('manifests/sources.json'))
        for module in provenance['modules']:
            raw=z.read(module['source_sha256_manifest'])
            if hashlib.sha256(raw).hexdigest()!=module['source_manifest_sha256']:raise ValueError('Source manifest digest mismatch: '+module['module'])
            for name,row in json.loads(raw)['files'].items():
                contents=z.read(name)
                if len(contents)!=row['bytes'] or hashlib.sha256(contents).hexdigest()!=row['sha256']:raise ValueError('Source content mismatch: '+name)
                count+=1
    print(f'Verified {count} canonical source files in {len(provenance["modules"])} modules',flush=True)
    return count

def main():
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--ref',default='HEAD');parser.add_argument('--verify',type=Path);args=parser.parse_args()
    if args.verify:verify(args.verify);return
    commit=git('rev-parse','--verify','--end-of-options',args.ref+'^{commit}').decode().strip()
    version=json.loads(git('show',commit+':modpack/manifest.json'))['version']
    out=ROOT/'artifacts/releases'/f'Create263-Pack-v{version}-sources.zip';out.parent.mkdir(parents=True,exist_ok=True)
    temporary=out.with_name(out.name+'.tmp')
    if temporary.exists():raise ValueError('Existing temporary export will not be overwritten: '+str(temporary))
    rows=[]
    for row in git('ls-tree','-r','-z','--full-tree',commit).split(b'\0'):
        if not row:continue
        meta,path=row.split(b'\t',1);mode,kind,sha=meta.split()
        if kind!=b'blob':raise ValueError('Unsupported non-blob source entry: '+path.decode())
        rows.append((mode,sha,path.decode('utf-8')))
    forbidden=[name for _,_,name in rows if restricted_source(name)]
    if forbidden:raise ValueError('Ref contains restricted assets or dependency JARs: '+str(forbidden[:10]))
    timestamp=datetime.datetime.fromtimestamp(int(git('show','-s','--format=%ct',commit)),datetime.timezone.utc).timetuple()[:6]
    process=subprocess.Popen(['git','cat-file','--batch'],cwd=ROOT,stdin=subprocess.PIPE,stdout=subprocess.PIPE)
    try:
        with zipfile.ZipFile(temporary,'w',zipfile.ZIP_DEFLATED,compresslevel=9) as archive:
            for mode,sha,name in rows:
                process.stdin.write(sha+b'\n');process.stdin.flush()
                header=process.stdout.readline().split()
                if header[:2]!=[sha,b'blob']:raise ValueError('Invalid Git object header for '+name)
                size=int(header[2]);data=process.stdout.read(size)
                if len(data)!=size or process.stdout.read(1)!=b'\n':raise ValueError('Truncated Git object '+name)
                info=zipfile.ZipInfo(name,timestamp);info.create_system=3;info.external_attr=int(mode,8)<<16;info.compress_type=zipfile.ZIP_DEFLATED
                archive.writestr(info,data,compresslevel=9)
        process.stdin.close()
        if process.wait(timeout=10):raise ValueError('Git object export failed')
        verify(temporary);temporary.replace(out)
    finally:
        if process.poll() is None:process.terminate();process.wait(timeout=10)
        temporary.unlink(missing_ok=True)
    print(out.relative_to(ROOT).as_posix(),hashlib.sha256(out.read_bytes()).hexdigest(),flush=True)
if __name__=='__main__':main()
