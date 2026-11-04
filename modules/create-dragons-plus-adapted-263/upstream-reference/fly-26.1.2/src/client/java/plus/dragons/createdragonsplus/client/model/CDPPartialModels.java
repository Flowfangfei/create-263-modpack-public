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

package plus.dragons.createdragonsplus.client.model;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import net.minecraft.resources.Identifier;
import plus.dragons.createdragonsplus.CreateDragonsPlus;

public final class CDPPartialModels {
    private CDPPartialModels() {}

    public static void register() {
        registerRarePackage(CreateDragonsPlus.id("rare_blaze_package"), 12, 10);
        registerRarePackage(CreateDragonsPlus.id("rare_marble_gate_package"), 12, 10);
    }

    private static void registerRarePackage(Identifier id, int width, int height) {
        AllPartialModels.PACKAGES.put(id, PartialModel.of(id.withPrefix("item/")));
        AllPartialModels.PACKAGE_RIGGING.put(
                id, PartialModel.of(Create.asResource("item/package/rigging_" + width + "x" + height)));
    }
}
