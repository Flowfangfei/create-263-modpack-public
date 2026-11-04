package dev.khloeleclair.create.additionallogistics.compat.computercraft;

import com.simibubi.create.compat.Mods;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import dev.khloeleclair.create.additionallogistics.CreateAdditionalLogistics;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.util.function.Function;

public class CALComputerCraftProxy {

    private static final Function<SmartBlockEntity, ? extends AbstractEventfulComputerBehavior> fallbackFactory = FallbackEventfulComputerBehavior::new;
    @Nullable
    private static Function<SmartBlockEntity, ? extends AbstractEventfulComputerBehavior> computerFactory;

    public static void register() {
        Mods.COMPUTERCRAFT.executeIfInstalled(() -> CALComputerCraftProxy::registerWithDependency);
    }

    private static void registerWithDependency() {
        try {
            // This implementation is built only with a Create distribution that provides its peripherals.
            Class<?> type = Class.forName("dev.khloeleclair.create.additionallogistics.compat.computercraft.implementation.CALComputerBehavior");
            var constructor = type.getConstructor(SmartBlockEntity.class);
            computerFactory = be -> {
                try { return (AbstractEventfulComputerBehavior) constructor.newInstance(be); }
                catch (ReflectiveOperationException ex) { throw new IllegalStateException(ex); }
            };
            type.getMethod("registerItemDetailProviders").invoke(null);
            type.getMethod("registerApis").invoke(null);
        } catch (ReflectiveOperationException ex) {
            CreateAdditionalLogistics.LOGGER.warn("ComputerCraft integration is unavailable in this Create Adapted build; using fallback behavior", ex);
        }
    }

    public static <T extends SmartBlockEntity> void registerPeripheralCapability(
            RegisterCapabilitiesEvent event, BlockEntityType<T> type,
            Function<T, AbstractEventfulComputerBehavior> behavior) {
        if (Mods.COMPUTERCRAFT.isLoaded())
            CALComputerCraftCapabilities.register(event, type, behavior);
    }

    public static AbstractEventfulComputerBehavior behavior(SmartBlockEntity sbe) {
        if (computerFactory == null)
            return fallbackFactory.apply(sbe);
        return computerFactory.apply(sbe);
    }

}
