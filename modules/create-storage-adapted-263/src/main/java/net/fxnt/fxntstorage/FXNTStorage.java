package net.fxnt.fxntstorage;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.providers.ProviderType;
import net.fxnt.fxntstorage.backpack.BackpackItem;
import net.fxnt.fxntstorage.backpack.inventory.BackpackContainer;
import net.fxnt.fxntstorage.backpack.upgrade.UpgradeRegistry;
import net.fxnt.fxntstorage.compat.CuriosCompat;
import net.fxnt.fxntstorage.compat.constructionstick.ConstructionStickCompat;
import net.fxnt.fxntstorage.config.ConfigManager;
import net.fxnt.fxntstorage.container.StorageBoxEntityRenderer;
import net.fxnt.fxntstorage.init.*;
import net.fxnt.fxntstorage.network.PacketHandler;
import net.fxnt.fxntstorage.reserve_storage.ReserveStorageBoxEntityRenderer;
import net.fxnt.fxntstorage.simple_storage.CompactingRecipeHelper;
import net.fxnt.fxntstorage.simple_storage.SimpleStorageBoxEntityRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(FXNTStorage.MOD_ID)
public class FXNTStorage {

    public static final String MOD_ID = "fxntstorage";
    public static final Logger LOGGER = LogManager.getLogger(FXNTStorage.class);
    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID)
            .defaultCreativeTab((ResourceKey<CreativeModeTab>) null)
            .addDataGenerator(ProviderType.LANG, ModLangProvider::provide);
    public static final int MAX_EFFECTS_PER_SONG = 3;

    public static final boolean CURIOS_LOADED = ModList.get().isLoaded(ModCompats.CURIOS);
    public static final boolean EMI_LOADED = ModList.get().isLoaded(ModCompats.EMI);
    public static final boolean REI_LOADED = ModList.get().isLoaded(ModCompats.REI);
    public static final boolean THIRST_LOADED = ModList.get().isLoaded(ModCompats.THIRST_WAS_RECLAIMED);

    public FXNTStorage(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, ConfigManager.ClientConfig.CLIENT_SPEC);
        modContainer.registerConfig(ModConfig.Type.SERVER, ConfigManager.ServerConfig.SERVER_SPEC);


        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(this::registerCapabilities);
        modEventBus.register(new PacketHandler());

        ModBlocks.register();
        ModBlockEntities.register();
        ModItems.register();
        ModTabs.register(modEventBus);
        ModEffects.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModDataComponents.register(modEventBus);
        ModLootConditionTypes.register(modEventBus);
        ModLootFunctionTypes.register(modEventBus);
        ModConditions.register(modEventBus);
        ModAttachmentTypes.register(modEventBus);

        UpgradeRegistry.register();

        REGISTRATE.registerEventListeners(modEventBus);

        if (CURIOS_LOADED) loadCuriosCompat(modEventBus);
        if (ModList.get().isLoaded(ModCompats.CONSTRUCTION_STICK)) ConstructionStickCompat.init();
        if (ModList.get().isLoaded(ModCompats.EVERY_COMPAT)) loadOptionalIntegration("net.fxnt.fxntstorage.compat.everycomp.EveryCompCompat");
    }

    private static void loadOptionalIntegration(String className) {
        try {
            Class.forName(className).getMethod("init").invoke(null);
        } catch (ClassNotFoundException unavailable) {
            LOGGER.warn("Optional integration {} is preserved in optionalIntegrations and is not enabled for this 26.3 build", className);
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("Failed to initialize optional integration " + className, failure);
        }
    }

    private static void loadCuriosCompat(IEventBus bus) {
        NeoForge.EVENT_BUS.addListener(CuriosCompat::keepBackpack);
        NeoForge.EVENT_BUS.addListener(CuriosCompat::onCurioChange);
        bus.addListener(CuriosCompat::registerCapabilities);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModUnpackers.registerHandlers();
            ModInventoryIdentifiers.registerHandlers();
        });
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.RESERVE_STORAGE_BOX_ENTITY.get(), (e, d) -> net.fxnt.fxntstorage.util.StorageItemTransfer.block(e, e::getAutomationHandler));
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.STORAGE_BOX_ENTITY.get(), (e, d) -> net.fxnt.fxntstorage.util.StorageItemTransfer.block(e, e::getItemHandler));
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.SIMPLE_STORAGE_BOX_ENTITY.get(), (e, d) -> net.fxnt.fxntstorage.util.StorageItemTransfer.block(e, e::getCapabilityHandler));
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.BACKPACK_ENTITY.get(), (e, d) -> net.fxnt.fxntstorage.util.StorageItemTransfer.block(e, e::getItemHandler));
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.STORAGE_CONTROLLER_ENTITY.get(), (e, d) -> net.fxnt.fxntstorage.util.StorageItemTransfer.block(e, e::getItemHandler));
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.STORAGE_INTERFACE_ENTITY.get(), (e, d) -> net.fxnt.fxntstorage.util.StorageItemTransfer.block(e, e::getItemHandler));
        event.registerBlockEntity(Capabilities.Item.BLOCK, ModBlockEntities.STORAGE_INTERFACE_FILTERED_ENTITY.get(), (e, d) -> net.fxnt.fxntstorage.util.StorageItemTransfer.block(e, e::getItemHandler));
        event.registerItem(
                Capabilities.Item.ITEM,
                (itemStack, context) -> net.fxnt.fxntstorage.util.StorageItemTransfer.backpack(context),
                ModBlocks.BACKPACK.get(),
                ModBlocks.ANDESITE_BACKPACK.get(),
                ModBlocks.COPPER_BACKPACK.get(),
                ModBlocks.BRASS_BACKPACK.get(),
                ModBlocks.HARDENED_BACKPACK.get()
        );
    }

    public static Identifier modLoc(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @EventBusSubscriber(modid = MOD_ID)
    public static class ModEvents {

        @SubscribeEvent
        public static void register(RegisterEvent event) {
            event.register(BuiltInRegistries.RECIPE_SERIALIZER.key(), ModRecipes::register);
        }
    }

}
