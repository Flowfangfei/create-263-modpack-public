package com.railwayteam.railways.content.fuel.fabric;

import com.railwayteam.railways.content.fuel.LiquidFuelTrainHandler;
import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageWrapper;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.world.level.Level;
import com.simibubi.create.compat.neoforge263.fluids.capability.IFluidHandler.FluidAction;
import org.jetbrains.annotations.Nullable;

public final class LiquidFuelTrainHandlerImpl {
    private static final int FUEL_DRAIN_AMOUNT = 100;

    private LiquidFuelTrainHandlerImpl() {
    }

    public static int handleFuelDraining(MountedFluidStorageWrapper fluidFuels) {
        return handleFuelDraining(fluidFuels, null);
    }

    public static int handleFuelDraining(
        MountedFluidStorageWrapper fluidFuels,
        @Nullable Level fuelValues // 26.3: FuelValues is gone
    ) {
        for (int slot = 0; slot < fluidFuels.getTanks(); slot++) {
            FluidStack stack = fluidFuels.getFluidInTank(slot);
            if (stack.isEmpty() || stack.getAmount() < FUEL_DRAIN_AMOUNT) {
                continue;
            }

            int burnTime = LiquidFuelTrainHandler.handleFuelChecking(stack, fuelValues);
            if (burnTime <= 0) {
                continue;
            }

            FluidStack requested = stack.copyWithAmount(FUEL_DRAIN_AMOUNT);
            if (fluidFuels.drain(requested, FluidAction.SIMULATE).getAmount() != FUEL_DRAIN_AMOUNT) {
                continue;
            }
            if (fluidFuels.drain(requested, FluidAction.EXECUTE).getAmount() == FUEL_DRAIN_AMOUNT) {
                return burnTime;
            }
        }
        return 0;
    }
}
