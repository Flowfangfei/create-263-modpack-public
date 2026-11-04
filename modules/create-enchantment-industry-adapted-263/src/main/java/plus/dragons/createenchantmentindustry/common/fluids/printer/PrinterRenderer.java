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

package plus.dragons.createenchantmentindustry.common.fluids.printer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import plus.dragons.createenchantmentindustry.client.model.CEIPartialModels;
import plus.dragons.createdragonsplus.common.processing.blaze.BlazeBlockRenderer;
import net.createmod.catnip.api.client.render.FluidRenderHelper;
import static plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBlockEntity.PROCESSING_TIME;
public class PrinterRenderer implements BlockEntityRenderer<PrinterBlockEntity, PrinterRenderer.State> {
    private static final int PISTON_MOVING_TIME = 5;
    public static final class State extends BlockEntityRenderState { PrinterBlockEntity printer; float partialTicks; }
    public PrinterRenderer(Context context) {}
    public State createRenderState() { return new State(); }
    public void extractRenderState(PrinterBlockEntity printer, State state, float partialTicks, Vec3 camera, ModelFeatureRenderer.CrumblingOverlay overlay) {
        BlockEntityRenderer.super.extractRenderState(printer, state, partialTicks, camera, overlay); state.printer = printer; state.partialTicks = partialTicks;
    }
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        var printer = state.printer; if (printer == null) return;
        var tank = printer.tank.getPrimaryTank(); var fluidStack = tank.getRenderedFluid();
        float fluidLevel = tank.getFluidLevel().getValue(state.partialTicks);
        if (!fluidStack.isEmpty() && fluidLevel != 0) {
            boolean top = fluidStack.getFluidType().isLighterThanAir();
            fluidLevel = Math.max(fluidLevel, .175f) * (11 / 16f);
            float min = 2.5f / 16f, max = min + 11 / 16f;
            FluidRenderHelper.submitFluidBox(collector, fluidStack, min, top ? max - fluidLevel : min, min, max, top ? max : min + fluidLevel, max, poseStack, state.lightCoords, false, true);
        }
        float progress = getProgress(printer.processingTicks - state.partialTicks);
        poseStack.pushPose();
        for (var nozzle : new dev.engine_room.flywheel.lib.model.baked.PartialModel[]{CEIPartialModels.PRINTER_NOZZLE_TOP, CEIPartialModels.PRINTER_NOZZLE_BOTTOM}) {
            poseStack.translate(0, 3 * progress / 32f, 0); BlazeBlockRenderer.submitPart(nozzle, poseStack, collector);
        }
        poseStack.popPose(); poseStack.pushPose(); poseStack.translate(0, -progress / 2f, 0);
        BlazeBlockRenderer.submitPart(CEIPartialModels.PRINTER_PISTON, poseStack, collector); poseStack.popPose();
    }
    public static float getProgress(float partialTicks) {
        if (partialTicks < 0) {
            return 0;
        } else if (partialTicks < PISTON_MOVING_TIME) {
            return Mth.lerp(partialTicks / PISTON_MOVING_TIME, 0, 1);
        } else if (partialTicks < PROCESSING_TIME - PISTON_MOVING_TIME) {
            return 1;
        } else if (partialTicks < PROCESSING_TIME) {
            return Mth.lerp((PROCESSING_TIME - partialTicks) / PISTON_MOVING_TIME, 0, 1);
        }
        return 0;
    }
}
