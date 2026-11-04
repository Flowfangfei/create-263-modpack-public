from pathlib import Path
import re
r=Path(__file__).parent;s=r/'src/neoforge/java'
p=s/'com/george_vi/electroenergetics/foundation/ProperOilAndWaterloggedBlock.java';t=(r/'src/main/java'/p.relative_to(s)).read_text(encoding='utf-8').replace('com.george_vi.electroenergetics.foundation.lang.Lang','net.createmod.catnip.api.lang.Lang')
at=t.index('    EnumProperty<LoggedState>')
t=t[:at]+'''    static void updateFluid(net.minecraft.world.level.LevelReader level, net.minecraft.world.level.ScheduledTickAccess ticks, BlockState state, BlockPos pos) {
        Fluid fluid=state.hasProperty(LOGGED_STATE) ? state.getValue(LOGGED_STATE).getFluid() :
            state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED)
            && state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED) ? Fluids.WATER : Fluids.EMPTY;
        if (fluid!=Fluids.EMPTY) ticks.scheduleTick(pos,fluid,fluid.getTickDelay(level));
    }
'''+t[at:];p.write_text(t,encoding='utf-8')
for p in s.rglob('*.java'):
    t=p.read_text(encoding='utf-8');old=t
    t=t.replace('updateWater(level, ticks, state, pos);','com.george_vi.electroenergetics.foundation.ProperOilAndWaterloggedBlock.updateFluid(level,ticks,state,pos);')
    t=t.replace('FMLLoader.getDist()', 'FMLLoader.getCurrent().getDist()').replace('new CatnipPacketRegistry(CreateElectroEnergetics.ID, 1)', 'new CatnipPacketRegistry(CreateElectroEnergetics.ID, "1")')
    t=t.replace('CEENetwork.NETWORK.sendToServer(', 'net.createmod.catnip.api.client.network.ClientNetworkHelper.INSTANCE.sendToServer(')
    t=t.replace('import com.zurrtum.create.AllSoundEvents;', 'import com.simibubi.create.AllSoundEvents;')
    t=t.replace('.render(graphics);', '.render(graphics,0,0);')
    # Add the required explicit prediction mode to inventory returns.
    offset=0
    while True:
        marker='.placeItemBackInInventory(';a=t.find(marker,offset)
        if a<0:break
        start=a+len(marker);i=start;depth=1;commas=0
        while depth:
            c=t[i]
            if c=='(':depth+=1
            elif c==')':depth-=1
            elif c==',' and depth==1:commas+=1
            i+=1
        if commas==0:t=t[:i-1]+', net.minecraft.util.Prediction.SERVER_ONLY'+t[i-1:];offset=i+48
        else:offset=i
    if 'private static final int placementHelperId' in t:
        t=t.replace('private static final int placementHelperId','private static final IPlacementHelper placementHelperId').replace('PlacementHelpers.get(placementHelperId)','placementHelperId')
        t=t.replace('.placeInWorld(level, (BlockItem) stack.getItem(), player, hand)', '.placeInWorld(level, (BlockItem) stack.getItem(), player, hand, hitResult)')
    t=re.sub(r'public void releaseUsing\(([^)]*)\) \{\n        onStopUsing\(([^;]+);\n    }',r'public boolean releaseUsing(\1) {\n        onStopUsing(\2;\n        return true;\n    }',t)
    t=re.sub(r'((?:CEE|Create)Lang\.builder\(\)(?:(?!;).)*?)\.addTo\((\w+)\);',r'CEELang.addTo(\1, \2);',t,flags=re.S)
    if t!=old:p.write_text(t,encoding='utf-8')
p=s/'com/george_vi/electroenergetics/foundation/nodes/NodeConfigurator.java';t=p.read_text(encoding='utf-8').replace('id.intValue()', 'id');p.write_text(t,encoding='utf-8')
p=s/'com/george_vi/electroenergetics/foundation/CEELang.java';t=p.read_text(encoding='utf-8');i=t.rfind('}');t=t[:i]+'''    public static void addTo(LangBuilder builder, java.util.List<net.minecraft.network.chat.Component> target) { target.add(builder.component()); }
    public static void addTo(LangBuilder builder, java.util.function.Consumer<net.minecraft.network.chat.Component> target) { target.accept(builder.component()); }
'''+t[i:];p.write_text(t,encoding='utf-8')
