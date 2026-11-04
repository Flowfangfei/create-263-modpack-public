package com.george_vi.electroenergetics.simulation.util;

import com.george_vi.electroenergetics.CreateElectroEnergetics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.LevelResource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.function.Function;

/** Reads the old unnamespaced save once; the original file stays available as a backup. */
public final class LegacySavedData {
    private LegacySavedData() {}

    public static <T extends SavedData> T load(ServerLevel level, SavedDataType<T> type, String oldName,
                                              Function<CompoundTag, T> decoder) {
        T current = level.getDataStorage().get(type);
        if (current != null) return current;
        Path root = level.getServer().getWorldPath(LevelResource.ROOT);
        LinkedHashSet<Path> folders = new LinkedHashSet<>();
        folders.add(DimensionType.getStorageFolder(level.dimension(), root).resolve("data"));
        if (level.dimension() == Level.OVERWORLD) folders.add(root.resolve("data"));
        if (level.dimension() == Level.NETHER) folders.add(root.resolve("DIM-1/data"));
        if (level.dimension() == Level.END) folders.add(root.resolve("DIM1/data"));
        for (Path folder : folders) {
            Path legacy = folder.resolve(oldName + ".dat");
            if (!Files.isRegularFile(legacy)) continue;
            try {
                CompoundTag rootTag = NbtIo.readCompressed(legacy, NbtAccounter.unlimitedHeap());
                T restored = decoder.apply(rootTag.getCompoundOrEmpty("data"));
                level.getDataStorage().set(type, restored);
                CreateElectroEnergetics.LOGGER.info("Migrated CEE saved data from {}", legacy);
                return restored;
            } catch (IOException | RuntimeException exception) {
                throw new IllegalStateException("Cannot migrate CEE data from " + legacy, exception);
            }
        }
        return level.getDataStorage().computeIfAbsent(type);
    }
}
