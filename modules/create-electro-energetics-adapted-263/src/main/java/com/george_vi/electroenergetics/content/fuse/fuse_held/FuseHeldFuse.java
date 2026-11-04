package com.george_vi.electroenergetics.content.fuse.fuse_held;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CEEDataComponents;
import com.george_vi.electroenergetics.content.cut_off_switch.SwitchingBehaviour;
import com.george_vi.electroenergetics.content.fuse.BlownFuseTracker;
import com.george_vi.electroenergetics.content.fuse.FuseHolderBlock;
import com.george_vi.electroenergetics.foundation.SendSparkPacket;
import com.george_vi.electroenergetics.foundation.base.DirectionalRolledDeviceBlock;
import com.george_vi.electroenergetics.foundation.device.ElectricalDevice;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.events.network.CEENetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class FuseHeldFuse extends FuseHoldable {

    @Override
    public void preTick(CompoundTag data, int id1, int id2, BridgeCollector.Builder bridges, Level level, BlockPos pos) {
        SwitchingBehaviour behaviour = new SwitchingBehaviour(data.getCompoundOrEmpty("Behaviour"));
        double r = behaviour.resistance();
        if (r < 1e+10d)
            bridges.resistor(id1, id2, r);
    }

    @Override
    public void postTick(CompoundTag data, int id1, int id2, SimulationResults results, Level level, BlockPos pos) {
        SwitchingBehaviour behaviour = new SwitchingBehaviour(data.getCompoundOrEmpty("Behaviour"));
        boolean isBroken = data.getBooleanOr("Broken", false);
        float setAmperage = (float) data.getDoubleOr("SetAmperage", 0d);

        double voltage = Math.abs(results.getVoltageAt(pos, id1, id2));
        double current = voltage / behaviour.resistance();

        float temp = data.getFloatOr("Temp", 0f);
        temp = ElectricalDevice.updateTemp(temp, (float) current);

        behaviour.isClosed = !isBroken;
        behaviour.postTick(voltage / 2, net.minecraft.world.phys.Vec3.atCenterOf(pos), level);

        if (current < 1 || isBroken)
            temp = 0;
        data.putFloat("Temp", temp);

        if (temp > ElectricalDevice.finalTempAt(setAmperage)) {
            if (!isBroken) {
                Player player = level.getNearestPlayer(pos.getX(), pos.getY(), pos.getZ(), 30, false);
                if (player != null)
                    BlownFuseTracker.onFuseBlow(player, pos);

                if (level.isLoaded(pos)) {
                    BlockState state = level.getBlockState(pos);
                    if (state.getBlock() instanceof FuseHolderBlock block) {
                        Vec3 sparkPos = block.getNodePosition(level, pos, state, id1).add(block.getNodePosition(level, pos, state, id2)).scale(0.5).add(Vec3.atLowerCornerOf(pos));
                        sparkPos = sparkPos.relative(state.getValue(DirectionalRolledDeviceBlock.FACING), 0.25);
                        CEENetwork.NETWORK.sendToClientsAround((ServerLevel) level, sparkPos, 20, new SendSparkPacket(sparkPos, SendSparkPacket.SparkSize.SMALL));
                    }
                }

                data.putBoolean("UpdateThisTick", true);
            }

            data.putBoolean("Broken", true);

        }
        data.put("Behaviour", behaviour.write());
    }

    @Override
    public NonNullList<ItemStack> getDrops(CompoundTag data) {
        ItemStack stack = data.getBooleanOr("Broken", false) ? CEEBlocks.BROKEN_FUSE.asStack() : CEEBlocks.FUSE.asStack();
        int setAmperage = (int) data.getDoubleOr("SetAmperage", 0d);
        if (setAmperage != 100)
            stack.set(CEEDataComponents.FUSE_AMPERAGE, setAmperage);
        return NonNullList.of(Items.AIR.getDefaultInstance(), stack);
    }

    @Override
    public boolean isValid(ItemStack stack) {
        return CEEBlocks.FUSE.isIn(stack) || CEEBlocks.BROKEN_FUSE.isIn(stack);
    }

    @Override
    public void onPlace(CompoundTag data, ItemStack stack, Level level, BlockPos pos) {
        if (CEEBlocks.BROKEN_FUSE.isIn(stack))
            data.putBoolean("Broken", true);
        int currentAmperage = stack.getOrDefault(CEEDataComponents.FUSE_AMPERAGE, 100);

        data.putDouble("SetAmperage", currentAmperage);
    }
}
