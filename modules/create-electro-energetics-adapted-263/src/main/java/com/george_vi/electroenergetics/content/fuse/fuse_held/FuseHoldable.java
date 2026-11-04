package com.george_vi.electroenergetics.content.fuse.fuse_held;

import com.george_vi.electroenergetics.CEETags;
import com.george_vi.electroenergetics.CreateElectroEnergetics;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public abstract class FuseHoldable {
    public static Map<Identifier, FuseHoldable> ALL = new HashMap<>();

    public static FuseHoldable register(FuseHoldable fuseHoldable, Identifier location) {
        ALL.put(location, fuseHoldable);
        return fuseHoldable;
    }

    public abstract void preTick(CompoundTag data, int id1, int id2, BridgeCollector.Builder bridges, Level level, BlockPos pos);

    public void postTick(CompoundTag data, int id1, int id2, SimulationResults results, Level level, BlockPos pos) {};

    public abstract NonNullList<ItemStack> getDrops(CompoundTag data);

    public Identifier getID() {
        for (Map.Entry<Identifier, FuseHoldable> e : ALL.entrySet()) {
            if (e.getValue() == this)
                return e.getKey();
        }
        return CreateElectroEnergetics.rl("empty");
    }

    public abstract boolean isValid(ItemStack stack);

    public void onPlace(CompoundTag data, ItemStack stack, Level level, BlockPos pos) {};

    /**
     * When the player right-clicks it.
     * @return false if the fuse is supposed to be taken off.
     */
    public boolean interact(CompoundTag data, ItemStack stack, Level level, BlockPos pos) {
        return false;
    }

    public static class CopperConductor extends FuseHoldable {

        @Override
        public void preTick(CompoundTag data, int id1, int id2, BridgeCollector.Builder bridges, Level level, BlockPos pos) {
            bridges.resistor(id1, id2, 0.1);
        }

        @Override
        public NonNullList<ItemStack> getDrops(CompoundTag data) {
            return NonNullList.of(Items.AIR.getDefaultInstance(), CEETags.itemFromTag(CEETags.FUSE_BYPASS_ITEM).getDefaultInstance());
        }

        @Override
        public boolean isValid(ItemStack stack) {
            return stack.is(CEETags.FUSE_BYPASS_ITEM);
        }
    }

}
