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

package plus.dragons.createdragonsplus.common.processing.blaze;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.content.processing.burner.BlazeBurnerRenderer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import java.util.List;
import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.createmod.catnip.api.math.AngleHelper;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/** Modern extract/submit renderer, retaining the machines' animated rods, head, hats and goggles. */
public class BlazeBlockRenderer<T extends BlazeBlockEntity> implements BlockEntityRenderer<T, BlazeBlockRenderer.State> {
    public static class State extends BlockEntityRenderState {
        public BlazeBlockEntity blockEntity;
        public float partialTicks;
    }

    public BlazeBlockRenderer(BlockEntityRendererProvider.Context context) {}
    @Override public State createRenderState() { return new State(); }
    @Override public void extractRenderState(T blockEntity, State state, float partialTicks, Vec3 cameraPos,
            ModelFeatureRenderer.CrumblingOverlay overlay) {
        BlockEntityRenderState.extractBase(blockEntity, state, overlay);
        state.blockEntity = blockEntity;
        state.partialTicks = partialTicks;
    }
    @Override @SuppressWarnings("unchecked")
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        T blockEntity = (T) state.blockEntity;
        if (blockEntity == null || !blockEntity.hasLevel()) return;
        submitAdditional(blockEntity, state.partialTicks, pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY);
        HeatLevel heat = blockEntity.getHeatLevelForRender();
        if (heat == HeatLevel.NONE) return;
        float animation = blockEntity.headAnimation.getValue(state.partialTicks) * .175f;
        float angle = AngleHelper.rad(blockEntity.headAngle.getValue(state.partialTicks));
        float time = AnimationTickHolder.getRenderTime();
        float seeded = time + (blockEntity.hashCode() % 13) * 16f;
        float scale = heat.isAtLeast(HeatLevel.FADING) ? 64f : 16f;
        float headY = Mth.sin(seeded / 16f) / scale - animation * .75f;
        pose.pushPose();
        transformBlaze(blockEntity, pose);
        submitRotated(BlazeBurnerRenderer.getBlazeModel(heat, animation > .125f), headY, angle, pose, collector);
        submitRotated(blockEntity.getGogglesModel(heat), headY + .5f, angle, pose, collector);
        submitRotated(blockEntity.getHatModel(heat), headY + .75f, angle + Mth.PI, pose, collector);
        if (heat.isAtLeast(HeatLevel.FADING)) {
            submitTranslated(heat == HeatLevel.SEETHING ? AllPartialModels.BLAZE_BURNER_SUPER_RODS : AllPartialModels.BLAZE_BURNER_RODS,
                Mth.sin(seeded / 16f + Mth.PI) / scale + animation + .125f, pose, collector);
            submitTranslated(heat == HeatLevel.SEETHING ? AllPartialModels.BLAZE_BURNER_SUPER_RODS_2 : AllPartialModels.BLAZE_BURNER_RODS_2,
                Mth.sin(seeded / 16f + Mth.PI / 2) / scale + animation - 3f / 16f, pose, collector);
            if (animation > .125f) BlazeBurnerRenderer.submitScrollingFlame(pose, collector, heat, angle, time, LightCoordsUtil.FULL_BRIGHT);
        }
        pose.popPose();
    }
    protected void submitAdditional(T blockEntity, float partialTicks, PoseStack pose, SubmitNodeCollector collector, int light, int overlay) {}
    protected void transformBlaze(T blockEntity, PoseStack pose) {}
    private static void submitTranslated(PartialModel model, float y, PoseStack pose, SubmitNodeCollector collector) {
        pose.pushPose(); pose.translate(0, y, 0); submitPart(model, pose, collector); pose.popPose();
    }
    private static void submitRotated(PartialModel model, float y, float angle, PoseStack pose, SubmitNodeCollector collector) {
        if (model == null) return;
        pose.pushPose(); pose.translate(.5, y + .5, .5); pose.rotate(Axis.YP.rotation(angle)); pose.translate(-.5, -.5, -.5);
        submitPart(model, pose, collector); pose.popPose();
    }
    public static void submitPart(PartialModel model, PoseStack pose, SubmitNodeCollector collector) {
        if (model == null || model.get() == null) return;
        var part = model.get();
        var type = (part.materialFlags() & BakedQuad.FLAG_TRANSLUCENT) != 0 ? RenderTypes.translucentMovingBlock() : RenderTypes.cutoutMovingBlock();
        collector.submitBlockModel(pose, type, List.of(part), BlockModelRenderState.EMPTY_TINTS,
            LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
    }
}
