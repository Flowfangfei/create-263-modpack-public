from pathlib import Path
import re
r=Path(__file__).parent;s=r/'src/neoforge/java'
def replace_center(t,method,factory):
    pattern='.'+method+'()'; offset=0
    while True:
        index=t.find(pattern,offset)
        if index<0:break
        j=index-1;depth=0
        while j>=0:
            c=t[j]
            if c in ')]':depth+=1
            elif c in '([':
                if depth:depth-=1
                else:break
            elif depth==0 and (c.isspace() or c in '=,;{}:+-*/!<>?&|'):break
            j-=1
        start=j+1;expr=t[start:index]
        if not expr:offset=index+len(pattern);continue
        new=factory+'('+expr+')';t=t[:start]+new+t[index+len(pattern):];offset=start+len(new)
    return t
for p in s.rglob('*.java'):
    t=p.read_text(encoding='utf-8');old=t
    t=t.replace('ChunkPos.asLong(', 'ChunkPos.pack(')
    t=t.replace('FMLEnvironment.production','net.neoforged.fml.loading.FMLLoader.getCurrent().isProduction()')
    t=replace_center(t,'getCenter','Vec3.atCenterOf');t=replace_center(t,'getBottomCenter','Vec3.atBottomCenterOf')
    if 'Vec3.at' in t and 'import net.minecraft.world.phys.Vec3;' not in t:
        package_end=t.index(';')+1;t=t[:package_end]+'\nimport net.minecraft.world.phys.Vec3;'+t[package_end:]
    if t!=old:p.write_text(t,encoding='utf-8')
