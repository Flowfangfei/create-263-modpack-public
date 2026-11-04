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

package plus.dragons.createdragonsplus.common.behaviours;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createdragonsplus.common.registry.CDPCapabilities;

/**
 * Convenience base class that hosts Create behaviours for a wrapped, non-Create block entity.
 * Register the wrapper with {@link CDPCapabilities#BEHAVIOUR_PROVIDER}.
 *
 * @param <T> wrapped block entity type
 */
public abstract class SmartBlockEntityBehaviourProvider<T extends BlockEntity> extends SmartBlockEntity
        implements BehaviourProvider {
    protected final T blockEntity;

    protected SmartBlockEntityBehaviourProvider(T blockEntity) {
        super(blockEntity.getType(), blockEntity.getBlockPos(), blockEntity.getBlockState());
        this.blockEntity = blockEntity;
    }

    @Override
    public @Nullable Level getLevel() {
        return blockEntity.getLevel();
    }

    @Override
    public void setLevel(Level level) {
        blockEntity.setLevel(level);
    }
}
