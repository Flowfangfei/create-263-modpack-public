#!/usr/bin/env python3
"""Migrate only checked-in Storage Ponder resources; never accepts a world path."""
from __future__ import annotations
import argparse,copy,gzip,hashlib,json,struct
from dataclasses import dataclass
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
TEMPLATES=ROOT/'modules/create-storage-adapted-263/src/main/resources/assets/fxntstorage/ponder'
@dataclass
class Tag:
    kind:int
    value:object
class Reader:
    def __init__(self,data):self.data=data;self.pos=0
    def take(self,n):
        if n<0 or self.pos+n>len(self.data):raise ValueError('Truncated NBT')
        out=self.data[self.pos:self.pos+n];self.pos+=n;return out
    def number(self,code):return struct.unpack('>'+code,self.take(struct.calcsize('>'+code)))[0]
    def string(self):return self.take(self.number('H'))
    def payload(self,k,depth=0):
        if depth>128:raise ValueError('NBT too deep')
        if k in range(1,7):v=self.number({1:'b',2:'h',3:'i',4:'q',5:'f',6:'d'}[k])
        elif k==8:v=self.string()
        elif k in (7,11,12):
            n=self.number('i');v=(n,self.take(n*{7:1,11:4,12:8}[k]))
        elif k==9:
            child,n=self.number('B'),self.number('i')
            if n<0 or n>1000000 or child>12 or (not child and n):raise ValueError('Invalid list')
            v=(child,[self.payload(child,depth+1) for _ in range(n)])
        elif k==10:
            v={}
            while (child:=self.number('B')):
                name=self.string().decode('utf-8')
                if child>12 or name in v:raise ValueError('Invalid compound')
                v[name]=self.payload(child,depth+1)
        else:raise ValueError('Invalid NBT tag')
        return Tag(k,v)
def nstr(raw):return struct.pack('>H',len(raw))+raw
def named(name,tag):return bytes([tag.kind])+nstr(name.encode('utf-8'))+payload(tag)
def payload(t):
    k,v=t.kind,t.value
    if k in range(1,7):return struct.pack('>'+{1:'b',2:'h',3:'i',4:'q',5:'f',6:'d'}[k],v)
    if k==8:return nstr(v)
    if k in (7,11,12):return struct.pack('>i',v[0])+v[1]
    if k==9:return bytes([v[0]])+struct.pack('>i',len(v[1]))+b''.join(payload(x) for x in v[1])
    if k==10:return b''.join(named(n,t) for n,t in v.items())+b'\0'
    raise ValueError('Unknown type')
def decode(raw):
    r=Reader(raw);kind=r.number('B');name=r.string();root=r.payload(kind)
    if kind!=10 or r.pos!=len(raw):raise ValueError('Bad root/trailing bytes')
    if bytes([kind])+nstr(name)+payload(root)!=raw:raise ValueError('NBT type-preserving roundtrip failed')
    return name,root
def text(t):
    if t.kind!=8:raise ValueError('Expected string')
    return t.value.decode('utf-8')
def integer(t):
    if t.kind not in (1,2,3,4):raise ValueError('Expected integer')
    return t.value
def compound(t):
    if t.kind!=10:raise ValueError('Expected compound')
    return t.value
def sequence(t):
    if t.kind!=9:raise ValueError('Expected list')
    return t.value[1]
def num(value):return Tag(3,value)
def string(value):return Tag(8,value.encode('utf-8'))
def item(old):
    d=compound(old)
    if not d:return Tag(10,{})
    if set(d)-{'id','Count','count','ActualCount','Slot'}:raise ValueError('Unexpected legacy item fields; refusing to discard data')
    if text(d['id'])=='minecraft:air' or integer(d.get('count',d.get('Count',num(1))))<=0:return Tag(10,{})
    return Tag(10,{'id':string(text(d['id'])),'count':num(1)})
def migrate(nbt):
    d=compound(nbt);identity=text(d['id']);old=d['Items']
    simple=identity=='fxntstorage:simple_storage_box_entity'
    if old.kind==10:
        inv=compound(old)
        if 'StorageFormat' in inv:return None
        if set(inv)!={'Items','Size'}:raise ValueError('Unknown legacy inventory document')
        size=integer(inv['Size']);entries=sequence(inv['Items'])
    else:
        if not simple or old.kind!=9:raise ValueError('Unexpected old inventory shape')
        size=integer(d['slotCount']);entries=sequence(old)
    if size<1 or size>4096:raise ValueError('Invalid slot count')
    slots={};items_before=[]
    for entry in entries:
        e=compound(entry);slot=integer(e['Slot']);amount=integer(e.get('ActualCount',e.get('count',e.get('Count',num(0)))))
        if not 0<=slot<size or slot in slots or not 0<amount<=84934656:raise ValueError('Invalid slot/quantity')
        prototype=item(entry);slots[slot]=(prototype,amount);items_before.append({'slot':slot,'id':text(compound(prototype)['id']),'amount':amount})
    if simple:
        if size not in (11,12,13):raise ValueError('Unknown Simple layout')
        mapped={}
        for slot,(prototype,amount) in slots.items():
            new=slot
            if size==11:
                if slot>=2:new=slot+1
                elif slot==1:new=upgrade_slot(prototype)
            elif size==13:
                if slot in (0,1):new=0
                elif slot==2:raise ValueError('Unexpected populated legacy unused slot')
                elif slot==3:new=upgrade_slot(prototype)
                else:new=slot-1
            if new in mapped:
                if mapped[new][0]!=prototype:raise ValueError('Incompatible old half-slot items')
                amount+=mapped[new][1]
            mapped[new]=(prototype,amount)
        slots=mapped;destination_size=12
    else:destination_size=size
    new_entries=[Tag(10,{'item':slots[i][0],'amount':num(slots[i][1])}) if i in slots else Tag(10,{'item':Tag(10,{}),'amount':num(0)}) for i in range(destination_size)]
    d['Items']=Tag(10,{'StorageFormat':num(1),'Size':num(destination_size),'Entries':Tag(9,(10,new_entries))})
    if simple and 'filterItem' in d:
        if 'FilterItem' in d:raise ValueError('Ambiguous old/new filter')
        d['FilterItem']=item(d['filterItem'])
    if simple and 'FilterItem' in d:d['FilterItem']=item(d['FilterItem'])
    if simple:
        for before,after in [('maxItemCapacity','MaxItemCapacity'),('storedAmount','StoredAmount'),('voidUpgrade','VoidUpgrade')]:
            if before in d and after not in d:d[after]=copy.deepcopy(d[before])
    items_after=[{'slot':i,'id':text(compound(p)['id']),'amount':n} for i,(p,n) in slots.items()]
    if sum(x['amount'] for x in items_before)!=sum(x['amount'] for x in items_after):raise ValueError('Item quantities changed')
    return {'id':identity,'old_slots':size,'new_slots':destination_size,'items_before':items_before,'items_after':items_after}
def upgrade_slot(prototype):
    identity=text(compound(prototype)['id'])
    if identity=='fxntstorage:storage_box_void_upgrade':return 1
    if identity=='fxntstorage:storage_box_compacting_upgrade':return 2
    raise ValueError('Unknown shared-slot upgrade')
def main():
    parser=argparse.ArgumentParser(description=__doc__);parser.add_argument('--apply',action='store_true');parser.add_argument('--from-backup',action='store_true',help='Reproduce from local pre-migration resource backups');args=parser.parse_args()
    report=[];pending=[]
    backup=ROOT/'.local/storage-ponder-template-originals'
    previous_path=ROOT/'.local/storage-ponder-template-migration-applied.json'
    previous=json.loads(previous_path.read_text(encoding='utf-8')) if args.from_backup else {}
    previous_hashes={r['path']:r['after_sha256'] for r in previous.get('records',[])}
    for path in sorted(TEMPLATES.rglob('*.nbt')):
        current=path.read_bytes()
        if args.from_backup:
            if hashlib.sha256(current).hexdigest()!=previous_hashes.get(path.relative_to(ROOT).as_posix()):raise ValueError('Current source differs from prior applied manifest')
            original=(backup/path.relative_to(TEMPLATES)).read_bytes()
        else:original=current
        raw=gzip.decompress(original);name,tree=decode(raw);before=copy.deepcopy(tree);rows=[]
        for block in sequence(compound(tree)['blocks']):
            b=compound(block)
            if 'nbt' not in b:continue
            d=compound(b['nbt']);identity=text(d['id']) if 'id' in d else ''
            if identity not in {'fxntstorage:storage_box_entity','fxntstorage:simple_storage_box_entity','fxntstorage:reserve_storage_box_entity'} or 'Items' not in d:continue
            row=migrate(b['nbt'])
            if row:row['position']=[integer(x) for x in sequence(b['pos'])];rows.append(row)
        if not rows:continue
        # Compare every unrelated tag (including vanilla BEs/components) with original types.
        old_blocks=sequence(compound(before)['blocks']);new_blocks=sequence(compound(tree)['blocks'])
        allowed={'Items','FilterItem','MaxItemCapacity','StoredAmount','VoidUpgrade'}
        for a,b in zip(old_blocks,new_blocks):
            av=compound(a);bv=compound(b)
            if a==b:continue
            if {k:v for k,v in av.items() if k!='nbt'}!={k:v for k,v in bv.items() if k!='nbt'}:raise ValueError('Changed block/palette reference')
            if {k:v for k,v in compound(av['nbt']).items() if k not in allowed}!={k:v for k,v in compound(bv['nbt']).items() if k not in allowed}:raise ValueError('Changed unrelated BE data')
        if {k:v for k,v in compound(before).items() if k!='blocks'}!={k:v for k,v in compound(tree).items() if k!='blocks'}:raise ValueError('Changed structure metadata')
        new_raw=bytes([10])+nstr(name)+payload(tree);assert decode(new_raw)[1]==tree
        converted=gzip.compress(new_raw,compresslevel=9,mtime=0)
        pending.append((path,original,current,converted))
        report.append({'path':path.relative_to(ROOT).as_posix(),'before_sha256':hashlib.sha256(original).hexdigest(),'after_sha256':hashlib.sha256(converted).hexdigest(),'inventories':rows})
    if args.apply:
        backup=ROOT/'.local/storage-ponder-template-originals'
        for path,original,current,converted in pending:
            dest=backup/path.relative_to(TEMPLATES);dest.parent.mkdir(parents=True,exist_ok=True)
            if dest.exists() and dest.read_bytes()!=original:raise ValueError('Conflicting template backup')
            if not dest.exists():dest.write_bytes(original)
        for path,original,current,converted in pending:
            if path.read_bytes()!=current:raise ValueError('Concurrent resource edit')
            path.write_bytes(converted)
    output={'schema':'storage-ponder-template-migration/v1','applied':args.apply,'files':len(report),'inventories':sum(len(r['inventories']) for r in report),'records':report,'scope':'Checked-in Storage Ponder templates only; no save, codec, registry or vanilla inventory changes.'}
    destination=ROOT/'.local'/('storage-ponder-template-migration-applied.json' if args.apply else 'storage-ponder-template-migration-plan.json')
    destination.parent.mkdir(exist_ok=True);destination.write_text(json.dumps(output,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print(json.dumps({k:v for k,v in output.items() if k!='records'},ensure_ascii=False));print('Manifest: '+str(destination))
if __name__=='__main__':main()
