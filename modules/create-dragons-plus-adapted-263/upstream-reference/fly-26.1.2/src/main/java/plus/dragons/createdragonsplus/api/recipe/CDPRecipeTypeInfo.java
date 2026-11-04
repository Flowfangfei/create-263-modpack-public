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

package plus.dragons.createdragonsplus.api.recipe;

import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public final class CDPRecipeTypeInfo<R extends Recipe<?>> {
    private final Identifier id;
    private final Supplier<? extends RecipeType<R>> type;
    private final Supplier<? extends RecipeSerializer<R>> serializer;

    public CDPRecipeTypeInfo(
            Identifier id,
            Supplier<? extends RecipeType<R>> type,
            Supplier<? extends RecipeSerializer<R>> serializer) {
        this.id = id;
        this.type = type;
        this.serializer = serializer;
    }

    public Identifier getId() {
        return id;
    }

    public RecipeType<R> getType() {
        return Objects.requireNonNull(type.get(), () -> "Recipe type is not registered yet: " + id);
    }

    public RecipeSerializer<R> getSerializer() {
        return Objects.requireNonNull(serializer.get(), () -> "Recipe serializer is not registered yet: " + id);
    }
}
