/*
 * Steam 'n' Rails
 * Copyright (c) 2024-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.multiloader.fluid.fabric;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.railwayteam.railways.multiloader.fluid.MultiloaderFluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.DataComponentFluidIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

/** Loader-neutral fluid stack measured in millibuckets, matching Create Adapted and NeoForge. */
public final class MultiloaderFluidStackImpl extends MultiloaderFluidStack {
    private static final Codec<MultiloaderFluidStackImpl> DIRECT_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            BuiltInRegistries.FLUID.byNameCodec().fieldOf("fluid")
                .forGetter(MultiloaderFluidStackImpl::getFluid),
            Codec.LONG.fieldOf("amount").forGetter(MultiloaderFluidStackImpl::getAmount),
            CompoundTag.CODEC.optionalFieldOf("tag").forGetter(stack -> Optional.ofNullable(stack.tag))
        ).apply(instance, (fluid, amount, tag) -> new MultiloaderFluidStackImpl(fluid, amount, tag.orElse(null)))
    );

    private Fluid fluid;
    private long amount;
    private @Nullable CompoundTag tag;

    public MultiloaderFluidStackImpl(Fluid fluid, long amount, @Nullable CompoundTag tag) {
        this.fluid = fluid;
        this.amount = amount;
        this.tag = tag == null ? null : tag.copy();
    }

    public static Codec<MultiloaderFluidStack> makeCodec() {
        return DIRECT_CODEC.xmap(stack -> stack, stack -> (MultiloaderFluidStackImpl) stack);
    }

    public static MultiloaderFluidStack makeEmpty() {
        return new MultiloaderFluidStackImpl(Fluids.EMPTY, 0, null);
    }

    public static MultiloaderFluidStack create(Fluid fluid, long amount, @Nullable CompoundTag nbt) {
        return new MultiloaderFluidStackImpl(fluid, amount, nbt);
    }

    public static MultiloaderFluidStack loadFluidStackFromNBT(CompoundTag nbt) {
        String encodedId = nbt.getString("FluidName").orElse("minecraft:empty");
        Identifier id = Identifier.tryParse(encodedId);
        Fluid fluid = id == null ? Fluids.EMPTY : BuiltInRegistries.FLUID.getValue(id);
        if (fluid == null) {
            fluid = Fluids.EMPTY;
        }
        long amount = nbt.getLong("Amount").orElse(0L);
        CompoundTag fluidTag = nbt.getCompound("Tag").map(CompoundTag::copy).orElse(null);
        return new MultiloaderFluidStackImpl(fluid, amount, fluidTag);
    }

    public static MultiloaderFluidStack readFromPacket(FriendlyByteBuf buffer) {
        Fluid fluid = BuiltInRegistries.FLUID.getValue(buffer.readIdentifier());
        long amount = buffer.readVarLong();
        CompoundTag tag = buffer.readNbt();
        return new MultiloaderFluidStackImpl(fluid == null ? Fluids.EMPTY : fluid, amount, tag);
    }

    @Override
    public MultiloaderFluidStack setAmount(long amount) {
        this.amount = amount;
        return this;
    }

    @Override
    public Fluid getFluid() {
        return fluid;
    }

    @Override
    public long getAmount() {
        return amount;
    }

    @Override
    public boolean isEmpty() {
        return fluid == Fluids.EMPTY || amount <= 0;
    }

    @Override
    public boolean isFluidEqual(MultiloaderFluidStack other) {
        return other != null && other.getFluid() == fluid && areFluidStackTagsEqual(this, other);
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag nbt) {
        nbt.putString("FluidName", BuiltInRegistries.FLUID.getKey(fluid).toString());
        nbt.putLong("Amount", amount);
        if (tag != null) {
            nbt.put("Tag", tag.copy());
        }
        return nbt;
    }

    @Override
    public void setTag(CompoundTag tag) {
        this.tag = tag == null ? null : tag.copy();
    }

    @Override
    public @Nullable CompoundTag getTag() {
        return tag;
    }

    @Override
    public Component getDisplayName() {
        if (isEmpty()) {
            return Component.empty();
        }
        return toNeoForgeStack().getHoverName();
    }

    @Override
    public FriendlyByteBuf writeToPacket(FriendlyByteBuf buffer) {
        buffer.writeIdentifier(BuiltInRegistries.FLUID.getKey(fluid));
        buffer.writeVarLong(amount);
        buffer.writeNbt(tag);
        return buffer;
    }

    @Override
    public MultiloaderFluidStack copy() {
        return new MultiloaderFluidStackImpl(fluid, amount, tag);
    }

    @Override
    public boolean containsFluid(@NotNull MultiloaderFluidStack other) {
        return isFluidEqual(other) && amount >= other.getAmount();
    }

    @Override
    public boolean isFluidStackIdentical(MultiloaderFluidStack other) {
        return other != null && amount == other.getAmount() && isFluidEqual(other);
    }

    @Override
    public boolean isFluidEqual(@NotNull ItemStack other) {
        return !isEmpty() && com.simibubi.create.compat.neoforge263.fluids.FluidUtil.getFluidContained(other)
            .map(contained -> FluidStack.isSameFluidSameComponents(toNeoForgeStack(), contained))
            .orElse(false);
    }

    @Override
    public boolean isLighterThanAir() {
        return !isEmpty() && fluid.getFluidType().isLighterThanAir();
    }

    @Override
    public SizedFluidIngredient asFluidIngredient() {
        FluidStack stack = toNeoForgeStack();
        return new SizedFluidIngredient(DataComponentFluidIngredient.of(false, stack), stack.getAmount());
    }

    private FluidStack toNeoForgeStack() {
        FluidStack stack = new FluidStack(fluid, clampAmount(amount));
        if (tag != null && !stack.isEmpty()) {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag.copy()));
        }
        return stack;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof MultiloaderFluidStack stack && isFluidStackIdentical(stack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fluid, amount, tag);
    }

    private static int clampAmount(long amount) {
        return (int) Math.max(0, Math.min(Integer.MAX_VALUE, amount));
    }
}
