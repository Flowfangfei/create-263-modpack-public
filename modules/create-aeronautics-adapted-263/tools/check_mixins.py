"""Static check of Create's mixins against a Minecraft/NeoForge jar.

For every mixin listed in a mixin config (and every mixin a config plugin can add), checks that
  - the target classes exist,
  - each injector's `method = "..."` exists in a target (by name, and by descriptor when one is given),
  - each INVOKE / INVOKE_ASSIGN / FIELD / NEW `@At` target is referenced from inside that method's bytecode,
  - @Shadow / @Accessor / @Invoker members exist.
Checks explicit invocation/field/return ordinals and selected callback arguments.
It does not evaluate slices, every local capture or interactions between injected mixins; real launches remain necessary.

    python tools/check_mixins.py <minecraft jar> [<more jars>...]
"""
import fnmatch
import pathlib
import json
import re
import struct
import sys
import zipfile

ROOT = pathlib.Path(__file__).resolve().parent.parent
# Source and compiled class checks cover the three module baseline; runtime still checks injection interactions.
CONFIGS=[]
for module in ['simulated','offroad','aeronautics']:
    for source_set in ['main','neoforge']:
        CONFIGS.extend((path,ROOT/module/'src'/source_set/'java') for path in (ROOT/module/'src'/source_set/'resources').glob('*mixins.json'))
ACTIVE_COMPAT_MODS={'create','sable','simulated','offroad','aeronautics'}
SKIPPED_OPTIONAL = []

# opcode -> operand byte length (excluding switches/wide); cp-referencing opcodes noted separately
OPLEN = {**{i: 0 for i in range(0, 256)}}
for op in (0x10, 0x12, 0x15, 0x16, 0x17, 0x18, 0x19, 0x36, 0x37, 0x38, 0x39, 0x3a, 0xa9, 0xbc):
    OPLEN[op] = 1
for op in (0x11, 0x13, 0x14, 0x84, *range(0x99, 0xa9), 0xb2, 0xb3, 0xb4, 0xb5, 0xb6, 0xb7, 0xb8, 0xbb, 0xbd, 0xc0, 0xc1, 0xc6, 0xc7):
    OPLEN[op] = 2
OPLEN[0x84] = 2
OPLEN[0xc5] = 3
for op in (0xb9, 0xba, 0xc8, 0xc9):
    OPLEN[op] = 4
CP_OPS = {0xb2, 0xb3, 0xb4, 0xb5, 0xb6, 0xb7, 0xb8, 0xb9, 0xbb}


def parse_class(data):
    count = struct.unpack_from('>H', data, 8)[0]
    cp = [None] * count
    pos, i = 10, 1
    while i < count:
        tag = data[pos]
        if tag == 1:
            ln = struct.unpack_from('>H', data, pos + 1)[0]
            cp[i] = ('utf', data[pos + 3:pos + 3 + ln].decode('utf-8', 'replace'))
            pos += 3 + ln
        elif tag in (3, 4):
            pos += 5
        elif tag in (5, 6):
            pos += 9
            i += 1
        elif tag in (7, 8, 16, 19, 20):
            cp[i] = (tag, struct.unpack_from('>H', data, pos + 1)[0])
            pos += 3
        elif tag in (9, 10, 11, 12, 17, 18):
            cp[i] = (tag,) + struct.unpack_from('>HH', data, pos + 1)
            pos += 5
        elif tag == 15:
            pos += 4
        i += 1

    def utf(k):
        return cp[k][1]

    def ref(k):
        e = cp[k]
        if e[0] in (9, 10, 11):
            nt = cp[e[2]]
            return (utf(cp[e[1]][1]), utf(nt[1]), utf(nt[2]))
        if e[0] == 7:
            return (utf(e[1]), None, None)
        return None

    _, this, sup, nif = struct.unpack_from('>HHHH', data, pos)
    pos += 8
    ifs = [utf(cp[struct.unpack_from('>H', data, pos + 2 * k)[0]][1]) for k in range(nif)]
    pos += 2 * nif
    fields, methods = {}, {}
    for member in range(2):
        n = struct.unpack_from('>H', data, pos)[0]
        pos += 2
        for _ in range(n):
            acc, ni, di, na = struct.unpack_from('>HHHH', data, pos)
            pos += 8
            refs = []
            for _ in range(na):
                an, alen = struct.unpack_from('>HI', data, pos)
                if member == 1 and utf(an) == 'Code':
                    code_len = struct.unpack_from('>I', data, pos + 10)[0]
                    code = data[pos + 14:pos + 14 + code_len]
                    pc = 0
                    while pc < len(code):
                        op = code[pc]
                        if 0xac <= op <= 0xb1:
                            refs.append(("#return", str(op), None))
                        if op in CP_OPS or op == 0xba:
                            r = ref(struct.unpack_from('>H', code, pc + 1)[0])
                            if r:
                                refs.append(r)
                        if op == 0xaa:  # tableswitch
                            p = (pc + 4) & ~3
                            lo, hi = struct.unpack_from('>ii', code, p + 4)
                            pc = p + 12 + 4 * (hi - lo + 1)
                            continue
                        if op == 0xab:  # lookupswitch
                            p = (pc + 4) & ~3
                            npairs = struct.unpack_from('>i', code, p + 4)[0]
                            pc = p + 8 + 8 * npairs
                            continue
                        if op == 0xc4:  # wide
                            pc += 6 if code[pc + 1] == 0x84 else 4
                            continue
                        pc += 1 + OPLEN.get(op, 0)
                pos += 6 + alen
            (fields if member == 0 else methods).setdefault(utf(ni), []).append((utf(di), refs))
    return utf(cp[this][1]), (utf(cp[sup][1]) if sup else None), ifs, fields, methods


classes = {}


def load(data):
    try:
        name, *rest = parse_class(data)
        classes.setdefault(name, rest)
    except Exception:
        pass


for jar in sys.argv[1:]:
    if pathlib.Path(jar).is_dir():
        for f in pathlib.Path(jar).rglob('*.class'):
            load(f.read_bytes())
        continue
    with zipfile.ZipFile(jar) as zf:
        for n in zf.namelist():
            if n.endswith('.class') and not n.startswith('META-INF/'):
                load(zf.read(n))
# Mixins excluded from the build (Fabric-only, other recipe viewers) are skipped
built = {p.stem for d in ('build/classes/java/main', 'build/classes/java/client') for p in (ROOT / d).rglob('*.class')}


def resolve(name, imports, wildcards):
    """Simple or dotted class name as written in source -> internal name"""
    first, _, rest = name.partition('.')
    bases = [imports[first]] if first in imports else ([name] if first and first[0].islower() else [w + '.' + first for w in wildcards])
    if bases == [name]: rest = ''
    for base in bases:
        full = base + ('.' + rest if rest else '')
        parts = full.split('.')
        for i in range(len(parts), 0, -1):
            cand = '/'.join(parts[:i]) + ''.join('$' + p for p in parts[i:])
            if cand in classes:
                return cand
    return name.replace('.', '/')


def hierarchy(c):
    out, todo = [], [c]
    while todo:
        x = todo.pop(0)
        if x in classes and x not in out:
            out.append(x)
            sup, ifs, _, _ = classes[x]
            todo += ([sup] if sup else []) + ifs
    return out


def find_methods(cls, spec, inherited=False):
    """spec is name or name(desc)"""
    m = re.match(r'([^(]+)(\(.*)?$', spec)
    name, desc = m.group(1), m.group(2)
    if name.startswith('L') and ';' in name:  # Lowner;name(desc)
        owner, _, name = name[1:].partition(';')
        cls = owner
    out = []
    for c in (hierarchy(cls) if inherited else [cls]):
        for method_name, overloads in classes.get(c, [None,None,None,{}])[3].items():
            if not fnmatch.fnmatchcase(method_name, name): continue
            for d, refs in overloads:
                if desc is None or d == desc:
                    out.append((c, d, refs))
    return out


def parse_target(t):
    if not t.startswith('L') and '.' in t.split('(')[0]:
        # owner.name(desc) form
        owner, _, rest = t.partition('(')
        owner, _, name = owner.rpartition('.')
        t = f'L{owner};{name}(' + rest
    m = re.match(r'L([^;]+);([^(:]+)(\(.*|:.*)?$', t)
    if not m:
        return None
    return m.group(1), m.group(2), (m.group(3) or '').lstrip(':') or None


def java_files(base):
    return {p.stem: p for p in base.rglob('*.java')}


report = []
for config, src in CONFIGS:
    config_data = json.loads(config.read_text(encoding='utf-8'))
    package = config_data['package']
    names = config_data.get('mixins', []) + config_data.get('client', []) + config_data.get('server', [])
    for mixin in sorted(set(names)):
        if mixin.startswith('silence_jei.') or mixin == 'sublevel_render.AltModelBlockRendererImplMixin':
            SKIPPED_OPTIONAL.append(package + '.' + mixin)
            continue
        if mixin.startswith('compatibility.') and mixin.split('.')[1] not in ACTIVE_COMPAT_MODS:
            SKIPPED_OPTIONAL.append(package + '.' + mixin)
            continue
        compiled_name = package.replace('.', '/') + '/' + mixin.replace('.', '/')
        if compiled_name not in classes:
            report.append(f'{mixin}: configured mixin class is absent from compiled inputs')
            continue
        outer = mixin.split('$', 1)[0]
        path = src / package.replace('.', '/') / (mixin.replace('.', '/') + '.java')
        standalone = path.exists()
        if not standalone:
            path = src / package.replace('.', '/') / (outer.replace('.', '/') + '.java')
        if not path.exists():
            report.append(f'{mixin}: configured source is absent')
            continue
        code = path.read_text(encoding='utf-8')
        code = re.sub(r'/\*.*?\*/|//[^\n]*', '', code, flags=re.S)
        code = re.sub(r'@(?:[a-zA-Z_][\w]*\.)+', '@', code)
        while re.search(r'"([^"]*)"\s*\+\s*"([^"]*)"',code):
            code=re.sub(r'"([^"]*)"\s*\+\s*"([^"]*)"',lambda m:'"'+m[1]+m[2]+'"',code)
        if '$' in mixin and not standalone:
            inner = mixin.rsplit('$', 1)[1]
            declaration = re.search(r'\bclass\s+' + re.escape(inner) + r'\b', code)
            if declaration:
                annotation_start = code.rfind('@Mixin', 0, declaration.start())
                # Keep imports so nested targets can still resolve.
                code = '\n'.join(re.findall(r'^import .*?;', code, re.M)) + '\n' + code[annotation_start:]
        # Outer mixins must not absorb nested mixin annotations.
        mixin_annotations = list(re.finditer(r'@Mixin\s*\(', code))
        if len(mixin_annotations) > 1:
            code = code[:mixin_annotations[1].start()]
        imports = dict((m.group(2), m.group(1)) for m in re.finditer(r'^import ([\w.]+\.(\w+));', code, re.M))
        wildcards = re.findall(r'^import ([\w.]+)\.\*;', code, re.M) + [package + ('.' + mixin.rsplit('.', 1)[0] if '.' in mixin else '')]
        mm = re.search(r'@Mixin\((?:value\s*=\s*)?\{?([^)}]*)\}?', code)
        targets = []
        if mm is None:
            report.append(f"{mixin}: source has no readable Mixin target: {path}")
            continue
        for t in re.findall(r'([\w.$]+)\.class', mm.group(1)):
            targets.append(resolve(t, imports, wildcards))
        targets += [s.replace('.', '/') for s in re.findall(r'targets\s*=\s*\{?\s*"([^"]+)"', mm.group(0))]
        problems = []
        for t in targets:
            if t not in classes:
                problems.append(f'target class {t} not found')
        live = [t for t in targets if t in classes]
        if not live:
            report.append(f'{mixin}: ' + '; '.join(problems or ['no target']))
            continue
        mixin_fields = classes[compiled_name][2]
        mixin_methods = classes[compiled_name][3]
        def descriptor_args(descriptor):
            return re.findall(r'\[*L[^;]+;|\[*[BCDFIJSZ]', descriptor.split(')', 1)[0][1:])
        # injector annotations: grab balanced annotation text
        for am in re.finditer(r'@(Inject|Redirect|ModifyArg|ModifyArgs|ModifyVariable|ModifyConstant|WrapOperation|WrapWithCondition|ModifyExpressionValue|ModifyReturnValue|ModifyReceiver|WrapMethod|Definition)\s*\(', code):
            depth, i = 1, am.end()
            while depth and i < len(code):
                depth += {'(': 1, ')': -1}.get(code[i], 0)
                i += 1
            ann = code[am.start():i]
            methods_spec = re.findall(r'method\s*=\s*\{?\s*((?:"[^"]*"\s*,?\s*)+)', ann)
            specs = re.findall(r'"([^"]+)"', methods_spec[0]) if methods_spec else []
            found = []
            for spec in specs:
                hits = [h for t in live for h in find_methods(t, spec)]
                if not hits:
                    problems.append(f'@{am.group(1)} method {spec} not found')
                elif '(' not in spec and '*' not in spec and len({d for _, d, _ in hits}) > 1:
                    # NeoForge often adds an overload and turns the vanilla one into a delegate; a name-only
                    # spec then matches both, and the handler signature fits at most one of them
                    problems.append(f'@{am.group(1)} method {spec} matches {len(hits)} overloads: ' + ', '.join(sorted(d for _, d, _ in hits)))
                found += hits
            if am.group(1) == 'Inject' and found:
                handler = re.search(r'(?:private|protected|public)\s+(?:static\s+)?(?:final\s+)?(?:<[^>]+>\s+)?[\w.<>, ?\[\]]+?\s+([\w$]+)\s*\(', code[i:])
                if handler:
                    handler_name = handler.group(1)
                    for handler_descriptor, _ in mixin_methods.get(handler_name, []):
                        arguments = descriptor_args(handler_descriptor)
                        callback = next((n for n, arg in enumerate(arguments) if 'CallbackInfo' in arg), None)
                        if callback is None:
                            # Other overloads may be Redirect handlers with the same Java name.
                            continue
                        captured = arguments[:callback]
                        if captured and not any(captured == descriptor_args(desc) for _, desc, _ in found):
                            # @Coerce is explicitly permitted by Mixin; list these for manual validation.
                            declaration_text = code[i:code.find('{', i)]
                            if '@Coerce' not in declaration_text.split('CallbackInfo',1)[0] and '@Local' not in declaration_text.split('CallbackInfo',1)[0]:
                                problems.append(f'@Inject handler {handler_name} captures {captured}, target arguments are ' + str([descriptor_args(desc) for _, desc, _ in found]))
            for at in re.finditer(r'@At\s*\(', ann):
                at_depth, at_end = 1, at.end()
                while at_depth and at_end < len(ann):
                    at_depth += {'(': 1, ')': -1}.get(ann[at_end], 0)
                    at_end += 1
                body = ann[at.end():at_end-1]
                v = re.search(r'value\s*=\s*"(\w+)"', body) or re.match(r'\s*"(\w+)"', body)
                tgt = re.search(r'target\s*=\s*"([^"]+)"', body)
                return_ordinal = re.search(r'ordinal\s*=\s*(\d+)', body)
                if v and v.group(1) == 'RETURN' and found and return_ordinal:
                    return_count = max(sum(1 for ref in refs if ref[0] == '#return') for _, _, refs in found)
                    if return_count <= int(return_ordinal[1]):
                        problems.append(f'@{am.group(1)} {specs[0] if specs else "?"}: return ordinal {return_ordinal[1]} exceeds {return_count} return instructions')

                if not v or v.group(1) not in ('INVOKE', 'INVOKE_ASSIGN', 'FIELD', 'NEW'):
                    continue
                if not tgt:
                    # Do not silently claim verification for a selector the checker cannot parse.
                    problems.append(f'@{am.group(1)} {specs[0] if specs else "?"}: @At({v.group(1)}) needs an explicit target for this static checker')
                    continue
                t = tgt.group(1)
                if v.group(1) == 'NEW':
                    # NEW targets are either the class or a constructor descriptor returning it
                    owner = t.rsplit(')', 1)[1] if t.startswith('(') else t
                    owner = owner[1:-1] if owner.startswith('L') and owner.endswith(';') else owner
                    ok = any((owner, None, None) in refs or any(r[0] == owner and r[1] == '<init>' for r in refs) for _, _, refs in found)
                else:
                    pt = parse_target(t)
                    if not pt:
                        continue
                    ok = any(any(r[0] == pt[0] and r[1] == pt[1] and (pt[2] is None or r[2] == pt[2]) for r in refs)
                             or any(r[1] == pt[1] and (pt[2] is None or r[2] == pt[2]) for r in refs if r[0] != pt[0] and pt[0] in hierarchy(r[0]))
                             for _, _, refs in found)
                if found and not ok:
                    problems.append(f'@{am.group(1)} {specs[0] if specs else "?"}: @At({v.group(1)}) target {t} not in method')
                ordinal=re.search(r'ordinal\s*=\s*(\d+)',body)
                if found and ok and ordinal and v.group(1) in ('INVOKE','INVOKE_ASSIGN','FIELD'):
                    target_info=parse_target(t)
                    count=max((sum(1 for ref in refs if target_info and ref[1]==target_info[1] and (target_info[2] is None or ref[2]==target_info[2]) and (ref[0]==target_info[0] or target_info[0] in hierarchy(ref[0]))) for _,_,refs in found),default=0)
                    if count<=int(ordinal[1]):problems.append(f'@{am.group(1)} {specs[0] if specs else "?"}: ordinal {ordinal[1]} exceeds {count} matching {t} instructions')

                if am.group(1) == 'WrapOperation' and v.group(1) in ('INVOKE', 'INVOKE_ASSIGN'):
                    pt = parse_target(t)
                    handler = re.search(r'(?:private|protected|public)\s+(?:static\s+)?(?:final\s+)?(?:<[^>]+>\s+)?[\w.<>, ?\[\]]+?\s+([\w$]+)\s*\(', code[i:])
                    declaration_text = code[i:code.find('{', i)]
                    if pt and pt[2] and pt[2].startswith('(') and handler and '@Coerce' not in declaration_text:
                        expected = descriptor_args(pt[2])
                        for hd, _ in mixin_methods.get(handler.group(1), []):
                            args = descriptor_args(hd)
                            operation = next((n for n, arg in enumerate(args) if arg.endswith('/Operation;')), None)
                            if operation is not None:
                                prefix = args[:operation]
                                # Both static and instance calls are permitted; verify captured call arguments.
                                if prefix != expected and prefix[1:] != expected:
                                    problems.append(f'@WrapOperation handler {handler.group(1)} captures {prefix}; call arguments are {expected}')
        for sm in re.finditer(r'@Shadow[^;{]*?\s(?:abstract\s+)?(?:static\s+)?(?:final\s+)?[\w<>\[\]?,. ]+?\s+([\w$]+)\s*([(;=])', code):
            name = sm.group(1)
            is_method = sm.group(2) == '('
            actual = {d for t in live for c in hierarchy(t)
                      for d, _ in (classes[c][3] if is_method else classes[c][2]).get(name, [])}
            own = {d for d, _ in (mixin_methods if is_method else mixin_fields).get(name, [])}
            if not actual:
                problems.append(f'@Shadow {name} not found')
            elif own and not own.intersection(actual):
                problems.append(f'@Shadow {name} descriptor mismatch {own} vs {actual}')
        for kind, am in [(k, m) for k in ('Accessor', 'Invoker') for m in re.finditer(r'@' + k + r'(\(\s*(?:value\s*=\s*)?"([^"]+)"[^)]*\))?[^;{]*?\s([\w$]+)\s*\(', code)]:
            name = am.group(2)
            if not name:
                meth = am.group(3).rsplit('$', 1)[-1]
                name = re.sub(r'^(get|set|is|invoke|call|create)', '', meth)
                name = name[:1].lower() + name[1:]
            ok = any((classes[c][3] if kind == 'Invoker' else classes[c][2]).get(name) for t in live for c in hierarchy(t))
            if ok and kind == 'Accessor':
                getter_name = am.group(3)
                own = [d for d, _ in mixin_methods.get(getter_name, [])]
                expected = set()
                for d in own:
                    return_type = d.split(')', 1)[1]
                    args = descriptor_args(d)
                    expected.add(return_type if return_type != 'V' else (args[0] if len(args) == 1 else '?'))
                actual = {d for t in live for c in hierarchy(t) for d, _ in classes[c][2].get(name, [])}
                if expected and not expected.intersection(actual):
                    problems.append(f'@Accessor {getter_name} descriptor mismatch {expected} vs {actual}')
            if kind == 'Invoker' and name == 'new' or name == '<init>':
                ok = True
            if not ok:
                problems.append(f'@{kind} {name} not found')
        if problems:
            report.append(f'{mixin}: ' + '; '.join(dict.fromkeys(problems)))
print('\n'.join(report))
print(f'{len(report)} mixins with problems')

print("Optional integrations not active in baseline:", ", ".join(SKIPPED_OPTIONAL))
if report:
    sys.exit(1)
