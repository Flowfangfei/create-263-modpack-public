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

package plus.dragons.createenchantmentindustry.common.fluids.experience;

import com.simibubi.create.AllItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.util.FakePlayer;
import plus.dragons.createdragonsplus.common.processing.blaze.BlazeBlock;

public abstract class BlazeExperienceBlock<T extends BlazeExperienceBlockEntity> extends BlazeBlock<T> {
    public BlazeExperienceBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        T blockEntity = getBlockEntity(level, pos);
        if (blockEntity == null)
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        boolean notConsume = player.hasInfiniteMaterials();
        boolean forceOverflow = !(player instanceof FakePlayer);
        var resultHolder = applyFuel(state, level, pos, stack, forceOverflow, notConsume, false);
        var result = resultHolder.result();
        if (result == InteractionResult.PASS)
            return InteractionResult.PASS;
        if (result == InteractionResult.FAIL)
            return InteractionResult.FAIL;
        var remainder = resultHolder.remainder();
        if (!remainder.isEmpty()) {
            if (stack.isEmpty())
                player.setItemInHand(hand, remainder);
            else
                player.getInventory().placeItemBackInInventory(remainder, net.minecraft.util.Prediction.SERVER_ONLY);
        }
        return InteractionResult.SUCCESS;
    }

    public static FuelApplication applyFuel(BlockState state, Level level, BlockPos pos, ItemStack stack, boolean forceOverflow, boolean notConsume, boolean simulate) {
        if (!state.hasBlockEntity())
            return FuelApplication.fail();

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof BlazeExperienceBlockEntity blaze))
            return FuelApplication.fail();

        if (stack.is(AllItems.CREATIVE_BLAZE_CAKE)) {
            if (!simulate)
                blaze.applyCreativeFuel();
            if (!notConsume)
                stack.shrink(1);
            return FuelApplication.success(ItemStack.EMPTY);
        }
        var fuel = ExperienceFuel.get(level, stack);
        if (fuel != null) {
            boolean applied = blaze.applyExperienceFuel(fuel, forceOverflow, simulate);
            if (applied) {
                if (!notConsume)
                    stack.shrink(1);
                ItemStack remainder = notConsume
                        ? ItemStack.EMPTY
                        : fuel.usingConvertTo().map(ItemStackTemplate::create).orElseGet(() -> stack.getItem().getCraftingRemainder() == null ? ItemStack.EMPTY : stack.getItem().getCraftingRemainder().create()).copy();
                return FuelApplication.success(remainder);
            }
            return FuelApplication.fail();
        }
        return FuelApplication.pass();
    }
    public record FuelApplication(InteractionResult result, ItemStack remainder) {
        static FuelApplication success(ItemStack remainder) {
            return new FuelApplication(InteractionResult.SUCCESS, remainder);
        }

        static FuelApplication fail() {
            return new FuelApplication(InteractionResult.FAIL, ItemStack.EMPTY);
        }

        static FuelApplication pass() {
            return new FuelApplication(InteractionResult.PASS, ItemStack.EMPTY);
        }
    }
}
