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

package plus.dragons.createdragonsplus.data.recipe;

import com.zurrtum.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import plus.dragons.createdragonsplus.CreateDragonsPlus;
import plus.dragons.createdragonsplus.common.recipe.UpdateRecipesEvent;
import plus.dragons.createdragonsplus.config.CDPConfig;
import plus.dragons.createdragonsplus.registry.CDPTags;

public final class CDPRuntimeRecipes {
    private static boolean registered;

    private CDPRuntimeRecipes() {}

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        UpdateRecipesEvent.EVENT.register(CDPRuntimeRecipes::update);
    }

    private static void update(UpdateRecipesEvent event) {
        if (CDPConfig.features().generateSandPaperPolishingRecipeForPolishedBlocks.get()) {
            BuiltInRegistries.BLOCK.entrySet().stream()
                    .filter(entry -> entry.getKey().identifier().getPath().contains("polished_"))
                    .forEach(entry -> {
                        Identifier polishedId = entry.getKey().identifier();
                        Identifier baseId = polishedId.withPath(path -> path.replace("polished_", ""));
                        var baseHolder = BuiltInRegistries.BLOCK.get(baseId);
                        if (baseHolder.isEmpty() || baseHolder.get().is(CDPTags.NOT_APPLICABLE_POLISHING)) {
                            return;
                        }
                        addPolishing(event, baseHolder.get().value(), entry.getValue());
                    });
        }
        if (CDPConfig.features().generateSandPaperPolishingRecipeForOxidizedBlocks.get()) {
            WeatheringCopper.PREVIOUS_BY_BLOCK.get().forEach((oxidized, previous) -> addPolishing(event, oxidized, previous));
        }
        if (CDPConfig.features().generateSandPaperPolishingRecipeForWaxedBlocks.get()) {
            HoneycombItem.WAX_OFF_BY_BLOCK.get().forEach((waxed, unwaxed) -> addPolishing(event, waxed, unwaxed));
        }
    }

    private static void addPolishing(UpdateRecipesEvent event, Block input, Block output) {
        if (input.asItem() == Items.AIR || output.asItem() == Items.AIR) {
            return;
        }
        Identifier inputId = BuiltInRegistries.BLOCK.getKey(input);
        Identifier id = CreateDragonsPlus.id(inputId.getNamespace() + "/" + inputId.getPath());
        ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, id);
        SandPaperPolishingRecipe recipe = new SandPaperPolishingRecipe(new ItemStackTemplate(output.asItem()), Ingredient.of(input.asItem()));
        event.addRecipe(new RecipeHolder<>(key, recipe));
    }
}
