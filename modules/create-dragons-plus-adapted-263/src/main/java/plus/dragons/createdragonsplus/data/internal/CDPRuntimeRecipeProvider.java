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

package plus.dragons.createdragonsplus.data.internal;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import java.util.concurrent.CompletableFuture;
import net.minecraft.advancements.Advancement.Builder;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.CachedOutput;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.DataMapHooks;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createdragonsplus.common.CDPCommon;
import plus.dragons.createdragonsplus.common.recipe.UpdateRecipesEvent;
import plus.dragons.createdragonsplus.common.registry.CDPBlocks;
import plus.dragons.createdragonsplus.config.CDPConfig;
import plus.dragons.createdragonsplus.data.recipe.CreateRecipeBuilders;

@EventBusSubscriber
public class CDPRuntimeRecipeProvider implements DataProvider {
    private final PackOutput packOutput;
    private final CompletableFuture<Provider> registries;
    public CDPRuntimeRecipeProvider(PackOutput output, CompletableFuture<Provider> registries) {
        this.packOutput = output; this.registries = registries;
    }
    @Override public CompletableFuture<?> run(CachedOutput cache) {
        return registries.thenCompose(lookup -> {
            List<CompletableFuture<?>> writes = new ArrayList<>();
            var paths = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
            var output = output(lookup, (id, recipe) -> {
                var json = Recipe.DIRECT_CODEC.encodeStart(lookup.createSerializationContext(JsonOps.INSTANCE), recipe).getOrThrow();
                writes.add(DataProvider.saveStable(cache, json, paths.json(id.identifier())));
            });
            if (CDPConfig.features().generateSandPaperPolishingRecipeForPolishedBlocks.get()) buildPolishedBlockRecipes(output);
            return CompletableFuture.allOf(writes.toArray(CompletableFuture[]::new));
        });
    }
    @Override public String getName() { return "Dragons Plus runtime polishing recipes"; }
    private static RecipeOutput output(Provider registries, BiConsumer<ResourceKey<Recipe<?>>, Recipe<?>> receiver) {
        return new RecipeOutput() {
            @Override public Builder advancement() { return Builder.advancement(); }
            @Override public void accept(ResourceKey<Recipe<?>> id, Recipe<?> recipe, AdvancementHolder advancement, ICondition... conditions) { receiver.accept(id, recipe); }
            @Override public <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> key) { return registries.lookupOrThrow(key); }
            @Override public <S> Stream<Holder.Reference<S>> listContextElements(ResourceKey<? extends Registry<? extends S>> key) { return registries.lookupOrThrow(key).listElements(); }
        };
    }
    private static void buildPolishedBlockRecipes(RecipeOutput output) {
        BuiltInRegistries.BLOCK.listElements()
                .filter(holder -> holder.key().identifier().getPath().contains("polished_"))
                .forEach(holder -> {
                    var polishedId = holder.key().identifier();
                    var baseId = polishedId.withPath(name -> name.replace("polished_", ""));
                    if (!BuiltInRegistries.BLOCK.containsKey(baseId))
                        return;
                    var polishedItem = holder.value().asItem();
                    var baseBlock = BuiltInRegistries.BLOCK.get(baseId);
                    if (baseBlock.isEmpty())
                        return;
                    var baseItem = baseBlock.get().value().asItem();
                    if (polishedItem == Items.AIR || baseItem == Items.AIR)
                        return;
                    CreateRecipeBuilders.polishing(automaticPolishingRecipeId(baseId))
                            .require(baseItem)
                            // The pack is generated before default item components are bound.
                            .output(new ProcessingOutput(polishedItem, 1, 1.0F))
                            .build(output);
                });
    }

    private static Identifier automaticPolishingRecipeId(Identifier baseId) {
        return CDPCommon.asResource(baseId.toString().replace(':', '/'));
    }

    private static void removeNotApplicablePolishedBlockRecipes(UpdateRecipesEvent event) {
        BuiltInRegistries.BLOCK.listElements()
                .filter(holder -> holder.key().identifier().getPath().contains("polished_"))
                .forEach(holder -> {
                    var polishedId = holder.key().identifier();
                    var baseId = polishedId.withPath(name -> name.replace("polished_", ""));
                    var baseBlock = BuiltInRegistries.BLOCK.get(baseId);
                    if (baseBlock.isEmpty() || !baseBlock.get().is(CDPBlocks.MOD_TAGS.notApplicablePolishing))
                        return;
                    var polishedItem = holder.value().asItem();
                    var baseItem = baseBlock.get().value().asItem();
                    if (polishedItem == Items.AIR || baseItem == Items.AIR)
                        return;
                    event.getRecipe(automaticPolishingRecipeId(baseId))
                            .filter(recipe -> recipe.value().getType() == AllRecipeTypes.SANDPAPER_POLISHING.getType())
                            .ifPresent(event::removeRecipe);
                });
    }

    private static void buildOxidizedBlockRecipes(RecipeOutput output) {
        DataMapHooks.INVERSE_OXIDIZABLES_DATAMAP.forEach((oxidized, polished) -> {
            var oxidizedItem = oxidized.asItem();
            var polishedItem = polished.asItem();
            if (oxidizedItem == Items.AIR || polishedItem == Items.AIR)
                return;
            var oxidizedId = BuiltInRegistries.BLOCK.getKey(oxidized);
            var recipeId = CDPCommon.asResource(oxidizedId.toString().replace(':', '/'));
            CreateRecipeBuilders.polishing(recipeId)
                    .require(oxidizedItem)
                    .output(new ProcessingOutput(polishedItem, 1, 1.0F))
                    .build(output);
        });
    }

    private static void buildWaxedBlockRecipes(RecipeOutput output) {
        DataMapHooks.INVERSE_WAXABLES_DATAMAP.forEach((waxed, polished) -> {
            var waxedItem = waxed.asItem();
            var polishedItem = polished.asItem();
            if (waxedItem == Items.AIR || polishedItem == Items.AIR)
                return;
            var waxedId = BuiltInRegistries.BLOCK.getKey(waxed);
            var recipeId = CDPCommon.asResource(waxedId.toString().replace(':', '/'));
            CreateRecipeBuilders.polishing(recipeId)
                    .require(waxedItem)
                    .output(new ProcessingOutput(polishedItem, 1, 1.0F))
                    .build(output);
        });
    }

    @SubscribeEvent
    public static void buildRecipesForUpdate(final UpdateRecipesEvent event) {
        if (CDPConfig.features().generateSandPaperPolishingRecipeForPolishedBlocks.get()) {
            removeNotApplicablePolishedBlockRecipes(event);
        }
        final RecipeOutput output = output(net.minecraft.server.RegistryLayer.createRegistryAccess().compositeAccess(),
            (id, recipe) -> event.addRecipe(new RecipeHolder<>(id, recipe)));

        if (CDPConfig.features().generateSandPaperPolishingRecipeForOxidizedBlocks.get()) {
            buildOxidizedBlockRecipes(output);
        }
        if (CDPConfig.features().generateSandPaperPolishingRecipeForWaxedBlocks.get()) {
            buildWaxedBlockRecipes(output);
        }
    }
}
