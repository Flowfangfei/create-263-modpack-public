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

import com.railwayteam.railways.content.shadow_realm.ShadowRealm;

import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.entity.AddTrainPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Converts the marked native AddTrainPacket into a relocation-only client snapshot. */
@Mixin(value = AddTrainPacket.class, remap = false)
public abstract class MixinAllHandleShadowTrain {
    // Build 2 schedules packets inside onAddTrain; wait until that dispatch has completed.
    // Source: https://github.com/ZurrTum/Create-Fly/blob/26.2/src/client/java/com/simibubi/create/client/AllHandle.java
    @Inject(method="handle",at=@At("HEAD"),cancellable=true)
    private void railways$beginShadowRestore(net.minecraft.world.entity.player.Player playerArgument,CallbackInfo ci) {
        AddTrainPacket packet=(AddTrainPacket)(Object)this;
        Train train = packet.train();
        if (!train.id.equals(ShadowRealm.clientPendingShadowTrainId)) {
            return;
        }

        ci.cancel();
        ShadowRealm.clientPendingShadowTrainId = null;
        ShadowRealm.clientShadowRestoringTrain = train;

        LocalPlayer player = Minecraft.getInstance().player;
        AccessorTrainRelocatorClient.railways$setRelocatingTrain(ShadowRealm.MARKER);
        AccessorTrainRelocatorClient.railways$setRelocatingOrigin(
            player == null ? net.minecraft.core.BlockPos.ZERO : player.blockPosition()
        );
        AccessorTrainRelocatorClient.railways$setRelocatingEntityId(-1);
        ShadowRealm.LOGGER.info("Shadow Realm client restore snapshot accepted for train {}", train.id);
    }
}
