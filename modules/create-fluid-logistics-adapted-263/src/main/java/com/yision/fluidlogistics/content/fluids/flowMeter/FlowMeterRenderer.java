package com.yision.fluidlogistics.content.fluids.flowMeter;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.yision.fluidlogistics.registry.AllPartialModels;

import net.createmod.catnip.api.data.Iterate;
import com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.Bufferer;
import com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.ModelBuffer;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class FlowMeterRenderer extends SmartBlockEntityRenderer<FlowMeterBlockEntity> {
	public FlowMeterRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(FlowMeterBlockEntity be, float partialTicks, PoseStack pose,
		MultiBufferSource buffer, int light, int overlay) {
		super.renderSafe(be, partialTicks, pose, buffer, light, overlay);
		BlockState state = be.getBlockState();
		Level level = be.getLevel();
		BlockPos pos = be.getBlockPos();
		float pivot = 5.75f / 16;
		float progress = Mth.lerp(partialTicks, be.prevDialState, be.dialState);

		for (Direction face : Iterate.directions) {
			if (!FlowMeterBlock.shouldRenderDialOnFace(level, pos, state, face))
				continue;
			ModelBuffer dial = orient(Bufferer.partial(AllPartialModels.FLOW_METER_DIAL, state), face);
			dial.translate(0, pivot, pivot)
				.rotate(Mth.HALF_PI * (1 - progress), Direction.EAST)
				.translate(0, -pivot, -pivot)
				.light(light).renderInto(pose, buffer.getBuffer(RenderTypes.solidMovingBlock()));
			orient(Bufferer.partial(AllPartialModels.FLOW_METER_HEAD, state), face)
				.light(light).renderInto(pose, buffer.getBuffer(RenderTypes.solidMovingBlock()));
		}
	}

	private static ModelBuffer orient(ModelBuffer buffer, Direction face) {
		return buffer.rotateCentered((-face.toYRot() - 90) * Mth.DEG_TO_RAD, Direction.UP);
	}

    // Explicit native entry point: the parent SafeBlockEntityRenderer.submit is empty.
    @Override public net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState createRenderState() {
        return new com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<FlowMeterBlockEntity>();
    }
    @Override public void extractRenderState(FlowMeterBlockEntity be,
        net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state, float partialTicks,
        net.minecraft.world.phys.Vec3 cameraPosition,
        net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay overlay) {
        net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState.extractBase(be, state, overlay);
        if (state instanceof com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<?> raw) {
            @SuppressWarnings("unchecked") var target = (com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<FlowMeterBlockEntity>) raw;
            target.blockEntity = be;
            target.partialTicks = partialTicks;
        }
    }
    @Override public void submit(net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state,
        com.mojang.blaze3d.vertex.PoseStack pose, net.minecraft.client.renderer.SubmitNodeCollector collector,
        net.minecraft.client.renderer.state.level.CameraRenderState camera) {
        if (!(state instanceof com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<?> captured)
            || !(captured.blockEntity instanceof FlowMeterBlockEntity be) || isInvalid(be)) return;
        var geometry = new com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.GeometryCollector(collector);
        renderSafe(be, captured.partialTicks, pose, geometry, state.lightCoords, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        geometry.submit();
    }
}
