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

package plus.dragons.createdragonsplus.api.registry;

import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.resources.Identifier;

/** A small stable handle used by the public API without exposing a registry builder implementation. */
public final class CDPRegistryEntry<T> implements Supplier<T> {
    private final Identifier id;
    private final Supplier<? extends T> value;

    public CDPRegistryEntry(Identifier id, Supplier<? extends T> value) {
        this.id = Objects.requireNonNull(id, "id");
        this.value = Objects.requireNonNull(value, "value");
    }

    @Override
    public T get() {
        return Objects.requireNonNull(value.get(), () -> "Registry entry is not available: " + id);
    }

    public Identifier getId() {
        return id;
    }

    public boolean is(T candidate) {
        return get() == candidate;
    }
}
