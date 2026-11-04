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
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderBehaviour;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderState;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.content.processing.burner.BlazeBurnerRenderer;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import plus.dragons.createdragonsplus.client.processing.blaze.BlazeRenderContext;
import plus.dragons.createdragonsplus.client.processing.blaze.BlazeRenderModels;

/** Client rendering attachment for {@link BlazeMovementBehaviour}. */
public class BlazeMovementRenderBehaviour implements MovementRenderBehaviour {
    public static BlazeMovementRenderBehaviour attachTo(BlazeMovementBehaviour behaviour) {
        return attachTo(behaviour, new BlazeMovementRenderBehaviour());
    }

    public static <T extends BlazeMovementRenderBehaviour> T attachTo(
            BlazeMovementBehaviour behaviour, T renderBehaviour) {
        if (behaviour.attachRender != null && behaviour.attachRender != renderBehaviour) {
            throw new IllegalStateException("Blaze movement behaviour already has a render attachment");
        }
        behaviour.attachRender = renderBehaviour;
        return renderBehaviour;
    }

    public void tick(MovementContext context) {
        if (!shouldRender(context) || context.position == null) {
            return;
        }
        RandomSource random = context.world.getRandom();
        Vec3 smokePos = context.position.add(
                VecHelper.offsetRandomly(Vec3.ZERO, random, 0.125F).multiply(1, 0, 1));
        if (random.nextInt(3) == 0 && context.motion.length() < 1 / 64F) {
            context.world.addParticle(
                    ParticleTypes.LARGE_SMOKE, smokePos.x, smokePos.y, smokePos.z, 0, 0, 0);
        }
        LerpedFloat headAngle = getHeadAngle(context);
        boolean quickTurn = !Mth.equal(context.relativeMotion.length(), 0);
        headAngle.chase(
                headAngle.getValue()
                        + AngleHelper.getShortestAngleDiff(headAngle.getValue(), getTargetAngle(context)),
                0.5F,
                quickTurn ? LerpedFloat.Chaser.EXP : LerpedFloat.Chaser.exp(5));
        headAngle.tickChaser();
        spawnParticles(context, BlazeBlock.getHeatLevelOf(context.state));
    }

    private boolean shouldRender(MovementContext context) {
        return BlazeBlock.getHeatLevelOf(context.state) != HeatLevel.NONE;
    }

    private LerpedFloat getHeadAngle(MovementContext context) {
        if (!(context.temporaryData instanceof LerpedFloat)) {
            context.temporaryData = LerpedFloat.angular().startWithValue(getTargetAngle(context));
        }
        return (LerpedFloat) context.temporaryData;
    }

    private float getTargetAngle(MovementContext context) {
        if (!Mth.equal(context.relativeMotion.length(), 0)
                && context.contraption.entity instanceof CarriageContraptionEntity carriage) {
            float angle = AngleHelper.deg(-Mth.atan2(context.relativeMotion.x, context.relativeMotion.z));
            return carriage.getInitialOrientation().getAxis() == Direction.Axis.X ? angle + 180 : angle;
        }
        Entity camera = Minecraft.getInstance().getCameraEntity();
        if (camera != null && !camera.isInvisible() && context.position != null) {
            Vec3 relative = context.contraption.entity.reverseRotation(
                    camera.position().subtract(context.position), 1);
            return AngleHelper.deg(-Mth.atan2(relative.z, relative.x)) - 90;
        }
        return 0;
    }

    private void spawnParticles(MovementContext context, HeatLevel heatLevel) {
        if (heatLevel == HeatLevel.NONE || context.position == null) {
            return;
        }
        RandomSource random = context.world.getRandom();
        if (random.nextInt(4) != 0) {
            return;
        }
        Vec3 flamePos = context.position
                .add(VecHelper.offsetRandomly(Vec3.ZERO, random, 0.5F)
                        .multiply(1, 0.25F, 1)
                        .normalize()
                        .scale(0.5 + random.nextDouble() * 0.25F))
                .add(0, 0.5, 0);
        double yMotion = random.nextDouble() * 0.0125F;
        if (heatLevel.isAtLeast(HeatLevel.SEETHING)) {
            context.world.addParticle(
                    ParticleTypes.SOUL_FIRE_FLAME, flamePos.x, flamePos.y, flamePos.z, 0, yMotion, 0);
        } else if (heatLevel.isAtLeast(HeatLevel.FADING)) {
            context.world.addParticle(
                    ParticleTypes.FLAME, flamePos.x, flamePos.y, flamePos.z, 0, yMotion, 0);
        }
    }

    @Override
    public MovementRenderState getRenderState(
            Vec3 camera,
            Font textRenderer,
            MovementContext context,
            VirtualRenderWorld renderWorld,
            Pose transform,
            Matrix4f worldMatrix) {
        if (!shouldRender(context)) {
            return null;
        }
        BlockState state = context.state;
        HeatLevel heatLevel = BlazeBlock.getHeatLevelOf(state);
        if (!heatLevel.isAtLeast(HeatLevel.FADING)) {
            heatLevel = HeatLevel.SMOULDERING;
        }
        float horizontalAngle = AngleHelper.rad(getHeadAngle(context).getValue(AnimationTickHolder.getPartialTicks(context.world)));
        BlazeMovementState renderState = new BlazeMovementState(transform, context.localPos);
        renderState.data = BlazeBurnerRenderer.getBlazeBurnerRenderData(
                context.world,
                state,
                heatLevel,
                0,
                horizontalAngle,
                false,
                false,
                null,
                context.hashCode());
        BlazeRenderModels.apply(
                renderState.data,
                new BlazeRenderContext(state, heatLevel, null, context.blockEntityData));
        return renderState;
    }

    public static class BlazeMovementState implements MovementRenderState {
        public BlazeBurnerRenderer.BlazeBurnerRenderData data;
        private final Pose transform;
        private final BlockPos pos;

        public BlazeMovementState(Pose transform, BlockPos pos) {
            this.transform = transform;
            this.pos = pos;
        }

        @Override
        public void submit(PoseStack matrices, SubmitNodeCollector queue) {
            matrices.pushPose();
            transform(matrices, transform, pos);
            data.submit(matrices, queue);
            matrices.popPose();
        }
    }
}
