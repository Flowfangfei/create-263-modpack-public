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

package plus.dragons.createdragonsplus.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;

public final class VaporizingBucketItem extends BucketItem {
    private final boolean dragonBreath;

    public VaporizingBucketItem(Fluid fluid, boolean dragonBreath, Properties properties) {
        super(fluid, properties);
        this.dragonBreath = dragonBreath;
    }

    @Override
    public boolean emptyContents(LivingEntity owner, Level level, BlockPos pos, BlockHitResult hitResult) {
        if (!level.environmentAttributes().getValue(EnvironmentAttributes.WATER_EVAPORATES, pos)) {
            return super.emptyContents(owner, level, pos, hitResult);
        }
        if (dragonBreath) {
            DragonBreathVaporization.vaporize(level, pos, owner, Math.toIntExact(FluidUnits.BUCKET));
        } else {
            DyeFluidVaporization.vaporize(level, pos);
        }
        return true;
    }
}
