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

package plus.dragons.createdragonsplus.common.kinetics.fan.ending;

import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.content.kinetics.fan.processing.FanProcessingType;
import com.zurrtum.create.foundation.recipe.RecipeApplier;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.PowerParticleOption;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createdragonsplus.common.recipe.CDPRecipeAccess;
import plus.dragons.createdragonsplus.config.CDPConfig;
import plus.dragons.createdragonsplus.registry.CDPRecipes;
import plus.dragons.createdragonsplus.registry.CDPTags;

public final class EndingFanProcessingType implements FanProcessingType {
    @Override
    public boolean isValidAt(Level level, BlockPos pos) {
        if (!CDPConfig.recipes().enableBulkEnding.get()) {
            return false;
        }
        return level.getFluidState(pos).is(CDPTags.FLUID_FAN_ENDING_CATALYSTS)
                || level.getBlockState(pos).is(CDPTags.FAN_ENDING_CATALYSTS);
    }

    @Override
    public int getPriority() {
        return 350;
    }

    @Override
    public boolean canProcess(ItemStack stack, Level level) {
        return CDPConfig.recipes().enableBulkEnding.get()
                && CDPRecipeAccess.getFirst(level, CDPRecipes.ENDING.getType(), new SingleRecipeInput(stack))
                        .isPresent();
    }

    @Override
    public @Nullable List<ItemStack> process(ItemStack stack, Level level) {
        SingleRecipeInput input = new SingleRecipeInput(stack);
        return CDPRecipeAccess.getFirst(level, CDPRecipes.ENDING.getType(), input)
                .map(recipe -> RecipeApplier.applyRecipeOn(
                        level.getRandom(), stack.getCount(), input, recipe.value()))
                .orElse(null);
    }

    @Override
    public void spawnProcessingParticles(Level level, Vec3 pos) {
        if (level.getRandom().nextInt(8) == 0) {
            level.addParticle(
                    PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, 1),
                    pos.x + (level.getRandom().nextFloat() - .5f) * .5f,
                    pos.y + .5f,
                    pos.z + (level.getRandom().nextFloat() - .5f) * .5f,
                    0,
                    1 / 8f,
                    0);
        }
    }

    @Override
    public void morphAirFlow(AirFlowParticleAccess particleAccess, RandomSource random) {
        particleAccess.setColor(Color.mixColors(0xB700D2, 0xDF00F9, random.nextFloat()));
        particleAccess.setAlpha(1f);
        if (random.nextFloat() < 1 / 32f) {
            particleAccess.spawnExtraParticle(PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, 1), 0);
        }
    }

    @Override
    public void affectEntity(Entity entity, Level level) {
        if (level.isClientSide()) {
            return;
        }
        if (entity instanceof LivingEntity living && living.isAffectedByPotions() && entity.tickCount % 5 == 0) {
            living.addEffect(new MobEffectInstance(MobEffects.INSTANT_DAMAGE, 1, 1));
        }
    }
}
