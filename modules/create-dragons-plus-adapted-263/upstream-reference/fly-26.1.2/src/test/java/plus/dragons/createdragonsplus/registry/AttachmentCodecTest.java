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

package plus.dragons.createdragonsplus.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mojang.serialization.JsonOps;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

class AttachmentCodecTest {
    @Test
    void coloringProgressRoundTrips() {
        var expected = new CDPAttachments.ColoringProgress(Identifier.parse("minecraft:red"), 42, 7);
        var encoded = CDPAttachments.ColoringProgress.CODEC
                .encodeStart(JsonOps.INSTANCE, expected)
                .getOrThrow();

        assertEquals(
                expected,
                CDPAttachments.ColoringProgress.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow());
    }

    @Test
    void contactColoringRoundTrips() {
        var expected = new CDPAttachments.ContactColoring(Identifier.parse("minecraft:blue"), 99);
        var encoded = CDPAttachments.ContactColoring.CODEC
                .encodeStart(JsonOps.INSTANCE, expected)
                .getOrThrow();

        assertEquals(
                expected,
                CDPAttachments.ContactColoring.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow());
    }
}
