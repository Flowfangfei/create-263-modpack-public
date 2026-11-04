/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.fabric_mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.railwayteam.railways.mixin_interfaces.IShadowTrain;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.graph.DimensionPalette;
import com.simibubi.create.content.trains.graph.TrackGraph;
import net.minecraft.resources.Identifier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.UUID;

/** Adds the persistent upstream-compatible ShadowKey field to Create Adapted trains. */
@Mixin(value = Train.class, remap = false)
public abstract class MixinTrainShadow implements IShadowTrain {
    @Unique
    private @Nullable Identifier railways$shadowKey;

    @Override
    public void railways$setShadow(@NotNull Identifier shadowKey) {
        railways$shadowKey = shadowKey;
    }

    @Override
    public void railways$clearShadow() {
        railways$shadowKey = null;
    }

    @Override
    public @Nullable Identifier railways$getShadowKey() {
        return railways$shadowKey;
    }

    @Inject(method = "write(Lcom/simibubi/create/content/trains/graph/DimensionPalette;Lnet/minecraft/core/HolderLookup$Provider;Lnet/minecraft/resources/RegistryOps;)Lnet/minecraft/nbt/CompoundTag;", at = @At("RETURN"))
    private void railways$writeExtras(DimensionPalette dimensions, HolderLookup.Provider registries,
                                     RegistryOps<Tag> ops, CallbackInfoReturnable<CompoundTag> cir) {
        if (railways$shadowKey != null) cir.getReturnValue().putString("ShadowKey", railways$shadowKey.toString());
    }

    @Inject(method = "read(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;Lnet/minecraft/resources/RegistryOps;Ljava/util/Map;Lcom/simibubi/create/content/trains/graph/DimensionPalette;)Lcom/simibubi/create/content/trains/entity/Train;", at = @At("RETURN"))
    private static void railways$readExtras(CompoundTag tag, HolderLookup.Provider registries,
            RegistryOps<Tag> ops, Map<UUID, TrackGraph> trackNetworks, DimensionPalette dimensions,
            CallbackInfoReturnable<Train> cir) {
        IShadowTrain train = (IShadowTrain) cir.getReturnValue();
        Identifier key = tag.getString("ShadowKey").map(Identifier::tryParse).orElse(null);
        if (key == null) train.railways$clearShadow();
        else train.railways$setShadow(key);
    }
}
