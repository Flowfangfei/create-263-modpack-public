from pathlib import Path
import re
r=Path(__file__).parent;s=r/'src/neoforge/java/com/george_vi/electroenergetics/ponder';u=r/'upstream-neoforge-reference/src/main/java/com/george_vi/electroenergetics/ponder'
for p in s.glob('*Scenes.java'):
    t=(u/p.name).read_text(encoding='utf-8').replace('ResourceLocation','Identifier')
    for var in re.findall(r'\bBlockPos\s+(\w+)\s*=',t):t=t.replace(var+'.getCenter()', 'net.minecraft.world.phys.Vec3.atCenterOf('+var+')')
    p.write_text(t,encoding='utf-8')
