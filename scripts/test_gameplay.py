#!/usr/bin/env python3
"""Run opt-in gameplay fixtures in a new project world and a separate JVM restart."""
from pathlib import Path
import argparse, datetime, hashlib, json, os, shutil, socket, subprocess, time
from build import ROOT, config
import test_three_addons as base

GROUPS = {
    'storage': ('create263.storageGameplayProbe', 'storage-gameplay-probe-result.json'),
    'processing': ('create263.processingGameplayProbe', 'processing-gameplay-probe-result.json'),
    'power-logistics': ('create263.powerLogisticsProbe', 'power-logistics-probe-result.json'),
}

def write(path, value):
    base.write_json(path, value)

def copy_runtime_package(package, dest):
    package = package.resolve()
    allowed = (ROOT / 'runtime').resolve()
    if not package.is_relative_to(allowed):
        raise ValueError('Candidate runtime package must be inside project/runtime')
    manifest = json.loads((package / 'package-manifest.json').read_text(encoding='utf-8'))
    required = {row['name'] for row in json.loads((ROOT / 'manifests/runtime-modules.json').read_text(encoding='utf-8'))['modules']}
    if set(manifest) != required: raise ValueError('Candidate must include all eighteen runtime modules')
    dest.mkdir(parents=True, exist_ok=True)
    for name, row in manifest.items():
        source = package / 'mods' / name
        if source.name != name or base.digest(source) != row['runtime_sha256']:
            raise ValueError('Candidate runtime hash mismatch: ' + name)
        shutil.copy2(source, dest / name)

def stage(groups, package=None):
    old_pointer = ROOT / '.local/last-three-addon-runtime.json'
    previous_pointer = old_pointer.read_bytes() if old_pointer.exists() else None
    original_jars = base.runtime_jars
    if package is not None: base.runtime_jars = lambda dest: copy_runtime_package(package, dest)
    original_pack = base.test_pack
    try:
        base.test_pack = lambda server: None  # These fixtures use native recipes, without the liquid-fuel test map.
        run = base.stage_server()
    finally:
        base.test_pack = original_pack
        base.runtime_jars = original_jars
        if previous_pointer is not None: old_pointer.write_bytes(previous_pointer)
    server = base.owned(run / 'server')
    with (server / 'server.properties').open('a', encoding='utf-8') as stream:
        stream.write('pause-when-empty-seconds=-1\n')
    inputs = {'created_at': datetime.datetime.now().astimezone().isoformat(timespec='seconds'),
        'groups': groups, 'game_directory': server.relative_to(ROOT).as_posix(),
        'runtime_package_directory': str(package.relative_to(ROOT)) if package else 'artifacts/runtime',
        'original_worlds_accessed': False,
        'runtime_jars': {p.name: base.digest(p) for p in sorted((server / 'mods').glob('*.jar'))},
        'probe_sources': {p.name: base.digest(p) for p in sorted((ROOT / 'tests/runtime-probe/src/main/java/dev/create263/probe').glob('*GameplayProbe.java'))}}
    write(run / 'gameplay-inputs.json', inputs)
    write(ROOT / '.local/last-gameplay-runtime.json', {'root': str(run)})
    return run

def execute(run, phase, groups, timeout):
    server = base.owned(run / 'server')
    if not (server / base.MARKER).is_file():
        raise ValueError('Missing disposable runtime marker')
    inputs = json.loads((run / 'gameplay-inputs.json').read_text(encoding='utf-8'))
    actual = {p.name: base.digest(p) for p in sorted((server / 'mods').glob('*.jar'))}
    if actual != inputs['runtime_jars']:
        raise ValueError('Staged JAR content differs from the owned runtime inputs')
    for group in groups:
        (server / GROUPS[group][1]).unlink(missing_ok=True)
    if 'power-logistics' in groups: (server / 'power-logistics-shutdown.json').unlink(missing_ok=True)
    with socket.socket() as sock:
        sock.bind(('127.0.0.1', 25590))
    env, _ = config()
    java = Path(env['JAVA_HOME']) / 'bin' / ('java.exe' if os.name == 'nt' else 'java')
    command = [str(java), '-Xms512M', '-Xmx3G', '--enable-native-access=ALL-UNNAMED',
        '--enable-native-access=sable', '-Dcreate263.probeRoot=' + str(server)]
    command += ['-D' + GROUPS[g][0] + '=true' for g in groups]
    if phase == 'reload': command.append('-Dcreate263.gameplayReload=true')
    command += ['@server-launch.args', 'nogui']
    log_path = run / ('gameplay-' + phase + '-server.log')
    reports, seen = {}, {}
    ready, timed_out = False, False
    with log_path.open('wb') as log:
        child = subprocess.Popen(command, cwd=server, env=env, stdin=subprocess.PIPE,
            stdout=log, stderr=subprocess.STDOUT)
        print(f'Owned gameplay {phase} server PID {child.pid}; {log_path.relative_to(ROOT)}', flush=True)
        deadline = time.monotonic() + timeout
        try:
            while child.poll() is None and time.monotonic() < deadline:
                text = log_path.read_text(encoding='utf-8', errors='replace')
                if not ready and 'Done (' in text:
                    ready = True
                    child.stdin.write(b'time query gametime\n'); child.stdin.flush()
                for group in groups:
                    path = server / GROUPS[group][1]
                    if not path.exists(): continue
                    try: report = json.loads(path.read_text(encoding='utf-8'))
                    except json.JSONDecodeError: continue
                    reports[group] = report
                    state = report.get('status', 'running')
                    if state != seen.get(group):
                        print(f'{phase}/{group}: {state}, checks={report.get("actual_check_count", len(report.get("checks", [])))}', flush=True)
                        seen[group] = state
                # Wait for independent groups even when another group fails.
                if len(reports) == len(groups) and all(r.get('status') in ('passed', 'failed') for r in reports.values()):
                    break
                time.sleep(1)
            else:
                timed_out = child.poll() is None
        finally:
            if child.poll() is None:
                try: child.stdin.write(b'save-all flush\nstop\n'); child.stdin.flush()
                except BrokenPipeError: pass
                try: child.wait(timeout=45)
                except subprocess.TimeoutExpired: child.kill(); child.wait()
    text = log_path.read_text(encoding='utf-8', errors='replace')
    shutdown = None
    if 'power-logistics' in groups:
        path = server / 'power-logistics-shutdown.json'
        if path.exists(): shutdown = json.loads(path.read_text(encoding='utf-8'))
        write(run / ('gameplay-' + phase + '-shutdown.json'), shutdown)
    result = {'phase': phase, 'startup_ready': ready, 'exit_code': child.returncode,
        'timed_out': timed_out, 'electrical_shutdown': shutdown, 'vanilla_time_success': 'The game time is ' in text,
        'reports': reports, 'log': log_path.relative_to(ROOT).as_posix(),
        'runtime_fingerprints_unchanged': {p.name: base.digest(p) for p in sorted((server / 'mods').glob('*.jar'))} == inputs['runtime_jars']}
    result['passed'] = (ready and not timed_out and child.returncode == 0 and result['vanilla_time_success']
        and ('power-logistics' not in groups or shutdown is not None and shutdown.get('passed') is True)
        and result['runtime_fingerprints_unchanged'] and len(reports) == len(groups) and all(r.get('passed') is True and r.get('status') == 'passed'
        and r.get('phase') == phase + '-complete' for r in reports.values()))
    write(run / ('gameplay-' + phase + '-server.json'), result)
    print(json.dumps({'phase': phase, 'passed': result['passed'], 'exit_code': child.returncode,
        'groups': {k: {'passed': v.get('passed'), 'checks': len(v.get('checks', [])),
        'fatal_error': v.get('fatal_error')} for k, v in reports.items()}}, ensure_ascii=False), flush=True)
    if not result['passed']: print('\n'.join(text.splitlines()[-30:]), flush=True)
    return result

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--group', choices=list(GROUPS), action='append')
    parser.add_argument('--timeout', type=int, default=360)
    parser.add_argument('--runtime-package', type=Path)
    args = parser.parse_args()
    groups = list(dict.fromkeys(args.group or GROUPS))
    package = args.runtime_package.resolve() if args.runtime_package else None
    run = stage(groups, package)
    first = execute(run, 'create', groups, args.timeout)
    second = execute(run, 'reload', groups, args.timeout) if first['passed'] else None
    summary = {'created_at': datetime.datetime.now().astimezone().isoformat(timespec='seconds'),
        'runtime': run.relative_to(ROOT).as_posix(), 'groups': groups,
        'create_passed': first['passed'], 'reload_passed': second['passed'] if second else None,
        'passed': first['passed'] and second is not None and second['passed'],
        'original_worlds_accessed': False, 'all_requested_features_verified': False}
    write(run / 'gameplay-summary.json', summary)
    print(json.dumps(summary, ensure_ascii=False), flush=True)
    return 0 if summary['passed'] else 1

if __name__ == '__main__': raise SystemExit(main())
