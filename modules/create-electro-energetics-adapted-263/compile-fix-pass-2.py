from pathlib import Path
import re
r=Path(__file__).parent;s=r/'src/neoforge/java';u=r/'upstream-neoforge-reference/src/main/java'
for name in ['CEEDataComponents','CEEMobEffects']:
    p=s/('com/george_vi/electroenergetics/'+name+'.java');p.write_text((u/p.relative_to(s)).read_text(encoding='utf-8'),encoding='utf-8')
for p in s.rglob('*.java'):
    t=p.read_text(encoding='utf-8');orig=t
    t=t.replace('.getNormal()', '.getUnitVec3i()')
    t=re.sub(r'(CEERegistries\.[A-Z_]+)\.get\(',r'\1.getValue(',t)
    t=re.sub(r'\.getCompound\(([^\n;]+?)\)(?!\s*\.(?:orElse|map|ifPresent|isPresent|isEmpty))',r'.getCompoundOrEmpty(\1)',t)
    t=re.sub(r'\.getUUID\(([^\n;]+?)\)',r'.read(\1, net.minecraft.core.UUIDUtil.CODEC).orElse(null)',t)
    t=re.sub(r'\.putUUID\(([^,\n]+), ([^)\n]+)\)',r'.store(\1, net.minecraft.core.UUIDUtil.CODEC, \2)',t)
    t=t.replace('CatnipServices.NETWORK.sendToServer(', 'net.createmod.catnip.api.client.network.ClientNetworkHelper.INSTANCE.sendToServer(')
    t=t.replace('public void handle(LocalPlayer player) {','public void handle(net.minecraft.world.entity.player.Player recipient) {\n        LocalPlayer player = (LocalPlayer) recipient;')
    t=t.replace('LevelRenderer.getLightColor(', 'net.minecraft.util.LightCoordsUtil.getLightCoords(')
    t=t.replace('RenderType.SOLID','net.minecraft.client.renderer.rendertype.RenderTypes.solidMovingBlock()').replace('RenderType.CUTOUT','net.minecraft.client.renderer.rendertype.RenderTypes.cutoutMovingBlock()').replace('RenderType.TRANSLUCENT','net.minecraft.client.renderer.rendertype.RenderTypes.translucentMovingBlock()')
    if 'import com.simibubi.create.foundation.render.RenderTypes;' in t:
        t=t.replace('import com.simibubi.create.foundation.render.RenderTypes;\n','')
        # The Create-specific emission factories keep their own class identity.
        t=t.replace('RenderTypes.additive()', 'com.simibubi.create.foundation.render.RenderTypes.additive()')
        t=t.replace('RenderTypes.translucent()', 'com.simibubi.create.foundation.render.RenderTypes.translucent()')
    t=re.sub(r'^        computerBehaviour.removePeripheral\(\);\n','',t,flags=re.M)
    if t!=orig:p.write_text(t,encoding='utf-8')
p=s/'com/george_vi/electroenergetics/CEEClient.java';t=p.read_text(encoding='utf-8').replace('import net.createmod.catnip.api.client.config.BaseConfigScreen;','import net.neoforged.neoforge.client.gui.ConfigurationScreen;').replace('new BaseConfigScreen(previousScreen, CreateElectroEnergetics.ID)','new ConfigurationScreen(container, previousScreen)');p.write_text(t,encoding='utf-8')
p=s/'com/george_vi/electroenergetics/content/electrical_panel/ElectricalPanelSlot.java';t=p.read_text(encoding='utf-8').replace('Vec3.atCenterOf(shape)','shape.getCenter()');p.write_text(t,encoding='utf-8')
