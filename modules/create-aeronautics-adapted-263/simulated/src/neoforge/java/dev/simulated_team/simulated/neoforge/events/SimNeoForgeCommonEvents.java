package dev.simulated_team.simulated.neoforge.events;


import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.command.SimCommand;
import dev.simulated_team.simulated.content.end_sea.EndSeaPhysicsData;
import dev.simulated_team.simulated.data.advancements.SimAdvancementTriggers;
import dev.simulated_team.simulated.data.advancements.SimAdvancements;
import dev.simulated_team.simulated.events.SimulatedCommonEvents;
import dev.simulated_team.simulated.index.SimArmInteractions;
import dev.simulated_team.simulated.index.SimSoundEvents;
import dev.simulated_team.simulated.index.SimTags;
import dev.simulated_team.simulated.index.neoforge.NeoForgeSimStats;
import dev.simulated_team.simulated.multiloader.energy.SingleBattery;
import dev.simulated_team.simulated.multiloader.energy.SingleBatteryWrapper;
import dev.simulated_team.simulated.multiloader.inventory.AbstractContainer;
import dev.simulated_team.simulated.multiloader.inventory.neoforge.ContainerWrapper;
import dev.simulated_team.simulated.multiloader.tanks.SingleTank;
import dev.simulated_team.simulated.multiloader.tanks.neoforge.SingleTankWrapper;
import dev.simulated_team.simulated.neoforge.service.NeoForgeSimConfigService;
import dev.simulated_team.simulated.neoforge.service.NeoForgeSimInventoryService;
import net.createmod.catnip.api.config.ConfigBase;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.concurrent.CompletableFuture;

public class SimNeoForgeCommonEvents {

	@SubscribeEvent
	public static void loadChunk(final ChunkEvent.Load event) {
		SimulatedCommonEvents.onChunkLoad(event.getLevel(), event.getChunk(), event.isNewChunk());
	}

	@SubscribeEvent
	public static void playerLoggedIn(final PlayerEvent.PlayerLoggedInEvent event) {
		final Player player = event.getEntity();
		SimulatedCommonEvents.onPlayerLoggedIn(player);
	}

	@SubscribeEvent
	public static void registerCommands(final RegisterCommandsEvent event) {
		SimCommand.register(event.getDispatcher(), event.getBuildContext());
	}

	@SubscribeEvent
	public static void serverStopped(final ServerStoppedEvent event) {
		SimulatedCommonEvents.onServerStopped(event.getServer());
	}

	@SubscribeEvent
	public static void postServerTick(final ServerTickEvent.Post event) {
		final MinecraftServer server = event.getServer();
		for (final ServerLevel level : server.getAllLevels()) {
			SimulatedCommonEvents.onServerTickEnd(level);
		}
	}

	@SubscribeEvent
	public static void syncDataPack(final OnDatapackSyncEvent event) {
		EndSeaPhysicsData.syncDataPacket(packet -> event.getRelevantPlayers().forEach(player -> player.connection.send(packet)));
	}

	@SubscribeEvent
	public static void addReloadListeners(final AddServerReloadListenersEvent event) {
		event.addListener(Simulated.path("end_sea_physics"),EndSeaPhysicsData.ReloadListener.INSTANCE);
	}

	@SubscribeEvent
	public static void rightClickBlock(final PlayerInteractEvent.RightClickBlock event) {
		final InteractionResult result = SimulatedCommonEvents.rightClickBlock(event.getLevel(), event.getPos(), event.getEntity(), event.getItemStack());
		if (result != null) {
			event.setCancellationResult(result);
			event.setCanceled(true);
		}
	}

	public static class ModBusEvents {

		@SubscribeEvent
		public static void modifyDefaultComponents(final ModifyDefaultComponentsEvent event) {
			SimulatedCommonEvents.modifyDefaultComponents((item, changes) -> event.modify(item, (builder, registries, actualItem) ->
                changes.accept(new SimulatedCommonEvents.ComponentBuilder() {
                    public <T> void set(net.minecraft.core.component.DataComponentType<T> type, T value) { builder.set(type, value); }
                })));
		}

		@SubscribeEvent
		public static void register(final RegisterEvent event) {
			SimArmInteractions.init();

			if (event.getRegistry() == BuiltInRegistries.TRIGGER_TYPES) {
				SimAdvancements.register();
				SimAdvancementTriggers.register();
			}
		}



		

		@SubscribeEvent
		public static void registerCapabilities(final RegisterCapabilitiesEvent event) {
			for (final NeoForgeSimInventoryService.InventoryGetterHolder<? extends BlockEntity> getter : NeoForgeSimInventoryService.inventoryGetters) {
				event.registerBlockEntity(Capabilities.Item.BLOCK, getter.type(), (be, dir) -> {
					final AbstractContainer container = getter.castBlockEntityAndGetInv(be, dir);
					if (container == null) {
						return null;
					}

					return new ContainerWrapper<>(container);
				});
			}

			for (final NeoForgeSimInventoryService.TankGetterHolder<? extends BlockEntity> getter : NeoForgeSimInventoryService.fluidTankGetters) {
				event.registerBlockEntity(Capabilities.Fluid.BLOCK, getter.type(), (be, dir) -> {
					final SingleTank container = getter.castBlockEntityAndGetInv(be, dir);
					if (container == null) {
						return null;
					}

					return new SingleTankWrapper(container);
				});
			}

			for (final NeoForgeSimInventoryService.EnergyGetterHolder<? extends BlockEntity> getter : NeoForgeSimInventoryService.energyGetters) {
				event.registerBlockEntity(Capabilities.Energy.BLOCK, getter.type(), (be, dir) -> {
					final SingleBattery battery = getter.castBlockEntityAndGetInv(be, dir);
					if (battery == null) {
						return null;
					}

					return new SingleBatteryWrapper(battery);
				});
			}
		}

		@SubscribeEvent
		public static void loadConfig(final ModConfigEvent.Loading event) {
			for (final ConfigBase config : NeoForgeSimConfigService.CONFIGS.values()) {
				if (config.specification == event.getConfig().getSpec()) {
					config.onLoad();
				}
			}

		}

		@SubscribeEvent
		public static void reloadConfig(final ModConfigEvent.Reloading event) {
			for (final ConfigBase config : NeoForgeSimConfigService.CONFIGS.values()) {
				if (config.specification == event.getConfig().getSpec()) {
					config.onReload();
				}
			}

		}

		@SubscribeEvent
		public static void postRegister(final FMLLoadCompleteEvent event) {
			NeoForgeSimStats.bootstrap();
		}
	}

}
