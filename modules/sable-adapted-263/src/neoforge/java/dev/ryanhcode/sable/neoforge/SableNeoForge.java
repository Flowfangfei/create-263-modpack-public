package dev.ryanhcode.sable.neoforge;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.SableCommonEvents;
import dev.ryanhcode.sable.SableConfig;
import dev.ryanhcode.sable.command.SableCommand;
import dev.ryanhcode.sable.command.argument.SubLevelSelectorModifiers;
import dev.ryanhcode.sable.fabric.network.FabricSablePacketContext;
import dev.ryanhcode.sable.index.SableAttributes;
import dev.ryanhcode.sable.index.SableTicketTypes;
import dev.ryanhcode.sable.network.tcp.SableTCPPacket;
import dev.ryanhcode.sable.network.tcp.SableTCPPackets;
import dev.ryanhcode.sable.physics.config.FloatingBlockMaterialDataHandler;
import dev.ryanhcode.sable.physics.config.block_properties.PhysicsBlockPropertiesDefinitionLoader;
import dev.ryanhcode.sable.physics.config.dimension_physics.DimensionPhysicsData;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.GameData;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(Sable.MOD_ID)
public final class SableNeoForge {
    private static boolean initialized;
    public SableNeoForge(ModContainer container, IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(SableRuntimeProbe::tick);
        var attributes = DeferredRegister.<Attribute>create(Registries.ATTRIBUTE, Sable.MOD_ID);
        SableAttributes.PUNCH_STRENGTH = attributes.register(SableAttributes.PUNCH_STRENGTH_NAME, () -> SableAttributes.PUNCH_STRENGTH_ATTRIBUTE);
        SableAttributes.PUNCH_COOLDOWN = attributes.register(SableAttributes.PUNCH_COOLDOWN_NAME, () -> SableAttributes.PUNCH_COOLDOWN_ATTRIBUTE);
        attributes.register(modBus);
        modBus.addListener(EventPriority.LOWEST, SableNeoForge::registerContent);
        modBus.addListener(SableNeoForge::registerPayloads);
        modBus.addListener(FMLCommonSetupEvent.class, event -> event.enqueueWork(SableAttributes::register));
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, event -> SableCommand.register(event.getDispatcher(), event.getBuildContext()));
        NeoForge.EVENT_BUS.addListener(AddServerReloadListenersEvent.class, event -> {
            event.addListener(PhysicsBlockPropertiesDefinitionLoader.ID, PhysicsBlockPropertiesDefinitionLoader.INSTANCE);
            event.addListener(DimensionPhysicsData.ReloadListener.ID, DimensionPhysicsData.ReloadListener.INSTANCE);
            event.addListener(FloatingBlockMaterialDataHandler.ReloadListener.ID, FloatingBlockMaterialDataHandler.ReloadListener.INSTANCE);
        });
        NeoForge.EVENT_BUS.addListener(OnDatapackSyncEvent.class, event -> event.getRelevantPlayers().forEach(player ->
            SableCommonEvents.syncDataPacket(dev.ryanhcode.sable.network.tcp.SablePacketSink.player(player))));
        container.registerConfig(ModConfig.Type.COMMON, SableConfig.SPEC);
    }
    private static void registerContent(RegisterEvent event) {
        var last = GameData.getRegistrationOrder().stream().reduce((a,b) -> b).orElseThrow();
        if (!event.getRegistryKey().identifier().equals(last) || initialized) return;
        initialized = true;
        SableTicketTypes.SUB_LEVEL_LOADED = new net.minecraft.server.level.TicketType(
            net.minecraft.server.level.TicketType.NO_TIMEOUT,
            net.minecraft.server.level.TicketType.FLAG_LOADING | net.minecraft.server.level.TicketType.FLAG_SIMULATION);
        Sable.init();
        SubLevelSelectorModifiers.registerModifiers();
    }
    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Sable.MOD_ID).versioned("3.0.0-adapted.1");
        SableTCPPackets.entries().forEach(entry -> registerPacket(registrar, entry));
    }
    private static <T extends SableTCPPacket> void registerPacket(PayloadRegistrar registrar, SableTCPPackets.Entry<T> entry) {
        if (entry.clientbound()) registrar.playToClient(entry.type(), entry.codec());
        else registrar.playToServer(entry.type(), entry.codec(), (payload, context) ->
            payload.handle(new FabricSablePacketContext(context.player())));
    }
}
