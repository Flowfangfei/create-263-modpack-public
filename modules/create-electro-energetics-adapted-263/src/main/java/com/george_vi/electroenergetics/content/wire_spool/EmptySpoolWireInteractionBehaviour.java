package com.george_vi.electroenergetics.content.wire_spool;

import com.george_vi.electroenergetics.CEEDataComponents;
import com.george_vi.electroenergetics.CEEItems;
import com.george_vi.electroenergetics.content.wire.interaction.WireInteractionBehaviour;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNodeConnection;
import com.george_vi.electroenergetics.foundation.nodes.NodeConnectionPoint;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import com.george_vi.electroenergetics.simulation.infrastructure.WireData;
import com.zurrtum.create.AllSoundEvents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class EmptySpoolWireInteractionBehaviour extends WireInteractionBehaviour {
    @Override
    public void interactWire(NodeConnectionPoint point, Level level, Player player, ItemStack stack) {
        if (!(player.level() instanceof ServerLevel sl))
            return;

        InfrastructureSavedData sd = InfrastructureSavedData.load(sl);

        WireData data = sd.removeConnection(new InWorldNodeConnection(point.node1(), point.node2()));

        AllSoundEvents.WRENCH_REMOVE.playOnServer(level, BlockPos.containing(point.posAt(net.minecraft.world.phys.Vec3.atCenterOf(point.node1().sourcePos()), net.minecraft.world.phys.Vec3.atCenterOf(point.node2().sourcePos()), data.wireType().getSag())));

        if (!player.isCreative()) {
            stack.shrink(1);
            player.getInventory().placeItemBackInInventory(data.wireType().getSpooledItem().getDefaultInstance());
        }

        if (stack.has(CEEDataComponents.SELECTED_NODE))
            stack.remove(CEEDataComponents.SELECTED_NODE);
    }

    @Override
    @net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
    public DisplayType getWireDisplayType(NodeConnectionPoint point, Level level, Player player, ItemStack stack) {
        return DisplayType.LINE;
    }

    @Override
    @net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
    public int getWireDisplayColor(NodeConnectionPoint point, Level level, Player player, ItemStack stack) {
        return 0xff7171;
    }

    @Override
    public boolean isActiveFor(ItemStack stack, Player player) {
        return CEEItems.EMPTY_SPOOL.isIn(stack);
    }

    @Override
    @net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
    public boolean canInteractOn(ClientLevel level, BlockState state, BlockPos pos) {
        return true;
    }
}
