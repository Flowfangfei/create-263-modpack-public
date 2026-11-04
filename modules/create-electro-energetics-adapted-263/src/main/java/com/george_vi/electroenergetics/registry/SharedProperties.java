package com.george_vi.electroenergetics.registry;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class SharedProperties {
    private SharedProperties() {}

    public static BlockBehaviour.Properties stone() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.STONE);
    }

    public static BlockBehaviour.Properties softMetal() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK);
    }

    public static BlockBehaviour.Properties copperMetal() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK.weathering().unaffected());
    }

    public static BlockBehaviour.Properties netheriteMetal() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.NETHERITE_BLOCK);
    }
}
