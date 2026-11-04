#!/usr/bin/env python3
"""Stage all runtime JARs without replacing release artifacts; repack changed build inputs only."""
from pathlib import Path
import datetime, importlib, json, shutil, zipfile
from build import ROOT
import test_three_addons as base
packaging = importlib.import_module('package')

def main():
    baseline = json.loads((ROOT / 'artifacts/runtime/package-manifest.json').read_text(encoding='utf-8'))
    rows = json.loads((ROOT / 'manifests/runtime-modules.json').read_text(encoding='utf-8'))['modules']
    stamp = datetime.datetime.now().strftime('%Y%m%d-%H%M%S-%f')
    target = base.owned(ROOT / 'runtime' / ('gameplay-assembly-' + stamp))
    target.mkdir(exist_ok=False)
    base.runtime_jars(target / 'runtime/mods')
    changed = [row for row in rows if base.digest(packaging.one(row['module'], row['artifact'])) != baseline[row['name']]['input_sha256']]
    if not changed: raise ValueError('No changed source-built runtime modules; use baseline package')
    updated = packaging.assemble(changed, target)
    combined = dict(baseline); combined.update(updated)
    base.write_json(target / 'runtime/package-manifest.json', combined)
    differences = {}
    for row in changed:
        name = row['name']
        with zipfile.ZipFile(ROOT / 'artifacts/runtime/mods' / name) as old, zipfile.ZipFile(target / 'runtime/mods' / name) as new:
            left = {n: old.read(n) for n in old.namelist() if not n.endswith('/')}
            right = {n: new.read(n) for n in new.namelist() if not n.endswith('/')}
            differences[name] = [n for n in sorted(set(left) | set(right)) if left.get(n) != right.get(n)]
    report = {'created_at': datetime.datetime.now().astimezone().isoformat(timespec='seconds'),
        'runtime_package': (target / 'runtime').relative_to(ROOT).as_posix(),
        'changed_jars': [row['name'] for row in changed], 'changed_entries': differences,
        'baseline_release_modified': False, 'distribution_payload_complete': False}
    base.write_json(target / 'candidate-diff.json', report)
    base.write_json(ROOT / '.local/last-gameplay-candidate.json', report)
    print(json.dumps(report, ensure_ascii=False, indent=2), flush=True)
    return 0

if __name__ == '__main__': raise SystemExit(main())
