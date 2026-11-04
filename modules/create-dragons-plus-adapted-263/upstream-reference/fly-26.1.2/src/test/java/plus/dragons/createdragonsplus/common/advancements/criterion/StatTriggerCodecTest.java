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

package plus.dragons.createdragonsplus.common.advancements.criterion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mojang.serialization.JsonOps;
import net.minecraft.SharedConstants;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.server.Bootstrap;
import net.minecraft.stats.Stats;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class StatTriggerCodecTest {
    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void roundTripsCustomStatAndBounds() {
        StatTrigger.Instance expected = new StatTrigger.Instance(Stats.CUSTOM.get(Stats.JUMP), MinMaxBounds.Ints.atLeast(3));
        var encoded = StatTrigger.Instance.CODEC
                .encodeStart(JsonOps.INSTANCE, expected)
                .getOrThrow();
        StatTrigger.Instance decoded = StatTrigger.Instance.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();

        assertEquals(expected.stat(), decoded.stat());
        assertEquals(expected.bounds(), decoded.bounds());
    }
}
