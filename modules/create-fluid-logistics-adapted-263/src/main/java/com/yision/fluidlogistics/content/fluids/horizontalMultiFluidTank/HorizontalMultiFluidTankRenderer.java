package com.yision.fluidlogistics.content.fluids.horizontalMultiFluidTank;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.yision.fluidlogistics.content.fluids.multiFluidTank.SmartMultiFluidTank;

import net.createmod.catnip.api.animation.LerpedFloat;
import net.createmod.catnip.api.client.render.FluidRenderHelper;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;

import net.neoforged.neoforge.fluids.FluidStack;

public class HorizontalMultiFluidTankRenderer extends SafeBlockEntityRenderer<HorizontalMultiFluidTankBlockEntity> {

	public HorizontalMultiFluidTankRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	protected void renderSafe(HorizontalMultiFluidTankBlockEntity be, float partialTicks, PoseStack ms,
		MultiBufferSource buffer,
		int light, int overlay) {
		if (!be.isController())
			return;
		if (!be.window) {
			return;
		}
		if (be.getTankInventory().isEmpty()) {
			return;
		}

		LerpedFloat[] fluidLevel = be.getFluidLevel();
		if (fluidLevel == null)
			return;

		float capSize = 1 / 4f;
		float tankHullSize = 1 / 16f + 1 / 128f;
		float minPuddleHeight = 1 / 16f;
		float totalHeight = be.getWidth() - 2 * tankHullSize - minPuddleHeight;

		SmartMultiFluidTank tank = be.getTankInventory();
		if (tank.isEmpty())
			return;
		FluidStack[] fluidStacks = tank.getFluids();
		float accHeight = 0;
		for (int i = 0; i < fluidStacks.length; i++) {
			FluidStack fluidStack = fluidStacks[i];
			if (fluidStack.isEmpty())
				continue;

			float level = fluidLevel[i].getValue(partialTicks);
			if (level < 1 / (512f * totalHeight))
				continue;
			float clampedLevel = Mth.clamp(level * totalHeight, 0, totalHeight);

			Axis axis = be.getAxis();
			float xMin = axis == Axis.X ? capSize : tankHullSize;
			float xMax = axis == Axis.X ? xMin + be.getHeight() - 2 * capSize : xMin + be.getWidth() - 2 * tankHullSize;
			float yMin = totalHeight + tankHullSize + minPuddleHeight - clampedLevel;
			float yMax = yMin + clampedLevel;
			float zMin = axis == Axis.Z ? capSize : tankHullSize;
			float zMax = axis == Axis.Z ? zMin + be.getHeight() - 2 * capSize : zMin + be.getWidth() - 2 * tankHullSize;

			ms.pushPose();
			ms.translate(0, clampedLevel - totalHeight + accHeight, 0);
			FluidRenderHelper.renderFluidBox(fluidStack, xMin, yMin, zMin, xMax, yMax, zMax, buffer,
				ms, light, false, true);
			ms.popPose();

			accHeight += clampedLevel;
		}
	}

	public boolean shouldRenderOffScreen(HorizontalMultiFluidTankBlockEntity be) { return be.isController(); }
    @Override public boolean shouldRenderOffScreen() { return true; }

    // Explicit native entry point: the parent SafeBlockEntityRenderer.submit is empty.
    @Override public net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState createRenderState() {
        return new com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<HorizontalMultiFluidTankBlockEntity>();
    }
    @Override public void extractRenderState(HorizontalMultiFluidTankBlockEntity be,
        net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state, float partialTicks,
        net.minecraft.world.phys.Vec3 cameraPosition,
        net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay overlay) {
        net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState.extractBase(be, state, overlay);
        if (state instanceof com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<?> raw) {
            @SuppressWarnings("unchecked") var target = (com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<HorizontalMultiFluidTankBlockEntity>) raw;
            target.blockEntity = be;
            target.partialTicks = partialTicks;
        }
    }
    @Override public void submit(net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state,
        com.mojang.blaze3d.vertex.PoseStack pose, net.minecraft.client.renderer.SubmitNodeCollector collector,
        net.minecraft.client.renderer.state.level.CameraRenderState camera) {
        if (!(state instanceof com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.LegacyRenderState<?> captured)
            || !(captured.blockEntity instanceof HorizontalMultiFluidTankBlockEntity be) || isInvalid(be)) return;
        var geometry = new com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.GeometryCollector(collector);
        renderSafe(be, captured.partialTicks, pose, geometry, state.lightCoords, net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        geometry.submit();
    }
}
