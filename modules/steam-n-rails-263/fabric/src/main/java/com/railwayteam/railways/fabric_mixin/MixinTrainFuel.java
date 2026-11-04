/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.fabric_mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.railwayteam.railways.content.fuel.LiquidFuelTrainHandler;
import com.railwayteam.railways.mixin_interfaces.IFuelInventory;
import com.railwayteam.railways.registry.CRTags;
import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageWrapper;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.Train;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Predicate;

@Mixin(value = Train.class, remap = false)
public abstract class MixinTrainFuel {
    @Shadow
    public List<Carriage> carriages;

    @Shadow
    public double speed;

    @Shadow
    public int fuelTicks;

    @Inject(
        method = "burnFuel",
        at = @At(value = "INVOKE", target = "Ljava/util/List;size()I", shift = At.Shift.AFTER),
        cancellable = true
    )
    private void railways$burnLiquidFuel(Level level, CallbackInfo ci) {
        boolean iterateFromBack = speed < 0;
        int carriageCount = carriages.size();
        Level fuelValues = level; // 26.3: FuelValues is gone

        for (int index = 0; index < carriageCount; index++) {
            int carriageIndex = iterateFromBack ? carriageCount - 1 - index : index;
            Carriage carriage = carriages.get(carriageIndex);
            MountedFluidStorageWrapper fuelFluids =
                ((IFuelInventory) carriage.storage).railways$getFluidFuels();
            if (fuelFluids == null) {
                continue;
            }

            fuelTicks += LiquidFuelTrainHandler.handleFuelDraining(fuelFluids, fuelValues);
            if (fuelTicks > 0) {
                ci.cancel();
                return;
            }
        }
    }

    @WrapOperation(method = "burnFuel", at = @At(value = "INVOKE",
        target = "Lcom/simibubi/create/compat/neoforge263/items/IItemHandlerModifiable;extractItem(IIZ)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack railways$rejectBlacklistedItemFuel(
        com.simibubi.create.compat.neoforge263.items.IItemHandlerModifiable inventory, int slot, int amount,
        boolean simulate, Operation<ItemStack> original) {
        ItemStack candidate = inventory.getStackInSlot(slot);
        return candidate.is(CRTags.AllItemTags.NOT_TRAIN_FUEL.tag)
            ? ItemStack.EMPTY : original.call(inventory, slot, amount, simulate);
    }
}
