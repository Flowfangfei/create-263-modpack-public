package com.yision.fluidlogistics.content.equipment.mechanicalFluidGun;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.content.fluids.FluidTransportBehaviour.AttachmentTypes.ComponentPartials;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.yision.fluidlogistics.registry.AllPartialModels;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.Bufferer;
import com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.ModelBuffer;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class MechanicalFluidGunRenderer extends KineticBlockEntityRenderer<MechanicalFluidGunBlockEntity> {

	public MechanicalFluidGunRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	protected void renderSafe(MechanicalFluidGunBlockEntity be, float partialTicks, PoseStack ms,
		MultiBufferSource buffer, int light, int overlay) {
		if (VisualizationManager.supportsVisualization(be.getLevel()))
			return;
		ms.pushPose();
		MechanicalFluidGunMount.rotateModel(ms, be.getBlockState());

		renderMountedCog(be, ms, buffer, light);

		float yaw = be.yaw.getValue(partialTicks);
		float pitch = be.pitch.getValue(partialTicks);

		renderYawingPartial(be, AllPartialModels.MECHANICAL_FLUID_GUN_BASE, yaw, ms, buffer, light);

		renderSpoutPart(be, AllPartialModels.MECHANICAL_FLUID_GUN_GUN_BODY, yaw, pitch, ms, buffer, light);
		renderSpoutPart(be, AllPartialModels.MECHANICAL_FLUID_GUN_GUNPOINT_TOP, yaw, pitch, ms, buffer, light);
		renderSpoutPart(be, AllPartialModels.MECHANICAL_FLUID_GUN_GUNPOINT_MIDDLE, yaw, pitch, ms, buffer, light);
		renderSpoutPart(be, AllPartialModels.MECHANICAL_FLUID_GUN_GUNPOINT_BOTTOM, yaw, pitch, ms, buffer, light);

		ms.popPose();

		if (be.shouldRenderSourceInterface()) {
			renderSourceInterface(be, ms, buffer, light);
		}

	}

	private void renderYawingPartial(MechanicalFluidGunBlockEntity be, PartialModel model, float yaw,
		PoseStack ms, MultiBufferSource buffer, int light) {
		ms.pushPose();
		transformYaw(ms, yaw);
		renderPartial(be, model, ms, buffer, light);
		ms.popPose();
	}

	private void renderSpoutPart(MechanicalFluidGunBlockEntity be, PartialModel model, float yaw, float pitch,
		PoseStack ms, MultiBufferSource buffer, int light) {
		ms.pushPose();
		transformYaw(ms, yaw);
		transformPitch(ms, pitch);
		renderPartial(be, model, ms, buffer, light);
		ms.popPose();
	}

	private void renderPartial(MechanicalFluidGunBlockEntity be, PartialModel model,
		PoseStack ms, MultiBufferSource buffer, int light) {
		ModelBuffer buf = Bufferer.partial(model, be.getBlockState());
		buf.light(light).renderInto(ms, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));
	}

	private void renderSourceInterface(MechanicalFluidGunBlockEntity be, PoseStack ms,
		MultiBufferSource buffer, int light) {
		Direction sourceDirection = MechanicalFluidGunMount.getMountFace(be.getBlockState()).getOpposite();
		PartialModel partial = com.simibubi.create.AllPartialModels.PIPE_ATTACHMENTS
			.get(ComponentPartials.DRAIN)
			.get(sourceDirection);
		ModelBuffer attachment = Bufferer.partial(partial, be.getBlockState());
		attachment.light(light).renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));
	}

	private void renderMountedCog(MechanicalFluidGunBlockEntity be, PoseStack ms, MultiBufferSource buffer, int light) {
		BlockState state = be.getBlockState();
		ModelBuffer cog = Bufferer.partial(AllPartialModels.MECHANICAL_FLUID_GUN_COG, state);
		float angle = KineticBlockEntityRenderer.getAngleForBe(be, be.getBlockPos(),
			MechanicalFluidGunMount.getMountFace(state).getAxis());
		KineticBlockEntityRenderer.kineticRotationTransform(cog, be, net.minecraft.core.Direction.Axis.Y, angle, light)
			.renderInto(ms, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));
	}

	static void transformYaw(PoseStack ms, float yaw) {
		ms.translate(0.5, 14.0 / 16.0, 0.5);
		ms.rotate(Axis.YP.rotationDegrees(yaw));
		ms.translate(-0.5, -14.0 / 16.0, -0.5);
	}

	static void transformPitch(PoseStack ms, float pitch) {
		ms.translate(MechanicalFluidGunTarget.PITCH_PIVOT_X, MechanicalFluidGunTarget.PITCH_PIVOT_Y,
			MechanicalFluidGunTarget.PITCH_PIVOT_Z);
		ms.rotate(Axis.XP.rotationDegrees(pitch));
		ms.translate(-MechanicalFluidGunTarget.PITCH_PIVOT_X, -MechanicalFluidGunTarget.PITCH_PIVOT_Y,
			-MechanicalFluidGunTarget.PITCH_PIVOT_Z);
	}

	protected ModelBuffer getRotatedModel(MechanicalFluidGunBlockEntity be, BlockState state) {
		return Bufferer.partial(AllPartialModels.MECHANICAL_FLUID_GUN_COG, state);
	}

	public boolean shouldRenderOffScreen(MechanicalFluidGunBlockEntity be) { return true; }
    @Override public boolean shouldRenderOffScreen() { return true; }

    // Explicit native entry point: the parent SafeBlockEntityRenderer.submit is empty.
    @Override public net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState createRenderState() {
        return new com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<MechanicalFluidGunBlockEntity>();
    }
    @Override public void extractRenderState(MechanicalFluidGunBlockEntity be,
        net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state, float partialTicks,
        net.minecraft.world.phys.Vec3 cameraPosition,
        net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay overlay) {
        net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState.extractBase(be, state, overlay);
        if (state instanceof com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<?> raw) {
            @SuppressWarnings("unchecked") var target = (com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<MechanicalFluidGunBlockEntity>) raw;
            target.blockEntity = be;
            target.partialTicks = partialTicks;
        }
    }
    @Override public void submit(net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state,
        com.mojang.blaze3d.vertex.PoseStack pose, net.minecraft.client.renderer.SubmitNodeCollector collector,
        net.minecraft.client.renderer.state.level.CameraRenderState camera) {
        if (!(state instanceof com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<?> captured)
            || !(captured.blockEntity instanceof MechanicalFluidGunBlockEntity be) || isInvalid(be)) return;
        var geometry = new com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.GeometryCollector(collector);
        renderSafe(be, captured.partialTicks, pose, geometry, state.lightCoords, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        geometry.submit();
    }
}
