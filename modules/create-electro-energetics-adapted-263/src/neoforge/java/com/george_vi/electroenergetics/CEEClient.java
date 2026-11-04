package com.george_vi.electroenergetics;

import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import java.util.function.Supplier;

@Mod(value = CreateElectroEnergetics.ID, dist = Dist.CLIENT)
public class CEEClient {
    public CEEClient(IEventBus modEventBus) {
        CEEPartialModels.register();
        modEventBus.addListener(CEEClient::onLoadComplete);
        modEventBus.addListener((net.neoforged.neoforge.client.event.RegisterItemModelsEvent event) ->
            event.register(com.george_vi.electroenergetics.client.render.HandToolModel.ID, com.george_vi.electroenergetics.client.render.HandToolModel.Unbaked.CODEC));
        modEventBus.addListener((net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers event) ->
            event.registerEntityRenderer(CEEEntityTypes.DETACHED_NODE.get(),net.minecraft.client.renderer.entity.NoopRenderer::new));
    }

    public static void onLoadComplete(FMLLoadCompleteEvent event) {
        ModContainer container = ModList.get()
                .getModContainerById(CreateElectroEnergetics.ID)
                .orElseThrow(() -> new IllegalStateException("Create: Electro Energetics mod container missing on LoadComplete"));
        Supplier<IConfigScreenFactory> configScreen = () -> (mc, previousScreen) -> new ConfigurationScreen(container, previousScreen);
        container.registerExtensionPoint(IConfigScreenFactory.class, configScreen);
    }
}
