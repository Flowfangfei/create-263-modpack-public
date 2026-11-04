/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2024 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.railwayteam.railways.fabric_mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.railwayteam.railways.mixin_interfaces.ICarriageBufferDistanceTracker;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.graph.DimensionPalette;
import com.simibubi.create.content.trains.graph.TrackGraph;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Carriage.class, remap = false)
public abstract class MixinCarriageBufferDistance implements ICarriageBufferDistanceTracker {
    @Unique
    private @Nullable Integer railways$leadingBufferDistance = null;

    @Unique
    private @Nullable Integer railways$trailingBufferDistance = null;

    @Override
    public @Nullable Integer railways$getLeadingDistance() {
        return railways$leadingBufferDistance;
    }

    @Override
    public @Nullable Integer railways$getTrailingDistance() {
        return railways$trailingBufferDistance;
    }

    @Override
    public void railways$setLeadingDistance(int distance) {
        railways$leadingBufferDistance = Math.max(0, distance);
    }

    @Override
    public void railways$setTrailingDistance(int distance) {
        railways$trailingBufferDistance = Math.max(0, distance);
    }

    @Inject(method = "write(Lcom/simibubi/create/content/trains/graph/DimensionPalette;Lnet/minecraft/core/HolderLookup$Provider;Lnet/minecraft/resources/RegistryOps;)Lnet/minecraft/nbt/CompoundTag;", at = @At("RETURN"))
    private void railways$writeBufferDistances(DimensionPalette dimensions, HolderLookup.Provider registries,
            RegistryOps<Tag> ops, CallbackInfoReturnable<CompoundTag> cir) {
        CompoundTag tag = cir.getReturnValue();
        if (railways$leadingBufferDistance != null) tag.putInt("LeadingBufferDistance", railways$leadingBufferDistance);
        if (railways$trailingBufferDistance != null) tag.putInt("TrailingBufferDistance", railways$trailingBufferDistance);
    }

    @Inject(method = "read(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;Lnet/minecraft/resources/RegistryOps;Lcom/simibubi/create/content/trains/graph/TrackGraph;Lcom/simibubi/create/content/trains/graph/DimensionPalette;)Lcom/simibubi/create/content/trains/entity/Carriage;", at = @At("RETURN"))
    private static void railways$readBufferDistances(CompoundTag tag, HolderLookup.Provider registries,
            RegistryOps<Tag> ops, TrackGraph graph, DimensionPalette dimensions, CallbackInfoReturnable<Carriage> cir) {
        ICarriageBufferDistanceTracker tracker = (ICarriageBufferDistanceTracker) cir.getReturnValue();
        tag.getInt("LeadingBufferDistance").ifPresent(tracker::railways$setLeadingDistance);
        tag.getInt("TrailingBufferDistance").ifPresent(tracker::railways$setTrailingDistance);
    }
}
