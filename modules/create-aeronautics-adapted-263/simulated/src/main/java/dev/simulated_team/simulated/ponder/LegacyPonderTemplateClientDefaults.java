package dev.simulated_team.simulated.ponder;

import net.minecraft.nbt.CompoundTag;

import java.util.Set;

/** Missing client-only values in Ponder resource templates and Ponder's in-memory backups. */
public final class LegacyPonderTemplateClientDefaults {
    private static final Set<String> TRACK_SWITCH_ENTITIES = Set.of(
            "railways:track_switch_andesite", "railways:track_switch_brass");

    private LegacyPonderTemplateClientDefaults() {
    }

    public static void adapt(final CompoundTag blockEntity) {
        // TrackSwitchBlockEntity starts with null state; its getter/client writer default to NORMAL.
        // Its writer serializes that enum as "normal". Never replace an existing key of any type.
        if (TRACK_SWITCH_ENTITIES.contains(blockEntity.getStringOr("id", ""))
                && !blockEntity.contains("SwitchState"))
            blockEntity.putString("SwitchState", "normal");
    }
}
