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
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FlowingFluid;

/** Compatibility facade for a source/flowing fluid pair and its bucket/block. */
public final class CDPFluidEntry<T extends FlowingFluid> {
    private final Identifier id;
    private final T source;
    private final T flowing;
    private final Supplier<? extends BucketItem> bucket;
    private final Supplier<? extends LiquidBlock> block;

    public CDPFluidEntry(
            Identifier id,
            T source,
            T flowing,
            Supplier<? extends BucketItem> bucket,
            Supplier<? extends LiquidBlock> block) {
        this.id = Objects.requireNonNull(id, "id");
        this.source = Objects.requireNonNull(source, "source");
        this.flowing = Objects.requireNonNull(flowing, "flowing");
        this.bucket = Objects.requireNonNull(bucket, "bucket");
        this.block = Objects.requireNonNull(block, "block");
    }

    public T get() {
        return flowing;
    }

    public Identifier getId() {
        return id;
    }

    public T getSource() {
        return source;
    }

    public T getFlowing() {
        return flowing;
    }

    public BucketItem getBucket() {
        return Objects.requireNonNull(bucket.get(), () -> "Bucket is not registered yet: " + id);
    }

    public LiquidBlock getBlock() {
        return Objects.requireNonNull(block.get(), () -> "Fluid block is not registered yet: " + id);
    }
}
