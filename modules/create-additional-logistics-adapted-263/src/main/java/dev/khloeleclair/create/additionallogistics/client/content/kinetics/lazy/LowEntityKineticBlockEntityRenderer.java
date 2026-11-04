package dev.khloeleclair.create.additionallogistics.client.content.kinetics.lazy;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.foundation.render.CreateVisualizationManager;
import dev.khloeleclair.create.additionallogistics.client.CALRenderModels;
import dev.khloeleclair.create.additionallogistics.common.content.kinetics.lazy.base.AbstractLowEntityKineticBlockEntity;
import dev.khloeleclair.create.additionallogistics.common.content.kinetics.lazy.flexible.EncasedFlexibleShaftBlock;
import dev.khloeleclair.create.additionallogistics.common.registries.CALPartialModels;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class LowEntityKineticBlockEntityRenderer extends KineticBlockEntityRenderer<AbstractLowEntityKineticBlockEntity> {
    public static boolean overrideVisualization;
    public LowEntityKineticBlockEntityRenderer(BlockEntityRendererProvider.Context context) { super(context); }
    @Override public void submit(BlockEntityRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!(state instanceof KineticRenderState kinetic) || !(kinetic.blockEntity instanceof AbstractLowEntityKineticBlockEntity be) || isInvalid(be)) return;
        var block = be.getBlockState();
        boolean encased = block.getBlock() instanceof EncasedFlexibleShaftBlock;
        boolean instanced = !overrideVisualization && CreateVisualizationManager.supportsVisualization(be.getLevel());
        float baseAngle = (getRenderTime(be, kinetic.partialTicks) * be.getSpeed() * 3f / 10) % 360;
        if (!instanced && block.getBlock() instanceof ICogWheel cog && block.hasProperty(BlockStateProperties.AXIS)) {
            var axis = block.getValue(BlockStateProperties.AXIS);
            pose.pushPose();
            pose.translate(.5, .5, .5);
            pose.rotate(rotation(axis, getAngleForBe(be, be.getBlockPos(), axis, kinetic.partialTicks)));
            pose.translate(-.5, -.5, -.5);
            CALRenderModels.submit(cog.isLargeCog() ? AllPartialModels.SHAFTLESS_LARGE_COGWHEEL : AllPartialModels.SHAFTLESS_COGWHEEL,
                Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE), true, pose, collector, RenderTypes.solidMovingBlock(), state.lightCoords);
            pose.popPose();
        }
        for (Direction dir : Direction.values()) {
            float modifier = be.getRotationSpeedModifier(dir);
            if (modifier == 0) continue;
            var opening = modifier == 1 ? (encased ? CALPartialModels.ENCASED_SHAFT_OPENING : CALPartialModels.SHAFT_OPENING)
                : (encased ? CALPartialModels.ENCASED_SHAFT_OPENING_REVERSED : CALPartialModels.SHAFT_OPENING_REVERSED);
            CALRenderModels.submit(opening, dir.getOpposite(), false, pose, collector, RenderTypes.cutoutMovingBlock(), state.lightCoords);
            if (instanced) continue;
            float angle = (baseAngle * modifier + getRotationOffsetForPosition(be, be.getBlockPos(), dir.getAxis())) / 180f * (float)Math.PI;
            pose.pushPose();
            pose.translate(.5, .5, .5);
            pose.rotate(rotation(dir.getAxis(), angle));
            pose.translate(-.5, -.5, -.5);
            CALRenderModels.submit(AllPartialModels.SHAFT_HALF, dir, false, pose, collector, RenderTypes.solidMovingBlock(), state.lightCoords);
            pose.popPose();
        }
    }
}
