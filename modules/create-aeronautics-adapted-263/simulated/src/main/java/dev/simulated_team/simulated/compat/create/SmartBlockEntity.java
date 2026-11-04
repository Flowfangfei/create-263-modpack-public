package dev.simulated_team.simulated.compat.create;
/** Uses the target Create implementation for native persistence and lifecycle. */
public abstract class SmartBlockEntity extends com.simibubi.create.foundation.blockEntity.SmartBlockEntity implements IHaveGoggleInformation,IHaveHoveringInformation {
    public SmartBlockEntity(net.minecraft.world.level.block.entity.BlockEntityType<?> type, net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state){super(type,pos,state);}

    public boolean addToTooltip(java.util.List<net.minecraft.network.chat.Component> tooltip,boolean sneaking){return false;}
    public boolean addToGoggleTooltip(java.util.List<net.minecraft.network.chat.Component> tooltip,boolean sneaking){return false;}
}
