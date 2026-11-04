package com.yision.fluidlogistics.content.fluids.redstoneFluidValve;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.yision.fluidlogistics.registry.AllPartialModels;

import net.createmod.catnip.api.math.AngleHelper;
import com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.Bufferer;
import com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.ModelBuffer;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class RedstoneFluidValveRenderer extends SmartBlockEntityRenderer<RedstoneFluidValveBlockEntity> {

	public RedstoneFluidValveRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(RedstoneFluidValveBlockEntity blockEntity, float partialTicks, PoseStack poseStack,
		MultiBufferSource buffer, int light, int overlay) {
		BlockState state = blockEntity.getBlockState();
		Direction facing = state.getValue(RedstoneFluidValveBlock.FACING);
		Axis pipeAxis = RedstoneFluidValveBlock.getPipeAxis(state);
		Axis shaftAxis = RedstoneFluidValveBlock.getCoverAxis(state, pipeAxis);
		int pointerRotationOffset = pipeAxis.isHorizontal() && shaftAxis == Axis.X || pipeAxis.isVertical() ? 90 : 0;
		float pointerRotation = Mth.lerp(blockEntity.pointer.getValue(partialTicks), 0, -90);
		ModelBuffer pointer = Bufferer.partial(AllPartialModels.REDSTONE_FLUID_VALVE_POINTER, state);
		pointer.center()
			.rotateYDegrees(AngleHelper.horizontalAngle(facing))
			.rotateXDegrees(facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 90)
			.rotateYDegrees(pointerRotationOffset + pointerRotation)
			.uncenter()
			.light(light)
			.overlay(overlay)
			.renderInto(poseStack, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));
	}


    // Explicit native entry point: the parent SafeBlockEntityRenderer.submit is empty.
    @Override public net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState createRenderState() {
        return new com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<RedstoneFluidValveBlockEntity>();
    }
    @Override public void extractRenderState(RedstoneFluidValveBlockEntity be,
        net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state, float partialTicks,
        net.minecraft.world.phys.Vec3 cameraPosition,
        net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay overlay) {
        net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState.extractBase(be, state, overlay);
        if (state instanceof com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<?> raw) {
            @SuppressWarnings("unchecked") var target = (com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<RedstoneFluidValveBlockEntity>) raw;
            target.blockEntity = be;
            target.partialTicks = partialTicks;
        }
    }
    @Override public void submit(net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state,
        com.mojang.blaze3d.vertex.PoseStack pose, net.minecraft.client.renderer.SubmitNodeCollector collector,
        net.minecraft.client.renderer.state.level.CameraRenderState camera) {
        if (!(state instanceof com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<?> captured)
            || !(captured.blockEntity instanceof RedstoneFluidValveBlockEntity be) || isInvalid(be)) return;
        var geometry = new com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.GeometryCollector(collector);
        renderSafe(be, captured.partialTicks, pose, geometry, state.lightCoords, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        geometry.submit();
    }
}
