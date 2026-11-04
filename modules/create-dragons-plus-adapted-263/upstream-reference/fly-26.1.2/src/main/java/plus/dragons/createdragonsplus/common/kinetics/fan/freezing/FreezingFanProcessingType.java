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

package plus.dragons.createdragonsplus.common.kinetics.fan.freezing;

import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.content.kinetics.fan.processing.FanProcessingType;
import com.zurrtum.create.foundation.recipe.RecipeApplier;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createdragonsplus.common.recipe.CDPRecipeAccess;
import plus.dragons.createdragonsplus.config.CDPConfig;
import plus.dragons.createdragonsplus.registry.CDPRecipes;
import plus.dragons.createdragonsplus.registry.CDPTags;

public final class FreezingFanProcessingType implements FanProcessingType {
    @Override
    public boolean isValidAt(Level level, BlockPos pos) {
        return CDPConfig.recipes().enableBulkFreezing.get() && level.getBlockState(pos).is(CDPTags.PASSIVE_BLOCK_FREEZERS);
    }

    @Override
    public int getPriority() {
        return 600;
    }

    @Override
    public boolean canProcess(ItemStack stack, Level level) {
        return CDPConfig.recipes().enableBulkFreezing.get()
                && CDPRecipeAccess.getFirst(level, CDPRecipes.FREEZING.getType(), new SingleRecipeInput(stack))
                        .isPresent();
    }

    @Override
    public @Nullable List<ItemStack> process(ItemStack stack, Level level) {
        SingleRecipeInput input = new SingleRecipeInput(stack);
        return CDPRecipeAccess.getFirst(level, CDPRecipes.FREEZING.getType(), input)
                .map(recipe -> RecipeApplier.applyRecipeOn(
                        level.getRandom(), stack.getCount(), input, recipe.value()))
                .orElse(null);
    }

    @Override
    public void spawnProcessingParticles(Level level, Vec3 pos) {
        if (level.getRandom().nextInt(8) == 0) {
            level.addParticle(
                    ParticleTypes.SNOWFLAKE,
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
        particleAccess.setColor(Color.mixColors(0xFFFFFF, 0x8ADCE8, random.nextFloat()));
        particleAccess.setAlpha(1f);
        if (random.nextInt(32) == 0) {
            particleAccess.spawnExtraParticle(ParticleTypes.SNOWFLAKE, 1 / 8f);
        }
    }

    @Override
    public void affectEntity(Entity entity, Level level) {
        if (level.isClientSide()) {
            return;
        }
        if (entity.canFreeze()) {
            entity.setTicksFrozen(Math.min(entity.getTicksRequiredToFreeze(), entity.getTicksFrozen()) + 3);
        }
        entity.extinguishFire();
    }
}
