package com.railwayteam.railways.content.animated_flywheel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.railwayteam.railways.content.palettes.PalettesColor;
import com.railwayteam.railways.content.palettes.PalettesFlywheelBlock;
import com.railwayteam.railways.registry.CRBlockPartials;
import com.railwayteam.railways.registry.CRPalettes;
import com.railwayteam.railways.internal.compat.client.render.CachedBuffers;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.contraptions.render.ActorVisual;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.Models;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.createmod.catnip.api.math.AngleHelper;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public final class FlywheelMovementRender {
    private FlywheelMovementRender() { }
    public static void register() {
        FlywheelMovementBehaviour.verifyMath();
        for (PalettesColor color:PalettesColor.values())
            if(getWheelModel(CRPalettes.Styles.FLYWHEEL.get(color).get().defaultBlockState())!=CRBlockPartials.FLYWHEELS.get(color))
                throw new IllegalStateException("Wrong flywheel model for " + color);
    }
    public static PartialModel getWheelModel(BlockState state) {
        return state.getBlock() instanceof PalettesFlywheelBlock block?CRBlockPartials.FLYWHEELS.get(block.getColor()):AllPartialModels.FLYWHEEL;
    }
    public static ActorVisual createVisual(VisualizationContext context,VirtualRenderWorld world,MovementContext movement) { return new FlywheelActorVisual(context,world,movement); }
    public static void submit(MovementContext context,PoseStack poses,SubmitNodeCollector collector,int light,float tick) {
        var axis=context.state.getValue(BlockStateProperties.AXIS);
        var direction=Direction.fromAxisAndDirection(axis,Direction.AxisDirection.POSITIVE);
        CachedBuffers.block(KineticBlockEntityRenderer.shaft(axis)).light(light)
            .rotateCenteredDegrees(KineticBlockEntityVisual.rotationOffset(context.state,axis,context.localPos),axis)
            .submit(net.minecraft.client.renderer.rendertype.RenderTypes.cutoutMovingBlock(),poses,collector);
        CachedBuffers.partial(getWheelModel(context.state),context.state).light(light).center()
            .rotate(new Quaternionf().rotateTo(0,1,0,direction.getStepX(),direction.getStepY(),direction.getStepZ()))
            .rotateYDegrees(FlywheelMovementBehaviour.getAngle(context,tick)).uncenter()
            .submit(net.minecraft.client.renderer.rendertype.RenderTypes.cutoutMovingBlock(),poses,collector);
    }
    private static final class FlywheelActorVisual extends ActorVisual {
        private final RotatingInstance shaft;
        private final TransformedInstance wheel;
        private final Matrix4f baseTransform = new Matrix4f();
        private float lastAngle = Float.NaN;

        private FlywheelActorVisual(
            VisualizationContext visualizationContext,
            VirtualRenderWorld simulationWorld,
            MovementContext movementContext
        ) {
            super(visualizationContext, simulationWorld, movementContext);

            BlockState state = movementContext.state;
            BlockPos pos = movementContext.localPos;
            Direction.Axis axis = state.getValue(BlockStateProperties.AXIS);
            Direction align = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);
            int blockLight = localBlockLight();

            shaft = instancerProvider.instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT))
                .createInstance();
            shaft.setRotationAxis(axis)
                .setRotationOffset(KineticBlockEntityVisual.rotationOffset(state, axis, pos))
                .setRotationalSpeed(0)
                .setPosition(pos)
                .rotateToFace(axis)
                .light(blockLight, 0)
                .setChanged();

            wheel = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.partial(getWheelModel(state)))
                .createInstance();
            wheel.translate(pos)
                .center()
                .rotate(new Quaternionf().rotateTo(0, 1, 0, align.getStepX(), align.getStepY(), align.getStepZ()))
                .light(blockLight, 0);
            baseTransform.set(wheel.pose);
            animate(FlywheelMovementBehaviour.getAngle(movementContext, 0));
        }

        @Override
        public void beginFrame() {
            float angle = FlywheelMovementBehaviour.getAngle(context, AnimationTickHolder.getPartialTicks());
            if (Math.abs(angle - lastAngle) < 0.001f)
                return;
            animate(angle);
        }

        private void animate(float angle) {
            wheel.setTransform(baseTransform)
                .rotateY(AngleHelper.rad(angle))
                .uncenter()
                .setChanged();
            lastAngle = angle;
        }

        @Override
        protected void _delete() {
            shaft.delete();
            wheel.delete();
        }
    }

}
