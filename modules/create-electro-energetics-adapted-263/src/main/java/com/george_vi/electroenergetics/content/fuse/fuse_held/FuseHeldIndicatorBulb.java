package com.george_vi.electroenergetics.content.fuse.fuse_held;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CEETags;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class FuseHeldIndicatorBulb extends FuseHoldable {

    @Override
    public void preTick(CompoundTag data, int id1, int id2, BridgeCollector.Builder bridges, Level level, BlockPos pos) {
        bridges.resistor(id1, id2, CEEConfigs.server().resistanceValues.indicatorBulbResistance.get());
    }

    @Override
    public void postTick(CompoundTag data, int id1, int id2, SimulationResults results, Level level, BlockPos pos) {
        float light = (float) Math.min(1, Math.abs(results.getVoltageAt(pos, id1, id2) / 70));
        float lastLight = data.getFloatOr("Light", 0f);
        if (Math.abs(light - lastLight) > 0.01) {
            data.putFloat("Light", light);
            data.putBoolean("UpdateThisTick", true);
        }
    }

    @Override
    public NonNullList<ItemStack> getDrops(CompoundTag data) {
        return NonNullList.of(Items.AIR.getDefaultInstance(), CEEBlocks.INDICATOR_BULB.asStack());
    }

    @Override
    public boolean interact(CompoundTag data, ItemStack stack, Level level, BlockPos pos) {
        if (stack.is(CEETags.FUSE_WRENCH))
            return false;
        if (stack.getItem() instanceof DyeItem di)
            data.putString("Color", stack.getOrDefault(net.minecraft.core.component.DataComponents.DYE, net.minecraft.world.item.DyeColor.WHITE).getSerializedName());

        return true;
    }

    @Override
    public boolean isValid(ItemStack stack) {
        return CEEBlocks.INDICATOR_BULB.isIn(stack);
    }
}
