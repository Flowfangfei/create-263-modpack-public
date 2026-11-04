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

package plus.dragons.createdragonsplus.fluid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class FluidUnitsTest {
    @Test
    void usesFabricBucketScale() {
        assertEquals(81_000L, FluidUnits.BUCKET);
        assertEquals(20_250L, FluidUnits.QUARTER_BUCKET);
        assertEquals(2_532L, FluidUnits.AUTOMATIC_COLORING);
    }

    @Test
    void convertsMilliBucketsWithCeiling() {
        assertEquals(0L, FluidUnits.fromMilliBuckets(0));
        assertEquals(81L, FluidUnits.fromMilliBuckets(1));
        assertEquals(20_250L, FluidUnits.fromMilliBuckets(250));
        assertEquals(81_000L, FluidUnits.fromMilliBuckets(1_000));
        assertThrows(IllegalArgumentException.class, () -> FluidUnits.fromMilliBuckets(-1));
    }
}
