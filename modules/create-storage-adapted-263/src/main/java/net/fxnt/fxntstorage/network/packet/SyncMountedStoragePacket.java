package net.fxnt.fxntstorage.network.packet;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.fxnt.fxntstorage.FXNTStorage;
import net.fxnt.fxntstorage.container.EnumProperties;
import net.fxnt.fxntstorage.container.StorageBox;
import net.fxnt.fxntstorage.reserve_storage.ReserveStorageBox;
import net.fxnt.fxntstorage.simple_storage.SimpleStorageBox;
import net.fxnt.fxntstorage.simple_storage.mounted.SimpleStorageBoxMountedMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncMountedStoragePacket(int contraptionId, BlockPos localPos, EnumProperties.StorageUsed fillLevel,
                                       CompoundTag nbt) implements CustomPacketPayload {
    public static final Type<SyncMountedStoragePacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(FXNTStorage.MOD_ID, "sync_mounted_storage"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncMountedStoragePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, SyncMountedStoragePacket::contraptionId,
            BlockPos.STREAM_CODEC, SyncMountedStoragePacket::localPos,
            NeoForgeStreamCodecs.enumCodec(EnumProperties.StorageUsed.class), SyncMountedStoragePacket::fillLevel,
            ByteBufCodecs.COMPOUND_TAG, SyncMountedStoragePacket::nbt,
            SyncMountedStoragePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof Player player && player.level().isClientSide()) {
                var level = player.level();
                Entity entity = level.getEntity(contraptionId());
                if (entity instanceof AbstractContraptionEntity contraptionEntity && !entity.isRemoved()
                        && contraptionEntity.getContraption() != null) {
                    StructureTemplate.StructureBlockInfo blockInfo = contraptionEntity.getContraption().getBlocks().get(localPos());
                    if (blockInfo == null) return;
                    CompoundTag newNbt = blockInfo.nbt();
                    if (newNbt == null) return;

                    CompoundTag nbt = nbt();
                    BlockState oldState = blockInfo.state();
                    BlockState newState;

                    if (oldState.getBlock() instanceof ReserveStorageBox) {
                        newNbt.putFloat("ReservePercent", nbt.getFloat("ReservePercent").orElse(0f));
                        newNbt.putString("ReserveSlotStatus", nbt.getString("ReserveSlotStatus").orElse(""));
                        newNbt.putInt("StoredAmount", nbt.getInt("StoredAmount").orElse(0));
                        newNbt.putFloat("PercentageUsed", nbt.getFloat("PercentageUsed").orElse(0f));
                        newNbt.putBoolean("VoidUpgrade", nbt.getBoolean("VoidUpgrade").orElse(false));
                        newState = oldState
                                .setValue(ReserveStorageBox.STORAGE_USED, fillLevel())
                                .setValue(ReserveStorageBox.VOID_UPGRADE, nbt.getBoolean("VoidUpgrade").orElse(false));
                    } else {
                        newNbt.putInt("StoredAmount", nbt.getInt("StoredAmount").orElse(0));
                        newNbt.putBoolean("VoidUpgrade", nbt.getBoolean("VoidUpgrade").orElse(false));
                        newNbt.putInt("MaxItemCapacity", nbt.getInt("MaxItemCapacity").orElse(0));

                        if (oldState.getBlock() instanceof SimpleStorageBox) {
                            if (nbt.contains("FilterItem")) {
                                newNbt.put("FilterItem", nbt.getCompound("FilterItem").orElse(new CompoundTag()));
                            }
                            newNbt.putBoolean("CompactingUpgrade", nbt.getBoolean("CompactingUpgrade").orElse(false));
                            newNbt.putInt("CompactingSelectedTier", nbt.getInt("CompactingSelectedTier").orElse(0));
                            boolean compacting = nbt.getBoolean("CompactingUpgrade").orElse(false);
                            boolean voidUpg = nbt.getBoolean("VoidUpgrade").orElse(false);
                            newState = oldState
                                    .setValue(SimpleStorageBox.STORAGE_USED, fillLevel())
                                    .setValue(SimpleStorageBox.COMPACTING, compacting)
                                    .setValue(SimpleStorageBox.VOID_UPGRADE, voidUpg);
                        } else { // StorageBox
                            newNbt.putFloat("PercentageUsed", nbt.getFloat("PercentageUsed").orElse(0f));
                            newState = oldState
                                    .setValue(StorageBox.STORAGE_USED, fillLevel())
                                    .setValue(StorageBox.VOID_UPGRADE, nbt.getBoolean("VoidUpgrade").orElse(false));
                        }
                    }

                    // Push the full NBT snapshot into an open menu to update data
                    if (player.containerMenu instanceof SimpleStorageBoxMountedMenu menu) {
                        if (menu.getContraptionId() == contraptionId() && menu.getLocalPos().equals(localPos()))
                            menu.applySyncedData(newNbt);
                    }

                    StructureTemplate.StructureBlockInfo newInfo = new StructureTemplate.StructureBlockInfo(blockInfo.pos(), newState, newNbt);
                    contraptionEntity.getContraption().getBlocks().put(localPos(), newInfo);
                    contraptionEntity.getContraption().resetClientContraption();
                }
            }
        });
    }
}
