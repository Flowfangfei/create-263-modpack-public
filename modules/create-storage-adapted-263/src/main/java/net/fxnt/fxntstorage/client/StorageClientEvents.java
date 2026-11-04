package net.fxnt.fxntstorage.client;

import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.providers.ProviderType;
import net.createmod.ponder.api.client.PonderIndex;
import net.fxnt.fxntstorage.backpack.BackpackItem;
import net.fxnt.fxntstorage.backpack.client.menu.BackpackScreen;
import net.fxnt.fxntstorage.backpack.client.renderer.*;
import net.fxnt.fxntstorage.backpack.client.tooltip.BackpackClientTooltip;
import net.fxnt.fxntstorage.backpack.client.tooltip.BackpackTooltip;
import net.fxnt.fxntstorage.backpack.inventory.BackpackContainer;
import net.fxnt.fxntstorage.backpack.upgrade.UpgradeRegistry;
import net.fxnt.fxntstorage.backpack.upgrade.jetpack.JetpackAirOverlay;
import net.fxnt.fxntstorage.backpack.upgrade.workshop.WorkshopFlywheelLayouts;
import net.fxnt.fxntstorage.backpack.upgrade.workshop.WorkshopFlywheelRenderer;
import net.fxnt.fxntstorage.config.ConfigManager;
import net.fxnt.fxntstorage.container.StorageBoxEntityRenderer;
import net.fxnt.fxntstorage.container.StorageBoxScreen;
import net.fxnt.fxntstorage.container.mounted.StorageBoxMountedScreen;
import net.fxnt.fxntstorage.init.*;
import net.fxnt.fxntstorage.network.PacketHandler;
import net.fxnt.fxntstorage.ponder.CsPonderPlugin;
import net.fxnt.fxntstorage.reserve_storage.ReserveStorageBoxEntityRenderer;
import net.fxnt.fxntstorage.reserve_storage.ReserveStorageBoxScreen;
import net.fxnt.fxntstorage.simple_storage.CompactingRecipeHelper;
import net.fxnt.fxntstorage.simple_storage.SimpleStorageBoxEntityRenderer;
import net.fxnt.fxntstorage.simple_storage.SimpleStorageBoxScreen;
import net.fxnt.fxntstorage.simple_storage.mounted.SimpleStorageBoxMountedScreen;
import net.fxnt.fxntstorage.util.KeybindHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
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
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import net.fxnt.fxntstorage.FXNTStorage;
import static net.fxnt.fxntstorage.FXNTStorage.MOD_ID;

@Mod(value = MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
public class StorageClientEvents {
    public StorageClientEvents(IEventBus bus, ModContainer modContainer) {
        ConfigManager.migrateClientConfigFile();
        WorkshopFlywheelRenderer.init();
        bus.addListener(StorageClientEvents::registerTooltipComponent);
        if (!ModList.get().isLoaded("configured"))
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, (container,previous)->new ConfigurationScreen(container,previous));
    }

    private static void registerTooltipComponent(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(BackpackTooltip.class, BackpackClientTooltip::new);
    }


    private static ConfigManager.ClientConfig.SimpleStorageBoxConnectedTextures lastCTMode;

    @SubscribeEvent
    public static void onConfigLoad(final ModConfigEvent.Loading event) {
        if (Objects.equals(event.getConfig().getModId(), MOD_ID) && event.getConfig().getType().equals(ModConfig.Type.CLIENT)) {
            lastCTMode = ConfigManager.ClientConfig.SIMPLE_STORAGE_CONNECTED_TEXTURES.get();
        }
    }

    @SubscribeEvent
    public static void onConfigReload(final ModConfigEvent.Reloading event) {
        if (Objects.equals(event.getConfig().getModId(), MOD_ID) && event.getConfig().getType().equals(ModConfig.Type.CLIENT)) {
            if (Minecraft.getInstance().getConnection() != null && Minecraft.getInstance().player != null) {
                ConfigManager.ClientConfig.sendClientSettings();
            }
            // Re-mesh chunks only when the connected-texture mode changes
            ConfigManager.ClientConfig.SimpleStorageBoxConnectedTextures ctMode =
                    ConfigManager.ClientConfig.SIMPLE_STORAGE_CONNECTED_TEXTURES.get();
            if (ctMode != lastCTMode) {
                lastCTMode = ctMode;
                if (Minecraft.getInstance().level != null) {
                    Minecraft.getInstance().levelRenderer.invalidateCompiledGeometry(Minecraft.getInstance().level,Minecraft.getInstance().options,Minecraft.getInstance().gameRenderer.mainCamera(),Minecraft.getInstance().getBlockColors());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onClientSetup(final FMLClientSetupEvent event) {
        PonderIndex.addPlugin(new CsPonderPlugin());
    }

    @SubscribeEvent
    public static void onRecipesUpdated(RecipesReceivedEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            CompactingRecipeHelper.rebuild();
        }
    }

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        addPlayerLayer(event, net.minecraft.world.entity.player.PlayerModelType.WIDE);
        addPlayerLayer(event, net.minecraft.world.entity.player.PlayerModelType.SLIM);
    }

    private static void addPlayerLayer(EntityRenderersEvent.AddLayers event, net.minecraft.world.entity.player.PlayerModelType skinType) {
        net.minecraft.client.renderer.entity.player.AvatarRenderer<net.minecraft.client.player.AbstractClientPlayer> playerRenderer = event.getPlayerRenderer(skinType);

        if (playerRenderer != null) {
            playerRenderer.addLayer(new BackpackRenderPlayer(playerRenderer));
        }
    }

    @SubscribeEvent
    public static void registerEntityRenderer(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.STORAGE_BOX_ENTITY.get(), StorageBoxEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SIMPLE_STORAGE_BOX_ENTITY.get(), SimpleStorageBoxEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SMART_PASSER_ENTITY.get(), SmartBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.STORAGE_INTERFACE_FILTERED_ENTITY.get(), SmartBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.BACKPACK_ENTITY.get(), BackpackBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RESERVE_STORAGE_BOX_ENTITY.get(), ReserveStorageBoxEntityRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.MAGNET_PICKUP_ENTITY.get(), NoopRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterClientReloadListeners(AddClientReloadListenersEvent event) {
        event.addListener(FXNTStorage.modLoc("backpack_flywheels"),new WorkshopFlywheelLayouts.ReloadListener());
        event.addListener(FXNTStorage.modLoc("backpack_model_shapes"),new BackpackModelShapes.ReloadListener());
    }

    @SubscribeEvent
    public static void onRegisterAdditionalModels(ModelEvent.RegisterStandalone event) {
        BackpackWornModels.register(event);
    }

    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        var models=event.getBakingResult().itemStackModels();
        for(Item item:BuiltInRegistries.ITEM) if(item instanceof BackpackItem) {
            var key=BuiltInRegistries.ITEM.getKey(item);
            var original=models.get(key);
            if(original!=null && !(original instanceof BackpackItemModel)) models.put(key,new BackpackItemModel(original));
        }
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.SIMPLE_STORAGE_BOX_MENU.get(), SimpleStorageBoxScreen::createScreen);
        event.register(ModMenuTypes.SIMPLE_STORAGE_BOX_MOUNTED_MENU.get(), SimpleStorageBoxMountedScreen::createScreen);
        event.register(ModMenuTypes.STORAGE_BOX_MENU.get(), StorageBoxScreen::createScreen);
        event.register(ModMenuTypes.STORAGE_BOX_MOUNTED_MENU.get(), StorageBoxMountedScreen::createScreen);
        event.register(ModMenuTypes.BACKPACK_MENU.get(), BackpackScreen::new);
        event.register(ModMenuTypes.RESERVE_STORAGE_BOX_MENU.get(), ReserveStorageBoxScreen::new);
    }

    @SubscribeEvent
    public static void onKeyRegister(RegisterKeyMappingsEvent event) {
        event.register(KeybindHandler.TOGGLE_BACKPACK_KEY);
        event.register(KeybindHandler.TOGGLE_JETPACK_KEY);
        event.register(KeybindHandler.TOGGLE_JETPACK_HOVER_KEY);
        event.register(KeybindHandler.ORE_MINE_ANY_BLOCK);
        event.register(KeybindHandler.COMPACTING_WHEEL_KEY);
    }

    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.AIR_LEVEL, Identifier.fromNamespaceAndPath(MOD_ID, "remaining_air"), JetpackAirOverlay.INSTANCE);
    }
}
