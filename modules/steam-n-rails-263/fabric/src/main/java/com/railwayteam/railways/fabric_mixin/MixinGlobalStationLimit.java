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
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.railwayteam.railways.mixin_interfaces.ILimitedGlobalStation;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.graph.DimensionPalette;
import com.simibubi.create.content.trains.station.GlobalStation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GlobalStation.class, remap = false)
public abstract class MixinGlobalStationLimit implements ILimitedGlobalStation {
    @Unique
    private static final String RAILWAYS$LIMIT_KEY = "LimitEnabled";

    @Shadow
    @Nullable
    public abstract Train getNearestTrain();

    @Unique
    private boolean railways$limitEnabled;

    @Override
    public boolean isStationEnabled() {
        return !railways$limitEnabled || getNearestTrain() == null;
    }

    @Override
    public Train getDisablingTrain() {
        return railways$limitEnabled ? getNearestTrain() : null;
    }

    @Override
    public Train orDisablingTrain(Train before, Train except) {
        if (before == null || before == except)
            before = getDisablingTrain();
        return before;
    }

    @Override
    public void setLimitEnabled(boolean limitEnabled) {
        this.railways$limitEnabled = limitEnabled;
    }

    @Override
    public boolean isLimitEnabled() {
        return railways$limitEnabled;
    }

    @Inject(method = "read(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;ZLcom/simibubi/create/content/trains/graph/DimensionPalette;)V", at = @At("TAIL"))
    private void railways$readLimit(CompoundTag tag, HolderLookup.Provider registries, boolean migration, DimensionPalette dimensions, CallbackInfo ci) {
        railways$limitEnabled = tag.getBooleanOr(RAILWAYS$LIMIT_KEY, false);
    }

    @Inject(method = "write(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;Lcom/simibubi/create/content/trains/graph/DimensionPalette;)V", at = @At("TAIL"))
    private void railways$writeLimit(CompoundTag tag, HolderLookup.Provider registries, DimensionPalette dimensions, CallbackInfo ci) {
        tag.putBoolean(RAILWAYS$LIMIT_KEY, railways$limitEnabled);
    }

    @Inject(method = "read(Lnet/minecraft/network/FriendlyByteBuf;Lcom/simibubi/create/content/trains/graph/DimensionPalette;)V", at = @At("TAIL"))
    private void railways$readNetLimit(FriendlyByteBuf buffer, DimensionPalette dimensions, CallbackInfo ci) {
        railways$limitEnabled = buffer.readBoolean();
    }

    @Inject(method = "write(Lnet/minecraft/network/FriendlyByteBuf;Lcom/simibubi/create/content/trains/graph/DimensionPalette;)V", at = @At("TAIL"))
    private void railways$writeNetLimit(FriendlyByteBuf buffer, DimensionPalette dimensions, CallbackInfo ci) {
        buffer.writeBoolean(railways$limitEnabled);
    }
}
