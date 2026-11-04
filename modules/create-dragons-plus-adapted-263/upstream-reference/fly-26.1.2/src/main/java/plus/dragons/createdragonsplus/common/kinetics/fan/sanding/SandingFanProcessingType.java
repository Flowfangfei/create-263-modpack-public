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

package plus.dragons.createdragonsplus.common.kinetics.fan.sanding;

import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.foundation.recipe.RecipeApplier;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createdragonsplus.common.kinetics.fan.DynamicParticleFanProcessingType;
import plus.dragons.createdragonsplus.common.recipe.CDPRecipeAccess;
import plus.dragons.createdragonsplus.config.CDPConfig;
import plus.dragons.createdragonsplus.registry.CDPRecipes;
import plus.dragons.createdragonsplus.registry.CDPTags;

public final class SandingFanProcessingType
        implements DynamicParticleFanProcessingType<SandingFanProcessingType.ParticleData> {
    @Override
    public boolean isValidAt(Level level, BlockPos pos) {
        return CDPConfig.recipes().enableBulkSanding.get() && level.getBlockState(pos).is(CDPTags.FAN_SANDING_CATALYSTS);
    }

    @Override
    public int getPriority() {
        return 700;
    }

    @Override
    public boolean canProcess(ItemStack stack, Level level) {
        if (!CDPConfig.recipes().enableBulkSanding.get()) {
            return false;
        }
        SingleRecipeInput input = new SingleRecipeInput(stack);
        return CDPRecipeAccess.getFirst(level, CDPRecipes.SANDING.getType(), input).isPresent()
                || CDPRecipeAccess.getAll(level, AllRecipeTypes.SANDPAPER_POLISHING).stream()
                        .filter(recipe -> recipe.value().matches(input, level))
                        .filter(AllRecipeTypes.CAN_BE_AUTOMATED)
                        .findFirst()
                        .isPresent();
    }

    @Override
    public @Nullable List<ItemStack> process(ItemStack stack, Level level) {
        SingleRecipeInput input = new SingleRecipeInput(stack);
        var cdp = CDPRecipeAccess.getFirst(level, CDPRecipes.SANDING.getType(), input);
        if (cdp.isPresent()) {
            return RecipeApplier.applyRecipeOn(
                    level.getRandom(), stack.getCount(), input, cdp.get().value());
        }
        return CDPRecipeAccess.getAll(level, AllRecipeTypes.SANDPAPER_POLISHING).stream()
                .filter(recipe -> recipe.value().matches(input, level))
                .filter(AllRecipeTypes.CAN_BE_AUTOMATED)
                .findFirst()
                .map(recipe -> ItemHelper.multipliedOutput(
                        recipe.value().assemble(input), stack.getCount()))
                .orElse(null);
    }

    @Override
    public @Nullable ParticleData getParticleDataAt(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        int color = 0xDBD3A0;
        if (state.getBlock() instanceof FallingBlock falling) {
            color = falling.getDustColor(state, level, pos);
        }
        return new ParticleData(state, color);
    }

    @Override
    public void spawnProcessingParticles(Level level, Vec3 pos, @Nullable ParticleData data) {
        if (level.getRandom().nextInt(8) == 0) {
            BlockState state = data == null ? Blocks.SAND.defaultBlockState() : data.state;
            level.addParticle(
                    new BlockParticleOption(ParticleTypes.FALLING_DUST, state),
                    pos.x + (level.getRandom().nextFloat() - .5f) * .5f,
                    pos.y + .5f,
                    pos.z + (level.getRandom().nextFloat() - .5f) * .5f,
                    0,
                    0,
                    0);
        }
        if (data != null) {
            data.playSound(level, pos);
        }
    }

    @Override
    public void morphAirFlow(AirFlowParticleAccess particleAccess, RandomSource random, @Nullable ParticleData data) {
        int color = data == null ? 0xDBD3A0 : data.color;
        BlockState state = data == null ? Blocks.SAND.defaultBlockState() : data.state;
        particleAccess.setColor(color);
        particleAccess.setAlpha(1f);
        if (random.nextInt(32) == 0) {
            particleAccess.spawnExtraParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, state), 0);
        }
    }

    @Override
    public void affectEntity(Entity entity, Level level) {
        if (!level.isClientSide()) {
            entity.extinguishFire();
        }
    }

    public static final class ParticleData {
        private final BlockState state;
        private final int color;
        private final Set<BlockPos> playedSoundPositions = new ObjectArraySet<>();

        public ParticleData(BlockState state, int color) {
            this.state = state;
            this.color = color;
        }

        private void playSound(Level level, Vec3 pos) {
            if (level.getGameTime() % 7 == 0) {
                if (playedSoundPositions.add(BlockPos.containing(pos))) {
                    AllSoundEvents.SANDING_SHORT.playAt(
                            level,
                            pos,
                            0.3f + 0.1f * level.getRandom().nextFloat(),
                            0.9f + 0.2f * level.getRandom().nextFloat(),
                            true);
                }
            } else {
                playedSoundPositions.clear();
            }
        }
    }
}
