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

package plus.dragons.createdragonsplus.client.fluids;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.FogEnvironment;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.fluid.FluidTintSource;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Vector3f;
import org.joml.Vector4f;
import plus.dragons.createdragonsplus.common.fluids.SolidRenderFluidType;

/** Loaded exclusively from CDPClient's Dist.CLIENT event registrations. */
public final class SolidFluidRendering {
    private static final float BASE_WATER_FOG_DISTANCE = 96.0F;
    private SolidFluidRendering() {}

    public static FluidModel.Unbaked createModel(SolidRenderFluidType type) {
        return new FluidModel.Unbaked(new Material(type.getStillTexture()), new Material(type.getFlowingTexture()), null,
                new FluidTintSource() {
                    @Override public int color(FluidState state) { return type.getModelTintColor(); }
                    @Override public int colorAsStack(FluidStack stack) { return type.getTintColor(stack); }
                });
    }
    public static IClientFluidTypeExtensions extensions(SolidRenderFluidType type) {
        return new IClientFluidTypeExtensions() {
            public Identifier getStillTexture() { return type.getStillTexture(); }
            public Identifier getFlowingTexture() { return type.getFlowingTexture(); }
            public int getTintColor(FluidStack stack) { return type.getTintColor(stack); }
            public int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) { return type.getBlockTintColor(); }
            @Override
            public void modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance, float darkenWorldAmount, Vector4f fluidFogColor) {
                Vector3f color = type.getCustomFogColor();
                if (color != null) fluidFogColor.set(color.x(), color.y(), color.z(), fluidFogColor.w());
            }
            @Override
            public void modifyFogRender(Camera camera, FogEnvironment environment, float renderDistance, float partialTick, FogData fogData) {
                fogData.environmentalStart = -8.0F;
                fogData.environmentalEnd = Math.max(0.25F, BASE_WATER_FOG_DISTANCE * type.getFogDistanceModifier());
            }
        };
    }
}
