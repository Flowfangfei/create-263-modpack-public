package com.forsteri.createliquidfuel.core;

import com.forsteri.createliquidfuel.CreateLiquidFuel;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;

/** Native NeoForge transactional capability; keeps the existing Stomach NBT key. */
public final class BurnerTank extends FluidStacksResourceHandler {
    public static final int CAPACITY = 1000;
    private final Runnable changed;
    private boolean loading;

    public BurnerTank(Runnable changed) {
        super(1, CAPACITY);
        this.changed = changed;
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        return !resource.isEmpty() && LiquidFuels.isFuel(resource.getFluid());
    }

    @Override
    protected void onContentsChanged(int index, FluidStack previous) {
        if (!loading) changed.run();
    }

    public FluidStack getFluid() {
        return getResource(0).toStack(getAmountAsInt(0));
    }

    public void read(HolderLookup.Provider registries, CompoundTag tag) {
        loading = true;
        try {
            DynamicOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
            FluidStack contents = FluidStack.OPTIONAL_CODEC.parse(ops, tag)
                .resultOrPartial(CreateLiquidFuel.LOGGER::warn).orElse(FluidStack.EMPTY);
            set(0, FluidResource.of(contents), Math.min(contents.getAmount(), CAPACITY));
        } finally {
            loading = false;
        }
    }

    public Tag write(HolderLookup.Provider registries) {
        return FluidStack.OPTIONAL_CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), getFluid()).getOrThrow();
    }
}
