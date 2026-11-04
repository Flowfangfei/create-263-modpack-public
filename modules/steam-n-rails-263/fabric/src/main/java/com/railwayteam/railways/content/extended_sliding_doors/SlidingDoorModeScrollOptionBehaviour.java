/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
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

package com.railwayteam.railways.content.extended_sliding_doors;

import com.railwayteam.railways.registry.CRIcons;
import net.createmod.catnip.api.math.AngleHelper;
import net.createmod.catnip.api.math.VecHelper;
import com.simibubi.create.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.content.decoration.slidingDoor.SlidingDoorBlock;
import com.simibubi.create.content.decoration.slidingDoor.SlidingDoorBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class SlidingDoorModeScrollOptionBehaviour extends ScrollOptionBehaviour<SlidingDoorMode> {

    public SlidingDoorModeScrollOptionBehaviour(SlidingDoorBlockEntity blockEntity) {
        super(
            SlidingDoorMode.class,
            Component.translatable("create.sliding_door.mode"),
            blockEntity,
            new SlidingDoorValueBoxTransform()
        );
        requiresWrench();
    }

    public static SlidingDoorModeScrollOptionBehaviour create(SmartBlockEntity blockEntity) {
        return new SlidingDoorModeScrollOptionBehaviour((SlidingDoorBlockEntity) blockEntity);
    }

    private static class SlidingDoorValueBoxTransform extends CenteredSideValueBoxTransform {
        SlidingDoorValueBoxTransform() {
            super((state, direction) -> {
                Direction facing = state.getValue(SlidingDoorBlock.FACING);
                boolean showAtAll = !state.getValue(SlidingDoorBlock.OPEN);
                return showAtAll && (direction == facing || direction == facing.getOpposite());
            });
        }

        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 8, 16);
        }

        @Override
        public Vec3 getLocalOffset(net.minecraft.world.level.LevelAccessor level, net.minecraft.core.BlockPos pos, BlockState state) {
            Vec3 location = VecHelper.voxelSpace(
                8,
                8,
                state.getValue(SlidingDoorBlock.FACING) == direction ? 3 : 16
            );
            location = VecHelper.rotateCentered(
                location,
                AngleHelper.horizontalAngle(getSide()),
                Direction.Axis.Y
            );
            return VecHelper.rotateCentered(location, AngleHelper.verticalAngle(getSide()), Direction.Axis.X);
        }
    }
}
