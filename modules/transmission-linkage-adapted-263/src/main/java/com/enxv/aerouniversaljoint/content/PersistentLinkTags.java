package com.enxv.aerouniversaljoint.content;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

/** Preserves the upstream {X,Y,Z} endpoint representation while accepting native codec arrays. */
final class PersistentLinkTags {
    private PersistentLinkTags() {}
    static void writeBlockPos(CompoundTag owner, String key, BlockPos pos) {
        CompoundTag value = new CompoundTag();
        value.putInt("X", pos.getX()); value.putInt("Y", pos.getY()); value.putInt("Z", pos.getZ());
        owner.put(key, value);
    }
    static Optional<BlockPos> readBlockPos(CompoundTag owner, String key) {
        Optional<CompoundTag> legacy = owner.getCompound(key);
        if (legacy.isPresent()) {
            CompoundTag value = legacy.get();
            if (value.contains("X") && value.contains("Y") && value.contains("Z"))
                return Optional.of(new BlockPos(value.getIntOr("X", 0), value.getIntOr("Y", 0), value.getIntOr("Z", 0)));
        }
        return owner.read(key, BlockPos.CODEC);
    }
}
