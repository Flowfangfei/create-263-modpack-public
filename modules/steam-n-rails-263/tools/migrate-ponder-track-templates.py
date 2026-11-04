#!/usr/bin/env python3
"""Normalize this module's fixed Ponder templates; never accepts a world path.

Uses the current Create BezierConnection.write()/VecHelper.writeNBTCompound()
shape. Original compressed templates are retained under the workspace .local/.
Run with --apply once, --check to audit, or --apply --from-backup to reproduce.
No Gradle or game process is started.
"""
from __future__ import annotations

import argparse
import copy
import gzip
import hashlib
import json
import math
import struct
import sys
from pathlib import Path

MODULE = Path(__file__).resolve().parents[1]
ROOT = MODULE.parents[1]
TEMPLATES = MODULE / 'common/src/main/resources/assets/railways/ponder'
BACKUP = ROOT / '.local/railways-ponder-track-originals'
MANIFEST = MODULE / 'tools/ponder-track-migration.json'
PLAN = ROOT / '.local/railways-ponder-track-migration-plan.json'
# Reuse the type-preserving NBT AST; its main() is guarded on import.
sys.path.insert(0, str(ROOT / 'scripts'))
from migrate_storage_ponder_templates import Tag, compound, decode, integer, nstr, payload, sequence, text

VECTOR_FIELDS = ('Starts', 'Axes', 'Normals')

def sha(raw):
    return hashlib.sha256(raw).hexdigest()

def pair(tag):
    values = sequence(tag)
    if len(values) != 2:
        raise ValueError('A BezierConnection couple must contain exactly two entries')
    return values

def vector(tag):
    if tag.kind != 9 or tag.value[0] != 6 or len(tag.value[1]) != 3:
        raise ValueError('Expected three TAG_Double vector components')
    values = [v.value for v in tag.value[1]]
    if any(not math.isfinite(v) for v in values):
        raise ValueError('Non-finite curve geometry')
    return values

def position(tag):
    if tag.kind == 11:
        if tag.value[0] != 3:
            raise ValueError('Expected three TAG_Int_Array coordinates')
        return list(struct.unpack('>iii', tag.value[1]))
    d = compound(tag)
    if set(d) != {'Pos'} or set(compound(d['Pos'])) != {'X', 'Y', 'Z'}:
        raise ValueError('Unexpected current Positions entry')
    coords = compound(d['Pos'])
    if any(coords[k].kind != 3 for k in ('X', 'Y', 'Z')):
        raise ValueError('Position coordinates must remain TAG_Int')
    return [coords[k].value for k in ('X', 'Y', 'Z')]

def geometry(connection, block_position):
    d = compound(connection)
    positions = [position(t) for t in pair(d['Positions'])]
    vectors = {}
    for field in VECTOR_FIELDS:
        vectors[field] = []
        for entry in pair(d[field]):
            if entry.kind == 10:
                if set(compound(entry)) != {'V'}:
                    raise ValueError('Unexpected vector wrapper fields')
                entry = compound(entry)['V']
            vectors[field].append(vector(entry))
    absolute = [[a + b for a, b in zip(block_position, p)] for p in positions]
    if absolute[0] != block_position:
        raise ValueError('First curve BE position must be the owning track')
    return {
        'relative_positions': positions,
        'absolute_positions': absolute,
        'relative_starts': vectors['Starts'],
        'absolute_starts': [[a + b for a, b in zip(block_position, v)] for v in vectors['Starts']],
        'axes': vectors['Axes'],
        'normals': vectors['Normals'],
        'primary': integer(d['Primary']),
        'girder': integer(d['Girder']),
        'material': text(d['Material']),
        'untouched_fields_sha256': sha(payload(Tag(10, {
            k: v for k, v in d.items() if k not in ('Positions', *VECTOR_FIELDS)
        }))),
    }

def normalize(connection):
    d = compound(connection)
    changes = []
    positions = d['Positions']
    if positions.kind == 9 and positions.value[0] == 11:
        entries = []
        for entry in pair(positions):
            xyz = position(entry)
            entries.append(Tag(10, {'Pos': Tag(10, {
                axis: Tag(3, value) for axis, value in zip(('X', 'Y', 'Z'), xyz)
            })}))
        d['Positions'] = Tag(9, (10, entries))
        changes.append('Positions')
    elif positions.kind != 9 or positions.value[0] != 10:
        raise ValueError('Unknown Positions representation; refusing lossy migration')
    for field in VECTOR_FIELDS:
        tag = d[field]
        if tag.kind == 9 and tag.value[0] == 9:
            for entry in pair(tag):
                vector(entry)
            d[field] = Tag(9, (10, [Tag(10, {'V': entry}) for entry in tag.value[1]]))
            changes.append(field)
        elif tag.kind != 9 or tag.value[0] != 10:
            raise ValueError('Unknown vector representation; refusing lossy migration')
    # Couple.deserializeEach accepts compound elements only. Smoothing has a
    # different integer wrapper and must never be treated as a vector.
    if 'Smoothing' in d:
        smoothing = d['Smoothing']
        if smoothing.kind != 9 or smoothing.value[0] != 10:
            raise ValueError('Unsupported Smoothing representation')
        for entry in pair(smoothing):
            if set(compound(entry)) != {'V'} or compound(entry)['V'].kind != 3:
                raise ValueError('Expected Smoothing compound {V: TAG_Int}')
    return changes

def restore_wrappers(connection, changes):
    d = compound(connection)
    if 'Positions' in changes:
        d['Positions'] = Tag(9, (11, [Tag(11, (3, struct.pack('>iii', *position(t))))
                                      for t in pair(d['Positions'])]))
    for field in VECTOR_FIELDS:
        if field in changes:
            d[field] = Tag(9, (9, [compound(t)['V'] for t in pair(d[field])]))

def validate_reciprocal_curves(rows):
    for row in rows:
        g = row['geometry']
        reverse = [r['geometry'] for r in rows
                   if r['geometry']['absolute_positions'] == g['absolute_positions'][::-1]]
        if len(reverse) != 1:
            raise ValueError('Missing/ambiguous reciprocal connection')
        other = reverse[0]
        for field in ('absolute_starts', 'axes', 'normals'):
            if other[field] != g[field][::-1]:
                raise ValueError('Reciprocal curve geometry differs: ' + field)
        if other['primary'] != 1 - g['primary'] or any(
            other[field] != g[field] for field in ('girder', 'material')
        ):
            raise ValueError('Reciprocal curve flags/material differ')

def migrate(raw):
    name, tree = decode(raw)
    before = copy.deepcopy(tree)
    restored = copy.deepcopy(tree)
    blocks = sequence(compound(tree)['blocks'])
    restored_blocks = sequence(compound(restored)['blocks'])
    rows = []
    changes_count = 0
    for block_index, block in enumerate(blocks):
        b = compound(block)
        if 'nbt' not in b:
            continue
        nbt = compound(b['nbt'])
        if 'Connections' not in nbt:
            continue
        if text(nbt['id']) != 'create:track':
            raise ValueError('Unexpected non-track Connections tag')
        block_position = [integer(v) for v in sequence(b['pos'])]
        for index, connection in enumerate(sequence(nbt['Connections'])):
            initial = geometry(connection, block_position)
            fields = normalize(connection)
            if geometry(connection, block_position) != initial:
                raise ValueError('Curve geometry/flags/material changed')
            old = sequence(compound(compound(restored_blocks[block_index])['nbt'])['Connections'])
            old[index] = copy.deepcopy(connection)
            restore_wrappers(old[index], fields)
            rows.append({'block_position': block_position, 'connection_index': index,
                         'normalized_fields': fields, 'geometry': initial})
            changes_count += len(fields)
    # Entire original NBT, including unrelated blocks, palette, compounds,
    # tag types, and ordering, must be recovered by removing only our wrappers.
    if restored != before or payload(restored) != payload(before):
        raise ValueError('Unrelated NBT bytes or tag types changed')
    validate_reciprocal_curves(rows)
    encoded = bytes([10]) + nstr(name) + payload(tree)
    if decode(encoded)[1] != tree:
        raise ValueError('Converted NBT does not roundtrip')
    return encoded, rows, changes_count

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    mode = parser.add_mutually_exclusive_group()
    mode.add_argument('--apply', action='store_true')
    mode.add_argument('--check', action='store_true')
    parser.add_argument('--from-backup', action='store_true')
    args = parser.parse_args()
    if args.from_backup and not args.apply:
        parser.error('--from-backup requires --apply')
    previous = json.loads(MANIFEST.read_text(encoding='utf-8')) if args.from_backup or args.check else None
    expected = {r['path']: r for r in previous['records']} if previous else {}
    records = []
    pending = []
    audited = 0
    for path in sorted(TEMPLATES.glob('*.nbt')):
        relative = path.relative_to(ROOT).as_posix()
        current = path.read_bytes()
        backup = BACKUP / path.name
        if args.from_backup or args.check:
            if relative in expected:
                if sha(current) != expected[relative]['after_sha256']:
                    raise ValueError('Current template differs from manifest: ' + relative)
                original = backup.read_bytes()
                if sha(original) != expected[relative]['before_sha256']:
                    raise ValueError('Original backup differs from manifest: ' + relative)
            else:
                original = current
        else:
            original = current
        encoded, rows, changed = migrate(gzip.decompress(original))
        audited += 1
        if not changed:
            continue
        converted = gzip.compress(encoded, compresslevel=9, mtime=0)
        # Normalization is idempotent and retains every curve.
        again, repeated, remaining = migrate(encoded)
        if again != encoded or remaining or len(repeated) != len(rows):
            raise ValueError('Migration is not idempotent')
        if args.check and current != converted:
            raise ValueError('Backup reproduction differs from current template')
        records.append({'path': relative, 'before_sha256': sha(original),
                        'after_sha256': sha(converted), 'connections': rows})
        pending.append((path, backup, original, current, converted))
    report = {
        'schema': 'railways-ponder-track-migration/v1',
        'scope': 'Fixed module Ponder resource directory only; no world path accepted.',
        'source_contracts': [
            'modules/create-adapted-263/src/main/java/com/simibubi/create/content/trains/track/BezierConnection.java#write(BlockPos)',
            'modules/create-adapted-263/Ponder/catnip/common/src/main/java/net/createmod/catnip/api/math/VecHelper.java#writeNBTCompound(Vec3)',
        ],
        'verification': {
            'original_type_preserving_roundtrip': True,
            'full_original_nbt_recovered_by_unwrapping': True,
            'curve_geometry_flags_and_material_unchanged': True,
            'reciprocal_connections_preserved': True,
            'normalization_idempotent': True,
            'runtime_validation': 'Required from the integrated client; not run by this tool.',
        },
        'templates_audited': audited,
        'files': len(records),
        'connections': sum(len(r['connections']) for r in records),
        'records': records,
    }
    if args.check:
        if report != previous:
            raise ValueError('Reproduced manifest differs from checked-in manifest')
    elif args.apply:
        # Check all conflicts before the first write; retain immutable originals.
        for path, backup, original, current, converted in pending:
            if backup.exists() and backup.read_bytes() != original:
                raise ValueError('Conflicting original backup')
            if path.read_bytes() != current:
                raise ValueError('Concurrent source edit')
        for path, backup, original, current, converted in pending:
            backup.parent.mkdir(parents=True, exist_ok=True)
            if not backup.exists():
                backup.write_bytes(original)
        for path, backup, original, current, converted in pending:
            path.write_bytes(converted)
        # A second plain --apply is a no-op, preserving the first provenance.
        if records:
            MANIFEST.write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    else:
        PLAN.parent.mkdir(parents=True, exist_ok=True)
        PLAN.write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps({k: v for k, v in report.items() if k not in ('source_contracts', 'records')}, ensure_ascii=False))
    print(('Verified manifest: ' if args.check else 'Manifest: ') + str(MANIFEST if args.apply or args.check else PLAN))

if __name__ == '__main__':
    main()
