package com.yision.fluidlogistics.content.schematics.cannon;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.schematics.cannon.LaunchedItem;
import com.simibubi.create.content.schematics.cannon.LaunchedItem.ForBelt;
import com.simibubi.create.content.schematics.cannon.LaunchedItem.ForBlockState;
import com.simibubi.create.content.schematics.cannon.LaunchedItem.ForEntity;
import com.simibubi.create.content.schematics.cannon.SchematicannonRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.yision.fluidlogistics.registry.AllPartialModels;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.api.client.render.FluidRenderHelper;
import com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.Bufferer;
import com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.ModelBuffer;
import net.createmod.catnip.impl.neoforge.render.VirtualRenderHelper;
import net.minecraft.client.Minecraft;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class CopperSchematicannonRenderer extends SafeBlockEntityRenderer<CopperSchematicannonBlockEntity> {

	public CopperSchematicannonRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	protected void renderSafe(CopperSchematicannonBlockEntity blockEntity, float partialTicks, PoseStack poseStack,
		MultiBufferSource buffer, int light, int overlay) {
		if (!blockEntity.flyingBlocks.isEmpty()) {
			renderLaunchedBlocks(blockEntity, partialTicks, poseStack, buffer, light, overlay);
		}

		if (VisualizationManager.supportsVisualization(blockEntity.getLevel())) {
			return;
		}

		BlockState state = blockEntity.getBlockState();
		double[] cannonAngles = SchematicannonRenderer.getCannonAngles(
			blockEntity, blockEntity.getBlockPos(), partialTicks);
		double yaw = cannonAngles[0];
		double pitch = cannonAngles[1];
		double recoil = SchematicannonRenderer.getRecoil(blockEntity, partialTicks);

		poseStack.pushPose();
		VertexConsumer vertexConsumer = buffer.getBuffer(RenderTypes.solidMovingBlock());

		ModelBuffer connector = Bufferer.partial(AllPartialModels.COPPER_SCHEMATICANNON_CONNECTOR, state);
		connector.translate(.5f, 0, .5f);
		connector.rotate((float) ((yaw + 90) / 180 * Math.PI), Direction.UP);
		connector.translate(-.5f, 0, -.5f);
		connector.light(light).renderInto(poseStack, vertexConsumer);

		ModelBuffer pipe = Bufferer.partial(AllPartialModels.COPPER_SCHEMATICANNON_PIPE, state);
		pipe.translate(.5f, 15 / 16f, .5f);
		pipe.rotate((float) ((yaw + 90) / 180 * Math.PI), Direction.UP);
		pipe.rotate((float) (pitch / 180 * Math.PI), Direction.SOUTH);
		pipe.translate(-.5f, -15 / 16f, -.5f);
		pipe.translate(0, -recoil / 100, 0);
		pipe.light(light).renderInto(poseStack, vertexConsumer);

		poseStack.popPose();
	}

	private static void renderLaunchedBlocks(CopperSchematicannonBlockEntity blockEntity, float partialTicks,
		PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
		for (LaunchedItem launched : blockEntity.flyingBlocks) {
			if (launched.ticksRemaining == 0) {
				continue;
			}

			Vec3 start = Vec3.atCenterOf(blockEntity.getBlockPos().above());
			Vec3 target = Vec3.atCenterOf(launched.target);
			Vec3 distance = target.subtract(start);

			double yDifference = target.y - start.y;
			double throwHeight = Math.sqrt(distance.lengthSqr()) * .6f + yDifference;
			Vec3 cannonOffset = distance.add(0, throwHeight, 0).normalize().scale(2);
			start = start.add(cannonOffset);
			yDifference = target.y - start.y;

			float progress = ((float) launched.totalTicks - (launched.ticksRemaining + 1 - partialTicks))
				/ launched.totalTicks;
			Vec3 blockLocationXZ = target.subtract(start).scale(progress).multiply(1, 0, 1);
			float t = progress;
			double yOffset = 2 * (1 - t) * t * throwHeight + t * t * yDifference;
			Vec3 blockLocation = blockLocationXZ.add(0.5, yOffset + 1.5, 0.5).add(cannonOffset);

			poseStack.pushPose();
			poseStack.translate(blockLocation.x, blockLocation.y, blockLocation.z);
			poseStack.translate(.125f, .125f, .125f);
			poseStack.rotate(Axis.YP.rotationDegrees(360 * t));
			poseStack.rotate(Axis.XP.rotationDegrees(360 * t));
			poseStack.translate(-.125f, -.125f, -.125f);

			if (launched instanceof ForBlockState blockState) {
				BlockState state = blockState instanceof ForBelt
					? AllBlocks.SHAFT.getDefaultState()
					: blockState.state;
				poseStack.scale(.3f, .3f, .3f);
				FluidShotPayload.Data shot = FluidShotPayload.read(blockState.data);
				if (shot == null) {
					com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.submitBlock(state,poseStack,buffer,light,overlay);
				} else {
					FluidRenderHelper.renderFluidBox(
						shot.requiredFluid(), 0, 0, 0, 1, 1, 1,
						buffer, poseStack, light, true, true);
				}
			} else if (launched instanceof ForEntity) {
				poseStack.scale(1.2f, 1.2f, 1.2f);
				com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.submitItem(launched.stack,ItemDisplayContext.GROUND,poseStack,buffer,blockEntity.getLevel(),light,overlay);
			}

			poseStack.popPose();

		}
	}

	public boolean shouldRenderOffScreen(CopperSchematicannonBlockEntity blockEntity) { return true; }
    @Override public boolean shouldRenderOffScreen() { return true; }

	@Override
	public int getViewDistance() {
		return 128;
	}

    // Explicit native entry point: the parent SafeBlockEntityRenderer.submit is empty.
    @Override public net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState createRenderState() {
        return new com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<CopperSchematicannonBlockEntity>();
    }
    @Override public void extractRenderState(CopperSchematicannonBlockEntity be,
        net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state, float partialTicks,
        net.minecraft.world.phys.Vec3 cameraPosition,
        net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay overlay) {
        net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState.extractBase(be, state, overlay);
        if (state instanceof com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<?> raw) {
            @SuppressWarnings("unchecked") var target = (com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<CopperSchematicannonBlockEntity>) raw;
            target.blockEntity = be;
            target.partialTicks = partialTicks;
        }
    }
    @Override public void submit(net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state,
        com.mojang.blaze3d.vertex.PoseStack pose, net.minecraft.client.renderer.SubmitNodeCollector collector,
        net.minecraft.client.renderer.state.level.CameraRenderState camera) {
        if (!(state instanceof com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<?> captured)
            || !(captured.blockEntity instanceof CopperSchematicannonBlockEntity be) || isInvalid(be)) return;
        var geometry = new com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.GeometryCollector(collector);
        renderSafe(be, captured.partialTicks, pose, geometry, state.lightCoords, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        geometry.submit();
    }
}
