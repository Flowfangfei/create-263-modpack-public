package dev.simulated_team.simulated.neoforge;

import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.index.SimBlocks;
import dev.simulated_team.simulated.index.neoforge.NeoForgeSimStats;
import dev.simulated_team.simulated.index.neoforge.SimNeoForgeRecipeTypes;
import dev.simulated_team.simulated.index.neoforge.SimParticleTypesImpl;
import dev.simulated_team.simulated.neoforge.events.SimNeoForgeCommonEvents;
import dev.simulated_team.simulated.neoforge.service.NeoForgeSimConfigService;
import dev.simulated_team.simulated.neoforge.service.NeoForgeSimEntityDataSerialization;
import dev.simulated_team.simulated.neoforge.service.compat.NeoForgeSimPeripheralService;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(Simulated.MOD_ID)
public final class SimulatedNeoForge {
    public static final CreativeModeTab TAB = CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + Simulated.MOD_ID + ".group"))
            .icon(() -> new ItemStack(SimBlocks.PHYSICS_ASSEMBLER.get()))
            .displayItems((parameters,output)->{
                java.util.Set<net.minecraft.world.item.Item> seen=new java.util.LinkedHashSet<>();
                dev.simulated_team.simulated.registrate.SimulatedRegistrate.TAB_ITEMS.forEach(supplier->{var item=supplier.get();if(item!=net.minecraft.world.item.Items.AIR&&seen.add(item))output.accept(item);});
            })
            .build();

    public SimulatedNeoForge(final IEventBus modEventBus, final ModContainer modContainer) {
        foundry.veil.platform.registry.NeoForgeRegistryBindings.bind(Simulated.MOD_ID,modEventBus);
        // deferred register tab
        final DeferredRegister<CreativeModeTab> tabRegister = DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, Simulated.MOD_ID);
        tabRegister.register("main_tab", () -> TAB);
        tabRegister.register(modEventBus);

        NeoForge.EVENT_BUS.register(SimNeoForgeCommonEvents.class);
        modEventBus.register(SimNeoForgeCommonEvents.ModBusEvents.class);

        modEventBus.addListener(foundry.veil.api.network.VeilPacketManager::registerPayloads);
        SimParticleTypesImpl.register(modEventBus);
        SimNeoForgeRecipeTypes.register(modEventBus);

        NeoForgeSimEntityDataSerialization.register(modEventBus);
        Simulated.getRegistrate().registerEventListeners(modEventBus);

        NeoForgeSimStats.register(modEventBus);

        if (ModList.get().isLoaded("computercraft")) {
            modEventBus.register(NeoForgeSimPeripheralService.class);
        }

        Simulated.init();
        NeoForgeSimConfigService.register(modContainer);
    }
}
