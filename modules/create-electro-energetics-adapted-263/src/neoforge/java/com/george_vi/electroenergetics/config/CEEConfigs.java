package com.george_vi.electroenergetics.config;

import net.createmod.catnip.api.config.ConfigBase;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public class CEEConfigs {

    private static final Map<ModConfig.Type, ConfigBase> CONFIGS = new EnumMap<>(ModConfig.Type.class);

    private static final Map<ModConfig.Type, ModConfig> LOADED=new EnumMap<>(ModConfig.Type.class);
    public static void reload() {
        for (ModConfig modConfig : LOADED.values()) {
            var loaded=modConfig.getLoadedConfig();
            if(loaded==null) continue;
            try (var parsed=com.electronwill.nightconfig.core.file.CommentedFileConfig.of(modConfig.getFullPath())) {
                parsed.load();
                modConfig.getSpec().correct(parsed);
                loaded.config().clear();
                loaded.config().putAll(parsed);
                modConfig.getSpec().acceptConfig(loaded);
                net.neoforged.fml.ModList.get().getModContainerById(modConfig.getModId()).orElseThrow()
                    .acceptEvent(new ModConfigEvent.Reloading(modConfig));
            }
        }
    }
    private static CServer server;
    private static CClient client;

    public static CServer server() {
        return server;
    }

    public static CClient client() {
        return client;
    }

    public static ConfigBase byType(ModConfig.Type type) {
        return CONFIGS.get(type);
    }

    private static <T extends ConfigBase> T register(Supplier<T> factory, ModConfig.Type side) {
        Pair<T, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(builder -> {
            T config = factory.get();
            config.registerAll(builder);
            return config;
        });

        T config = specPair.getLeft();
        config.specification = specPair.getRight();
        CONFIGS.put(side, config);
        return config;
    }

    public static void register(ModLoadingContext context, ModContainer container) {
        client = register(CClient::new, ModConfig.Type.CLIENT);
        server = register(CServer::new, ModConfig.Type.SERVER);

        for (Map.Entry<ModConfig.Type, ConfigBase> pair : CONFIGS.entrySet())
            container.registerConfig(pair.getKey(), pair.getValue().specification);
    }

    @SubscribeEvent
    public static void onLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getModId().equals(com.george_vi.electroenergetics.CreateElectroEnergetics.ID)) LOADED.put(event.getConfig().getType(),event.getConfig());
        for (ConfigBase config : CONFIGS.values())
            if (config.specification == event.getConfig()
                    .getSpec())
                config.onLoad();
    }

    @SubscribeEvent
    public static void onReload(ModConfigEvent.Reloading event) {
        for (ConfigBase config : CONFIGS.values())
            if (config.specification == event.getConfig()
                    .getSpec())
                config.onReload();
    }
}
