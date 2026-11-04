package com.yision.fluidlogistics.content.fluids.infiniteFluidTank;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;

import net.createmod.catnip.api.animation.LerpedFloat;
import net.createmod.catnip.api.client.render.FluidRenderHelper;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;

import net.neoforged.neoforge.fluids.FluidStack;

public class InfiniteFluidTankRenderer extends SafeBlockEntityRenderer<InfiniteFluidTankBlockEntity> {

	public InfiniteFluidTankRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	protected void renderSafe(InfiniteFluidTankBlockEntity be, float partialTicks, PoseStack ms,
		MultiBufferSource buffer, int light, int overlay) {
		LerpedFloat fluidLevel = be.getFluidLevel();
		if (fluidLevel == null)
			return;

		FluidStack fluidStack = be.getTankInventory().getFluid();
		if (fluidStack.isEmpty())
			return;

		float capHeight = 1 / 4f;
		float tankHullWidth = 1 / 16f + 1 / 128f;
		float minPuddleHeight = 1 / 16f;
		float totalHeight = 1 - 2 * capHeight - minPuddleHeight;

		float level = fluidLevel.getValue(partialTicks);
		if (level < 1 / (512f * totalHeight))
			return;

		float clampedLevel = Mth.clamp(level * totalHeight, 0, totalHeight);
		boolean top = fluidStack.getFluid().getFluidType().isLighterThanAir();

		float xMin = tankHullWidth;
		float xMax = 1 - tankHullWidth;
		float yMin = totalHeight + capHeight + minPuddleHeight - clampedLevel;
		float yMax = yMin + clampedLevel;

		if (top) {
			yMin += totalHeight - clampedLevel;
			yMax += totalHeight - clampedLevel;
		}

		float zMin = tankHullWidth;
		float zMax = 1 - tankHullWidth;

		ms.pushPose();
		ms.translate(0, clampedLevel - totalHeight, 0);
		FluidRenderHelper.renderFluidBox(fluidStack, xMin, yMin, zMin, xMax, yMax, zMax,
			buffer, ms, light, false, true);
		ms.popPose();
	}

    // Explicit native entry point: the parent SafeBlockEntityRenderer.submit is empty.
    @Override public net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState createRenderState() {
        return new com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<InfiniteFluidTankBlockEntity>();
    }
    @Override public void extractRenderState(InfiniteFluidTankBlockEntity be,
        net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state, float partialTicks,
        net.minecraft.world.phys.Vec3 cameraPosition,
        net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay overlay) {
        net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState.extractBase(be, state, overlay);
        if (state instanceof com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<?> raw) {
            @SuppressWarnings("unchecked") var target = (com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<InfiniteFluidTankBlockEntity>) raw;
            target.blockEntity = be;
            target.partialTicks = partialTicks;
        }
    }
    @Override public void submit(net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state,
        com.mojang.blaze3d.vertex.PoseStack pose, net.minecraft.client.renderer.SubmitNodeCollector collector,
        net.minecraft.client.renderer.state.level.CameraRenderState camera) {
        if (!(state instanceof com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<?> captured)
            || !(captured.blockEntity instanceof InfiniteFluidTankBlockEntity be) || isInvalid(be)) return;
        var geometry = new com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.GeometryCollector(collector);
        renderSafe(be, captured.partialTicks, pose, geometry, state.lightCoords, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        geometry.submit();
    }
}
