/*
 * Steam 'n' Rails
 * Copyright (c) 2025-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.content.fuel.tank;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.railwayteam.railways.registry.fabric.CRMountedStorageTypesImpl;
import com.simibubi.create.api.contraption.storage.SyncedMountedStorage;
import com.simibubi.create.api.contraption.storage.fluid.WrapperMountedFluidStorage;
import net.createmod.catnip.api.animation.LerpedFloat;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.compat.neoforge263.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class FuelTankMountedStorage
    extends WrapperMountedFluidStorage<FuelTankMountedStorage.Handler>
    implements SyncedMountedStorage {

    public static final MapCodec<FuelTankMountedStorage> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        ExtraCodecs.NON_NEGATIVE_INT.fieldOf("capacity").forGetter(FuelTankMountedStorage::getCapacity),
        FluidStack.OPTIONAL_CODEC.fieldOf("fluid").forGetter(FuelTankMountedStorage::getFluid)
    ).apply(instance, FuelTankMountedStorage::new));

    private boolean dirty;

    protected FuelTankMountedStorage(int capacity, FluidStack stack) {
        super(CRMountedStorageTypesImpl.FUEL_TANK, new Handler(capacity, stack));
        wrapped.onChange = () -> dirty = true;
    }

    @Override
    public void unmount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
        if (be instanceof FuelTankBlockEntity tank && tank.isController()) {
            tank.getTankInventory().setFluid(wrapped.getFluid());
        }
    }

    public FluidStack getFluid() {
        return wrapped.getFluid();
    }

    public int getCapacity() {
        return wrapped.getCapacity();
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void markClean() {
        dirty = false;
    }

    public void markDirty() {
        dirty = true;
    }

    @Override
    public void afterSync(Contraption contraption, BlockPos localPos) {
        BlockEntity be = contraption.getBlockEntityClientSide(localPos);
        if (!(be instanceof FuelTankBlockEntity tank)) {
            return;
        }

        FluidTank inventory = tank.getTankInventory();
        inventory.setFluid(getFluid());
        float fillLevel = inventory.getFluid().getAmount() / (float) inventory.getCapacity();
        if (tank.getFluidLevel() == null) {
            tank.setFluidLevel(LerpedFloat.linear().startWithValue(fillLevel));
        }
        tank.getFluidLevel().chase(fillLevel, 0.5, LerpedFloat.Chaser.EXP);
    }

    public static FuelTankMountedStorage fromTank(FuelTankBlockEntity tank) {
        FluidTank inventory = tank.getTankInventory();
        return new FuelTankMountedStorage(inventory.getCapacity(), inventory.getFluid().copy());
    }

    public static final class Handler extends FluidTank {
        private Runnable onChange = () -> {};
        private Handler(int capacity, FluidStack stack) {
            super(capacity);
            setFluid(stack);
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            return tankFuel(stack);
        }

        @Override
        protected void onContentsChanged() {
            onChange.run();
        }
    }

    private static boolean tankFuel(FluidStack stack) {
        return com.railwayteam.railways.content.fuel.LiquidFuelTrainHandler.isFuelForTanks(stack.getFluid());
    }
}
