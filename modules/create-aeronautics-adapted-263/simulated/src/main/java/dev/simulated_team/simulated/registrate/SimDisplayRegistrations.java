package dev.simulated_team.simulated.registrate;

import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.api.behaviour.display.DisplayTarget;
import net.minecraft.world.level.block.Block;

public final class SimDisplayRegistrations {
    private SimDisplayRegistrations() {
    }

    public static <T extends Block, P> NonNullUnaryOperator<BlockBuilder<T, P>> displaySource(
            final RegistryEntry<DisplaySource, ? extends DisplaySource> source
    ) {
        return builder -> builder.onRegisterAfter(com.simibubi.create.api.registry.CreateRegistries.DISPLAY_SOURCE, block -> DisplaySource.BY_BLOCK.add(block, source.get()));
    }

    public static <T extends Block, P> NonNullUnaryOperator<BlockBuilder<T, P>> displayTarget(
            final RegistryEntry<DisplayTarget, ? extends DisplayTarget> target
    ) {
        return builder -> builder.onRegisterAfter(com.simibubi.create.api.registry.CreateRegistries.DISPLAY_TARGET, block -> DisplayTarget.BY_BLOCK.register(block, target.get()));
    }
}
