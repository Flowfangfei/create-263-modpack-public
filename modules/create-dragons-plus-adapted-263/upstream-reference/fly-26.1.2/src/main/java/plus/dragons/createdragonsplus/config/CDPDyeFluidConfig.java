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

import com.zurrtum.create.catnip.config.ConfigBase;

public final class CDPDyeFluidConfig extends ConfigBase {
    public final ConfigBool dyeFluidBlockContactColorsItems = b(true, "dyeFluidBlockContactColorsItems", "Color item entities touching a dye-fluid source");
    public final ConfigBool dyeFluidBlockContactColorsLivingEntities = b(
            true, "dyeFluidBlockContactColorsLivingEntities", "Color living entities touching a dye-fluid source");
    public final ConfigBool dyeFluidBlockContactConsumesSource = b(false, "dyeFluidBlockContactConsumesSource", "Consume a dye-fluid source after a successful coloring");
    public final ConfigBool dyeFluidOpenPipeColorsItems = b(true, "dyeFluidOpenPipeColorsItems", "Color item entities sprayed by an open pipe");
    public final ConfigBool dyeFluidOpenPipeColorsLivingEntities = b(true, "dyeFluidOpenPipeColorsLivingEntities", "Color living entities sprayed by an open pipe");

    @Override
    public String getName() {
        return "dyeFluid";
    }
}
