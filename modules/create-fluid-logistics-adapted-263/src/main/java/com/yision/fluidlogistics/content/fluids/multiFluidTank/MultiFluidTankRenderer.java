package com.yision.fluidlogistics.content.fluids.multiFluidTank;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;

import net.createmod.catnip.api.animation.LerpedFloat;
import net.createmod.catnip.api.client.render.FluidRenderHelper;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;

import net.neoforged.neoforge.fluids.FluidStack;

public class MultiFluidTankRenderer extends SafeBlockEntityRenderer<MultiFluidTankBlockEntity> {

	public MultiFluidTankRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	protected void renderSafe(MultiFluidTankBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer,
		int light, int overlay) {
		if (!be.isController())
			return;
		if (be.getWindowStyle() == MultiFluidTankBlock.WindowStyle.NONE) {
			return;
		}
		if (be.tankInventory.isEmpty()) {
			return;
		}

		LerpedFloat[] fluidLevel = be.getFluidLevel();
		if (fluidLevel == null)
			return;

		float capHeight = 1 / 4f;
		float tankHullWidth = 1 / 16f + 1 / 128f;
		float minPuddleHeight = 1 / 16f;
		float totalHeight = be.height - 2 * capHeight - minPuddleHeight;

		SmartMultiFluidTank tank = be.tankInventory;
		if (tank.isEmpty())
			return;
		FluidStack[] fluidStacks = tank.getFluids();
		float accheight = 0;
		for (int i = 0; i < fluidStacks.length; i++) {
			FluidStack fluidStack = fluidStacks[i];
			if (fluidStack.isEmpty())
				continue;

			float level = fluidLevel[i].getValue(partialTicks);
			if (level < 1 / (512f * totalHeight))
				continue;
			float clampedLevel = Mth.clamp(level * totalHeight, 0, totalHeight);

			float xMin = tankHullWidth;
			float xMax = xMin + be.width - 2 * tankHullWidth;
			float yMin = totalHeight + capHeight + minPuddleHeight - clampedLevel;
			float yMax = yMin + clampedLevel;
			float zMin = tankHullWidth;
			float zMax = zMin + be.width - 2 * tankHullWidth;

			ms.pushPose();
			ms.translate(0, clampedLevel - totalHeight + accheight, 0);
			FluidRenderHelper.renderFluidBox(fluidStack, xMin, yMin, zMin, xMax, yMax, zMax, buffer,
				ms, light, false, true);
			ms.popPose();

			accheight += clampedLevel;
		}
	}

	public boolean shouldRenderOffScreen(MultiFluidTankBlockEntity be) { return be.isController(); }
    @Override public boolean shouldRenderOffScreen() { return true; }

    // Explicit native entry point: the parent SafeBlockEntityRenderer.submit is empty.
    @Override public net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState createRenderState() {
        return new com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<MultiFluidTankBlockEntity>();
    }
    @Override public void extractRenderState(MultiFluidTankBlockEntity be,
        net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state, float partialTicks,
        net.minecraft.world.phys.Vec3 cameraPosition,
        net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay overlay) {
        net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState.extractBase(be, state, overlay);
        if (state instanceof com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<?> raw) {
            @SuppressWarnings("unchecked") var target = (com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<MultiFluidTankBlockEntity>) raw;
            target.blockEntity = be;
            target.partialTicks = partialTicks;
        }
    }
    @Override public void submit(net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state,
        com.mojang.blaze3d.vertex.PoseStack pose, net.minecraft.client.renderer.SubmitNodeCollector collector,
        net.minecraft.client.renderer.state.level.CameraRenderState camera) {
        if (!(state instanceof com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<?> captured)
            || !(captured.blockEntity instanceof MultiFluidTankBlockEntity be) || isInvalid(be)) return;
        var geometry = new com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.GeometryCollector(collector);
        renderSafe(be, captured.partialTicks, pose, geometry, state.lightCoords, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        geometry.submit();
    }
}
