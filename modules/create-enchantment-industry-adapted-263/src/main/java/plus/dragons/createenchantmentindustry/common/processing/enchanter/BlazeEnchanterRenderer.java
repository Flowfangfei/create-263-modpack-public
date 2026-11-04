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

package plus.dragons.createenchantmentindustry.common.processing.enchanter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import plus.dragons.createdragonsplus.common.processing.blaze.BlazeBlockRenderer;

public class BlazeEnchanterRenderer extends BlazeBlockRenderer<BlazeEnchanterBlockEntity> {
    private final ItemModelResolver itemRenderer;

    public BlazeEnchanterRenderer(Context context) {
        super(context);
        this.itemRenderer = context.itemModelResolver();
    }

    @Override
    protected void submitAdditional(BlazeEnchanterBlockEntity blockEntity, float partialTicks, PoseStack poseStack, SubmitNodeCollector bufferSource, int light, int overlay) {
        EnchanterBehaviorTemplateItemRenderer.renderOnBlockEntity(blockEntity, partialTicks, poseStack, bufferSource, light, overlay);
        var item = blockEntity.heldItem;
        if (!item.isEmpty())
            renderItem(blockEntity, item, blockEntity.processingTime, partialTicks, poseStack, bufferSource, light, overlay);

    }

    protected void renderItem(BlazeEnchanterBlockEntity blockEntity, ItemStack item, int processingTime, float partialTicks, PoseStack poseStack, SubmitNodeCollector bufferSource, int light, int overlay) {
        Level level = blockEntity.getLevel();
        assert level != null;
        var blockPos = blockEntity.getBlockPos();
        float renderTicks = AnimationTickHolder.getTicks() + partialTicks;
        float animation = processingTime == -1
                ? 0
                : Mth.sin((processingTime + partialTicks) / 20f);
        float height = 1.25f + (1 + animation) * .25f;
        float xRot = (renderTicks * 5 + blockPos.getX()) % 360;
        float zRot = (renderTicks * 5 + blockPos.getZ()) % 360;
        poseStack.pushPose();
        poseStack.translate(.5f, height, .5f);
        poseStack.rotate(Axis.XP.rotationDegrees(xRot));
        poseStack.rotate(Axis.ZP.rotationDegrees(zRot));
        poseStack.scale(.5f, .5f, .5f);
        var itemState = new ItemStackRenderState();
        itemRenderer.updateForTopItem(itemState, item, ItemDisplayContext.FIXED, level, null, blockEntity.hashCode());
        itemState.submit(poseStack, bufferSource, light, overlay, 0);
        poseStack.popPose();
    }
}
