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

package plus.dragons.createenchantmentindustry.common.kinetics.grindstone;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour.TankSegment;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.createmod.catnip.api.client.render.FluidRenderHelper;
import net.minecraft.client.Minecraft;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;

public class GrindstoneDrainRenderer extends KineticBlockEntityRenderer<GrindstoneDrainBlockEntity> {
    private final ItemModelResolver itemModels;
    public GrindstoneDrainRenderer(Context context) { super(context); itemModels = context.itemModelResolver(); }
    public static class State extends BlockEntityRenderState { GrindstoneDrainBlockEntity drain; float partialTicks; }
    private final java.util.Map<BlockEntityRenderState, State> customStates = new java.util.WeakHashMap<>();
    @Override public void extractRenderState(GrindstoneDrainBlockEntity drain, BlockEntityRenderState state, float partialTicks, Vec3 camera, ModelFeatureRenderer.CrumblingOverlay overlay) {
        super.extractRenderState(drain, state, partialTicks, camera, overlay); var custom = customStates.computeIfAbsent(state, key -> new State()); custom.drain = drain; custom.partialTicks = partialTicks;
    }
    @Override public void submit(BlockEntityRenderState raw, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        var state = customStates.get(raw); if (state != null && state.drain != null) renderSafe(state.drain, state.partialTicks, pose, collector, raw.lightCoords, 0); super.submit(raw, pose, collector, camera);
    }

    protected void renderSafe(GrindstoneDrainBlockEntity drain, float partialTicks, PoseStack poseStack, SubmitNodeCollector buffer, int light, int overlay) {
        renderItems(drain, partialTicks, poseStack, buffer, light, overlay);
        renderFluid(drain, partialTicks, poseStack, buffer, light);

    }

    @Override
    protected BlockState getRenderedBlockState(GrindstoneDrainBlockEntity be) {
        var axis = getRotationAxisOf(be);
        return CEIBlocks.MECHANICAL_GRINDSTONE.getDefaultState().setValue(RotatedPillarKineticBlock.AXIS, axis);
    }

    protected void renderItems(GrindstoneDrainBlockEntity drain, float partialTicks, PoseStack poseStack, SubmitNodeCollector buffer, int light, int overlay) {
        if (drain.inventory.isEmpty())
            return;

        boolean alongZ = drain.getBlockState().getValue(HorizontalKineticBlock.HORIZONTAL_FACING).getAxis() == Direction.Axis.Z;

        float duration = drain.inventory.recipeDuration;
        boolean moving = duration != 0;
        float offset = moving ? (drain.inventory.remainingTime) / duration : 0;
        float processingSpeed = Mth.clamp(drain.getRelativeSpeed() / 32, 1, 128);
        if (moving) {
            offset = Mth.clamp(offset + (-partialTicks + .5f) * processingSpeed / duration, 0.125f, 1f);
            if (!drain.inventory.appliedRecipe)
                offset += 1;
            offset /= 2;
        }
        if (drain.getSpeed() == 0)
            offset = .5f;
        else if (drain.getSpeed() < 0 ^ alongZ)
            offset = 1 - offset;

        int count = 0;
        for (int i = 1; i < drain.inventory.getSlots(); i++)
            if (!drain.inventory.getStackInSlot(i).isEmpty())
                count++;

        poseStack.pushPose();
        if (alongZ)
            poseStack.rotate(Axis.YP.rotationDegrees(90));
        poseStack.translate(count <= 1 ? .5f : .25f, 0, 1 - offset);
        poseStack.translate(alongZ ? -1 : 0, 0, 0);
        var yDelta = Mth.sin(offset * Mth.PI);
        var yOffset = Mth.lerp(yDelta, 13 / 16f, 1f);
        int rendered = 0;

        for (int i = 0; i < drain.inventory.getSlots(); i++) {
            ItemStack stack = drain.inventory.getStackInSlot(i);
            if (stack.isEmpty())
                continue;
            var itemState = new ItemStackRenderState();
            itemModels.updateForTopItem(itemState, stack, ItemDisplayContext.FIXED, drain.getLevel(), null, 0);
            boolean blockItem = stack.getItem() instanceof net.minecraft.world.item.BlockItem;
            poseStack.pushPose();
            poseStack.translate(0, blockItem ? yOffset + 0.1125f : yOffset, 0);
            if (i > 0 && count > 1) {
                poseStack.translate((.5f / (count - 1)) * rendered, 0, 0);
                TransformStack.of(poseStack).nudge(i * 133);
            }
            poseStack.scale(.5f, .5f, .5f);
            poseStack.rotate(Axis.XP.rotationDegrees(90));
            itemState.submit(poseStack, buffer, light, overlay, 0);
            rendered++;
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private void renderFluid(GrindstoneDrainBlockEntity drain, float partialTicks, PoseStack poseStack, SubmitNodeCollector buffer, int light) {
        SmartFluidTankBehaviour tank = drain.tank;
        if (tank == null)
            return;

        TankSegment primaryTank = tank.getPrimaryTank();
        FluidStack fluidStack = primaryTank.getRenderedFluid();
        float level = primaryTank.getFluidLevel().getValue(partialTicks);

        if (!fluidStack.isEmpty() && level != 0) {
            float min = 2f / 16f;
            float max = min + (12 / 16f);
            float minY = 5f / 16f;
            level *= (7 / 16f);
            FluidRenderHelper.submitFluidBox(buffer,
                    fluidStack,
                    min, minY, min,
                    max, minY + level, max,
                    poseStack, light,
                    false, false);
        }
    }
}
