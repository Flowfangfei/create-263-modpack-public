package dev.eriksonn.aeronautics;

import com.simibubi.create.content.kinetics.base.SingleAxisRotatingVisual;
import dev.engine_room.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import com.simibubi.create.foundation.item.ItemDescription;
import dev.eriksonn.aeronautics.content.ponder.AeroPonderPlugin;
import dev.eriksonn.aeronautics.content.blocks.mounted_potato_cannon.MountedPotatoCannonVisual;
import dev.eriksonn.aeronautics.content.blocks.propeller.bearing.gyroscopic_propeller_bearing.GyroscopicPropellerBearingVisual;
import dev.eriksonn.aeronautics.content.blocks.propeller.bearing.propeller_bearing.PropellerBearingVisual;
import dev.eriksonn.aeronautics.content.blocks.propeller.small.andesite.AndesitePropellerVisual;
import dev.eriksonn.aeronautics.content.blocks.propeller.small.wooden.WoodenPropellerVisual;
import dev.eriksonn.aeronautics.index.AeroClickInteractions;
import dev.eriksonn.aeronautics.index.AeroBlockEntityTypes;
import dev.eriksonn.aeronautics.index.AeroBlocks;
import dev.eriksonn.aeronautics.index.AeroPartialModels;
import dev.eriksonn.aeronautics.index.client.AeroClientRegistries;
import dev.eriksonn.aeronautics.index.client.AeroRenderTypes;
import dev.eriksonn.aeronautics.index.client.AeroSituationalMusic;
import net.createmod.ponder.api.client.PonderIndex;
import dev.simulated_team.simulated.compat.create.LegacyItemTooltips;
import dev.simulated_team.simulated.compat.create.SableSubLevelRendererCompat;

public class AeronauticsClient {
    public static void init() {
        AeroRenderTypes.init();
        registerVisuals();
        registerSableImmediateRenderers();

        AeroBlocks.DYED_ENVELOPE_BLOCKS.forEach(block ->
                ItemDescription.useKey(block, "block.aeronautics.white_envelope"));
        AeroBlocks.ENVELOPE_ENCASED_SHAFTS.forEach(block ->
                ItemDescription.useKey(block, "block.aeronautics.white_envelope_encased_shaft"));

        LegacyItemTooltips.register(Aeronautics.getRegistrate());

        PonderIndex.addPlugin(new AeroPonderPlugin());

        AeroPartialModels.init();
        AeroClickInteractions.init();
    }

    private static void registerSableImmediateRenderers() {
        SableSubLevelRendererCompat.registerLegacyRenderers(
                Aeronautics.LOGGER,
                AeroBlockEntityTypes.HOT_AIR_BURNER.get(),
                AeroBlockEntityTypes.STEAM_VENT.get(),
                AeroBlockEntityTypes.PROPELLER_BEARING.get(),
                AeroBlockEntityTypes.GYROSCOPIC_PROPELLER_BEARING.get(),
                AeroBlockEntityTypes.SMART_PROPELLER.get(),
                AeroBlockEntityTypes.ANDESITE_PROPELLER.get(),
                AeroBlockEntityTypes.WOODEN_PROPELLER.get(),
                AeroBlockEntityTypes.MOUNTED_POTATO_CANNON.get()
        );
    }

    private static void registerVisuals() {
        SimpleBlockEntityVisualizer.builder(AeroBlockEntityTypes.PROPELLER_BEARING.get()).factory(PropellerBearingVisual::new).skipVanillaRender(dev.simulated_team.simulated.compat.create.SmartBlockEntityRenderer::shouldSkipVanillaRender).apply();
        SimpleBlockEntityVisualizer.builder(AeroBlockEntityTypes.GYROSCOPIC_PROPELLER_BEARING.get()).factory(GyroscopicPropellerBearingVisual::new).skipVanillaRender(dev.simulated_team.simulated.compat.create.SmartBlockEntityRenderer::shouldSkipVanillaRender).apply();
        SimpleBlockEntityVisualizer.builder(AeroBlockEntityTypes.ENVELOPE_ENCASED_SHAFT.get()).factory(SingleAxisRotatingVisual::shaft).skipVanillaRender(dev.simulated_team.simulated.compat.create.SmartBlockEntityRenderer::shouldSkipVanillaRender).apply();
        SimpleBlockEntityVisualizer.builder(AeroBlockEntityTypes.ANDESITE_PROPELLER.get()).factory(AndesitePropellerVisual::new).skipVanillaRender(dev.simulated_team.simulated.compat.create.SmartBlockEntityRenderer::shouldSkipVanillaRender).apply();
        SimpleBlockEntityVisualizer.builder(AeroBlockEntityTypes.WOODEN_PROPELLER.get()).factory(WoodenPropellerVisual::new).skipVanillaRender(dev.simulated_team.simulated.compat.create.SmartBlockEntityRenderer::shouldSkipVanillaRender).apply();
        SimpleBlockEntityVisualizer.builder(AeroBlockEntityTypes.MOUNTED_POTATO_CANNON.get()).factory(MountedPotatoCannonVisual::new).skipVanillaRender(dev.simulated_team.simulated.compat.create.SmartBlockEntityRenderer::shouldSkipVanillaRender).apply();
    }
}
