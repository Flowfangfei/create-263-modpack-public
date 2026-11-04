package dev.ryanhcode.sable.neoforge.platform;

import dev.ryanhcode.sable.platform.SablePlotPlatform;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.storage.SerializableChunkData;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.world.LevelChunkAuxiliaryLightManager;
import net.neoforged.neoforge.event.level.ChunkDataEvent;

/** Preserves NeoForge light and attachment data for Sable's saved plot chunks. */
public final class SablePlotPlatformImpl implements SablePlotPlatform {
    @Override public void readLightData(CompoundTag tag, RegistryAccess access, LevelChunk chunk) {
        if (tag.get(LevelChunkAuxiliaryLightManager.LIGHT_NBT_KEY) instanceof ListTag lights)
            chunk.getAuxLightManager(chunk.getPos()).deserializeNBT(lights);
    }
    @Override public void readChunkAttachments(CompoundTag tag, RegistryAccess access, LevelChunk chunk) {
        if (tag.get(AttachmentHolder.ATTACHMENTS_NBT_KEY) instanceof CompoundTag attachments)
            chunk.readAttachmentsFromNBT(access, attachments);
    }
    @Override public void postLoad(CompoundTag tag, LevelChunk chunk) {
        var level = chunk.getLevel();
        // Sable saves a plot-specific schema, not vanilla region NBT (no Status/xPos).
        // The chunk is fully restored before this hook; NeoForge's typed event needs
        // a vanilla snapshot of that restored state, including attachments and lights.
        var data = SerializableChunkData.copyOf((net.minecraft.server.level.ServerLevel) level, chunk);
        NeoForge.EVENT_BUS.post(new ChunkDataEvent.Load(chunk, data));
    }
    @Override public void writeLightData(CompoundTag tag, RegistryAccess access, LevelChunk chunk) {
        var lights = chunk.getAuxLightManager(chunk.getPos()).serializeNBT();
        if (lights != null) tag.put(LevelChunkAuxiliaryLightManager.LIGHT_NBT_KEY, lights);
    }
    @Override public void writeChunkAttachments(CompoundTag tag, RegistryAccess access, LevelChunk chunk) {
        var attachments = chunk.writeAttachmentsToNBT(access);
        if (attachments != null) tag.put(AttachmentHolder.ATTACHMENTS_NBT_KEY, attachments);
    }
}
