package dev.simulated_team.simulated.ponder;

import dev.simulated_team.simulated.Simulated;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.NbtOps;

import java.util.Set;

/** Coordinate compatibility for Ponder resource data and Ponder's in-memory scene backups only. */
public final class LegacyPonderTemplatePositions {
    private static final Set<String> ENTITIES = Set.of("create_connected:item_silo",
            "fluidlogistics:multi_fluid_tank", "fluidlogistics:horizontal_multi_fluid_tank");
    private static final Set<String> AXES = Set.of("X", "Y", "Z");

    private LegacyPonderTemplatePositions() {
    }

    public static void adapt(final CompoundTag blockEntity) {
        if (!ENTITIES.contains(blockEntity.getStringOr("id", "")))
            return;
        adaptPosition(blockEntity, "LastKnownPos");
        adaptPosition(blockEntity, "Controller");
    }

    private static void adaptPosition(final CompoundTag blockEntity, final String field) {
        // Current lists/int arrays are already correct. Never apply this to Create's own tank/belt readers.
        if (!(blockEntity.get(field) instanceof CompoundTag old))
            return;
        if (!old.keySet().equals(AXES) || !(old.get("X") instanceof IntTag x)
                || !(old.get("Y") instanceof IntTag y) || !(old.get("Z") instanceof IntTag z)) {
            Simulated.LOGGER.warn("Kept legacy Ponder position {}.{}: unknown coordinate fields or numeric types",
                    blockEntity.getStringOr("id", ""), field);
            return;
        }
        // These are recorded coordinates, not offsets: preserve their exact values without rebasing.
        BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, new BlockPos(x.intValue(), y.intValue(), z.intValue()))
                .resultOrPartial(error -> Simulated.LOGGER.warn("Kept legacy Ponder position {}.{}: {}",
                        blockEntity.getStringOr("id", ""), field, error))
                .ifPresent(position -> blockEntity.put(field, position));
    }
}
