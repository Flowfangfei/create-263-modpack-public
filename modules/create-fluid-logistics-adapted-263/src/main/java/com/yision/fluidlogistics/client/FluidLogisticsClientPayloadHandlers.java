package com.yision.fluidlogistics.client;

import com.yision.fluidlogistics.FluidLogistics;
import com.yision.fluidlogistics.infrastructure.network.FluidLogisticsPackets;
import net.createmod.catnip.net.base.BasePacketPayload;
import net.createmod.catnip.net.base.CatnipPacketRegistry;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

/** Installs handlers separately from the common payload/codec registration in NeoForge 26.3. */
@EventBusSubscriber(modid = FluidLogistics.MODID, value = Dist.CLIENT)
public final class FluidLogisticsClientPayloadHandlers {
    private FluidLogisticsClientPayloadHandlers() {}

    @SubscribeEvent
    public static void register(RegisterClientPayloadHandlersEvent event) {
        for (FluidLogisticsPackets packet : FluidLogisticsPackets.values())
            register(event, packet.packetType());
    }

    private static <T extends BasePacketPayload> void register(RegisterClientPayloadHandlersEvent event,
            CatnipPacketRegistry.PacketType<T> packet) {
        if (ClientboundPacketPayload.class.isAssignableFrom(packet.clazz())) {
            // The native two-argument overload dispatches on MAIN. Keep every payload's
            // existing handler and its actual receiving player; do not register serverbound packets.
            event.register(packet.type(), (payload, context) ->
                    ((ClientboundPacketPayload) payload).handle(context.player()));
        }
    }
}
