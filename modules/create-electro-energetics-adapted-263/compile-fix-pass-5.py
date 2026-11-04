from pathlib import Path
import re,zipfile
r=Path(__file__).parent;s=r/'src/neoforge/java';base='com/george_vi/electroenergetics/'
# Restore platform renderer contracts removed by CreateFly's separate client registry.
restore=['content/wire/WireAttachmentType.java','content/fuse/fuse_held/FuseHoldable.java','content/electrical_panel/attachments/ElectricFanAttachment.java','content/electrical_panel/attachments/EnergyMeterAttachment.java','content/electrical_panel/attachments/GaugePanelAttachment.java','content/electrical_panel/attachments/TriPolarEnergyMeterAttachment.java']
for rel in restore:
    p=s/base/rel;t=(r/'upstream-neoforge-reference/src/main/java'/base/rel).read_text(encoding='utf-8')
    t=re.sub(r'\bResourceLocation\b','Identifier',t)
    t=t.replace('net.minecraft.client.renderer.MultiBufferSource','net.createmod.catnip.impl.client.render.MultiBufferSource').replace('net.minecraft.client.renderer.RenderType','net.minecraft.client.renderer.rendertype.RenderType')
    t=t.replace('import net.createmod.catnip.render.CachedBuffers;','import com.george_vi.electroenergetics.CEEPartialModels.Bufferer;').replace('CachedBuffers.','Bufferer.')
    t=re.sub(r'\bRenderType\.(solid|cutout|translucent)\(\)',lambda m:'net.minecraft.client.renderer.rendertype.RenderTypes.'+{'solid':'solidMovingBlock','cutout':'cutoutMovingBlock','translucent':'translucentMovingBlock'}[m[1]]+'()',t)
    t=re.sub(r'\bRenderType\.(SOLID|CUTOUT|TRANSLUCENT)\b',lambda m:'net.minecraft.client.renderer.rendertype.RenderTypes.'+{'SOLID':'solidMovingBlock','CUTOUT':'cutoutMovingBlock','TRANSLUCENT':'translucentMovingBlock'}[m[1]]+'()',t)
    p.write_text(t,encoding='utf-8')
# Highest-Minecraft block implementations bring the modern neighbor/analog-output signatures.
classes=set();bybase={}
for jar in (r.parent/'steam-n-rails-263/tooling/create-adapted/libs').glob('*.jar'):
    with zipfile.ZipFile(jar) as z:
        for n in z.namelist():
            if n.endswith('.class') and '$' not in n:
                c=n[:-6].replace('/','.');classes.add(c);bybase.setdefault(c.rsplit('.',1)[-1],set()).add(c)
for rel in ['content/connector/ConnectorBlock.java','content/connector/DoubleConnectorBlock.java','content/energy_meter/EnergyMeterBlock.java','content/energy_meter/TriPolarEnergyMeterBlock.java','content/railway_electrification/catenary/CatenaryHolderBlock.java']:
    p=s/base/rel;t=(r/'src/main/java'/base/rel).read_text(encoding='utf-8').replace('com.george_vi.electroenergetics.foundation.lang.Lang','net.createmod.catnip.api.lang.Lang')
    for cls in re.findall(r'^import (com\.zurrtum\.create\.[\w.]+);',t,re.M):
        direct=cls.replace('com.zurrtum.create.','com.simibubi.create.');cand=bybase.get(cls.rsplit('.',1)[-1],set())
        new=direct if direct in classes else next(iter(cand)) if len(cand)==1 else None
        if new is None:raise RuntimeError('Unresolved actual Create class '+cls)
        t=t.replace(cls,new)
    t=t.replace('updateWater(level, ticks, state, pos);','com.george_vi.electroenergetics.foundation.ProperOilAndWaterloggedBlock.updateFluid(level,ticks,state,pos);')
    p.write_text(t,encoding='utf-8')
# Field-level API adjustments identified by the compiler.
for p in s.rglob('*.java'):
    t=p.read_text(encoding='utf-8');old=t
    t=t.replace('mc.options.hideGui || ', '').replace('ChatFormatting.RED.getColor()', '0xff5555')
    t=t.replace('graphics.drawString(', 'graphics.text(').replace('event.getCamera().getPosition()', 'event.getCamera().position()')
    t=t.replace('level.getMinBuildHeight()', 'level.getMinY()').replace('level.getMaxBuildHeight()', 'level.getMaxY()')
    t=t.replace('Vec3.atCenterOf(select.', 'select.') if False else t
    # The accidental center rewrite also touched non-BlockPos APIs; preserve their real getCenter methods.
    t=re.sub(r'Vec3\.atCenterOf\((select\.[^;\n]+?)\)',r'\1.getCenter()',t) if False else t
    if p.name.endswith('Scenes.java'):
        t=re.sub(r'Vec3\.atCenterOf\(([^;\n]+)\)',r'\1.getCenter()',t)
    t=t.replace('Vec3.atCenterOf(firstPlacement)', 'firstPlacement.getCenter()').replace('Vec3.atCenterOf(secondPlacement)', 'secondPlacement.getCenter()')
    if p.name in ['ElectricMotorRenderer.java','AlternatorBrushesRenderer.java','PotentiometerRenderer.java','VariacRenderer.java']:
        t=t.replace('    @Override\n    protected ModelBuffer getRotatedModel(', '    protected ModelBuffer getRotatedModel(')
        t=t.replace('getRenderType(be, state)','net.minecraft.client.renderer.rendertype.RenderTypes.solidMovingBlock()')
    if t!=old:p.write_text(t,encoding='utf-8')
# Use the modern ItemStack codec for panel-link persistence.
p=s/base/'content/electrical_panel/link/ElectricalPanelLink.java';t=p.read_text(encoding='utf-8').replace('linkFrequency.save(registries)','ItemStack.CODEC.encodeStart(registries.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), linkFrequency).getOrThrow()').replace('ItemStack.parse(registries, itemTag).orElse(ItemStack.EMPTY)','ItemStack.CODEC.parse(registries.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE), itemTag).result().orElse(ItemStack.EMPTY)');p.write_text(t,encoding='utf-8')
# Register the deliberately invisible node entity using the modern entity renderer event.
p=s/base/'CEEEntityTypes.java';t=p.read_text(encoding='utf-8').replace('            .renderer(() -> NoopRenderer::new)\n','');p.write_text(t,encoding='utf-8')
p=s/base/'CEEClient.java';t=p.read_text(encoding='utf-8').replace('        modEventBus.addListener(CEEClient::onLoadComplete);','''        modEventBus.addListener(CEEClient::onLoadComplete);
        modEventBus.addListener((net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers event) ->
            event.registerEntityRenderer(CEEEntityTypes.DETACHED_NODE.get(),net.minecraft.client.renderer.entity.NoopRenderer::new));''');p.write_text(t,encoding='utf-8')
