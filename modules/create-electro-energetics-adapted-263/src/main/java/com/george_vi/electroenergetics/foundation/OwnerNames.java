package com.george_vi.electroenergetics.foundation;

import net.minecraft.server.players.NameAndId;
import net.minecraft.world.level.Level;

import java.util.UUID;

public final class OwnerNames {
    private OwnerNames() {}

    public static String lastKnown(Level level, UUID owner) {
        if (owner == null || level.getServer() == null) return null;
        return level.getServer().services().nameToIdCache().get(owner).map(NameAndId::name).orElse(null);
    }
}
