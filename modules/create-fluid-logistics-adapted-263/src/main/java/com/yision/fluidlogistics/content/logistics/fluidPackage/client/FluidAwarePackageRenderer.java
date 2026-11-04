package com.yision.fluidlogistics.content.logistics.fluidPackage.client;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.logistics.box.PackageEntity;
import com.simibubi.create.content.logistics.box.PackageRenderer;
import com.yision.fluidlogistics.content.logistics.fluidPackage.FluidPackageItem;
import com.yision.fluidlogistics.registry.AllPartialModels;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.api.math.AngleHelper;
import com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.Bufferer;
import com.yision.fluidlogistics.foundation.render.FluidLogisticsRenderCompat.ModelBuffer;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;

public class FluidAwarePackageRenderer extends PackageRenderer {

	public FluidAwarePackageRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	@Override
	public void render(PackageEntity entity, float yaw, float partialTicks, PoseStack ms,
		MultiBufferSource buffer, int light) {
		if (entity.box.isEmpty() || !(entity.box.getItem() instanceof FluidPackageItem)) {
			super.render(entity, yaw, partialTicks, ms, buffer, light);
			return;
		}

		PartialModel model = AllPartialModels.getFluidPackageModel(entity.box);
		if (model == null || model.get() == null) {
			super.render(entity, yaw, partialTicks, ms, buffer, light);
			return;
		}

		renderFluidPackageBox(entity, yaw, ms, buffer, light, model);
		renderFluidContents(entity, yaw, ms, buffer, light);
	}

	private static void renderFluidPackageBox(PackageEntity entity, float yaw, PoseStack ms,
		MultiBufferSource buffer, int light, PartialModel model) {
		ModelBuffer sbb = Bufferer.partial(model, Blocks.AIR.defaultBlockState());
		sbb.translate(-.5, .02, -.5)
			.rotateCentered(-AngleHelper.rad(yaw + 90), Direction.UP)
			.light(light)
			.nudge(entity.getId());

		sbb.renderInto(ms, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));
	}

	private static void renderFluidContents(PackageEntity entity, float yaw, PoseStack ms,
		MultiBufferSource buffer, int light) {
		ms.pushPose();

		TransformStack.of(ms)
			.rotate(-AngleHelper.rad(yaw + 90), Direction.UP)
			.nudge(entity.getId());

		ms.translate(0, .02, 0);

		FluidPackageItemRenderer.renderFluidContentsForEntity(entity.box, ms, buffer, light);

		ms.popPose();
	}
}
