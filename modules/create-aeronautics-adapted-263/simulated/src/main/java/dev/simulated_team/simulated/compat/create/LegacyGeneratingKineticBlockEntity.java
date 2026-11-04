package dev.simulated_team.simulated.compat.create;
/** Uses the target Create implementation for native persistence and lifecycle. */
public abstract class LegacyGeneratingKineticBlockEntity extends com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity implements LegacyKineticTooltipProvider {
    public LegacyGeneratingKineticBlockEntity(net.minecraft.world.level.block.entity.BlockEntityType<?> type, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state){super(type,pos,state);}
}
