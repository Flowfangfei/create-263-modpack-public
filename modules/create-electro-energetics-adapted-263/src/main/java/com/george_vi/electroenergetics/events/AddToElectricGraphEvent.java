package com.george_vi.electroenergetics.events;

import com.george_vi.electroenergetics.simulation.CircuitBuilder;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import net.minecraft.server.level.ServerLevel;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import java.util.function.Consumer;

public class AddToElectricGraphEvent {
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static final Event<Consumer<AddToElectricGraphEvent>> EVENT = EventFactory.createArrayBacked((Class<Consumer<AddToElectricGraphEvent>>) (Class<?>) Consumer.class,
        listeners -> event -> {
            for (Consumer<AddToElectricGraphEvent> listener : listeners) listener.accept(event);
        });

    public final CircuitBuilder builder;
    public final ServerLevel level;
    public final InfrastructureSavedData sd;

    public AddToElectricGraphEvent(CircuitBuilder builder, ServerLevel level, InfrastructureSavedData sd) {
        this.builder = builder;
        this.level = level;
        this.sd = sd;
    }
}
