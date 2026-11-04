package com.simibubi.create.compat.sophisticated;

import java.util.Optional;
import java.util.UUID;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorage;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.render.ClientContraption;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.RegistryOps.HolderLookupAdapter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.inventory.ITrackedContentsItemResourceHandler;
import net.p3pp3rf1y.sophisticatedcore.settings.memory.MemorySettingsCategory;
import net.p3pp3rf1y.sophisticatedcore.upgrades.ITickableUpgrade;
import net.p3pp3rf1y.sophisticatedstorage.block.ChestBlock;
import net.p3pp3rf1y.sophisticatedstorage.block.ContentsFilteredItemHandler;
import net.p3pp3rf1y.sophisticatedstorage.block.IStorageBlock;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageBlockEntity;
import net.p3pp3rf1y.sophisticatedstorage.block.StorageWrapper;

public final class SophisticatedMountedStorage extends MountedStorageBridge {
	private static final MapCodec<Provider> REGISTRIES = ExtraCodecs.retrieveContext(ops ->
		ops instanceof RegistryOps<?> registryOps && registryOps.lookupProvider instanceof HolderLookupAdapter adapter
			? DataResult.success(adapter.lookupProvider)
			: DataResult.error(() -> "Mounted Sophisticated storage requires registry context"));
	public static final MapCodec<SophisticatedMountedStorage> CODEC = RecordCodecBuilder.mapCodec(instance ->
		instance.group(
			BlockState.CODEC.fieldOf("state").forGetter(storage -> storage.state),
			CompoundTag.CODEC.fieldOf("block_entity").forGetter(SophisticatedMountedStorage::snapshot),
			REGISTRIES.forGetter(storage -> storage.registries)
		).apply(instance, SophisticatedMountedStorage::new));

	private final BlockState state;
	private final CompoundTag data;
	private final Provider registries;
	private final StorageWrapper wrapper;
	private boolean dirty;
	private boolean client;

	public SophisticatedMountedStorage(BlockState state, CompoundTag data, Provider registries) {
		super(SophisticatedContraptionCompat.STORAGE.get(), new ItemStack(state.getBlock()));
		this.state = state;
		this.data = data.copy();
		this.registries = registries;
		IStorageBlock storageBlock = (IStorageBlock) state.getBlock();
		StorageBlockEntity restored = (StorageBlockEntity) BlockEntity.loadStatic(BlockPos.ZERO, state, this.data, registries);
		if (restored == null)
			throw new IllegalArgumentException("Cannot restore mounted storage " + state);

		this.wrapper = new StorageWrapper(() -> this::changed, this::changed, this::changed) {
			private ITrackedContentsItemResourceHandler lockedHandler;

			@Override
			public Optional<UUID> getContentsUuid() {
				if (contentsUuid == null)
					contentsUuid = UUID.randomUUID();
				return Optional.of(contentsUuid);
			}

			@Override protected void onUpgradeRefresh() { changed(); }
			@Override public int getDefaultNumberOfInventorySlots() { return storageBlock.getNumberOfInventorySlots(); }
			@Override public int getDefaultNumberOfUpgradeSlots() { return storageBlock.getNumberOfUpgradeSlots(); }
			@Override public int getBaseStackSizeMultiplier() { return storageBlock.getBaseStackSizeMultiplier(); }
			@Override protected boolean isAllowedInStorage(ItemResource resource) { return true; }
			@Override public ItemStack getWrappedStorageStack() { return getStorageStack(); }
			@Override public Component getDisplayName() { return restored.getDisplayName(); }
			@Override public String getStorageType() { return restored.getStorageWrapper().getStorageType(); }

			@Override
			protected boolean emptyInventorySlotsAcceptItems() {
				return !SophisticatedMountedStorage.this.data.getBooleanOr("locked", false);
			}

			@Override
			public ITrackedContentsItemResourceHandler getInventoryForInputOutput() {
				if (!SophisticatedMountedStorage.this.data.getBooleanOr("locked", false))
					return super.getInventoryForInputOutput();
				if (lockedHandler == null) {
					lockedHandler = new ContentsFilteredItemHandler(
						() -> super.getInventoryForInputOutput(),
						() -> getInventoryHandler().getSlotTracker(),
						() -> getSettingsHandler().getTypeCategory(MemorySettingsCategory.class), true);
				}
				return lockedHandler;
			}
		};
		wrapper.deserialize(TagValueInput.create(ProblemReporter.DISCARDING, registries,
			this.data.getCompoundOrEmpty("storageWrapper")));
		dirty = false;
	}

	private void changed() { dirty = true; }

	public CompoundTag snapshot() {
		TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
		wrapper.serialize(output);
		CompoundTag snapshot = data.copy();
		snapshot.put("storageWrapper", output.buildResult());
		return snapshot;
	}

	@Override public IStorageWrapper getStorageWrapper() { return wrapper; }

	private boolean isSecondaryChest() {
		return state.getBlock() instanceof ChestBlock && state.getValue(ChestBlock.TYPE) == ChestType.LEFT;
	}

	@Override
	public int getSlots() {
		return !data.getBooleanOr("packed", false) && !isSecondaryChest() ? super.getSlots() : 0;
	}

	@Override public void updateWithSyncedStorageStack(ItemStack stack, boolean update) { }
	@Override public boolean isDirty() { return !client && dirty; }
	@Override public void markClean() { dirty = false; }

	@Override
	public void afterSync(Contraption contraption, BlockPos pos) {
		client = true;
		if (contraption.entity != null) {
			ClientContraption clientContraption = contraption.getOrCreateClientContraptionLazy();
			BlockEntity blockEntity = clientContraption.getBlockEntity(pos);
			if (blockEntity instanceof StorageBlockEntity)
				blockEntity.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, registries, snapshot()));
		}
	}

	@Override
	public void unmount(Level level, BlockState placedState, BlockPos pos, BlockEntity blockEntity) {
		if (blockEntity instanceof StorageBlockEntity storage) {
			if (state.getBlock() instanceof ChestBlock && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
				storage.setBeingUpgraded(true);
				BlockState restoredState = blockEntity.getBlockState().setValue(ChestBlock.TYPE, state.getValue(ChestBlock.TYPE));
				level.setBlock(pos, restoredState, 18);
				SophisticatedContraptionCompat.finishChestRestore(storage);
			}
			blockEntity.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), snapshot()));
			blockEntity.setChanged();
			level.sendBlockUpdated(pos, blockEntity.getBlockState(), blockEntity.getBlockState(), storage.isBeingUpgraded() ? 2 : 3);
		}
	}

	@Override
	public boolean handleInteraction(ServerPlayer player, Contraption contraption, StructureBlockInfo info) {
		if (data.getBooleanOr("packed", false))
			return false;
		Vec3 globalPos = contraption.entity.toGlobalVector(Vec3.atCenterOf(info.pos()), 0);
		return isMenuValid(player, contraption, globalPos) && open(player, contraption.entity, info.pos());
	}

	public boolean open(ServerPlayer player, AbstractContraptionEntity entity, BlockPos pos) {
		if (!isSecondaryChest()) {
			return player.openMenu(new SimpleMenuProvider(
				(containerId, inventory, menuPlayer) -> new MovingStorageMenu(containerId, menuPlayer, entity.getId(), pos),
				wrapper.getDisplayName()), buffer -> {
					buffer.writeVarInt(entity.getId());
					buffer.writeBlockPos(pos);
				}).isPresent();
		}
		BlockPos primaryPos = pos.relative(ChestBlock.getConnectedDirection(state));
		MountedItemStorage primary = entity.getContraption().getStorage().getAllItemStorages().get(primaryPos);
		return primary instanceof SophisticatedMountedStorage storage && !storage.isSecondaryChest()
			&& storage.open(player, entity, primaryPos);
	}

	public void tick(AbstractContraptionEntity entity, BlockPos pos) {
		if (!isSecondaryChest()) {
			wrapper.onInit(entity.level());
			for (ITickableUpgrade upgrade : wrapper.getUpgradeHandler().getWrappersThatImplement(ITickableUpgrade.class))
				upgrade.tick(entity, entity.level(), BlockPos.containing(entity.toGlobalVector(Vec3.atCenterOf(pos), 0)));
		}
	}

	public static final class Type extends MountedItemStorageType<SophisticatedMountedStorage> {
		public Type() { super(SophisticatedMountedStorage.CODEC); }

		@Override
		public SophisticatedMountedStorage mount(Level level, BlockState state, BlockPos pos, BlockEntity blockEntity) {
			return blockEntity instanceof StorageBlockEntity
				? new SophisticatedMountedStorage(state, blockEntity.saveWithFullMetadata(level.registryAccess()), level.registryAccess())
				: null;
		}
	}
}
