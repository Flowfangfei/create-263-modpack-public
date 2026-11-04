package com.george_vi.electroenergetics;

import com.george_vi.electroenergetics.content.electrical_panel.special_interaction.AnalogPanelAttachmentChangeStatePacket;
import com.george_vi.electroenergetics.content.electrical_panel.special_interaction.SetMenuPanelAttachmentOptionsPacket;
import com.george_vi.electroenergetics.content.electrical_panel.special_interaction.SetPanelAttachmentOptionsPacket;
import com.george_vi.electroenergetics.content.energy_meter.ChangeEnergyMeterStatePacket;
import com.george_vi.electroenergetics.content.fuse.ConfigureFusePacket;
import com.george_vi.electroenergetics.content.railway_electrification.catenary.ClearCatenaryPacket;
import com.george_vi.electroenergetics.content.railway_electrification.catenary.SendCatenaryPacket;
import com.george_vi.electroenergetics.content.railway_electrification.gauges.SyncTrainGaugeDataPacket;
import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.ChangeTrainSoundTypePacket;
import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.UpdateElectricTrainSoundPacket;
import com.george_vi.electroenergetics.content.transmission_distribution.transformer.ConfigureTransformerAttachmentPacket;
import com.george_vi.electroenergetics.content.wire.*;
import com.george_vi.electroenergetics.content.wire.interaction.InteractDetachedNodePacket;
import com.george_vi.electroenergetics.content.wire.interaction.InteractWirePacket;
import com.george_vi.electroenergetics.content.wire_spool.ChangeLengthWirePacket;
import com.george_vi.electroenergetics.foundation.SendSparkPacket;
import com.george_vi.electroenergetics.simulation.RequestVoltageDataPacket;
import com.george_vi.electroenergetics.simulation.SendVoltageDataPacket;
import com.george_vi.electroenergetics.simulation.infrastructure.SendNodeDataPacket;
import com.george_vi.electroenergetics.events.network.BasePacketPayload;
import com.george_vi.electroenergetics.events.network.ServerboundPacketPayload;
import com.george_vi.electroenergetics.events.network.ClientboundPacketPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.Locale;

public enum CEEPackets implements BasePacketPayload.PacketTypeProvider {
    SEND_WIRE_CONNECTIONS(SendWireConnectionsPacket.class, SendWireConnectionsPacket.STREAM_CODEC),
    SEND_VOLTAGE_DATA(SendVoltageDataPacket.class, SendVoltageDataPacket.STREAM_CODEC),
    SEND_NODE_DATA(SendNodeDataPacket.class, SendNodeDataPacket.STREAM_CODEC),
    CLEAR_WIRE_CONNECTIONS(ClearWireConnectionsPacket.class, ClearWireConnectionsPacket.STREAM_CODEC),
    SEND_CATENARY(SendCatenaryPacket.class, SendCatenaryPacket.STREAM_CODEC),
    CLEAR_CATENARY(ClearCatenaryPacket.class, ClearCatenaryPacket.STREAM_CODEC),
    INTERACT_WIRE(InteractWirePacket.class, InteractWirePacket.STREAM_CODEC),
    CHANGE_LENGTH(ChangeLengthWirePacket.class, ChangeLengthWirePacket.STREAM_CODEC),
    INTERACT_DETACHED_NODE(InteractDetachedNodePacket.class, InteractDetachedNodePacket.STREAM_CODEC),
    CHANGE_ENERGY_METER_STATE(ChangeEnergyMeterStatePacket.class, ChangeEnergyMeterStatePacket.STREAM_CODEC),
    CONFIGURE_TRANSFORMER_ATTACHMENT(ConfigureTransformerAttachmentPacket.class, ConfigureTransformerAttachmentPacket.STREAM_CODEC),
    UPDATE_ELECTRIC_TRAIN_SOUND(UpdateElectricTrainSoundPacket.class, UpdateElectricTrainSoundPacket.STREAM_CODEC),
    CHANGE_TRAIN_SOUND_TYPE(ChangeTrainSoundTypePacket.class, ChangeTrainSoundTypePacket.STREAM_CODEC),
    SEND_SPARK(SendSparkPacket.class, SendSparkPacket.STREAM_CODEC),
    SEND_WIRE_PARTICLE(SendWireParticlesPacket.class, SendWireParticlesPacket.STREAM_CODEC),
    SEND_POSITIONED_WIRE_PARTICLE(SendPositionedWireParticlesPacket.class, SendPositionedWireParticlesPacket.STREAM_CODEC),
    SEND_QUADRATIC_PARTICLES(SendQuadraticParticlesPacket.class, SendQuadraticParticlesPacket.STREAM_CODEC),
    REQUEST_VOLTAGE_DATA(RequestVoltageDataPacket.class, RequestVoltageDataPacket.STREAM_CODEC),
    SYNC_TRAIN_GAUGE_DATA(SyncTrainGaugeDataPacket.class, SyncTrainGaugeDataPacket.STREAM_CODEC),
    CONFIGURE_FUSE(ConfigureFusePacket.class, ConfigureFusePacket.STREAM_CODEC),
    ANALOG_LEVER_PANEL_CHANGE_STATE(AnalogPanelAttachmentChangeStatePacket.class, AnalogPanelAttachmentChangeStatePacket.STREAM_CODEC),
    SET_MENU_PANEL_ATTACHMENT_OPTIONS(SetMenuPanelAttachmentOptionsPacket.class, SetMenuPanelAttachmentOptionsPacket.STREAM_CODEC),
    SET_PANEL_ATTACHMENT_OPTIONS(SetPanelAttachmentOptionsPacket.class, SetPanelAttachmentOptionsPacket.STREAM_CODEC),
    ;

    private final CustomPacketPayload.Type<?> type;
    private final StreamCodec<? super RegistryFriendlyByteBuf, ?> codec;
    private final boolean serverbound;

    <T extends BasePacketPayload> CEEPackets(Class<T> clazz, StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        this.type = new CustomPacketPayload.Type<>(CreateElectroEnergetics.rl(name().toLowerCase(Locale.ROOT)));
        this.codec = codec;
        this.serverbound = ServerboundPacketPayload.class.isAssignableFrom(clazz);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends CustomPacketPayload> CustomPacketPayload.Type<T> getType() {
        return (CustomPacketPayload.Type<T>) type;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void register() {
        for (CEEPackets packet : values()) {
            if (packet.serverbound) {
                PayloadTypeRegistry.serverboundPlay().register((CustomPacketPayload.Type) packet.type, (StreamCodec) packet.codec);
                ServerPlayNetworking.registerGlobalReceiver(packet.<ServerboundPacketPayload>getType(), (payload, context) ->
                    context.server().execute(() -> {
                        if (!context.player().isSpectator())
                            payload.handle(context.player());
                    }));
            } else {
                PayloadTypeRegistry.clientboundPlay().register((CustomPacketPayload.Type) packet.type, (StreamCodec) packet.codec);
            }
        }
        com.george_vi.electroenergetics.events.network.ServerConfigPacket.register();
    }

    public boolean isServerbound() { return serverbound; }

    @Environment(EnvType.CLIENT)
    public static void registerClient() {
        com.george_vi.electroenergetics.events.network.CEEClientNetworking.register();
    }
}
