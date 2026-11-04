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

package plus.dragons.createdragonsplus.common.processing.blaze;

import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Server-safe base block entity for addon-owned Blaze machines. */
public abstract class BlazeBlockEntity extends SmartBlockEntity {
    public final LerpedFloat headAnimation = LerpedFloat.linear();
    public final LerpedFloat headAngle = LerpedFloat.angular();

    protected BlazeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public abstract boolean isActive();

    public abstract boolean isCreative();

    public abstract HeatLevel getHeatLevel();

    @Override
    public void tick() {
        super.tick();
        if (level == null) {
            return;
        }
        if (level.isClientSide()) {
            if (shouldTickAnimation()) {
                tickAnimation();
            }
            if (!isVirtual()) {
                spawnParticles(getHeatLevelFromBlock());
            }
            return;
        }
        if (!isCreative()) {
            updateBlockState();
        }
    }

    protected boolean shouldTickAnimation() {
        return BlazeClientBridge.INSTANCE.shouldTickAnimation(this);
    }

    protected void tickAnimation() {
        BlazeClientBridge.INSTANCE.tickAnimation(this);
    }

    public HeatLevel getHeatLevelFromBlock() {
        return BlazeBlock.getHeatLevelOf(getBlockState());
    }

    public HeatLevel getHeatLevelForRender() {
        HeatLevel heatLevel = getHeatLevelFromBlock();
        return heatLevel.isAtLeast(HeatLevel.FADING) ? heatLevel : HeatLevel.SMOULDERING;
    }

    public void updateBlockState() {
        setBlockHeat(getHeatLevel());
    }

    protected void onHeatChange(HeatLevel currentHeat, HeatLevel newHeat) {}

    protected void setBlockHeat(HeatLevel newHeat) {
        HeatLevel currentHeat = getHeatLevelFromBlock();
        if (currentHeat == newHeat || level == null) {
            return;
        }
        onHeatChange(currentHeat, newHeat);
        level.setBlockAndUpdate(worldPosition, getBlockState().setValue(BlazeBlock.HEAT_LEVEL, newHeat));
        notifyUpdate();
    }

    protected void playSound() {
        if (level == null) {
            return;
        }
        level.playSound(
                null,
                worldPosition,
                SoundEvents.BLAZE_SHOOT,
                SoundSource.BLOCKS,
                0.125F + level.getRandom().nextFloat() * 0.125F,
                0.75F - level.getRandom().nextFloat() * 0.25F);
    }

    protected void spawnParticles(HeatLevel heatLevel) {
        if (level == null || heatLevel == HeatLevel.NONE) {
            return;
        }
        RandomSource random = level.getRandom();
        Vec3 center = VecHelper.getCenterOf(worldPosition);
        Vec3 smokePos = center.add(VecHelper.offsetRandomly(Vec3.ZERO, random, 0.125F).multiply(1, 0, 1));
        if (random.nextInt(4) != 0) {
            return;
        }
        boolean empty = level.getBlockState(worldPosition.above())
                .getCollisionShape(level, worldPosition.above())
                .isEmpty();
        if (empty || random.nextInt(8) == 0) {
            level.addParticle(ParticleTypes.LARGE_SMOKE, smokePos.x, smokePos.y, smokePos.z, 0, 0, 0);
        }
        double yMotion = empty ? 0.0625F : random.nextDouble() * 0.0125F;
        Vec3 flamePos = center.add(VecHelper.offsetRandomly(Vec3.ZERO, random, 0.5F)
                .multiply(1, 0.25F, 1)
                .normalize()
                .scale((empty ? 0.25F : 0.5F) + random.nextDouble() * 0.125F))
                .add(0, 0.5, 0);
        if (heatLevel.isAtLeast(HeatLevel.SEETHING)) {
            level.addParticle(
                    ParticleTypes.SOUL_FIRE_FLAME, flamePos.x, flamePos.y, flamePos.z, 0, yMotion, 0);
        } else if (heatLevel.isAtLeast(HeatLevel.FADING)) {
            level.addParticle(ParticleTypes.FLAME, flamePos.x, flamePos.y, flamePos.z, 0, yMotion, 0);
        }
    }

    protected void spawnParticleBurst(boolean soul) {
        if (level == null) {
            return;
        }
        Vec3 center = VecHelper.getCenterOf(worldPosition);
        RandomSource random = level.getRandom();
        for (int i = 0; i < 20; i++) {
            Vec3 offset = VecHelper.offsetRandomly(Vec3.ZERO, random, 0.5F)
                    .multiply(1, 0.25F, 1)
                    .normalize();
            Vec3 pos = center.add(offset.scale(0.5 + random.nextDouble() * 0.125F)).add(0, 0.125, 0);
            Vec3 motion = offset.scale(1 / 32F);
            level.addParticle(
                    soul ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME,
                    pos.x,
                    pos.y,
                    pos.z,
                    motion.x,
                    motion.y,
                    motion.z);
        }
    }
}
