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

package plus.dragons.createdragonsplus.api;

import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import plus.dragons.createdragonsplus.api.recipe.CDPRecipeTypeInfo;
import plus.dragons.createdragonsplus.api.registry.CDPFluidEntry;
import plus.dragons.createdragonsplus.api.registry.CDPRegistryEntry;
import plus.dragons.createdragonsplus.common.behaviours.BehaviourProvider;
import plus.dragons.createdragonsplus.common.processing.blaze.BlazeBlock;
import plus.dragons.createdragonsplus.common.processing.blaze.BlazeBlockEntity;
import plus.dragons.createdragonsplus.common.processing.blaze.BlazeMovementBehaviour;
import plus.dragons.createdragonsplus.common.recipe.UpdateRecipesEvent;
import plus.dragons.createdragonsplus.config.StressConfig;
import plus.dragons.createdragonsplus.fluid.dye.DyeVariant;
import plus.dragons.createdragonsplus.fluid.dye.DyeVariantRegistry;

/** Compile-only downstream consumer that protects the public addon API. */
final class BlazeAndStressApiConsumer {
    private BlazeAndStressApiConsumer() {}

    static void registerStress(
            ConsumerStressConfig config,
            Identifier blockId,
            Block block,
            CDPRegistryEntry<Block> entry) {
        config.setImpact(blockId, 4);
        config.setCapacity(block, 16);
        config.setNoImpact(entry);
        entry.getId();
        entry.get();
        entry.is(block);
    }

    static void consumeCommonApi(
            CDPFluidEntry<?> fluid,
            BehaviourProvider behaviourProvider,
            BlazeMovementBehaviour movement) {
        fluid.getId();
        fluid.getSource();
        fluid.getFlowing();
        fluid.getBucket();
        fluid.getBlock();
        behaviourProvider.getAllBehaviours();
        movement.disableBlockEntityRendering();
        UpdateRecipesEvent.EVENT.register(event -> event.recipes());
    }

    static CreateDragonsPlusPlugin createEarlyDyeVariantPlugin(DyeVariant variant) {
        return registry -> registry.add(variant);
    }

    static void populateDyeVariants(DyeVariantRegistry.Builder registry, DyeVariant variant) {
        registry.add(variant);
    }

    static <R extends Recipe<?>> void consumeRecipeType(CDPRecipeTypeInfo<R> recipeType) {
        recipeType.getId();
        recipeType.getType();
        recipeType.getSerializer();
    }

    abstract static class ConsumerBlazeBlock extends BlazeBlock<ConsumerBlazeBlockEntity> {
        protected ConsumerBlazeBlock(Properties properties) {
            super(properties);
        }
    }

    abstract static class ConsumerBlazeBlockEntity extends BlazeBlockEntity {
        protected ConsumerBlazeBlockEntity(
                BlockEntityType<?> type, BlockPos pos, BlockState state) {
            super(type, pos, state);
        }

        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public boolean isCreative() {
            return false;
        }

        @Override
        public HeatLevel getHeatLevel() {
            return HeatLevel.KINDLED;
        }
    }

    static class ConsumerStressConfig extends StressConfig {
        ConsumerStressConfig() {
            super("api_consumer");
        }

        @Override
        protected int getVersion() {
            return 2;
        }
    }
}
