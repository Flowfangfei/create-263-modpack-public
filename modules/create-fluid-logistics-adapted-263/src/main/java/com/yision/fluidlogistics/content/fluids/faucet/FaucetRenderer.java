package com.yision.fluidlogistics.content.fluids.faucet;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.content.fluids.FluidTransportBehaviour.AttachmentTypes.ComponentPartials;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.yision.fluidlogistics.registry.AllPartialModels;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.api.client.render.FluidRenderHelper;
import com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.Bufferer;
import com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.ModelBuffer;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

import net.neoforged.neoforge.fluids.FluidStack;

public class FaucetRenderer extends SmartBlockEntityRenderer<FaucetBlockEntity> {
	private static final float BELT_STREAM_BOTTOM = -1f / 16f;
	private static final float STREAM_MIN = 6f / 16f;
	private static final float STREAM_MAX = 10f / 16f;
	private static final float STREAM_TOP = 4f / 16f;

	public FaucetRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(FaucetBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

		if (be.shouldRenderSourceInterface()) {
			Direction sourceSide = be.getBlockState().getValue(FaucetBlock.FACING).getOpposite();
			renderAttachment(be, ms, buffer, light, sourceSide, ComponentPartials.DRAIN);
		}

		if (!be.hasFluidToRender() || !be.getBlockState().getValue(FaucetBlock.OPEN)) {
			return;
		}

		FluidStack fluidStack = be.getRenderingFluid();
		if (fluidStack.isEmpty()) {
			return;
		}

		ms.pushPose();
		Direction facing = be.getBlockState().getValue(FaucetBlock.FACING);
		switch (facing) {
		case SOUTH -> rotateY(ms, 180);
		case WEST -> rotateY(ms, 270);
		case EAST -> rotateY(ms, 90);
		default -> {
		}
		}

		renderFluidStream(be, fluidStack, ms, buffer, light);
		ms.popPose();
	}

	private void rotateY(PoseStack ms, float angle) {
		ms.translate(0.5, 0, 0.5);
		ms.rotate(Axis.YP.rotationDegrees(angle));
		ms.translate(-0.5, 0, -0.5);
	}

	private void renderFluidStream(FaucetBlockEntity be, FluidStack fluidStack, PoseStack ms,
		MultiBufferSource buffer, int light) {
		StreamBounds bounds = new StreamBounds(STREAM_MIN, be.isProcessingOnBelt() ? BELT_STREAM_BOTTOM : -8f / 16f,
			STREAM_MIN, STREAM_MAX, STREAM_TOP, STREAM_MAX);

		if (be.isProcessing()) {
			float progress = be.getProcessingTicks() / 20f;
			float scale = 0.75f + 0.25f * Mth.sin(progress * (float) Math.PI);
			float center = 0.5f;
			bounds = bounds.scaleHorizontally(center, scale);
		}

		renderFluidBox(fluidStack, bounds, buffer, ms, light);
	}

	private void renderAttachment(FaucetBlockEntity be, PoseStack ms, MultiBufferSource buffer, int light,
		Direction direction, ComponentPartials partialType) {
		PartialModel partial = partialType == ComponentPartials.DRAIN
			? AllPartialModels.FAUCET_SOURCE_INTERFACE.get(direction)
			: null;
		if (partial == null) {
			return;
		}
		ModelBuffer attachment = Bufferer.partial(partial, be.getBlockState());
		attachment.light(light).renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));
	}

	private void renderFluidBox(FluidStack fluidStack, StreamBounds bounds, MultiBufferSource buffer, PoseStack ms,
		int light) {
		FluidRenderHelper.renderFluidBox(fluidStack, bounds.xMin(), bounds.yMin(), bounds.zMin(),
			bounds.xMax(), bounds.yMax(), bounds.zMax(), buffer, ms, light, true, true);
	}

	private record StreamBounds(float xMin, float yMin, float zMin, float xMax, float yMax, float zMax) {
		private StreamBounds scaleHorizontally(float center, float scale) {
			return new StreamBounds(scale(center, xMin, scale), yMin, scale(center, zMin, scale),
				scale(center, xMax, scale), yMax, scale(center, zMax, scale));
		}

		private static float scale(float center, float coordinate, float scale) {
			return center + (coordinate - center) * scale;
		}
	}

    // Explicit native entry point: the parent SafeBlockEntityRenderer.submit is empty.
    @Override public net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState createRenderState() {
        return new com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<FaucetBlockEntity>();
    }
    @Override public void extractRenderState(FaucetBlockEntity be,
        net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state, float partialTicks,
        net.minecraft.world.phys.Vec3 cameraPosition,
        net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay overlay) {
        net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState.extractBase(be, state, overlay);
        if (state instanceof com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<?> raw) {
            @SuppressWarnings("unchecked") var target = (com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<FaucetBlockEntity>) raw;
            target.blockEntity = be;
            target.partialTicks = partialTicks;
        }
    }
    @Override public void submit(net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state,
        com.mojang.blaze3d.vertex.PoseStack pose, net.minecraft.client.renderer.SubmitNodeCollector collector,
        net.minecraft.client.renderer.state.level.CameraRenderState camera) {
        if (!(state instanceof com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<?> captured)
            || !(captured.blockEntity instanceof FaucetBlockEntity be) || isInvalid(be)) return;
        var geometry = new com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.GeometryCollector(collector);
        renderSafe(be, captured.partialTicks, pose, geometry, state.lightCoords, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        geometry.submit();
    }
}
