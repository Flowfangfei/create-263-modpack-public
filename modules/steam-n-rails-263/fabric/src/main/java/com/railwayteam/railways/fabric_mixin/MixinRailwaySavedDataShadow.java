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
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.railwayteam.railways.Railways;
import com.railwayteam.railways.mixin_interfaces.IShadowTrain;
import com.railwayteam.railways.mixin_interfaces.RailwaySavedDataDuck;
import com.simibubi.create.content.trains.RailwaySavedData;
import com.simibubi.create.content.trains.entity.Train;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Keeps shadow trains persisted in RailwaySavedData while excluding them from live train ticks. */
@Mixin(value = RailwaySavedData.class, remap = false)
public abstract class MixinRailwaySavedDataShadow implements RailwaySavedDataDuck {
    @Unique
    private final Map<UUID, Train> railways$shadowTrains = new HashMap<>();

    @Unique
    private final Map<Identifier, UUID> railways$shadowKeys = new HashMap<>();

    @Override
    public Map<UUID, Train> railway$getShadowTrains() {
        return railways$shadowTrains;
    }

    @Override
    public Map<Identifier, UUID> railways$getShadowKeys() {
        return railways$shadowKeys;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @WrapOperation(
        method = "save(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/resources/RegistryOps;)Lnet/minecraft/nbt/CompoundTag;",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Map;values()Ljava/util/Collection;",
            ordinal = 2
        )
    )
    private Collection railways$appendShadowTrains(
        Map liveTrains,
        Operation<Collection> original
    ) {
        LinkedHashMap<UUID, Train> combined = new LinkedHashMap<>();
        for (Object value : original.call(liveTrains)) {
            Train train = (Train) value;
            combined.put(train.id, train);
        }
        combined.putAll(railways$shadowTrains);
        return new ArrayList<>(combined.values());
    }

    @Inject(method = "load(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/resources/RegistryOps;)Lcom/simibubi/create/content/trains/RailwaySavedData;", at = @At("RETURN"))
    private static void railways$separateLoadedShadowTrains(CompoundTag tag, RegistryOps<Tag> ops,
            CallbackInfoReturnable<RailwaySavedData> cir) {
        RailwaySavedData savedData = cir.getReturnValue();
        RailwaySavedDataDuck shadowData = (RailwaySavedDataDuck) savedData;
        var iterator = savedData.getTrains().values().iterator();
        while (iterator.hasNext()) {
            Train train = iterator.next();
            Identifier shadowKey = ((IShadowTrain) train).railways$getShadowKey();
            if (shadowKey == null) continue;
            iterator.remove();
            shadowData.railway$getShadowTrains().put(train.id, train);
            UUID previous = shadowData.railways$getShadowKeys().putIfAbsent(shadowKey, train.id);
            if (previous != null && !previous.equals(train.id))
                Railways.LOGGER.warn("Duplicate persisted shadow key {} for trains {} and {}; keeping the first key mapping", shadowKey, previous, train.id);
        }
    }
}
