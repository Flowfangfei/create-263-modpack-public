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

import plus.dragons.createdragonsplus.CreateDragonsPlus;

public final class CDPFeaturesConfig extends FeaturesConfig {
    private final ConfigGroup fluid = group(1, "fluid", "Fluid feature elements");
    public final ConfigFeature dyeFluids = feature(true, "fluid/dye", "Enable dye fluids").addAlias("dye_fluids");
    public final ConfigFeature dragonBreathFluid = feature(true, "fluid/dragon_breath", "Enable Dragon's Breath fluid")
            .addAlias("dragons_breath_fluid");
    public final ConfigFeature dragonBreathFluidDripstoneDuplication = feature(
            true,
            "fluid/dragon_breath/dripstone_duplication",
            "Allow Dragon's Breath fluid to fill cauldrons through pointed dripstone");
    public final ConfigFeature dyeFluidsLavaInteractionGenerateColoredConcrete = feature(
            true,
            "fluid/dye/lava_interaction_generate_colored_concrete",
            "Create colored concrete when dye fluid meets lava");
    public final ConfigFeature airCurrentBlockInteraction = feature(
            true, "fluid/air_current_block_interaction", "Allow air currents to interact through fluids");
    private final ConfigGroup block = group(1, "block", "Block feature elements");
    public final ConfigFeature fluidHatch = feature(true, "block/fluid_hatch", "Enable Fluid Hatch").addAlias("fluid_hatch");
    private final ConfigGroup item = group(1, "item", "Item feature elements");
    public final ConfigFeature blazeUpgradeSmithingTemplate = feature(
            false, "item/blaze_upgrade_smithing_template", "Enable Blaze Upgrade Smithing Template")
                    .addAlias("blaze_upgrade_smithing_template");
    private final ConfigGroup recipe = group(1, "recipe", "Generated recipe feature elements");
    public final ConfigFeature generateAutomaticBrewingRecipeForDragonBreathFluid = feature(
            true, "recipe/automatic_brewing/dragon_breath", "Generate automated Dragon's Breath brewing recipes");
    public final ConfigFeature generateSandPaperPolishingRecipeForPolishedBlocks = feature(
            true, "recipe/sand_paper_polishing/polished_blocks", "Generate polishing recipes for polished blocks");
    public final ConfigFeature generateSandPaperPolishingRecipeForOxidizedBlocks = feature(
            true, "recipe/sand_paper_polishing/oxidized_blocks", "Generate polishing recipes for oxidized blocks");
    public final ConfigFeature generateSandPaperPolishingRecipeForWaxedBlocks = feature(
            true, "recipe/sand_paper_polishing/waxed_blocks", "Generate polishing recipes for waxed blocks");

    public CDPFeaturesConfig() {
        super(CreateDragonsPlus.ID);
    }
}
