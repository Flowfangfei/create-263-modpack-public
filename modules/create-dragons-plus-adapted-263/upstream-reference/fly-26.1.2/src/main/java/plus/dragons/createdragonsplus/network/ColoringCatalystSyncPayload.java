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

import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import plus.dragons.createdragonsplus.CreateDragonsPlus;

public record ColoringCatalystSyncPayload(
        Map<Identifier, Identifier> fluids, Map<Identifier, Identifier> blocks) implements CustomPacketPayload {
    public static final Type<ColoringCatalystSyncPayload> TYPE = new Type<>(CreateDragonsPlus.id("coloring_catalysts"));
    public static final StreamCodec<FriendlyByteBuf, ColoringCatalystSyncPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ColoringCatalystSyncPayload decode(FriendlyByteBuf buffer) {
            return new ColoringCatalystSyncPayload(readMap(buffer), readMap(buffer));
        }

        @Override
        public void encode(FriendlyByteBuf buffer, ColoringCatalystSyncPayload payload) {
            writeMap(buffer, payload.fluids());
            writeMap(buffer, payload.blocks());
        }
    };

    public ColoringCatalystSyncPayload {
        fluids = Map.copyOf(fluids);
        blocks = Map.copyOf(blocks);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static Map<Identifier, Identifier> readMap(FriendlyByteBuf buffer) {
        int size = ByteBufCodecs.VAR_INT.decode(buffer);
        LinkedHashMap<Identifier, Identifier> values = new LinkedHashMap<>(size);
        for (int index = 0; index < size; index++) {
            values.put(Identifier.STREAM_CODEC.decode(buffer), Identifier.STREAM_CODEC.decode(buffer));
        }
        return values;
    }

    private static void writeMap(FriendlyByteBuf buffer, Map<Identifier, Identifier> values) {
        ByteBufCodecs.VAR_INT.encode(buffer, values.size());
        values.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    Identifier.STREAM_CODEC.encode(buffer, entry.getKey());
                    Identifier.STREAM_CODEC.encode(buffer, entry.getValue());
                });
    }
}
