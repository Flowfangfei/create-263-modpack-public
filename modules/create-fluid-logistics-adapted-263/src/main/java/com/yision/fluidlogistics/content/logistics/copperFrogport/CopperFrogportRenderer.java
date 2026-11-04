package com.yision.fluidlogistics.content.logistics.copperFrogport;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.yision.fluidlogistics.registry.AllPartialModels;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.Bufferer;
import com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.ModelBuffer;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class CopperFrogportRenderer extends SmartBlockEntityRenderer<CopperFrogportBlockEntity> {

	public CopperFrogportRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(CopperFrogportBlockEntity blockEntity, float partialTicks, PoseStack poseStack,
		MultiBufferSource buffer, int light, int overlay) {


		if (VisualizationManager.supportsVisualization(blockEntity.getLevel())) {
			return;
		}

		CopperFrogportAnimationState animation = CopperFrogportAnimationState.create(blockEntity, partialTicks);

		poseStack.pushPose();
		applyAttachmentTransform(poseStack, animation);

		ModelBuffer body = Bufferer.partial(
			AllPartialModels.COPPER_FROGPORT_BODY,
			blockEntity.getBlockState());
		body.center()
			.rotateYDegrees(animation.yaw())
			.uncenter()
			.light(light)
			.overlay(overlay)
			.renderInto(poseStack, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));

		ModelBuffer head = Bufferer.partial(
			blockEntity.goggles
				? AllPartialModels.COPPER_FROGPORT_HEAD_GOGGLES
				: AllPartialModels.COPPER_FROGPORT_HEAD,
			blockEntity.getBlockState());
		head.center()
			.rotateYDegrees(animation.yaw())
			.uncenter()
			.translate(8 / 16f, 10 / 16f, 11 / 16f)
			.rotateXDegrees(animation.headPitch())
			.translateBack(8 / 16f, 10 / 16f, 11 / 16f)
			.light(light)
			.overlay(overlay)
			.renderInto(poseStack, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));

		ModelBuffer tongue = Bufferer.partial(AllPartialModels.COPPER_FROGPORT_TONGUE,
			blockEntity.getBlockState());
		tongue.center()
			.rotateYDegrees(animation.yaw())
			.uncenter()
			.translate(8 / 16f, 10 / 16f, 11 / 16f)
			.rotateXDegrees(animation.tonguePitch())
			.scale(1, 1, animation.tongueLength() / (7 / 16f))
			.translateBack(8 / 16f, 10 / 16f, 11 / 16f)
			.light(light)
			.overlay(overlay)
			.renderInto(poseStack, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));

		poseStack.popPose();

		if (animation.animating()) {
			renderPackage(
				blockEntity,
				poseStack,
				buffer,
				light,
				overlay,
				animation);
		}
	}

	private void renderPackage(CopperFrogportBlockEntity blockEntity, PoseStack poseStack,
		MultiBufferSource buffer, int light, int overlay,
		CopperFrogportAnimationState animation) {
		if (blockEntity.animatedPackage == null || animation.packageScale() < 0.45) {
			return;
		}

		Identifier key = BuiltInRegistries.ITEM.getKey(blockEntity.animatedPackage.getItem());
		if (key == BuiltInRegistries.ITEM.getDefaultKey()) {
			return;
		}

		ModelBuffer rig = Bufferer.partial(
			com.simibubi.create.AllPartialModels.PACKAGE_RIGGING.get(key),
			blockEntity.getBlockState());
		ModelBuffer box = Bufferer.partial(
			com.simibubi.create.AllPartialModels.PACKAGES.get(key),
			blockEntity.getBlockState());

		boolean depositing = blockEntity.currentlyDepositing;
		var worldOffset = animation.packageOffset(blockEntity);
		for (ModelBuffer rendered : new ModelBuffer[] { box, rig }) {
			rendered.translate(worldOffset)
				.center()
				.scale(animation.packageScale())
				.uncenter()
				.light(light)
				.overlay(overlay)
				.renderInto(poseStack, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));
			if (!depositing) {
				break;
			}
		}
	}

	private static void applyAttachmentTransform(PoseStack poseStack,
		CopperFrogportAnimationState animation) {
		poseStack.translate(0.5, 0.5, 0.5);
		poseStack.rotate(animation.orientation());
		poseStack.translate(-0.5, -0.5, -0.5);
	}

    // Explicit native entry point: the parent SafeBlockEntityRenderer.submit is empty.
    @Override public net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState createRenderState() {
        return new com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<CopperFrogportBlockEntity>();
    }
    @Override public void extractRenderState(CopperFrogportBlockEntity be,
        net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state, float partialTicks,
        net.minecraft.world.phys.Vec3 cameraPosition,
        net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay overlay) {
        net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState.extractBase(be, state, overlay);
        if (state instanceof com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<?> raw) {
            @SuppressWarnings("unchecked") var target = (com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<CopperFrogportBlockEntity>) raw;
            target.blockEntity = be;
            target.partialTicks = partialTicks;
        }
    }
    @Override public void submit(net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state,
        com.mojang.blaze3d.vertex.PoseStack pose, net.minecraft.client.renderer.SubmitNodeCollector collector,
        net.minecraft.client.renderer.state.level.CameraRenderState camera) {
        if (!(state instanceof com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<?> captured)
            || !(captured.blockEntity instanceof CopperFrogportBlockEntity be) || isInvalid(be)) return;
        if (be.addressFilter != null && !be.addressFilter.isBlank()) submitNameplateOnHover(be,Component.literal(be.addressFilter),captured.partialTicks,pose,collector,camera,state.lightCoords);
        var geometry = new com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.GeometryCollector(collector);
        renderSafe(be, captured.partialTicks, pose, geometry, state.lightCoords, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        geometry.submit();
    }
}
