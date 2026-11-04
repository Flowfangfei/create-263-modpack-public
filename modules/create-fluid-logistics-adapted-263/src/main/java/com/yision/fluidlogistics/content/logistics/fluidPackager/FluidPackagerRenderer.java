package com.yision.fluidlogistics.content.logistics.fluidPackager;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.logistics.packager.PackagerBlock;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import com.simibubi.create.content.logistics.packager.PackagerRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.yision.fluidlogistics.registry.AllPartialModels;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.api.math.AngleHelper;
import com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.Bufferer;
import com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.ModelBuffer;
import net.minecraft.client.Minecraft;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class FluidPackagerRenderer<T extends PackagerBlockEntity> extends SmartBlockEntityRenderer<T> {

	private final PartialModel trayModel;
	private final boolean hideTrayAfterHalfway;

	public FluidPackagerRenderer(Context context) {
		this(context, AllPartialModels.FLUID_PACKAGER_TRAY, true);
	}

	protected FluidPackagerRenderer(Context context, PartialModel trayModel, boolean hideTrayAfterHalfway) {
		super(context);
		this.trayModel = trayModel;
		this.hideTrayAfterHalfway = hideTrayAfterHalfway;
	}

	@Override
	protected void renderSafe(T be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

		ItemStack renderedBox = be.getRenderedBox();
		float trayOffset = be.getTrayOffset(partialTicks);
		BlockState blockState = be.getBlockState();
		Direction facing = blockState.getValue(PackagerBlock.FACING)
			.getOpposite();

		if (!VisualizationManager.supportsVisualization(be.getLevel())) {
			var hatchModel = getSharedHatchModel(be);

			ModelBuffer sbb = Bufferer.partial(hatchModel, blockState);
			sbb.translate(Vec3.atLowerCornerOf(facing.getUnitVec3i())
				.scale(.49999f))
				.rotateYCenteredDegrees(AngleHelper.horizontalAngle(facing))
				.rotateXCenteredDegrees(AngleHelper.verticalAngle(facing))
				.light(light)
				.renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));

			if (!hideTrayAfterHalfway || trayOffset <= 0.5f) {
				sbb = Bufferer.partial(trayModel, blockState);
				sbb.translate(Vec3.atLowerCornerOf(facing.getUnitVec3i())
					.scale(trayOffset))
					.rotateYCenteredDegrees(facing.toYRot())
					.light(light)
					.renderInto(ms, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));
			}
		}

		if (!renderedBox.isEmpty() && (!hideTrayAfterHalfway || trayOffset <= 0.5f)) {
			ms.pushPose();
			var msr = TransformStack.of(ms);
			msr.translate(Vec3.atLowerCornerOf(facing.getUnitVec3i())
				.scale(trayOffset))
				.translate(.5f, .5f, .5f)
				.rotateYDegrees(facing.toYRot())
				.translate(0, 2 / 16f, 0)
				.scale(1.49f, 1.49f, 1.49f);
            com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.submitItem(renderedBox,ItemDisplayContext.FIXED,ms,buffer,be.getLevel(),light,overlay);
			ms.popPose();
		}
	}

	public static PartialModel getTrayModel() {
		return AllPartialModels.FLUID_PACKAGER_TRAY;
	}

	public static PartialModel getHatchModel(FluidPackagerBlockEntity be) {
		return getSharedHatchModel(be);
	}

	public static boolean isHatchOpen(FluidPackagerBlockEntity be) {
		return PackagerRenderer.isHatchOpen(be);
	}

	protected static PartialModel getSharedHatchModel(PackagerBlockEntity be) {
		return PackagerRenderer.isHatchOpen(be)
			? AllPartialModels.FLUID_PACKAGER_HATCH_OPEN
			: AllPartialModels.FLUID_PACKAGER_HATCH_CLOSED;
	}

    // Explicit native entry point: the parent SafeBlockEntityRenderer.submit is empty.
    @Override public net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState createRenderState() {
        return new com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<T>();
    }
    @Override public void extractRenderState(T be,
        net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state, float partialTicks,
        net.minecraft.world.phys.Vec3 cameraPosition,
        net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay overlay) {
        net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState.extractBase(be, state, overlay);
        if (state instanceof com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<?> raw) {
            @SuppressWarnings("unchecked") var target = (com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<T>) raw;
            target.blockEntity = be;
            target.partialTicks = partialTicks;
        }
    }
    @Override public void submit(net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state,
        com.mojang.blaze3d.vertex.PoseStack pose, net.minecraft.client.renderer.SubmitNodeCollector collector,
        net.minecraft.client.renderer.state.level.CameraRenderState camera) {
        if (!(state instanceof com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<?> captured)
            || captured.blockEntity == null) return;
        @SuppressWarnings("unchecked") T be = (T) captured.blockEntity;
        if (isInvalid(be)) return;
        var geometry = new com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.GeometryCollector(collector);
        renderSafe(be, captured.partialTicks, pose, geometry, state.lightCoords, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        geometry.submit();
    }
}
