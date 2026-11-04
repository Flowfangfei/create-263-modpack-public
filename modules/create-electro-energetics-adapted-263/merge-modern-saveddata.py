from pathlib import Path
import json,hashlib
r=Path(__file__).parent;src=r/'src/neoforge/java';m=json.loads((r/'source-fusion-manifest.json').read_text());entries={e['path']:e for e in m['sources']}
for path in ['com/george_vi/electroenergetics/simulation/infrastructure/InfrastructureSavedData.java','com/george_vi/electroenergetics/devices/device/DevicesSavedData.java','com/george_vi/electroenergetics/simulation/util/LegacySavedData.java']:
    p=r/'src/main/java'/path;t=p.read_text(encoding='utf-8')
    t=t.replace('com.zurrtum.create.catnip.data.Pair','net.createmod.catnip.api.data.Pair').replace('com.zurrtum.create.catnip.math.VecHelper','net.createmod.catnip.api.math.VecHelper').replace('com.zurrtum.create.catnip.nbt.NBTHelper','net.createmod.catnip.api.nbt.NBTHelper')
    t=t.replace('import com.george_vi.electroenergetics.compat.SpatialIntegration;','import dev.ryanhcode.sable.companion.SableCompanion;').replace('SpatialIntegration.isDynamic(level, node.sourcePos())','SableCompanion.INSTANCE.getContaining(level, node.sourcePos()) != null')
    if path.endswith('DevicesSavedData.java'):
        t=t.replace('java.util.function.Function<BlockPos, BlockPos> transform','dev.ryanhcode.sable.api.SubLevelAssemblyHelper.AssemblyTransform transform')
    dest=src/path;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_text(t,encoding='utf-8')
    e=entries.get(path)
    if not e:e={'path':path};m['sources'].append(e)
    e['origin']='createfly-26.2-1033dcb653a3';e['snapshot_sha256']=hashlib.sha256(p.read_bytes()).hexdigest();e['adaptation']='NeoForge Sable assembly and actual Catnip imports retained'
(r/'source-fusion-manifest.json').write_text(json.dumps(m,indent=2),encoding='utf-8')
