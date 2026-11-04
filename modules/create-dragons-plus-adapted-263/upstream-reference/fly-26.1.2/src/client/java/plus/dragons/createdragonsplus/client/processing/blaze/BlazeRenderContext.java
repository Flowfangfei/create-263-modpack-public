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

package plus.dragons.createdragonsplus.client.processing.blaze;

import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createdragonsplus.common.processing.blaze.BlazeBlockEntity;

/** Rendering inputs shared by stationary, Flywheel, and contraption Blaze renderers. */
public record BlazeRenderContext(
        BlockState state,
        HeatLevel heatLevel,
        @Nullable BlazeBlockEntity blockEntity,
        @Nullable CompoundTag blockEntityData) {
    public boolean moving() {
        return blockEntity == null;
    }
}
