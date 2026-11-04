package com.tom.createores.compat;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** Minimal replacement for Create's removed datagen property shortcut. */
public final class CreateFlySharedProperties {
    private CreateFlySharedProperties() {
    }

    public static Block copperMetal() {
        return Blocks.COPPER_BLOCK;
    }
}
