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

package plus.dragons.createdragonsplus.common.recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createdragonsplus.api.registry.CDPRegistryEntry;

/**
 * Loader-neutral base for downstream recipe builders.
 *
 * <p>Conditions are attached to the recipe object through Fabric's datagen condition carrier and are emitted as
 * {@code fabric:load_conditions} by {@code FabricRecipeProvider}.
 */
public abstract class BaseRecipeBuilder<R extends Recipe<?>, B extends BaseRecipeBuilder<R, ?>>
        implements Consumer<RecipeOutput> {
    protected final @Nullable String directory;
    protected final List<ResourceCondition> conditions = new ArrayList<>();
    protected @Nullable Identifier id;

    protected BaseRecipeBuilder(@Nullable String directory) {
        this.directory = directory;
    }

    protected abstract B builder();

    public abstract RecipeHolder<R> build();

    public @Nullable AdvancementHolder buildAdvancement() {
        return null;
    }

    public @Nullable String getDirectory() {
        return directory;
    }

    public @Nullable Identifier getId() {
        return id;
    }

    public List<ResourceCondition> getConditions() {
        return List.copyOf(conditions);
    }

    @Override
    public final void accept(RecipeOutput output) {
        RecipeHolder<R> holder = build();
        Identifier identifier = holder.id().identifier();
        if (directory != null) {
            identifier = identifier.withPrefix(directory + "/");
        }
        ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, identifier);
        if (!conditions.isEmpty()) {
            FabricDataGenHelper.addConditions(holder.value(), conditions.toArray(ResourceCondition[]::new));
        }
        output.accept(key, holder.value(), buildAdvancement());
    }

    public B withId(Identifier id) {
        this.id = id;
        return builder();
    }

    public B withCondition(ResourceCondition condition) {
        conditions.add(condition);
        return builder();
    }

    public final B withoutCondition(ResourceCondition condition) {
        return withCondition(ResourceConditions.not(condition));
    }

    public final B withAllCondition(ResourceCondition... conditions) {
        Collections.addAll(this.conditions, conditions);
        return builder();
    }

    public final B withAnyCondition(ResourceCondition... conditions) {
        return withCondition(ResourceConditions.or(conditions));
    }

    public final B withMod(String mod) {
        return withCondition(ResourceConditions.allModsLoaded(mod));
    }

    public final B withoutMod(String mod) {
        return withoutCondition(ResourceConditions.allModsLoaded(mod));
    }

    public final B withItem(Identifier location) {
        return withCondition(ResourceConditions.registryContains(ResourceKey.create(Registries.ITEM, location)));
    }

    public final B withItem(CDPRegistryEntry<? extends Item> item) {
        return withItem(item.getId());
    }

    public final B withoutItem(Identifier location) {
        return withoutCondition(ResourceConditions.registryContains(ResourceKey.create(Registries.ITEM, location)));
    }

    public final B withoutItem(CDPRegistryEntry<? extends Item> item) {
        return withoutItem(item.getId());
    }

    public final B withTag(TagKey<Item> tag) {
        return withCondition(ResourceConditions.tagsPopulated(tag));
    }

    public final B withoutTag(TagKey<Item> tag) {
        return withoutCondition(ResourceConditions.tagsPopulated(tag));
    }
}
