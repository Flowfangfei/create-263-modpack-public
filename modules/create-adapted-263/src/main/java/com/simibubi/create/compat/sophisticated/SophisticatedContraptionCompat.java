package com.simibubi.create.compat.sophisticated;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

import com.simibubi.create.api.contraption.BlockMovementChecks;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.p3pp3rf1y.sophisticatedstorage.block.IStorageBlock;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;

public final class SophisticatedContraptionCompat {
	public static final String ID = "create_sophisticated_compat";
	private static final Set<StorageBlockEntity> RESTORING_CHESTS =
		Collections.newSetFromMap(new IdentityHashMap<>());
	private static final DeferredRegister<MountedItemStorageType<?>> STORAGES =
		DeferredRegister.create(CreateRegistries.MOUNTED_ITEM_STORAGE_TYPE, ID);
	public static final DeferredHolder<MountedItemStorageType<?>, SophisticatedMountedStorage.Type> STORAGE =
		STORAGES.register("storage", SophisticatedMountedStorage.Type::new);
	private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, ID);
	public static final DeferredHolder<MenuType<?>, MenuType<MovingStorageMenu>> MENU =
		MENUS.register("storage", () -> IMenuTypeExtension.create(MovingStorageMenu::fromBuffer));
	public static final DeferredHolder<MenuType<?>, MenuType<MovingStorageSettingsMenu>> SETTINGS =
		MENUS.register("settings", () -> IMenuTypeExtension.create(MovingStorageSettingsMenu::fromBuffer));

	static void finishChestRestore(StorageBlockEntity chest) {
		RESTORING_CHESTS.add(chest);
	}

	public SophisticatedContraptionCompat(IEventBus modEventBus) {
		STORAGES.register(modEventBus);
		MENUS.register(modEventBus);
		modEventBus.addListener(this::setup);
		NeoForge.EVENT_BUS.addListener(this::tick);
		NeoForge.EVENT_BUS.addListener(this::finishRestores);
	}

	private void finishRestores(ServerTickEvent.Post event) {
		List<StorageBlockEntity> restored = List.copyOf(RESTORING_CHESTS);
		RESTORING_CHESTS.clear();
		restored.forEach(chest -> chest.setBeingUpgraded(false));
		restored.forEach(chest -> {
			Level level = chest.getLevel();
			if (level != null && !chest.isRemoved()) {
				BlockState state = chest.getBlockState();
				level.sendBlockUpdated(chest.getBlockPos(), state, state, 3);
				level.updateNeighborsAt(chest.getBlockPos(), state.getBlock());
			}
		});
	}

	private void setup(FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			BuiltInRegistries.BLOCK.forEach(block -> {
				if (block instanceof IStorageBlock)
					MountedItemStorageType.REGISTRY.register(block, STORAGE.get());
			});
			BlockMovementChecks.registerMovementAllowedCheck((state, level, pos) ->
				level.getBlockEntity(pos) instanceof StorageBlockEntity storage && storage.isLinkedStorage()
					? BlockMovementChecks.CheckResult.FAIL : BlockMovementChecks.CheckResult.PASS);
		});
	}

	private void tick(EntityTickEvent.Post event) {
		if (event.getEntity() instanceof AbstractContraptionEntity entity
			&& !entity.level().isClientSide() && entity.getContraption() != null) {
			entity.getContraption().getStorage().getAllItemStorages().forEach((pos, storage) -> {
				if (storage instanceof SophisticatedMountedStorage sophisticated)
					sophisticated.tick(entity, pos);
			});
		}
	}
}
