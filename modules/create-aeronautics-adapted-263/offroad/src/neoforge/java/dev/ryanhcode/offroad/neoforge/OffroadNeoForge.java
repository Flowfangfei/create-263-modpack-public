package dev.ryanhcode.offroad.neoforge;
import dev.ryanhcode.offroad.Offroad;
import dev.ryanhcode.offroad.events.OffroadCommonEvents;
import dev.ryanhcode.offroad.neoforge.service.NeoForgeOffroadConfigService;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
@Mod(Offroad.MOD_ID)
public class OffroadNeoForge {
    public OffroadNeoForge(IEventBus modBus,ModContainer container){
        foundry.veil.platform.registry.NeoForgeRegistryBindings.bind(Offroad.MOD_ID,modBus);
        modBus.register(NeoForgeOffroadConfigService.class);
        Offroad.getRegistrate().registerEventListeners(modBus);
        modBus.addListener((RegisterEvent event)->{
            if(event.getRegistry()==net.minecraft.core.registries.BuiltInRegistries.TRIGGER_TYPES){dev.ryanhcode.offroad.index.OffroadAdvancements.init();dev.ryanhcode.offroad.data.OffroadAdvancementTriggers.register();}
        });
        modBus.addListener((ModifyDefaultComponentsEvent event)->OffroadCommonEvents.modifyDefaultComponents((item,changes)->event.modify(item,(builder,registries,actualItem)->changes.accept(new dev.simulated_team.simulated.events.SimulatedCommonEvents.ComponentBuilder(){public <T> void set(net.minecraft.core.component.DataComponentType<T> type,T value){builder.set(type,value);}}))));
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.tick.LevelTickEvent.Post event)->OffroadCommonEvents.tickLevelEvent(event.getLevel()));
        Offroad.init();
        NeoForgeOffroadConfigService.register(container);
    }
}
