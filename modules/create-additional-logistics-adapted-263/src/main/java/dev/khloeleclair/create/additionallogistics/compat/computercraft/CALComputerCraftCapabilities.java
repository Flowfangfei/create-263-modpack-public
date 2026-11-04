package dev.khloeleclair.create.additionallogistics.compat.computercraft;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import dan200.computercraft.api.peripheral.PeripheralCapability;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.util.function.Function;

/** Loaded only after the proxy has established that ComputerCraft is installed. */
final class CALComputerCraftCapabilities {
    private CALComputerCraftCapabilities() {}

    static <T extends SmartBlockEntity> void register(RegisterCapabilitiesEvent event, BlockEntityType<T> type,
            Function<T, AbstractEventfulComputerBehavior> behavior) {
        event.registerBlockEntity(PeripheralCapability.get(), type,
                (be, side) -> behavior.apply(be).getPeripheralCapability());
    }
}
