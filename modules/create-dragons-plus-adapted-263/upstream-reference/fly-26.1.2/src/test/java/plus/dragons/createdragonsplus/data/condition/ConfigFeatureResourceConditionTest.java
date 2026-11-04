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

package plus.dragons.createdragonsplus.data.condition;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mojang.serialization.JsonOps;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

class ConfigFeatureResourceConditionTest {
    @Test
    void codecPreservesNamespacedFeatureId() {
        var expected = new ConfigFeatureResourceCondition(
                Identifier.parse("create_dragons_plus:fluid/dragon_breath"));
        var json = ConfigFeatureResourceCondition.CODEC
                .codec()
                .encodeStart(JsonOps.INSTANCE, expected)
                .getOrThrow();
        var decoded = ConfigFeatureResourceCondition.CODEC
                .codec()
                .parse(JsonOps.INSTANCE, json)
                .getOrThrow();

        assertEquals(expected, decoded);
    }
}
