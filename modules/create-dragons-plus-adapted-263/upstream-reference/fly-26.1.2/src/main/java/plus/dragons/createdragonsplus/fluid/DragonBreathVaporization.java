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
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.PowerParticleOption;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import plus.dragons.createdragonsplus.registry.CDPAttachments;

public final class DragonBreathVaporization {
    private DragonBreathVaporization() {}

    public static void vaporize(Level level, BlockPos pos, LivingEntity owner, int amount) {
        level.playSound(
                null,
                pos,
                SoundEvents.FIRE_EXTINGUISH,
                SoundSource.BLOCKS,
                0.5F,
                2.6F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.8F);
        level.levelEvent(2006, pos, -1);
        if (level.isClientSide()) {
            return;
        }
        float fraction = Math.max(0.01F, Math.min(1.0F, amount / (float) FluidUnits.BUCKET));
        AreaEffectCloud cloud = new AreaEffectCloud(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        cloud.setOwner(owner);
        cloud.setCustomParticle(PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, 1.0F));
        cloud.setRadius(2.0F * fraction);
        cloud.setDuration(Math.max(2, Math.round(200 * fraction)));
        cloud.setRadiusPerTick(-0.01F);
        cloud.addEffect(new MobEffectInstance(MobEffects.INSTANT_DAMAGE, 1, 1));
        cloud.setAttached(CDPAttachments.DRAGON_BREATH_CLOUD, true);
        level.addFreshEntity(cloud);
    }
}
