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

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.netty.buffer.Unpooled;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

class ColoringCatalystSyncPayloadTest {
    @Test
    void streamCodecRoundTripsBothMaps() {
        Map<Identifier, Identifier> fluids = new LinkedHashMap<>();
        fluids.put(Identifier.parse("test:z"), Identifier.withDefaultNamespace("blue"));
        fluids.put(Identifier.parse("test:a"), Identifier.withDefaultNamespace("red"));
        var original = new ColoringCatalystSyncPayload(
                fluids, Map.of(Identifier.parse("test:block"), Identifier.withDefaultNamespace("green")));
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());

        ColoringCatalystSyncPayload.STREAM_CODEC.encode(buffer, original);
        ColoringCatalystSyncPayload decoded = ColoringCatalystSyncPayload.STREAM_CODEC.decode(buffer);

        assertEquals(original, decoded);
        buffer.release();
    }
}
