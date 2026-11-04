package com.george_vi.electroenergetics.compat.computercraft;

import com.george_vi.electroenergetics.CEEBlockEntityTypes;
import com.george_vi.electroenergetics.compat.computercraft.peripherals.*;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.compat.computercraft.AbstractComputerBehaviour;
import com.zurrtum.create.compat.computercraft.implementation.ComputerBehaviour;
import com.zurrtum.create.compat.computercraft.implementation.peripherals.SyncedPeripheral;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;

import dan200.computercraft.api.peripheral.PeripheralLookup;

import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Function;

public final class CEEComputerBehaviour extends ComputerBehaviour {
    public CEEComputerBehaviour(SmartBlockEntity blockEntity) {
        super(blockEntity);
    }

    private static <T extends SmartBlockEntity> void register(
            BlockEntityType<T> type, Function<T, SyncedPeripheral<T>> factory) {
        BlockEntityBehaviour.add(type, CEEComputerBehaviour::new);
        PeripheralLookup.get()
                .registerForBlockEntity(
                        (blockEntity, direction) -> {
                            if (blockEntity.getBehaviour(AbstractComputerBehaviour.TYPE)
                                    instanceof CEEComputerBehaviour behaviour) {
                                if (behaviour.peripheral == null)
                                    behaviour.peripheral = factory.apply(blockEntity);
                                return behaviour.peripheral;
                            }
                            return null;
                        },
                        type);
    }

    public static void register() {
        register(CEEBlockEntityTypes.ACCUMULATOR.get(), AccumulatorPeripheral::new);
        register(CEEBlockEntityTypes.VOLTMETER.get(), ElectricGaugePeripheral::new);
        register(CEEBlockEntityTypes.AMMETER.get(), ElectricGaugePeripheral::new);
        register(CEEBlockEntityTypes.ENERGY_METER.get(), EnergyMeterPeripheral::new);
    }
}
