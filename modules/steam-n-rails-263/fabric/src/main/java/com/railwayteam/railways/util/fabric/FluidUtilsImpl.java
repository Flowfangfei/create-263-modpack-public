package com.railwayteam.railways.util.fabric;

import com.railwayteam.railways.content.fuel.tank.FuelTankBlockEntity;
import com.railwayteam.railways.multiloader.fluid.MultiloaderFluidStack;
import com.railwayteam.railways.internal.compat.create.content.processing.recipe.ProcessingRecipe;
import com.railwayteam.railways.internal.compat.create.content.processing.recipe.ProcessingRecipeBuilder;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

public class FluidUtilsImpl {
    public static boolean canUseAsFuelStorage(BlockEntity be) {
        return be instanceof FuelTankBlockEntity;
    }

    public static Fluid getFluid(Object o) {
        if (o instanceof FluidResource resource) // NeoForge: instead of Fabric's FluidVariant
            return resource.getFluid();
        if (o instanceof FluidStack stack)
            return stack.getFluid();
        if (o instanceof MultiloaderFluidStack stack)
            return stack.getFluid();
        if (o instanceof Fluid fluid)
            return fluid;
        throw new IllegalArgumentException("Expected a fluid stack, fluid variant, or fluid, got: " + o);
    }

    public static void addFluidOutput(ProcessingRecipeBuilder<ProcessingRecipe<?>> b, Fluid fluid, long amount, @Nullable CompoundTag nbt) {
        FluidStack output = new FluidStack(fluid, (int) Math.max(0, Math.min(Integer.MAX_VALUE, amount)));
        if (nbt != null && !output.isEmpty()) {
            output.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.of(nbt.copy()));
        }
        b.withFluidOutputs(output);
    }
}
