/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package plus.dragons.createdragonsplus.network;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import plus.dragons.createdragonsplus.data.map.CDPColoringCatalysts;

public final class CDPNetworking {
    private static boolean registered;

    private CDPNetworking() {}

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        PayloadTypeRegistry.clientboundConfiguration()
                .register(ColoringCatalystSyncPayload.TYPE, ColoringCatalystSyncPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay()
                .register(ColoringCatalystSyncPayload.TYPE, ColoringCatalystSyncPayload.STREAM_CODEC);
        ServerConfigurationConnectionEvents.CONFIGURE.register((handler, server) -> {
            if (ServerConfigurationNetworking.canSend(handler, ColoringCatalystSyncPayload.TYPE)) {
                ServerConfigurationNetworking.send(handler, CDPColoringCatalysts.snapshot().payload());
            }
        });
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> {
            if (ServerPlayNetworking.canSend(player, ColoringCatalystSyncPayload.TYPE)) {
                ServerPlayNetworking.send(player, CDPColoringCatalysts.snapshot().payload());
            }
        });
    }
}
