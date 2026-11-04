package com.simibubi.create.compat.sophisticated;

import java.util.Optional;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.common.gui.ISyncedContainer;
import net.p3pp3rf1y.sophisticatedcore.common.gui.StorageContainerMenuBase;
import net.p3pp3rf1y.sophisticatedcore.util.NoopStorageWrapper;

public final class MovingStorageMenu extends StorageContainerMenuBase<IStorageWrapper> implements ISyncedContainer {
	final AbstractContraptionEntity entity;
	final BlockPos pos;

	public MovingStorageMenu(int containerId, Player player, int entityId, BlockPos pos) {
		super(SophisticatedContraptionCompat.MENU.get(), containerId, player,
			storage(player, entityId, pos).getStorageWrapper(), NoopStorageWrapper.INSTANCE, -1, false);
		this.entity = (AbstractContraptionEntity) player.level().getEntity(entityId);
		this.pos = pos;
	}

	static SophisticatedMountedStorage storage(Player player, int entityId, BlockPos pos) {
		if (player.level().getEntity(entityId) instanceof AbstractContraptionEntity entity
			&& entity.getContraption().getStorage().getAllItemStorages().get(pos) instanceof SophisticatedMountedStorage storage)
			return storage;
		throw new IllegalArgumentException("Mounted storage is unavailable");
	}

	public static MovingStorageMenu fromBuffer(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
		return new MovingStorageMenu(containerId, inventory.player, buffer.readVarInt(), buffer.readBlockPos());
	}

	@Override public Optional<BlockPos> getBlockPosition() { return Optional.empty(); }
	@Override public Optional<Entity> getEntity() { return Optional.of(entity); }

	@Override
	public boolean stillValid(Player player) {
		return entity.isAlive() && player.distanceToSqr(entity.toGlobalVector(Vec3.atCenterOf(pos), 0)) < 64;
	}

	@Override protected boolean storageItemHasChanged() { return false; }
	@Override public boolean detectSettingsChangeAndReload() { return false; }

	@Override
	public void openSettings() {
		if (player.level().isClientSide()) {
			sendToServer(message -> message.putString("action", "openSettings"));
		} else if (stillValid(player)) {
			player.openMenu(new SimpleMenuProvider(
				(containerId, inventory, menuPlayer) -> new MovingStorageSettingsMenu(containerId, menuPlayer, entity.getId(), pos),
				Component.translatable("gui.sophisticatedstorage.settings.title")), buffer -> {
					buffer.writeVarInt(entity.getId());
					buffer.writeBlockPos(pos);
				});
		}
	}

	@Override
	public void handlePacket(CompoundTag message) {
		if ("openSettings".equals(message.getStringOr("action", "")))
			openSettings();
		else
			super.handlePacket(message);
	}
}
