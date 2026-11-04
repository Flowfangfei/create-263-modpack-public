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

package plus.dragons.createdragonsplus.common.kinetics.fan.coloring;

import com.zurrtum.create.content.kinetics.fan.processing.FanProcessingType;
import com.zurrtum.create.content.kinetics.mixer.MixingRecipe;
import com.zurrtum.create.content.processing.recipe.HeatCondition;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.content.processing.recipe.SizedIngredient;
import com.zurrtum.create.foundation.fluid.FluidStackIngredient;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.foundation.recipe.RecipeApplier;
import com.zurrtum.create.foundation.utility.BlockHelper;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createdragonsplus.common.recipe.CDPRecipeAccess;
import plus.dragons.createdragonsplus.config.CDPConfig;
import plus.dragons.createdragonsplus.data.map.CDPColoringCatalysts;
import plus.dragons.createdragonsplus.fluid.dye.DyeVariant;
import plus.dragons.createdragonsplus.registry.CDPAttachments;
import plus.dragons.createdragonsplus.registry.CDPFluids;
import plus.dragons.createdragonsplus.registry.CDPRecipes;
import plus.dragons.createdragonsplus.registry.CDPTags;

public final class ColoringFanProcessingType implements FanProcessingType {
    private static final int CONTACT_COLORING_COOLDOWN = 10;
    private final DyeVariant variant;

    public ColoringFanProcessingType(DyeVariant variant) {
        this.variant = variant;
    }

    public DyeVariant variant() {
        return variant;
    }

    @Override
    public boolean isValidAt(Level level, BlockPos pos) {
        if (!CDPConfig.recipes().enableBulkColoring.get()) {
            return false;
        }
        Fluid fluid = level.getFluidState(pos).getType();
        return variant.id().equals(CDPColoringCatalysts.fluidColor(fluid))
                || variant.id().equals(CDPColoringCatalysts.blockColor(level.getBlockState(pos).getBlock()));
    }

    @Override
    public int getPriority() {
        return 500;
    }

    @Override
    public boolean canProcess(ItemStack stack, Level level) {
        if (!CDPConfig.recipes().enableBulkColoring.get()) {
            return false;
        }
        ColoringRecipeInput input = new ColoringRecipeInput(variant.id(), stack);
        return CDPRecipeAccess.getFirst(level, CDPRecipes.COLORING.getType(), input).isPresent()
                || findAutomaticColoringResult(stack, level).isPresent();
    }

    @Override
    public @Nullable List<ItemStack> process(ItemStack stack, Level level) {
        ColoringRecipeInput input = new ColoringRecipeInput(variant.id(), stack);
        var recipe = CDPRecipeAccess.getFirst(level, CDPRecipes.COLORING.getType(), input);
        if (recipe.isPresent()) {
            return RecipeApplier.applyRecipeOn(
                    level.getRandom(), stack.getCount(), input, recipe.get().value());
        }
        return findAutomaticColoringResult(stack, level)
                .map(result -> ItemHelper.multipliedOutput(result, stack.getCount()))
                .orElse(null);
    }

    public Optional<BlockState> processBlockState(BlockState state, Level level) {
        Block block = state.getBlock();
        if (block.asItem().getDefaultInstance().isEmpty()) {
            return Optional.empty();
        }
        List<ItemStack> output = process(new ItemStack(block), level);
        if (output == null || output.size() != 1) {
            return Optional.empty();
        }
        Block result = Block.byItem(output.getFirst().getItem());
        if (result == Blocks.AIR || result == block) {
            return Optional.empty();
        }
        return Optional.of(BlockHelper.copyProperties(state, result.defaultBlockState()));
    }

    public boolean processBlock(Level level, BlockPos pos, BlockState originalState) {
        if (level.getBlockEntity(pos) != null || !level.getBlockState(pos).equals(originalState)) {
            return false;
        }
        return processBlockState(originalState, level)
                .map(result -> level.setBlockAndUpdate(pos, result))
                .orElse(false);
    }

    public Optional<MixingRecipe> createMixingRecipe(ItemStack input, Level level) {
        if (input.isEmpty()) {
            return Optional.empty();
        }
        ColoringRecipeInput recipeInput = new ColoringRecipeInput(variant.id(), input);
        var explicit = CDPRecipeAccess.getFirst(level, CDPRecipes.COLORING.getType(), recipeInput);
        List<ProcessingOutput> outputs;
        Ingredient ingredient;
        int fluidAmount;
        if (explicit.isPresent()) {
            ColoringRecipe recipe = explicit.get().value();
            outputs = recipe.results();
            ingredient = recipe.ingredient();
            fluidAmount = recipe.dyeFluidAmount();
        } else {
            Optional<ItemStack> automatic = findAutomaticColoringResult(input, level);
            if (automatic.isEmpty()) {
                return Optional.empty();
            }
            outputs = List.of(new ProcessingOutput(automatic.get()));
            ingredient = Ingredient.of(input.getItem());
            fluidAmount = ColoringRecipe.DEFAULT_DYE_FLUID_AMOUNT;
        }
        if (outputs.isEmpty() || outputs.size() > 4) {
            return Optional.empty();
        }
        var fluid = CDPFluids.DYES_BY_VARIANT.get(variant.id());
        if (fluid == null) {
            return Optional.empty();
        }
        return Optional.of(new MixingRecipe(
                100,
                outputs,
                List.of(),
                HeatCondition.NONE,
                List.of(new FluidStackIngredient(fluid.getSource(), DataComponentPatch.EMPTY, fluidAmount)),
                List.of(new SizedIngredient(ingredient, 1))));
    }

    @Override
    public void spawnProcessingParticles(Level level, Vec3 pos) {
        if (level.getRandom().nextInt(8) == 0) {
            level.addParticle(
                    new DustParticleOptions(variant.color(), 2),
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
        particleAccess.setColor(variant.color());
        particleAccess.setAlpha(1f);
    }

    @Override
    public void affectEntity(Entity entity, Level level) {
        if (level.isClientSide()) {
            return;
        }
        if (entity instanceof LivingEntity living && advanceColoringProgress(living)) {
            applyColoringImmediately(living, level);
        }
        if (entity instanceof EnderMan || entity.getType() == EntityType.SNOW_GOLEM || entity.getType() == EntityType.BLAZE) {
            entity.hurtServer((ServerLevel) level, entity.damageSources().drown(), 2);
        }
        if (entity.isOnFire()) {
            entity.clearFire();
            level.playSound(
                    null,
                    entity.blockPosition(),
                    SoundEvents.GENERIC_EXTINGUISH_FIRE,
                    SoundSource.NEUTRAL,
                    0.7f,
                    1.6f + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.4f);
        }
    }

    public boolean applyContactColoring(ItemEntity entity, Level level) {
        if (level.isClientSide() || entity.isRemoved()) {
            return false;
        }
        List<ItemStack> processed = process(entity.getItem(), level);
        if (processed == null) {
            return false;
        }
        ArrayList<ItemStack> stacks = new ArrayList<>(processed);
        if (stacks.isEmpty()) {
            entity.discard();
            return true;
        }
        entity.setItem(stacks.removeFirst());
        for (ItemStack additional : stacks) {
            if (!additional.isEmpty()) {
                ItemEntity created = new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(), additional.copy());
                created.setDeltaMovement(entity.getDeltaMovement());
                level.addFreshEntity(created);
            }
        }
        return true;
    }

    public boolean applyContactColoring(LivingEntity entity, Level level) {
        if (level.isClientSide() || entity.isRemoved()) {
            return false;
        }
        var previous = entity.getAttached(CDPAttachments.CONTACT_COLORING);
        if (previous != null
                && previous.color().equals(variant.id())
                && entity.tickCount - previous.lastProcessTick() < CONTACT_COLORING_COOLDOWN) {
            return false;
        }
        entity.setAttached(
                CDPAttachments.CONTACT_COLORING, new CDPAttachments.ContactColoring(variant.id(), entity.tickCount));
        return applyColoringImmediately(entity, level);
    }

    private boolean advanceColoringProgress(LivingEntity entity) {
        int duration = AllConfigs.server().kinetics.fanProcessingTime.get();
        var previous = entity.getAttached(CDPAttachments.COLORING_PROGRESS);
        if (previous == null || !previous.color().equals(variant.id())) {
            entity.setAttached(
                    CDPAttachments.COLORING_PROGRESS,
                    new CDPAttachments.ColoringProgress(variant.id(), entity.tickCount, duration));
            return duration == 0;
        }
        int elapsed = entity.tickCount - previous.lastProcessTick();
        int remaining = elapsed < 0 || elapsed > 1 ? duration : Math.max(0, previous.remainingTicks() - 1);
        if (remaining == 0) {
            entity.removeAttached(CDPAttachments.COLORING_PROGRESS);
            return true;
        }
        entity.setAttached(
                CDPAttachments.COLORING_PROGRESS,
                new CDPAttachments.ColoringProgress(variant.id(), entity.tickCount, remaining));
        return false;
    }

    private boolean applyColoringImmediately(LivingEntity entity, Level level) {
        boolean changed = false;
        var color = variant.vanillaColor();
        if (color != null) {
            if (entity instanceof Sheep sheep && sheep.getColor() != color) {
                sheep.setColor(color);
                changed = true;
            } else if (entity instanceof Cat cat && cat.getCollarColor() != color) {
                cat.setCollarColor(color);
                changed = true;
            } else if (entity instanceof Wolf wolf && wolf.getCollarColor() != color) {
                wolf.setCollarColor(color);
                changed = true;
            }
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack equipped = entity.getItemBySlot(slot);
            if (equipped.isEmpty()) {
                continue;
            }
            Optional<ItemStack> colored = applyColoring(equipped, level);
            if (colored.isPresent()) {
                ItemStack result = colored.get();
                result.setCount(equipped.getCount());
                entity.setItemSlot(slot, result);
                changed = true;
            }
        }
        return changed;
    }

    private Optional<ItemStack> applyColoring(ItemStack stack, Level level) {
        ColoringRecipeInput input = new ColoringRecipeInput(variant.id(), stack);
        var recipe = CDPRecipeAccess.getFirst(level, CDPRecipes.COLORING.getType(), input);
        if (recipe.isPresent()) {
            List<ItemStack> outputs = recipe.get().value().assemble(input, level.getRandom());
            return outputs.stream().findFirst();
        }
        return findAutomaticColoringResult(stack, level);
    }

    private Optional<ItemStack> findAutomaticColoringResult(ItemStack stack, Level level) {
        if (stack.is(CDPTags.NOT_APPLICABLE_COLORING)) {
            return Optional.empty();
        }
        ItemStack dye = variant.dyeItemStack();
        if (dye.isEmpty()) {
            return Optional.empty();
        }
        CraftingInput oneInput = CraftingInput.of(2, 1, List.of(stack.copyWithCount(1), dye.copyWithCount(1)));
        Optional<ItemStack> one = findCraftingResult(oneInput, dye, level, 1);
        if (one.isPresent()) {
            return one;
        }
        NonNullList<ItemStack> items = NonNullList.withSize(9, stack.copyWithCount(1));
        items.set(4, dye.copyWithCount(1));
        CraftingInput eightInput = CraftingInput.of(3, 3, items);
        return findCraftingResult(eightInput, dye, level, 8).map(result -> result.copyWithCount(1));
    }

    private static Optional<ItemStack> findCraftingResult(
            CraftingInput input, ItemStack dye, Level level, int expectedCount) {
        for (var holder : CDPRecipeAccess.getAll(level, net.minecraft.world.item.crafting.RecipeType.CRAFTING)) {
            CraftingRecipe recipe = holder.value();
            try {
                if (!recipe.matches(input, level)) {
                    continue;
                }
                ItemStack result = recipe.assemble(input);
                if (!result.isEmpty() && result.getCount() == expectedCount && !result.is(dye.getItem())) {
                    return Optional.of(result);
                }
            } catch (RuntimeException exception) {
                // Some modded crafting recipes cannot be safely probed with synthetic inputs.
            }
        }
        return Optional.empty();
    }
}
