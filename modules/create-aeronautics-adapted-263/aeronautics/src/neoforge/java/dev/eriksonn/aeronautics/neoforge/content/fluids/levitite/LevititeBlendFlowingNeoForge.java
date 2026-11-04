package dev.eriksonn.aeronautics.neoforge.content.fluids.levitite;
import dev.eriksonn.aeronautics.api.levitite_blend_crystallization.LevititeBlendDummyInterface;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
public class LevititeBlendFlowingNeoForge extends BaseFlowingFluid.Flowing implements LevititeBlendDummyInterface {
    public LevititeBlendFlowingNeoForge(Properties properties){super(properties);}
    @Override public void tick(net.minecraft.server.level.ServerLevel level,net.minecraft.core.BlockPos pos,net.minecraft.world.level.block.state.BlockState blockState,net.minecraft.world.level.material.FluidState fluidState){
        super.tick(level,pos,blockState,fluidState);LevititeBlendDummyInterface.super.levititeBlendTick(level,pos,fluidState);
    }
}
