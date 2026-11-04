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

package plus.dragons.createenchantmentindustry.common.processing.classic_enchanter;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.createmod.catnip.api.math.AngleHelper;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.util.LightCoordsUtil;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import plus.dragons.createdragonsplus.common.processing.blaze.BlazeBlockRenderer;
import plus.dragons.createenchantmentindustry.common.CEICommon;

public class ClassicBlazeEnchanterRenderer extends BlazeBlockRenderer<ClassicBlazeEnchanterBlockEntity> {
    public static final net.minecraft.resources.Identifier BOOK_TEXTURE = CEICommon.asResource("textures/block/blaze_enchanter_book.png");
    private static final float PI = 3.14159265358979323846f;
    private final BookModel bookModel;
    private final ItemModelResolver itemRenderer;

    public ClassicBlazeEnchanterRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
        this.itemRenderer = context.itemModelResolver();
    }

    @Override
    protected void submitAdditional(ClassicBlazeEnchanterBlockEntity blockEntity, float partialTicks, PoseStack poseStack, SubmitNodeCollector bufferSource, int light, int overlay) {
        ClassicEnchanterBehaviourIndicatorRenderer.renderOnBlockEntity(blockEntity, partialTicks, poseStack, bufferSource, light, overlay);
        var item = blockEntity.heldItem;
        float horizontalAngle = AngleHelper.rad(blockEntity.headAngle().getValue(partialTicks));
        renderBook(blockEntity, partialTicks, horizontalAngle, poseStack, bufferSource);
        if (!item.isEmpty())
            renderItem(blockEntity, item, blockEntity.processingTime, partialTicks, poseStack, bufferSource, light, overlay);

    }

    protected void renderItem(ClassicBlazeEnchanterBlockEntity blockEntity, ItemStack item, int processingTime, float partialTicks, PoseStack poseStack, SubmitNodeCollector bufferSource, int light, int overlay) {
        Level level = blockEntity.getLevel();
        assert level != null;
        var blockPos = blockEntity.getBlockPos();
        float renderTicks = AnimationTickHolder.getTicks();
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

    protected void renderBook(ClassicBlazeEnchanterBlockEntity be,
            float partialTicks, float horizontalAngle,
            PoseStack ps, SubmitNodeCollector buffer) {
        ps.pushPose();
        ps.translate(0.5, 0.25, 0.5);
        float time = AnimationTickHolder.getRenderTime();
        ps.translate(0.0, 0.1f + Mth.sin(time * 0.1f) * 0.01, 0.0);
        ps.rotate(Axis.YP.rotation(horizontalAngle + PI / 2));
        ps.rotate(Axis.ZP.rotationDegrees(80.0f));
        ps.scale(1.2f, 1.2f, 1.2f);
        float flip = Mth.lerp(partialTicks, be.oFlip, be.flip);
        float page0 = Mth.frac(flip + 0.25f) * 1.6f - 0.3f;
        float page1 = Mth.frac(flip + 0.75f) * 1.6f - 0.3f;
        var animation = BookModel.State.forAnimation(time, Mth.clamp(page0, 0.0f, 1.0f), Mth.clamp(page1, 0.0f, 1.0f), 1);
        buffer.submitCustomGeometry(ps, RenderTypes.entitySolid(BOOK_TEXTURE), new BookGeometry(bookModel, animation));
        ps.popPose();
    }

    @Override
    protected void transformBlaze(ClassicBlazeEnchanterBlockEntity blockEntity, PoseStack pose) { pose.translate(0, .2f, 0); }
    private record BookGeometry(BookModel model, BookModel.State animation) implements SubmitNodeCollector.CustomGeometryRenderer {
        public void render(PoseStack.Pose pose, VertexConsumer consumer) {
            model.setupAnim(animation);
            PoseStack matrices = new PoseStack();
            matrices.last().pose().set(pose.pose()); matrices.last().normal().set(pose.normal());
            model.renderToBuffer(matrices, consumer, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, -1);
        }
    }
}
