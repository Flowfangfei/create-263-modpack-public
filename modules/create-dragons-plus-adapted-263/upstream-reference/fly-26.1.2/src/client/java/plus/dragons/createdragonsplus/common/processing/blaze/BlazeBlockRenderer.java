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
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.content.processing.burner.BlazeBurnerRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createdragonsplus.client.processing.blaze.BlazeRenderContext;
import plus.dragons.createdragonsplus.client.processing.blaze.BlazeRenderModels;

/** Create-Fly render-state base renderer for addon-owned Blaze machines. */
public class BlazeBlockRenderer<T extends BlazeBlockEntity>
        implements BlockEntityRenderer<T, BlazeBlockRenderState> {
    protected final BlockEntityRendererProvider.Context context;
    private final boolean skipBlazeWhenVisualizationActive;

    public BlazeBlockRenderer(BlockEntityRendererProvider.Context context) {
        this(context, false);
    }

    protected BlazeBlockRenderer(
            BlockEntityRendererProvider.Context context, boolean skipBlazeWhenVisualizationActive) {
        this.context = context;
        this.skipBlazeWhenVisualizationActive = skipBlazeWhenVisualizationActive;
    }

    @Override
    public BlazeBlockRenderState createRenderState() {
        return new BlazeBlockRenderState();
    }

    @Override
    public void extractRenderState(
            T blockEntity,
            BlazeBlockRenderState state,
            float tickProgress,
            Vec3 cameraPos,
            @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(blockEntity, state, crumblingOverlay);
        HeatLevel heatLevel = blockEntity.getHeatLevelForRender();
        state.blazeContext = new BlazeRenderContext(blockEntity.getBlockState(), heatLevel, blockEntity, null);
        state.blazeData = null;
        Level level = blockEntity.getLevel();
        if (level != null
                && heatLevel != HeatLevel.NONE
                && (!skipBlazeWhenVisualizationActive
                        || !VisualizationManager.supportsVisualization(level))) {
            float animation = blockEntity.headAnimation.getValue(tickProgress) * 0.175F;
            float horizontalAngle = AngleHelper.rad(blockEntity.headAngle.getValue(tickProgress));
            state.blazeData = BlazeBurnerRenderer.getBlazeBurnerRenderData(
                    level,
                    blockEntity.getBlockState(),
                    heatLevel,
                    animation,
                    horizontalAngle,
                    heatLevel.isAtLeast(HeatLevel.FADING),
                    false,
                    null,
                    blockEntity.hashCode());
            BlazeRenderModels.apply(state.blazeData, state.blazeContext);
        }
        extractAdditionalRenderState(blockEntity, state, tickProgress, cameraPos, crumblingOverlay);
    }

    protected void extractAdditionalRenderState(
            T blockEntity,
            BlazeBlockRenderState state,
            float tickProgress,
            Vec3 cameraPos,
            @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {}

    @Override
    public void submit(
            BlazeBlockRenderState state,
            PoseStack matrices,
            SubmitNodeCollector queue,
            CameraRenderState cameraState) {
        submitBeforeBlaze(state, matrices, queue, cameraState);
        if (state.blazeData != null) {
            matrices.pushPose();
            transformBlaze(state, matrices);
            state.blazeData.submit(matrices, queue);
            matrices.popPose();
        }
        submitAfterBlaze(state, matrices, queue, cameraState);
    }

    protected void submitBeforeBlaze(
            BlazeBlockRenderState state,
            PoseStack matrices,
            SubmitNodeCollector queue,
            CameraRenderState cameraState) {}

    protected void transformBlaze(BlazeBlockRenderState state, PoseStack matrices) {}

    protected void submitAfterBlaze(
            BlazeBlockRenderState state,
            PoseStack matrices,
            SubmitNodeCollector queue,
            CameraRenderState cameraState) {}

    public static void tickAnimation(BlazeBlockEntity blockEntity) {
        boolean active = blockEntity.getHeatLevelFromBlock().isAtLeast(HeatLevel.FADING)
                && blockEntity.isActive();
        if (active) {
            blockEntity.headAngle.chase(
                    (AngleHelper.horizontalAngle(blockEntity
                            .getBlockState()
                            .getOptionalValue(BlazeBlock.FACING)
                            .orElse(Direction.SOUTH))
                            + 180)
                            % 360,
                    0.125F,
                    Chaser.EXP);
            blockEntity.headAngle.tickChaser();
        } else {
            float target = 0;
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null && !player.isInvisible()) {
                double x = blockEntity.isVirtual() ? -4 : player.getX();
                double z = blockEntity.isVirtual() ? -10 : player.getZ();
                double dx = x - (blockEntity.getBlockPos().getX() + 0.5);
                double dz = z - (blockEntity.getBlockPos().getZ() + 0.5);
                target = AngleHelper.deg(-Mth.atan2(dz, dx)) - 90;
            }
            target = blockEntity.headAngle.getValue()
                    + AngleHelper.getShortestAngleDiff(blockEntity.headAngle.getValue(), target);
            blockEntity.headAngle.chase(target, 0.25F, Chaser.exp(5));
            blockEntity.headAngle.tickChaser();
        }
        blockEntity.headAnimation.chase(active ? 1 : 0, 0.25F, Chaser.exp(0.25F));
        blockEntity.headAnimation.tickChaser();
    }
}
