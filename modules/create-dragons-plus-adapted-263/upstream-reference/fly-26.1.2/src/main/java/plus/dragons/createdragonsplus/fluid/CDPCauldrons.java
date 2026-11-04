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
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.cauldron.CauldronInteractions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import plus.dragons.createdragonsplus.config.CDPConfig;
import plus.dragons.createdragonsplus.registry.CDPBlocks;
import plus.dragons.createdragonsplus.registry.CDPFluids;

public final class CDPCauldrons {
    public static final CauldronInteraction.Dispatcher DRAGON_BREATH = CauldronInteractions.newDispatcher("create_dragons_plus_dragon_breath");

    private CDPCauldrons() {}

    public static void register() {
        CauldronInteractions.EMPTY.put(
                CDPFluids.DRAGON_BREATH.getBucket(), CDPCauldrons::emptyBucketIntoEmptyCauldron);
        CauldronInteractions.EMPTY.put(Items.DRAGON_BREATH, CDPCauldrons::emptyBottleIntoEmptyCauldron);
        DRAGON_BREATH.put(Items.BUCKET, CDPCauldrons::fillBucket);
        DRAGON_BREATH.put(Items.GLASS_BOTTLE, CDPCauldrons::fillBottle);
        DRAGON_BREATH.put(Items.DRAGON_BREATH, CDPCauldrons::emptyBottle);
    }

    private static InteractionResult emptyBucketIntoEmptyCauldron(
            BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack stack) {
        if (!enabled()) {
            return InteractionResult.PASS;
        }
        return CauldronInteractions.emptyBucket(
                level,
                pos,
                player,
                hand,
                stack,
                CDPBlocks.DRAGON_BREATH_CAULDRON
                        .get()
                        .defaultBlockState()
                        .setValue(DragonBreathCauldronBlock.LEVEL, DragonBreathCauldronBlock.MAX_LEVEL),
                SoundEvents.BUCKET_EMPTY_LAVA);
    }

    private static InteractionResult fillBucket(
            BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack stack) {
        if (!enabled()) {
            return InteractionResult.PASS;
        }
        return CauldronInteractions.fillBucket(
                state,
                level,
                pos,
                player,
                hand,
                stack,
                new ItemStack(CDPFluids.DRAGON_BREATH.getBucket()),
                candidate -> candidate.getValue(DragonBreathCauldronBlock.LEVEL) == DragonBreathCauldronBlock.MAX_LEVEL,
                SoundEvents.BUCKET_FILL_LAVA);
    }

    private static InteractionResult emptyBottleIntoEmptyCauldron(
            BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack stack) {
        if (!enabled()) {
            return InteractionResult.PASS;
        }
        return emptyContainer(
                level,
                pos,
                player,
                hand,
                stack,
                new ItemStack(Items.GLASS_BOTTLE),
                CDPBlocks.DRAGON_BREATH_CAULDRON.get().defaultBlockState(),
                SoundEvents.BOTTLE_EMPTY);
    }

    private static InteractionResult emptyBottle(
            BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack stack) {
        if (!enabled() || state.getValue(DragonBreathCauldronBlock.LEVEL) == DragonBreathCauldronBlock.MAX_LEVEL) {
            return InteractionResult.PASS;
        }
        return emptyContainer(
                level,
                pos,
                player,
                hand,
                stack,
                new ItemStack(Items.GLASS_BOTTLE),
                DragonBreathCauldronBlock.raiseFillLevel(state),
                SoundEvents.BOTTLE_EMPTY);
    }

    private static InteractionResult fillBottle(
            BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack stack) {
        if (!enabled()) {
            return InteractionResult.PASS;
        }
        return fillContainer(
                level,
                pos,
                player,
                hand,
                stack,
                new ItemStack(Items.DRAGON_BREATH),
                DragonBreathCauldronBlock.lowerFillLevel(state),
                SoundEvents.BOTTLE_FILL_DRAGONBREATH);
    }

    private static InteractionResult fillContainer(
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            ItemStack emptyStack,
            ItemStack filledStack,
            BlockState newState,
            SoundEvent sound) {
        if (!level.isClientSide()) {
            Item item = emptyStack.getItem();
            player.setItemInHand(hand, ItemUtils.createFilledResult(emptyStack, player, filledStack));
            player.awardStat(Stats.USE_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(item));
            level.setBlockAndUpdate(pos, newState);
            level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
        }
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult emptyContainer(
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            ItemStack filledStack,
            ItemStack emptyStack,
            BlockState newState,
            SoundEvent sound) {
        if (!level.isClientSide()) {
            Item item = filledStack.getItem();
            player.setItemInHand(hand, ItemUtils.createFilledResult(filledStack, player, emptyStack));
            player.awardStat(Stats.FILL_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(item));
            level.setBlockAndUpdate(pos, newState);
            level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
        }
        return InteractionResult.SUCCESS;
    }

    private static boolean enabled() {
        return CDPConfig.features().dragonBreathFluid.get();
    }
}
