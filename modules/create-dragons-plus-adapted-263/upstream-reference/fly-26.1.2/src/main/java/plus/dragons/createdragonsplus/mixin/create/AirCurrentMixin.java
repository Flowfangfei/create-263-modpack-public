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

package plus.dragons.createdragonsplus.mixin.create;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.content.kinetics.fan.AirCurrent;
import com.zurrtum.create.content.kinetics.fan.IAirCurrentSource;
import com.zurrtum.create.content.kinetics.fan.processing.FanProcessingType;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import plus.dragons.createdragonsplus.common.kinetics.fan.AirCurrentAccess;
import plus.dragons.createdragonsplus.common.kinetics.fan.AirCurrentSegmentAccess;
import plus.dragons.createdragonsplus.common.kinetics.fan.DynamicParticleFanProcessingType;
import plus.dragons.createdragonsplus.registry.CDPFanProcessingTypes;

@Mixin(AirCurrent.class)
public abstract class AirCurrentMixin implements AirCurrentAccess {
    @Shadow
    public float maxDistance;

    @Shadow
    public List<?> segments;

    @Shadow
    public boolean pushing;

    @Shadow
    @Final
    public IAirCurrentSource source;

    @ModifyExpressionValue(method = "rebuild", at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/content/kinetics/fan/processing/FanProcessingType;getAt(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Lcom/zurrtum/create/content/kinetics/fan/processing/FanProcessingType;"))
    private @Nullable FanProcessingType createDragonsPlus$recognizeFacingDragonHead(
            @Nullable FanProcessingType original,
            @Local(name = "world") Level level,
            @Local(name = "currentPos") BlockPos pos) {
        var state = level.getBlockState(pos);
        var direction = source.getAirFlowDirection();
        if (state.is(Blocks.DRAGON_HEAD)) {
            var facing = RotationSegment.convertToDirection(state.getValue(SkullBlock.ROTATION))
                    .orElse(direction);
            if (direction == facing) {
                return CDPFanProcessingTypes.ENDING;
            }
        } else if (state.is(Blocks.DRAGON_WALL_HEAD)
                && state.getValue(WallSkullBlock.FACING) == direction) {
                    return CDPFanProcessingTypes.ENDING;
                }
        return original;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @WrapOperation(method = "rebuild", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lcom/zurrtum/create/content/kinetics/fan/AirCurrent$AirCurrentSegment;type:Lcom/zurrtum/create/content/kinetics/fan/processing/FanProcessingType;"))
    private void createDragonsPlus$rememberParticleData(
            @Coerce Object segment,
            FanProcessingType type,
            Operation<Void> original,
            @Local(name = "world") Level level,
            @Local(name = "currentPos") BlockPos pos) {
        original.call(segment, type);
        if (type instanceof DynamicParticleFanProcessingType dynamic) {
            ((AirCurrentSegmentAccess) segment)
                    .createDragonsPlus$setParticleData(dynamic, dynamic.getParticleDataAt(level, pos));
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @WrapOperation(method = "tickAffectedEntities", at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/content/kinetics/fan/processing/FanProcessingType;spawnProcessingParticles(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/phys/Vec3;)V"))
    private void createDragonsPlus$spawnParticlesWithData(
            FanProcessingType type,
            Level level,
            Vec3 pos,
            Operation<Void> original,
            @Local(name = "entityDistance") double distance) {
        if (type instanceof DynamicParticleFanProcessingType dynamic) {
            AirCurrentSegmentAccess segment = createDragonsPlus$getSegmentAt((float) distance);
            Object data = segment == null ? null : segment.createDragonsPlus$getParticleData(dynamic);
            dynamic.spawnProcessingParticles(level, pos, data);
        } else {
            original.call(type, level, pos);
        }
    }

    @Override
    public @Nullable AirCurrentSegmentAccess createDragonsPlus$getSegmentAt(float offset) {
        if (offset < 0 || offset > maxDistance) {
            return null;
        }
        if (pushing) {
            for (Object value : segments) {
                AirCurrentSegmentAccess segment = (AirCurrentSegmentAccess) value;
                if (offset <= segment.createDragonsPlus$getEndOffset()) {
                    return segment;
                }
            }
        } else {
            for (Object value : segments) {
                AirCurrentSegmentAccess segment = (AirCurrentSegmentAccess) value;
                if (offset >= segment.createDragonsPlus$getEndOffset()) {
                    return segment;
                }
            }
        }
        return null;
    }

    @Mixin(targets = "com.zurrtum.create.content.kinetics.fan.AirCurrent$AirCurrentSegment")
    public abstract static class AirCurrentSegmentMixin implements AirCurrentSegmentAccess {
        @Shadow
        private @Nullable FanProcessingType type;

        @Unique
        private @Nullable Object createDragonsPlus$particleData;

        @Override
        @Accessor("type")
        public abstract @Nullable FanProcessingType createDragonsPlus$getType();

        @Override
        @Accessor("endOffset")
        public abstract int createDragonsPlus$getEndOffset();

        @SuppressWarnings("unchecked")
        @Override
        public <T> @Nullable T createDragonsPlus$getParticleData(DynamicParticleFanProcessingType<T> expectedType) {
            return type == expectedType ? (T) createDragonsPlus$particleData : null;
        }

        @Override
        public <T> void createDragonsPlus$setParticleData(
                DynamicParticleFanProcessingType<T> expectedType, @Nullable T particleData) {
            if (type == expectedType) {
                createDragonsPlus$particleData = particleData;
            }
        }
    }
}
