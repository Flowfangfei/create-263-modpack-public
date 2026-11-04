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

package plus.dragons.createdragonsplus.data.map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.gson.JsonParser;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import plus.dragons.createdragonsplus.fluid.dye.DyeVariantRegistry;

class CDPColoringCatalystsTest {
    private static final Identifier RED = Identifier.withDefaultNamespace("red");
    private static final Identifier BLUE = Identifier.withDefaultNamespace("blue");
    private static final Identifier GREEN = Identifier.withDefaultNamespace("green");

    @BeforeAll
    static void bootstrapDyeVariants() {
        DyeVariantRegistry.bootstrap();
    }

    @Test
    void higherPriorityLayersOverrideMatchingSelectors() {
        var low = new CDPColoringCatalysts.Layer(
                false, ordered(Map.entry("minecraft:water", RED), Map.entry("#c:water", BLUE)));
        var high = new CDPColoringCatalysts.Layer(
                false, ordered(Map.entry("minecraft:water", GREEN)));

        Map<String, Identifier> merged = CDPColoringCatalysts.mergeLayers(List.of(low, high)).values();
        assertEquals(
                ordered(Map.entry("#c:water", BLUE), Map.entry("minecraft:water", GREEN)),
                merged);
        assertEquals(List.of("#c:water", "minecraft:water"), List.copyOf(merged.keySet()));
    }

    @Test
    void replaceClearsAllLowerPriorityValues() {
        var low = new CDPColoringCatalysts.Layer(false, Map.of("minecraft:water", RED));
        var replacement = new CDPColoringCatalysts.Layer(true, Map.of("minecraft:lava", BLUE));

        assertEquals(
                Map.of("minecraft:lava", BLUE),
                CDPColoringCatalysts.mergeLayers(List.of(low, replacement)).values());
    }

    @Test
    void parsesDirectTagAndConditionalObjectValues() {
        var root = JsonParser.parseString("""
                {
                  "replace": true,
                  "values": {
                    "minecraft:water": "minecraft:red",
                    "#c:water": { "value": "minecraft:blue" },
                    "not an id": "minecraft:green",
                    "minecraft:lava": "test:unknown_dye"
                  }
                }
                """).getAsJsonObject();

        CDPColoringCatalysts.Layer parsed = CDPColoringCatalysts.parseLayer(root, "unit test");

        assertNotNull(parsed);
        assertEquals(true, parsed.replace());
        assertEquals(
                ordered(Map.entry("minecraft:water", RED), Map.entry("#c:water", BLUE)),
                parsed.values());
    }

    @SafeVarargs
    private static Map<String, Identifier> ordered(Map.Entry<String, Identifier>... entries) {
        LinkedHashMap<String, Identifier> result = new LinkedHashMap<>();
        for (var entry : entries) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
