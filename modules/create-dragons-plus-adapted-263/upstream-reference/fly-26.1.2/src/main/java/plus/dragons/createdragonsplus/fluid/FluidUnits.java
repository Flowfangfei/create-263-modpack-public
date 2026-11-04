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

public final class FluidUnits {
    public static final long BUCKET = 81_000L;
    public static final long QUARTER_BUCKET = BUCKET / 4L;
    public static final long AUTOMATIC_COLORING = ceilDiv(QUARTER_BUCKET, 8L);

    private FluidUnits() {}

    public static long fromMilliBuckets(long milliBuckets) {
        if (milliBuckets < 0) {
            throw new IllegalArgumentException("Fluid amount cannot be negative: " + milliBuckets);
        }
        return ceilDiv(Math.multiplyExact(milliBuckets, BUCKET), 1_000L);
    }

    private static long ceilDiv(long dividend, long divisor) {
        return dividend == 0 ? 0 : 1 + (dividend - 1) / divisor;
    }
}
