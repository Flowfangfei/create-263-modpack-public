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

package plus.dragons.createdragonsplus.integration.simulated.common.fluids.tank;
import com.mojang.blaze3d.vertex.PoseStack;
import net.createmod.catnip.api.client.render.FluidRenderHelper;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.TypedInstance;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
public class FragileFluidTankRenderer implements BlockEntityRenderer<FragileFluidTankBlockEntity, FragileFluidTankRenderer.State> {
    public static class State extends BlockEntityRenderState { FluidStack fluid; float level; boolean top; }
    public FragileFluidTankRenderer(BlockEntityRendererProvider.Context context) {}
    @Override public State createRenderState() { return new State(); }
    @Override public void extractRenderState(FragileFluidTankBlockEntity tank, State state, float partialTicks, Vec3 camera,
            ModelFeatureRenderer.CrumblingOverlay overlay) {
        BlockEntityRenderState.extractBase(tank, state, overlay);
        var segment = tank.tank.getPrimaryTank();
        state.fluid = segment.getRenderedFluid().copy(); state.level = segment.getFluidLevel().getValue(partialTicks);
        state.top = !state.fluid.isEmpty() && state.fluid.getFluid().getFluidType().isLighterThanAir();
    }
    @Override public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.fluid.isEmpty() || state.level == 0) return;
        float min = 2.5f / 16f, max = min + 11f / 16f;
        float height = Math.max(state.level, .175f) * 11f / 16f;
        float minY = state.top ? max - height : min, maxY = state.top ? max : min + height;
        FluidRenderHelper.submitFluidBox(collector, (TypedInstance<Fluid>) state.fluid, min, minY, min, max, maxY, max, pose, state.lightCoords, false, true);
    }
}
