package dev.ryanhcode.sable.neoforge;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.SableClient;
import dev.ryanhcode.sable.SableClientConfig;
import dev.ryanhcode.sable.fabric.network.FabricSablePacketContext;
import dev.ryanhcode.sable.network.tcp.SableTCPPacket;
import dev.ryanhcode.sable.network.tcp.SableTCPPackets;
import dev.ryanhcode.sable.physics.config.FloatingBlockMaterialDataHandler;
import dev.ryanhcode.sable.sublevel.render.SubLevelRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;
import java.util.concurrent.CompletableFuture;

@Mod(value = Sable.MOD_ID, dist = Dist.CLIENT)
public final class SableNeoForgeClient {
    public SableNeoForgeClient(ModContainer container, IEventBus modBus) {
        container.registerConfig(ModConfig.Type.CLIENT, SableClientConfig.SPEC);
        NeoForge.EVENT_BUS.addListener(net.neoforged.neoforge.client.event.ClientTickEvent.Post.class,event->SableClientSmoke.tick());
        modBus.addListener(FMLClientSetupEvent.class, event -> event.enqueueWork(SableClient::init));
        modBus.addListener(RegisterClientPayloadHandlersEvent.class, event ->
            SableTCPPackets.entries().stream().filter(SableTCPPackets.Entry::clientbound).forEach(entry -> registerPacket(event, entry)));
        modBus.addListener(AddClientReloadListenersEvent.class, event ->
            event.addListener(Sable.sablePath("sub_level_renderer"), (PreparableReloadListener) (state, prepare, barrier, apply) ->
                barrier.wait(null).thenRunAsync(() -> SubLevelRenderer.getDispatcher().onResourceManagerReload(state.resourceManager()), apply)));
        modBus.addListener(ModConfigEvent.Loading.class, event -> {
            if (event.getConfig().getSpec() == SableClientConfig.SPEC) SableClientConfig.onUpdate(false);
        });
        modBus.addListener(ModConfigEvent.Reloading.class, event -> {
            if (event.getConfig().getSpec() == SableClientConfig.SPEC) SableClientConfig.onUpdate(true);
        });
        NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingOut.class, event -> FloatingBlockMaterialDataHandler.clearMaterials());
    }
    private static <T extends SableTCPPacket> void registerPacket(RegisterClientPayloadHandlersEvent event, SableTCPPackets.Entry<T> entry) {
        event.register(entry.type(), (payload, context) -> payload.handle(new FabricSablePacketContext(Minecraft.getInstance().player)));
    }
}
