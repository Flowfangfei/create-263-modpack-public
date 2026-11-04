/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2024 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.railwayteam.railways.content.buffer.headstock;

import com.railwayteam.railways.content.buffer.IDyedBuffer;
import com.railwayteam.railways.util.BlockStateUtils;
import com.simibubi.create.content.decoration.copycat.CopycatBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class CopycatHeadstockBlockEntity extends CopycatBlockEntity implements IDyedBuffer {
    @Nullable
    protected DyeColor color;

    public CopycatHeadstockBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public @Nullable DyeColor getColor() {
        return color;
    }

    @Override
    protected void write(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries, boolean clientPacket) {
        net.minecraft.world.level.storage.TagValueOutput output = net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING, registries);
        super.write(tag, registries, clientPacket);

        if (color != null)
            output.putInt("Color", color.getId());

        tag.merge(output.buildResult());
    }

    @Override
    protected void read(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries, boolean clientPacket) {
        ValueInput input = net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, registries, tag);
        super.read(tag, registries, clientPacket);
        DyeColor prevColor = color;

        if (input.getInt("Color").isPresent())
            color = DyeColor.byId(input.getInt("Color").orElse(0));
        else
            color = null;

        if (clientPacket && prevColor != color)
            redrawColor();
    }

    protected void redrawColor() {
        if (hasLevel()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 16);
            level.getChunkSource()
                .getLightEngine()
                .checkBlock(worldPosition);
        }
    }

    public InteractionResult applyDyeIfValid(ItemStack stack) {
        if (!(stack.getItem()instanceof DyeItem dyeItem))
            return InteractionResult.PASS;
        DyeColor color = DyeColor.getColor(stack);
        if (color == this.color)
            return InteractionResult.PASS;
        if (level.isClientSide() && !isVirtual())
            return InteractionResult.SUCCESS;
        this.color = color;
        notifyUpdate();
        level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, worldPosition, Block.getId(BlockStateUtils.getWoolBlock(color).defaultBlockState()));
        return InteractionResult.SUCCESS;
    }
}
