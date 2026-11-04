package com.enxv.aerouniversaljoint.content;

import com.enxv.aerouniversaljoint.client.AeroUniversalJointPartials;
import com.mojang.blaze3d.vertex.PoseStack;

import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.enxv.aerouniversaljoint.client.render.FrozenKineticBlockEntityRenderer;
import dev.simulated_team.simulated.compat.create.KineticBlockEntityRenderer;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.content.blocks.swivel_bearing.SwivelBearingBlock;
import dev.simulated_team.simulated.index.SimPartialModels;
import dev.simulated_team.simulated.compat.render.CachedBuffers;
import dev.simulated_team.simulated.compat.render.SuperByteBuffer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class DampingStressBearingRenderer extends FrozenKineticBlockEntityRenderer<DampingStressBearingBlockEntity> {
    public DampingStressBearingRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    private void renderOutputBuffer(com.simibubi.create.content.kinetics.base.KineticBlockEntity be, SuperByteBuffer model, PoseStack pose, SubmitNodeCollector collector, int light) {
        kineticRotationTransform(model, be, getRotationAxisOf(be), getAngleForBe(be, be.getBlockPos(), getRotationAxisOf(be)), light).submit(pose, collector);
    }

    @Override
    protected void renderSafe(DampingStressBearingBlockEntity be, float partialTicks, PoseStack ms, SubmitNodeCollector buffer, int light, int overlay) {
        BlockState state = be.getBlockState();
        Direction facing = state.getValue(SwivelBearingBlock.FACING);
        Direction.Axis axis = ((IRotate) state.getBlock()).getRotationAxis(state);
        SubmitNodeCollector cutoutBuffer = buffer;
        KineticBlockEntity headKinetics = be.isAssembled() ? be.getOutputKinetics() : be;
        
        float headAngle;
        if (be.isAssembled()) {
            Float assembledAngle = computeAssembledHeadAngle(be, partialTicks);
            if (assembledAngle != null) {
                headAngle = assembledAngle;
            } else {
                headAngle = getAngleForBe(headKinetics, be.getBlockPos(), axis);
            }
        } else {
            headAngle = 0.0f;
        }

        SuperByteBuffer head = kineticRotationTransform(
                CachedBuffers.partialFacingVertical(AeroUniversalJointPartials.DAMPING_STRESS_BEARING_HEAD, state, facing),
                be,
                axis,
                headAngle,
                light);
        head.submit(ms, cutoutBuffer);

        renderOutputBuffer(be.isAssembled() ? be.getOutputKinetics() : be,
                CachedBuffers.partialFacing(SimPartialModels.SHAFT_SIXTEENTH, state, facing.getOpposite()),
                ms, cutoutBuffer, light);
        if (!be.isAssembled()) {
            renderRotatingBuffer(be, CachedBuffers.partialFacing(SimPartialModels.SHAFT_SIXTEENTH, state, facing), ms, cutoutBuffer, light);
        }
    }

    @Override
    protected SuperByteBuffer getRotatedModel(DampingStressBearingBlockEntity be, BlockState state) {
        return CachedBuffers.partialFacing(SimPartialModels.SHAFT_SIXTEENTH, state, state.getValue(SwivelBearingBlock.FACING).getOpposite());
    }

    @Nullable
    private static Float computeAssembledHeadAngle(DampingStressBearingBlockEntity be, float partialTicks) {
        if (!be.isAssembled() || be.getLevel() == null || be.getSubLevelID() == null) {
            return null;
        }

        SubLevel attached = SubLevelContainer.getContainer(be.getLevel()).getSubLevel(be.getSubLevelID());
        if (attached == null) {
            return null;
        }

        Pose3dc attachedPose = getRenderPose(attached, partialTicks);
        Pose3dc containingPose = getRenderPose(Sable.HELPER.getContaining(be), partialTicks);
        double angleDegrees = DampingStressBearingAngleHelper.extractRelativeAngleDegrees(
                be.getBlockState().getValue(SwivelBearingBlock.FACING),
                DampingStressBearingAngleHelper.resolvePlateFacing(be),
                containingPose,
                attachedPose);
        if (be.getBlockState().getValue(SwivelBearingBlock.FACING).getAxisDirection() == Direction.AxisDirection.NEGATIVE) {
            angleDegrees = -angleDegrees;
        }
        return (float) Math.toRadians(angleDegrees);
    }

    @Nullable
    private static Pose3dc getRenderPose(@Nullable SubLevel subLevel, float partialTicks) {
        if (subLevel == null) {
            return null;
        }
        if (subLevel instanceof ClientSubLevel clientSubLevel) {
            return clientSubLevel.renderPose(partialTicks);
        }
        return subLevel.logicalPose();
    }
}
