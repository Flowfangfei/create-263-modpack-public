package com.george_vi.electroenergetics.events;

import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import net.minecraft.server.level.ServerLevel;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import java.util.function.Consumer;

public class FinishElectricSimulationEvent {
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static final Event<Consumer<FinishElectricSimulationEvent>> EVENT = EventFactory.createArrayBacked((Class<Consumer<FinishElectricSimulationEvent>>) (Class<?>) Consumer.class,
        listeners -> event -> {
            for (Consumer<FinishElectricSimulationEvent> listener : listeners) listener.accept(event);
        });

    public final SimulationResults results;
    public final ServerLevel level;
    public final InfrastructureSavedData sd;

    public FinishElectricSimulationEvent(SimulationResults results, ServerLevel level, InfrastructureSavedData sd) {
        this.results = results;
        this.level = level;
        this.sd = sd;
    }
}
