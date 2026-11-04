package dev.simulated_team.simulated.ponder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;

import java.util.Set;

/** Normalizes confirmed legacy empty items only in in-memory Ponder block-entity templates. */
public final class LegacyPonderTemplateItems {
    private static final Set<String> LEGACY_EMPTY_ITEM_KEYS = Set.of("id", "Count");

    private LegacyPonderTemplateItems() {
    }

    /** The caller must restrict this helper to Ponder resource templates, never world/codec reads. */
    public static void adapt(final CompoundTag blockEntity) {
        switch (blockEntity.getStringOr("id", "")) {
            case "create:mechanical_arm" -> adaptEmptyItem(blockEntity, "HeldItem");
            case "create:brass_tunnel" -> adaptEmptyItem(blockEntity, "StackToDistribute");
            default -> {
            }
        }
    }

    private static void adaptEmptyItem(final CompoundTag blockEntity, final String key) {
        if (!(blockEntity.get(key) instanceof CompoundTag item)
                || !item.keySet().equals(LEGACY_EMPTY_ITEM_KEYS)
                || !item.getStringOr("id", "").equals("minecraft:air")
                || !(item.get("Count") instanceof NumericTag count)
                || count.getId() < Tag.TAG_BYTE || count.getId() > Tag.TAG_LONG)
            return;

        final long amount = count.longValue();
        if (amount != 0 && amount != 1)
            return;

        // Keep modern count, unknown payloads, and conflicting old/new fields untouched.
        blockEntity.put(key, new CompoundTag());
    }
}
