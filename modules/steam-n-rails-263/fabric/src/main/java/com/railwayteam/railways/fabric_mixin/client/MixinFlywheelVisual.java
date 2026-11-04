/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.fabric_mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.railwayteam.railways.content.animated_flywheel.FlywheelMovementRender;
import com.railwayteam.railways.content.palettes.PalettesFlywheelBlock;
import com.simibubi.create.content.kinetics.flywheel.FlywheelVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import com.simibubi.create.content.kinetics.flywheel.FlywheelBlockEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Selects the locometal wheel partial for block-entity Flywheel visuals. */
@Mixin(value = FlywheelVisual.class, remap = false)
public abstract class MixinFlywheelVisual {
    @WrapOperation(
        method = "<init>",
        at = @At(
            value = "FIELD",
            target = "Lcom/simibubi/create/AllPartialModels;FLYWHEEL:Ldev/engine_room/flywheel/lib/model/baked/PartialModel;",
            opcode = Opcodes.GETSTATIC
        )
    )
    private PartialModel railways$usePalettePartial(
        Operation<PartialModel> original,
        VisualizationContext context,
        FlywheelBlockEntity blockEntity
    ) {
        if (blockEntity.getBlockState().getBlock() instanceof PalettesFlywheelBlock)
            return FlywheelMovementRender.getWheelModel(blockEntity.getBlockState());
        return original.call();
    }
}
