package com.simibubi.create.compat.sophisticated;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.common.gui.SettingsContainerMenu;
import net.p3pp3rf1y.sophisticatedcore.network.SyncContainerClientDataPayload;

public final class MovingStorageSettingsMenu extends SettingsContainerMenu<IStorageWrapper> {
	final AbstractContraptionEntity entity;
	final BlockPos pos;

	public MovingStorageSettingsMenu(int containerId, Player player, int entityId, BlockPos pos) {
		super(SophisticatedContraptionCompat.SETTINGS.get(), containerId, player,
			MovingStorageMenu.storage(player, entityId, pos).getStorageWrapper());
		this.entity = (AbstractContraptionEntity) player.level().getEntity(entityId);
		this.pos = pos;
	}

	public static MovingStorageSettingsMenu fromBuffer(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
		return new MovingStorageSettingsMenu(containerId, inventory.player, buffer.readVarInt(), buffer.readBlockPos());
	}

	@Override public BlockPos getBlockPosition() { return pos; }
	@Override public void detectSettingsChangeAndReload() { }

	@Override
	public boolean stillValid(Player player) {
		return entity.isAlive() && player.distanceToSqr(entity.toGlobalVector(Vec3.atCenterOf(pos), 0)) < 64;
	}

	public void openInventory() {
		if (player.level().isClientSide()) {
			CompoundTag message = new CompoundTag();
			message.putString("action", "openInventory");
			ClientPacketDistributor.sendToServer(new SyncContainerClientDataPayload(message));
		} else if (player instanceof ServerPlayer serverPlayer && stillValid(player)) {
			MovingStorageMenu.storage(player, entity.getId(), pos).open(serverPlayer, entity, pos);
		}
	}

	@Override
	public void handlePacket(CompoundTag message) {
		if ("openInventory".equals(message.getStringOr("action", "")))
			openInventory();
		else
			super.handlePacket(message);
	}
}
