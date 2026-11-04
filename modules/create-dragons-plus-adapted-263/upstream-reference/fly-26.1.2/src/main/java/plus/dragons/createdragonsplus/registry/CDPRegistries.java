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

import plus.dragons.createdragonsplus.common.registry.CDPCriterions;
import plus.dragons.createdragonsplus.data.condition.ConfigFeatureResourceCondition;
import plus.dragons.createdragonsplus.data.map.CDPColoringCatalysts;
import plus.dragons.createdragonsplus.data.recipe.CDPRuntimeRecipes;
import plus.dragons.createdragonsplus.fluid.CDPCauldrons;
import plus.dragons.createdragonsplus.fluid.CDPOpenPipeEffects;
import plus.dragons.createdragonsplus.network.CDPNetworking;

public final class CDPRegistries {
    private static boolean registered;

    private CDPRegistries() {}

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        ConfigFeatureResourceCondition.register();
        CDPBlockEntities.register();
        CDPItems.register();
        CDPLoots.register();
        CDPCauldrons.register();
        CDPOpenPipeEffects.register();
        plus.dragons.createdragonsplus.fluid.hatch.FluidHatchTransfer.register();
        CDPCreativeModeTabs.register();
        CDPRecipes.register();
        CDPFanProcessingTypes.register();
        CDPItemAttributes.register();
        CDPCriterions.register();
        CDPColoringCatalysts.register();
        CDPRuntimeRecipes.register();
        CDPNetworking.register();
    }
}
