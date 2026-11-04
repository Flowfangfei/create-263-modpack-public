/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.fabric_mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.railwayteam.railways.config.CRConfigs;
import com.railwayteam.railways.content.shadow_realm.ShadowRealm;
import com.simibubi.create.content.trains.entity.TrainRelocator;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.track.BezierTrackPointLocation;
import com.simibubi.create.content.trains.entity.TrainRelocationPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

/** Lets Create's normal relocation UI operate on a train that is not in the client manager. */
@Mixin(value = TrainRelocator.class, remap = false)
public abstract class MixinTrainRelocatorClientShadow {
    @Shadow
    private static @Nullable UUID relocatingTrain;

    @Inject(method = "getRelocating", at = @At("HEAD"), cancellable = true)
    private static void railways$getShadowRelocating(CallbackInfoReturnable<Train> cir) {
        if (ShadowRealm.MARKER.equals(relocatingTrain)) {
            cir.setReturnValue(ShadowRealm.clientShadowRestoringTrain);
        }
    }

    @Inject(method = "clientTick", at = @At("HEAD"))
    private static void railways$clearFinishedShadowRestore(CallbackInfo ci) {
        if (!ShadowRealm.MARKER.equals(relocatingTrain)) {
            ShadowRealm.clientShadowRestoringTrain = null;
        }
    }

    @WrapOperation(
        method = "relocateClient",
        at = @At(
            value = "NEW",
            target = "(Ljava/util/UUID;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/Vec3;IZLcom/simibubi/create/content/trains/track/BezierTrackPointLocation;)Lcom/simibubi/create/content/trains/entity/TrainRelocationPacket;"
        )
    )
    private static TrainRelocationPacket railways$sendRealShadowTrainId(
        UUID trainId,
        BlockPos pos,
        Vec3 lookAngle,
        int entityId,
        boolean direction,
        @Nullable BezierTrackPointLocation hoveredBezier,
        Operation<TrainRelocationPacket> original
    ) {
        if (ShadowRealm.MARKER.equals(trainId) && ShadowRealm.clientShadowRestoringTrain != null) {
            trainId = ShadowRealm.clientShadowRestoringTrain.id;
        }
        return original.call(trainId, pos, lookAngle, entityId, direction, hoveredBezier);
    }

    @WrapOperation(
        method = {"clientTick", "onClicked"},
        at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/content/trains/entity/TrainRelocator;canInteractWith(Lnet/minecraft/client/player/LocalPlayer;Lnet/minecraft/core/BlockPos;D)Z"
        )
    )
    private static boolean railways$unrestrictShadowRestoreRange(
        LocalPlayer player,
        BlockPos origin,
        double distance,
        Operation<Boolean> original
    ) {
        if (ShadowRealm.MARKER.equals(relocatingTrain)
            || player != null && player.isCreative() && CRConfigs.server().unlimitedCreativeRelocation.get()) {
            return true;
        }
        return original.call(player, origin, distance);
    }
}
