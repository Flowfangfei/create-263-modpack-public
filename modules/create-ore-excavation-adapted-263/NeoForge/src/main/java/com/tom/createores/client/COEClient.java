package com.tom.createores.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions;

import com.tom.createores.CreateOreExcavation;
import com.tom.createores.Registration;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = CreateOreExcavation.MODID)
public class COEClient {

    @SubscribeEvent
    public static void registerScreens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event) {
        event.register(Registration.VEIN_ATLAS_MENU.get(), com.tom.createores.menu.OreVeinAtlasScreen::new);
    }

	@SubscribeEvent
	public static void registerLayer(RegisterLayerDefinitions event) {
		event.registerLayerDefinition(DrillRenderer.LAYER_LOCATION, DrillRenderer::createModel);
	}

    @SubscribeEvent
    public static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(Registration.DRILL_TILE.get(), DrillRenderer::new);
        event.registerBlockEntityRenderer(Registration.KINETIC_INPUT_TILE.get(), KineticInputBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(Registration.EXTRACTOR_TILE.get(), DrillRenderer::new);
        event.registerBlockEntityRenderer(Registration.SAMPLE_DRILL_TILE.get(), DrillRenderer::new);
    }
}
