package com.yision.fluidlogistics.content.logistics.fluidTransporter;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.fluids.FluidTransportBehaviour.AttachmentTypes.ComponentPartials;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.api.data.Iterate;
import com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.Bufferer;
import com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.ModelBuffer;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

public class FluidTransporterRenderer extends SmartBlockEntityRenderer<FluidTransporterBlockEntity> {

	public FluidTransporterRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(FluidTransporterBlockEntity blockEntity, float partialTicks, PoseStack poseStack,
		MultiBufferSource buffer, int light, int overlay) {
		super.renderSafe(blockEntity, partialTicks, poseStack, buffer, light, overlay);

		for (Direction direction : Iterate.directions) {
			if (!blockEntity.shouldRenderInterface(direction)) {
				continue;
			}

			renderAttachment(blockEntity, poseStack, buffer, light, direction, ComponentPartials.RIM_CONNECTOR);
			renderAttachment(blockEntity, poseStack, buffer, light, direction, ComponentPartials.DRAIN);
		}
	}

	private void renderAttachment(FluidTransporterBlockEntity blockEntity, PoseStack poseStack,
		MultiBufferSource buffer,
		int light, Direction direction, ComponentPartials partialType) {
		PartialModel partial = AllPartialModels.PIPE_ATTACHMENTS.get(partialType).get(direction);
		ModelBuffer attachment = Bufferer.partial(partial, blockEntity.getBlockState());
		attachment.light(light).renderInto(poseStack, buffer.getBuffer(RenderTypes.solidMovingBlock()));
	}

    // Explicit native entry point: the parent SafeBlockEntityRenderer.submit is empty.
    @Override public net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState createRenderState() {
        return new com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<FluidTransporterBlockEntity>();
    }
    @Override public void extractRenderState(FluidTransporterBlockEntity be,
        net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state, float partialTicks,
        net.minecraft.world.phys.Vec3 cameraPosition,
        net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay overlay) {
        net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState.extractBase(be, state, overlay);
        if (state instanceof com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<?> raw) {
            @SuppressWarnings("unchecked") var target = (com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<FluidTransporterBlockEntity>) raw;
            target.blockEntity = be;
            target.partialTicks = partialTicks;
        }
    }
    @Override public void submit(net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state,
        com.mojang.blaze3d.vertex.PoseStack pose, net.minecraft.client.renderer.SubmitNodeCollector collector,
        net.minecraft.client.renderer.state.level.CameraRenderState camera) {
        if (!(state instanceof com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<?> captured)
            || !(captured.blockEntity instanceof FluidTransporterBlockEntity be) || isInvalid(be)) return;
        var geometry = new com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.GeometryCollector(collector);
        renderSafe(be, captured.partialTicks, pose, geometry, state.lightCoords, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        geometry.submit();
    }
}
