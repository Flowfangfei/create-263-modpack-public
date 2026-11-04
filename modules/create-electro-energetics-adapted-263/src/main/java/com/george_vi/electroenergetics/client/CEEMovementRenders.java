package com.george_vi.electroenergetics.client;

import static net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.client.render.*;
import com.george_vi.electroenergetics.content.gauge.ElectricGaugeBlock;
import com.george_vi.electroenergetics.content.railway_electrification.pantograph.PantographBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderBehaviour;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderState;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.kinetics.gauge.GaugeBlock;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import org.joml.Matrix4f;

public final class CEEMovementRenders {
    public static void register() {
        MovementBehaviour.REGISTRY.get(CEEBlocks.VOLTMETER.get()).attachRender = new Gauge(true);
        MovementBehaviour.REGISTRY.get(CEEBlocks.AMMETER.get()).attachRender = new Gauge(false);
        MovementBehaviour.REGISTRY.get(CEEBlocks.PANTOGRAPH.get()).attachRender = new Pantograph();
        MovementBehaviour.REGISTRY.get(CEEBlocks.RAIL_CONTACT_SHOE.get()).attachRender = new Shoe();
    }

    private static class Gauge implements MovementRenderBehaviour {
        private final boolean voltmeter;

        Gauge(boolean voltmeter) {
            this.voltmeter = voltmeter;
        }

        @Override
        public MovementRenderState getRenderState(
                Vec3 camera,
                Font textRenderer,
                MovementContext context,
                VirtualRenderWorld renderWorld,
                PoseStack.Pose transform,
                Matrix4f worldMatrix4f) {
            RenderCollector buffer = new RenderCollector();
            collect(context, renderWorld, transform, worldMatrix4f, buffer);
            return buffer::submit;
        }

        private void collect(
                MovementContext context,
                VirtualRenderWorld renderWorld,
                PoseStack.Pose transform,
                Matrix4f worldMatrix4f,
                RenderCollector buffer) {
            PoseStack ms = new PoseStack();
            BlockState state = context.state;

            // Interpolate dial state for smooth animation
            float partialTicks = AnimationTickHolder.getPartialTicks();
            float progress =
                    Mth.lerp(
                            partialTicks,
                            context.data.getFloatOr("PrevDialState", 0f),
                            context.data.getFloatOr("DialState", 0f));

            // Get gauge properties
            Direction facing = state.getValue(ElectricGaugeBlock.FACING);
            boolean axisAlongFirst = state.getValue(GaugeBlock.AXIS_ALONG_FIRST_COORDINATE);

            // Determine which model to use
            PartialModel headModel =
                    voltmeter
                            ? AllPartialModels.GAUGE_HEAD_SPEED
                            : AllPartialModels.GAUGE_HEAD_STRESS;

            int light = LightCoordsUtil.getLightCoords(renderWorld, context.localPos);

            // Render on the correct face
            for (Direction direction : Iterate.directions) {
                if (!shouldRenderHeadOnFace(state, facing, axisAlongFirst, direction)) continue;

                float dialPivot = 5.75f / 16f;
                ModelBuffers.partial(AllPartialModels.GAUGE_DIAL, state)
                        .transform(transform)
                        .translate(context.localPos)
                        .rotateCentered(
                                (float) ((-direction.toYRot() - 90) / 180 * Math.PI), Direction.UP)
                        .translate(0, dialPivot, dialPivot)
                        .rotate((float) (Math.PI / 2 * -progress), Direction.EAST)
                        .translate(0, -dialPivot, -dialPivot)
                        .light(light)
                        .useLevelLight(context.world, worldMatrix4f)
                        .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));

                ModelBuffers.partial(headModel, state)
                        .transform(transform)
                        .translate(context.localPos)
                        .rotateCentered(
                                (float) ((-direction.toYRot() - 90) / 180 * Math.PI), Direction.UP)
                        .light(light)
                        .useLevelLight(context.world, worldMatrix4f)
                        .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));
            }
        }

        private static boolean shouldRenderHeadOnFace(
                BlockState state, Direction facing, boolean axisAlongFirst, Direction face) {
            if (face.getAxis().isVertical()) return false;
            if (face == facing.getOpposite()) return false;

            Direction.Axis gaugeAxis = getAxis(state, facing, axisAlongFirst);
            if (face.getAxis() == gaugeAxis) return false;
            if (gaugeAxis == Direction.Axis.Y && face != facing) return false;

            return true;
        }

        private static Direction.Axis getAxis(
                BlockState state, Direction facing, boolean axisAlongFirst) {
            Direction.Axis pistonAxis = facing.getAxis();

            if (pistonAxis == Direction.Axis.X)
                return axisAlongFirst ? Direction.Axis.Y : Direction.Axis.Z;
            if (pistonAxis == Direction.Axis.Y)
                return axisAlongFirst ? Direction.Axis.X : Direction.Axis.Z;
            return axisAlongFirst ? Direction.Axis.X : Direction.Axis.Y;
        }
    }

    private static class Pantograph implements MovementRenderBehaviour {

        @Override
        public MovementRenderState getRenderState(
                Vec3 camera,
                Font textRenderer,
                MovementContext context,
                VirtualRenderWorld renderWorld,
                PoseStack.Pose transform,
                Matrix4f worldMatrix4f) {
            RenderCollector buffer = new RenderCollector();
            collect(context, renderWorld, transform, worldMatrix4f, buffer);
            return buffer::submit;
        }

        private void collect(
                MovementContext context,
                VirtualRenderWorld renderWorld,
                PoseStack.Pose transform,
                Matrix4f worldMatrix4f,
                RenderCollector buffer) {
            PoseStack ms = new PoseStack();
            BlockState state = context.state;
            float extensionState =
                    Mth.lerp(
                            AnimationTickHolder.getPartialTicks(),
                            context.data.getFloatOr("PrevExtensionState", 0f),
                            context.data.getFloatOr("CurrentExtensionState", 0f));

            float yRot = state.getValue(FACING).toYRot();
            if (state.getValue(FACING).getAxis() == Direction.Axis.Z) yRot += 180;

            int light = LightCoordsUtil.getLightCoords(renderWorld, context.localPos);
            int color =
                    DyeColor.byName(
                                    context.blockEntityData.getStringOr("Color", "white"),
                                    DyeColor.WHITE)
                            .getTextureDiffuseColor();
            if (state.getValue(PantographBlock.DOUBLE)) {
                float rotationFactor = 27;

                ModelBuffers.partial(CEEPartialModels.PANTOGRAPH_BASE_DOUBLE, state)
                        .transform(transform)
                        .translate(context.localPos)
                        .center()
                        .rotateYDegrees(yRot)
                        .uncenter()
                        .color(color)
                        .light(light)
                        .useLevelLight(context.world, worldMatrix4f)
                        .renderInto(ms, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));

                ModelBuffers.partial(CEEPartialModels.PANTOGRAPH_LOWER_ARMS_DOUBLE, state)
                        .transform(transform)
                        .translate(context.localPos)
                        .center()
                        .rotateYDegrees(yRot)
                        .uncenter()
                        .translate(0, 0.375, 0.5)
                        .rotateXDegrees(-90 + extensionState * rotationFactor)
                        .color(color)
                        .light(light)
                        .useLevelLight(context.world, worldMatrix4f)
                        .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));

                float lowerArmRadians = (-90 + extensionState * rotationFactor) * Mth.DEG_TO_RAD;
                double armHingePosY = Mth.cos(lowerArmRadians) * 1.5;
                double armHingePosX = Mth.sin(lowerArmRadians) * 1.5;

                float b = (float) (-0.4f - Math.abs(armHingePosX));
                float a = Mth.sqrt(-b * b + 2f * 2f);

                ModelBuffers.partial(CEEPartialModels.PANTOGRAPH_UPPER_ARMS_DOUBLE, state)
                        .transform(transform)
                        .translate(context.localPos)
                        .center()
                        .rotateYDegrees(yRot)
                        .uncenter()
                        .translate(0, armHingePosY + 0.375, armHingePosX + 0.5)
                        .rotateX((float) Mth.atan2(a, b) - Mth.HALF_PI)
                        .color(color)
                        .light(light)
                        .useLevelLight(context.world, worldMatrix4f)
                        .renderInto(ms, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));

                ModelBuffers.partial(CEEPartialModels.PANTOGRAPH_CONNECTING_SURFACE_DOUBLE, state)
                        .transform(transform)
                        .translate(context.localPos)
                        .center()
                        .rotateYDegrees(yRot)
                        .uncenter()
                        .translate(0, 0.5625 + a + armHingePosY, 1)
                        .light(light)
                        .useLevelLight(context.world, worldMatrix4f)
                        .renderInto(ms, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));

                float springHingePosY = Mth.cos(lowerArmRadians + 0.5f) * 0.425f;
                float springHingePosX = Mth.sin(lowerArmRadians + 0.5f) * 0.425f;

                ModelBuffers.partial(CEEPartialModels.PANTOGRAPH_SPRINGS_DOUBLE, state)
                        .transform(transform)
                        .translate(context.localPos)
                        .center()
                        .rotateYDegrees(yRot)
                        .uncenter()
                        .translate(0, springHingePosY + 0.375, springHingePosX + 0.5f)
                        .scaleZ((-springHingePosX + 0.5f) * (16 / 13f))
                        .rotateXDegrees(0)
                        .light(light)
                        .useLevelLight(context.world, worldMatrix4f)
                        .renderInto(ms, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));

            } else {
                ModelBuffers.partial(CEEPartialModels.PANTOGRAPH_BASE, state)
                        .transform(transform)
                        .translate(context.localPos)
                        .center()
                        .rotateYDegrees(yRot)
                        .uncenter()
                        .color(color)
                        .light(light)
                        .useLevelLight(context.world, worldMatrix4f)
                        .renderInto(ms, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));

                ModelBuffers.partial(CEEPartialModels.PANTOGRAPH_LOWER_ARM, state)
                        .transform(transform)
                        .translate(context.localPos)
                        .center()
                        .rotateYDegrees(yRot)
                        .uncenter()
                        .translate(0, 0.375, 0.8125)
                        .rotateXDegrees(-75 + extensionState * 30)
                        .color(color)
                        .light(light)
                        .useLevelLight(context.world, worldMatrix4f)
                        .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));
                float lowerArmRadians = (-75 + extensionState * 30) * Mth.DEG_TO_RAD;

                double armHingePosY = Mth.cos(lowerArmRadians) * 1.875;
                double armHingePosX = Mth.sin(lowerArmRadians) * 1.875;

                ModelBuffers.partial(CEEPartialModels.PANTOGRAPH_UPPER_ARM, state)
                        .transform(transform)
                        .translate(context.localPos)
                        .center()
                        .rotateYDegrees(yRot)
                        .uncenter()
                        .translate(0, 0.375, 0.8125)
                        .translate(0, armHingePosY, armHingePosX)
                        .rotateXDegrees(-1 - extensionState * 50)
                        .color(color)
                        .light(light)
                        .useLevelLight(context.world, worldMatrix4f)
                        .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));

                ModelBuffers.partial(CEEPartialModels.PANTOGRAPH_UPPER_ARM_ARM, state)
                        .transform(transform)
                        .translate(context.localPos)
                        .center()
                        .rotateYDegrees(yRot)
                        .uncenter()
                        .translate(12 / 16f, 0.375, 0.8125)
                        .translate(0, armHingePosY, armHingePosX)
                        .rotateXDegrees(-1 - extensionState * 50)
                        .rotateYDegrees(12.5f)
                        .color(color)
                        .light(light)
                        .useLevelLight(context.world, worldMatrix4f)
                        .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));

                ModelBuffers.partial(CEEPartialModels.PANTOGRAPH_UPPER_ARM_ARM, state)
                        .transform(transform)
                        .translate(context.localPos)
                        .center()
                        .rotateYDegrees(yRot)
                        .uncenter()
                        .translate(4 / 16f, 0.375, 0.8125)
                        .translate(0, armHingePosY, armHingePosX)
                        .rotateXDegrees(-1 - extensionState * 50)
                        .rotateYDegrees(-12.5f)
                        .color(color)
                        .light(light)
                        .useLevelLight(context.world, worldMatrix4f)
                        .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));

                float upperArmRadians = (89 - extensionState * 50) * Mth.DEG_TO_RAD;
                ModelBuffers.partial(CEEPartialModels.PANTOGRAPH_CONNECTING_SURFACE, state)
                        .transform(transform)
                        .translate(context.localPos)
                        .center()
                        .rotateYDegrees(yRot)
                        .uncenter()
                        .translate(0, 0.375, 0.8125)
                        .translate(0, armHingePosY, armHingePosX)
                        .translate(
                                0, Mth.cos(upperArmRadians) * 1.9, Mth.sin(upperArmRadians) * 1.9)
                        .light(light)
                        .useLevelLight(context.world, worldMatrix4f)
                        .renderInto(ms, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));

                ModelBuffers.partial(CEEPartialModels.PANTOGRAPH_SPRINGS, state)
                        .transform(transform)
                        .translate(context.localPos)
                        .center()
                        .rotateYDegrees(yRot)
                        .uncenter()
                        .translate(0, 0.375, 0.25)
                        .rotateXDegrees(-22 + extensionState * -20)
                        .scale(1, 1, 1 + extensionState / 2)
                        .light(light)
                        .useLevelLight(context.world, worldMatrix4f)
                        .renderInto(ms, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));

                ModelBuffers.partial(CEEPartialModels.PANTOGRAPH_CONNECTING_ROD, state)
                        .transform(transform)
                        .translate(context.localPos)
                        .center()
                        .rotateYDegrees(yRot)
                        .uncenter()
                        .translate(0, 0.375, 0.1875)
                        .rotateXDegrees(-77 + extensionState * 43)
                        .color(color)
                        .light(light)
                        .useLevelLight(context.world, worldMatrix4f)
                        .renderInto(ms, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));
            }
        }
    }

    private static class Shoe implements MovementRenderBehaviour {

        @Override
        public MovementRenderState getRenderState(
                Vec3 camera,
                Font textRenderer,
                MovementContext context,
                VirtualRenderWorld renderWorld,
                PoseStack.Pose transform,
                Matrix4f worldMatrix4f) {
            RenderCollector buffer = new RenderCollector();
            collect(context, renderWorld, transform, worldMatrix4f, buffer);
            return buffer::submit;
        }

        private void collect(
                MovementContext context,
                VirtualRenderWorld renderWorld,
                PoseStack.Pose transform,
                Matrix4f worldMatrix4f,
                RenderCollector buffer) {
            double distanceY =
                    Mth.lerp(
                            AnimationTickHolder.getPartialTicks(),
                            context.data.getDoubleOr("PrevDistanceY", 0d),
                            context.data.getDoubleOr("DistanceY", 0d));

            PoseStack ms = new PoseStack();
            BlockState state = context.state;
            int light = LightCoordsUtil.getLightCoords(renderWorld, context.localPos);

            float yRot = state.getValue(FACING).toYRot();
            if (state.getValue(FACING).getAxis() == Direction.Axis.X) yRot += 180;

            ModelBuffers.partial(CEEPartialModels.RAIL_CONTACT_SHOE_CONTACT, state)
                    .transform(transform)
                    .translate(context.localPos)
                    .center()
                    .rotateYDegrees(yRot)
                    .uncenter()
                    .translate(0.5f, -distanceY, 0.5f)
                    .light(light)
                    .useLevelLight(context.world, worldMatrix4f)
                    .renderInto(ms, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));

            ModelBuffers.partial(CEEPartialModels.RAIL_CONTACT_SHOE_HINGES, state)
                    .transform(transform)
                    .translate(context.localPos)
                    .center()
                    .rotateYDegrees(yRot)
                    .uncenter()
                    .translate(0.5f, -distanceY, 0.5f)
                    .rotateX((float) -Mth.atan2(-distanceY - 0.25f, 0.5f) + Mth.PI)
                    .scaleZ(1.5f)
                    .light(light)
                    .useLevelLight(context.world, worldMatrix4f)
                    .renderInto(ms, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));
        }
    }
}
