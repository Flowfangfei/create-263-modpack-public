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

package plus.dragons.createdragonsplus.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

class StressConfigTest {
    private static final String MOD_ID = "stress_test";

    @Test
    void registersIdentifierDefaultsFluently() {
        StressConfig config = new StressConfig(MOD_ID);
        assertSame(config, config.setImpact(id("consumer"), 4));
        assertSame(config, config.setCapacity(id("source"), 16));
        assertSame(config, config.setNoImpact(id("free")));
        assertEquals("stressValues.v1", config.getName());
    }

    @Test
    void rejectsDuplicateEntries() {
        StressConfig config = new StressConfig(MOD_ID);
        config.setImpact(id("consumer"), 4);
        assertThrows(IllegalArgumentException.class, () -> config.setImpact(id("consumer"), 8));
    }

    @Test
    void rejectsForeignNamespacesAndInvalidValues() {
        StressConfig config = new StressConfig(MOD_ID);
        assertThrows(
                IllegalArgumentException.class,
                () -> config.setImpact(Identifier.fromNamespaceAndPath("other", "consumer"), 4));
        assertThrows(IllegalArgumentException.class, () -> config.setCapacity(id("source"), -1));
        assertThrows(IllegalArgumentException.class, () -> config.setImpact(id("nan"), Double.NaN));
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
