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

package plus.dragons.createdragonsplus.mixin.client.create;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.infrastructure.particle.AirFlowParticle;
import com.zurrtum.create.content.kinetics.fan.IAirCurrentSource;
import com.zurrtum.create.content.kinetics.fan.processing.FanProcessingType;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import plus.dragons.createdragonsplus.common.kinetics.fan.AirCurrentAccess;
import plus.dragons.createdragonsplus.common.kinetics.fan.AirCurrentSegmentAccess;
import plus.dragons.createdragonsplus.common.kinetics.fan.DynamicParticleFanProcessingType;

@Mixin(AirFlowParticle.class)
public abstract class AirFlowParticleMixin {
    @Shadow
    @Final
    private IAirCurrentSource source;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/content/kinetics/fan/processing/FanProcessingType;morphAirFlow(Lcom/zurrtum/create/content/kinetics/fan/processing/FanProcessingType$AirFlowParticleAccess;Lnet/minecraft/util/RandomSource;)V"))
    private void createDragonsPlus$morphWithParticleData(
            FanProcessingType type,
            FanProcessingType.AirFlowParticleAccess particle,
            RandomSource random,
            Operation<Void> original,
            @Local(name = "distance") double distance) {
        if (type instanceof DynamicParticleFanProcessingType dynamic) {
            AirCurrentAccess current = (AirCurrentAccess) source.getAirCurrent();
            AirCurrentSegmentAccess segment = current == null ? null : current.createDragonsPlus$getSegmentAt((float) distance);
            Object data = segment == null ? null : segment.createDragonsPlus$getParticleData(dynamic);
            dynamic.morphAirFlow(particle, random, data);
        } else {
            original.call(type, particle, random);
        }
    }
}
