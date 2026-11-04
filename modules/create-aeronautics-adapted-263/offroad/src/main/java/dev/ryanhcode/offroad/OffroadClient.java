package dev.ryanhcode.offroad;

import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import net.createmod.ponder.api.client.PonderIndex;
import dev.ryanhcode.offroad.content.ponder.OffroadPonderPlugin;
import dev.ryanhcode.offroad.content.blocks.borehead_bearing.BoreheadBearingVisual;
import dev.ryanhcode.offroad.content.blocks.rock_cutting_wheel.RockCuttingWheelActorRender;
import dev.ryanhcode.offroad.index.OffroadBlocks;
import dev.ryanhcode.offroad.index.OffroadBlockEntityTypes;
import dev.ryanhcode.offroad.index.OffroadPartialModels;
import dev.simulated_team.simulated.compat.create.LegacyItemTooltips;
import dev.simulated_team.simulated.compat.create.SableSubLevelRendererCompat;

public class OffroadClient {
	public static void init() {
		SimpleBlockEntityVisualizer.builder(OffroadBlockEntityTypes.BOREHEAD_BEARING.get())
				.factory(BoreheadBearingVisual::new).skipVanillaRender(dev.simulated_team.simulated.compat.create.SmartBlockEntityRenderer::shouldSkipVanillaRender).apply();
		registerSableImmediateRenderers();

		LegacyItemTooltips.register(Offroad.getRegistrate());

		PonderIndex.addPlugin(new OffroadPonderPlugin());

		OffroadPartialModels.init();
	}

	private static void registerSableImmediateRenderers() {
		SableSubLevelRendererCompat.registerLegacyRenderers(
				Offroad.LOGGER,
				OffroadBlockEntityTypes.BOREHEAD_BEARING.get(),
				OffroadBlockEntityTypes.ROCKCUTTING_WHEEL_BLOCK_ENTITY.get(),
				OffroadBlockEntityTypes.WHEEL_MOUNT.get()
		);
	}
}
