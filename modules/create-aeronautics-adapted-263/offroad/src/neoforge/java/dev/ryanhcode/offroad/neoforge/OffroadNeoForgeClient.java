package dev.ryanhcode.offroad.neoforge;

import dev.ryanhcode.offroad.Offroad;
import dev.ryanhcode.offroad.OffroadClient;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = Offroad.MOD_ID, dist = Dist.CLIENT)
public class OffroadNeoForgeClient {
	public OffroadNeoForgeClient(final IEventBus modBus, final ModContainer container) {
		this.listenClientEvents(modBus);
		container.registerExtensionPoint(IConfigScreenFactory.class, ((c, l) -> new ConfigurationScreen(c,l)));

		modBus.addListener(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent.class,event->event.enqueueWork(()->OffroadClient.init()));
	}

	private void listenClientEvents(final IEventBus modBus) {

	}
}
