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

package plus.dragons.createdragonsplus.client.ponder;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.ponder.api.registration.PonderSceneRegistrationHelper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import plus.dragons.createdragonsplus.client.ponder.scenes.CDPPonderScenesImpl;
import plus.dragons.createdragonsplus.registry.CDPFluids;

public final class CDPPonderScenes {
    private CDPPonderScenes() {}

    public static void register(PonderSceneRegistrationHelper<Identifier> helper) {
        PonderSceneRegistrationHelper<Item> registration = helper.withKeyFunction(BuiltInRegistries.ITEM::getKey);
        registration.forComponents(AllItems.ENCASED_FAN)
                .addStoryBoard("bulk_coloring", CDPPonderScenesImpl::bulkColoring)
                .addStoryBoard("bulk_freezing", CDPPonderScenesImpl::bulkFreezing)
                .addStoryBoard("bulk_sanding", CDPPonderScenesImpl::bulkSanding)
                .addStoryBoard("bulk_ending", CDPPonderScenesImpl::bulkEnding);

        List<Item> dyeBuckets = CDPFluids.DYES_BY_VARIANT.values().stream()
                .map(fluid -> (Item) fluid.getBucket())
                .toList();
        registration.forComponents(dyeBuckets).addStoryBoard("dye_fluids", CDPPonderScenesImpl::dyeFluids);

        List<Item> dragonBreathComponents = new ArrayList<>();
        dragonBreathComponents.add(CDPFluids.DRAGON_BREATH.getBucket());
        dragonBreathComponents.add(Items.DRAGON_BREATH);
        registration.forComponents(dragonBreathComponents)
                .addStoryBoard("dragon_breath_fluid", CDPPonderScenesImpl::dragonBreathFluid);
    }
}
