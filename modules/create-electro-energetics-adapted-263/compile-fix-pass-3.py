from pathlib import Path
import re
r=Path(__file__).parent;s=r/'src/neoforge/java'
for p in s.rglob('*.java'):
    t=p.read_text(encoding='utf-8');old=t;lines=[]
    for line in t.splitlines():
        if '.read()' in line and 'net.minecraft.core.UUIDUtil.CODEC).orElse(null)' in line:
            line=line.replace('.read()', '.getUUID()').replace(', net.minecraft.core.UUIDUtil.CODEC).orElse(null)', ')')
        lines.append(line)
    t='\n'.join(lines)+'\n'
    t=t.replace('Vec3.atCenterOf((Position)sourcePos)', 'Vec3.atCenterOf(sourcePos)')
    t=t.replace('Wirenet.minecraft.client.renderer.rendertype.RenderTypes.solidMovingBlock()', 'WireRenderType.SOLID')
    t=re.sub(r'NbtUtils\.readBlockPos\(([^,\n]+), ([^)\n]+)\)',r'\1.read(\2, BlockPos.CODEC)',t)
    # Codec encoding retains the historical packed-int BlockPos representation.
    t=re.sub(r'NbtUtils\.writeBlockPos\(([^()\n]+)\)',r'BlockPos.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, \1).getOrThrow()',t)
    t=re.sub(r'\.getIntArray\(("[^"]+")\)(?!\.orElse)',r'.getIntArray(\1).orElseGet(() -> new int[0])',t)
    t=t.replace('.registryOrThrow(', '.lookupOrThrow(').replace('.getHolderOrThrow(', '.getOrThrow(').replace('.getHolder(', '.get(')
    t=t.replace('AddToElectricGraphEvent.EVENT.invoker().accept(', 'net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(').replace('FinishElectricSimulationEvent.EVENT.invoker().accept(', 'net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(')
    t=t.replace('camera.getPosition()', 'camera.position()').replace('.getMainCamera()', '.mainCamera()').replace('.mainCamera().getPosition()', '.mainCamera().position()')
    t=re.sub(r'\b(?:level|world)\.getProfiler\(\)', 'net.minecraft.util.profiling.Profiler.get()',t)
    if t!=old:p.write_text(t,encoding='utf-8')
paths=['content/energy_meter/EnergyMeterScreen.java','content/energy_meter/PanelEnergyMeterScreen.java','content/transmission_distribution/transformer/TransformerScreen.java','CEEGuiTextures.java']
for path in paths:
    p=s/'com/george_vi/electroenergetics'/path;q=r/'src/main/java/com/george_vi/electroenergetics'/path;t=q.read_text(encoding='utf-8')
    mapping={'com.zurrtum.create.client.foundation.gui.AllIcons':'com.simibubi.create.foundation.gui.AllIcons','com.zurrtum.create.client.foundation.gui.widget.IconButton':'com.simibubi.create.foundation.gui.widget.IconButton','com.zurrtum.create.client.catnip.gui.AbstractSimiScreen':'net.createmod.catnip.api.client.gui.AbstractSimiScreen','com.zurrtum.create.client.catnip.gui.element.GuiGameElement':'net.createmod.catnip.api.client.gui.element.GuiGameElement','com.george_vi.electroenergetics.foundation.lang.Lang':'net.createmod.catnip.api.lang.Lang','com.zurrtum.create.client.catnip.gui.TextureSheetSegment':'net.createmod.catnip.api.client.gui.TextureSheetSegment','com.zurrtum.create.client.catnip.gui.UIRenderHelper':'net.createmod.catnip.api.client.gui.UIRenderHelper','com.zurrtum.create.client.catnip.gui.element.ScreenElement':'net.createmod.catnip.api.client.gui.element.ScreenElement','com.zurrtum.create.catnip.theme.Color':'net.createmod.catnip.api.theme.Color'}
    for a,b in mapping.items():t=t.replace(a,b)
    t=t.replace('import com.george_vi.electroenergetics.events.network.CEENetwork;','import net.createmod.catnip.api.client.network.ClientNetworkHelper;').replace('CEENetwork.sendToServer(', 'ClientNetworkHelper.INSTANCE.sendToServer(')
    t=t.replace('import org.lwjgl.glfw.GLFW;\n','').replace('GLFW.GLFW_MOUSE_BUTTON_LEFT','0')
    if path.endswith('CEEGuiTextures.java'):
        t=t.replace('Identifier getLocation()', 'Identifier getId()')
    else:
        t=t.replace('    @Override\n    protected void renderWindow(', '    protected void renderWindow(')
        at=t.index('{',t.index('public class '))+1
        t=t[:at]+'''
    protected int windowWidth, windowHeight, guiLeft, guiTop;
    protected void setWindowSize(int width, int height) { this.windowWidth=width; this.windowHeight=height; }
    protected void setWindowOffset(int x, int y) { guiLeft=(width-windowWidth)/2+x; guiTop=(height-windowHeight)/2+y; }
    @Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX,int mouseY,float partialTicks) {
        renderWindow(graphics,mouseX,mouseY,partialTicks);
        super.extractRenderState(graphics,mouseX,mouseY,partialTicks);
    }
'''+t[at:]
    p.write_text(t,encoding='utf-8')
