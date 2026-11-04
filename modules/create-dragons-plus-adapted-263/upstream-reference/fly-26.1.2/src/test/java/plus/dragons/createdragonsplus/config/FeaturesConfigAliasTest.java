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

import static org.junit.jupiter.api.Assertions.assertSame;

import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

class FeaturesConfigAliasTest {
    @Test
    void keepsCanonicalFeatureKeysAndLegacyAliases() {
        CDPFeaturesConfig config = new CDPFeaturesConfig();

        assertCanonical("fluid/dragon_breath/dripstone_duplication", config.dragonBreathFluidDripstoneDuplication);
        assertCanonical(
                "fluid/dye/lava_interaction_generate_colored_concrete",
                config.dyeFluidsLavaInteractionGenerateColoredConcrete);
        assertCanonical("fluid/air_current_block_interaction", config.airCurrentBlockInteraction);
        assertCanonical(
                "recipe/automatic_brewing/dragon_breath",
                config.generateAutomaticBrewingRecipeForDragonBreathFluid);
        assertCanonical(
                "recipe/sand_paper_polishing/polished_blocks",
                config.generateSandPaperPolishingRecipeForPolishedBlocks);
        assertCanonical(
                "recipe/sand_paper_polishing/oxidized_blocks",
                config.generateSandPaperPolishingRecipeForOxidizedBlocks);
        assertCanonical(
                "recipe/sand_paper_polishing/waxed_blocks",
                config.generateSandPaperPolishingRecipeForWaxedBlocks);
        assertAlias("fluid/dye", "dye_fluids", config.dyeFluids);
        assertAlias("fluid/dragon_breath", "dragons_breath_fluid", config.dragonBreathFluid);
        assertAlias("block/fluid_hatch", "fluid_hatch", config.fluidHatch);
        assertAlias(
                "item/blaze_upgrade_smithing_template",
                "blaze_upgrade_smithing_template",
                config.blazeUpgradeSmithingTemplate);
    }

    private static void assertCanonical(String name, FeaturesConfig.ConfigFeature expected) {
        assertSame(expected, FeaturesConfig.getFeatures().get(id(name)));
    }

    private static void assertAlias(
            String canonical, String alias, FeaturesConfig.ConfigFeature expected) {
        assertSame(expected, FeaturesConfig.getFeatures().get(id(canonical)));
        assertSame(expected, FeaturesConfig.getFeatures().get(id(alias)));
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("create_dragons_plus", path);
    }
}
