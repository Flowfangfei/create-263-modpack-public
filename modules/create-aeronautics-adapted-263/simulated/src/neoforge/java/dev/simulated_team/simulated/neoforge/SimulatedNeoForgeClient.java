package dev.simulated_team.simulated.neoforge;

import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.SimulatedClient;
import dev.simulated_team.simulated.index.SimSpriteShifts;
import dev.simulated_team.simulated.neoforge.events.SimNeoForgeClientEvents;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = Simulated.MOD_ID, dist = Dist.CLIENT)
public class SimulatedNeoForgeClient {

	public SimulatedNeoForgeClient(final IEventBus modEventBus, final ModContainer container) {
		// Register sprites before the first atlas stitch; Catnip refreshes them on reload.
		SimSpriteShifts.init();
		container.registerExtensionPoint(IConfigScreenFactory.class, ((c, l) -> new ConfigurationScreen(c,l)));

		NeoForge.EVENT_BUS.register(SimNeoForgeClientEvents.class);
        NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.client.event.ClientTickEvent.Post.class,event->SimClientSmoke.tick());
		modEventBus.register(SimNeoForgeClientEvents.ModBusEvents.class);
		SimulatedClient.PLUNGER_LAUNCHER_RENDER_HANDLER.registerListeners(NeoForge.EVENT_BUS);

		modEventBus.addListener(dev.simulated_team.simulated.index.SimRenderTypes::registerPipelines);
        modEventBus.addListener(foundry.veil.api.network.VeilPacketManager::registerClientReceivers);
        modEventBus.addListener(dev.simulated_team.simulated.compat.create.SimClientRegistration::register);
        modEventBus.addListener(dev.simulated_team.simulated.compat.create.GuiGameElement::registerRenderers);
        modEventBus.addListener(dev.simulated_team.simulated.client.model.SimulatedItemModels::register);
		modEventBus.addListener(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent.class,event->event.enqueueWork(()->SimulatedClient.init()));
	}

}