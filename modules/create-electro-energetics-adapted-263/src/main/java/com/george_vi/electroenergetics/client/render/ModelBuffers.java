package com.george_vi.electroenergetics.client.render;

import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public final class ModelBuffers {
    private ModelBuffers() {}

    public static GeometryBuffer partial(PartialModel model, BlockState state) {
        return new GeometryBuffer(CachedBuffers.partial(model, state));
    }

    public static GeometryBuffer partialFacing(PartialModel model, BlockState state) {
        return new GeometryBuffer(CachedBuffers.partialFacing(model, state));
    }

    public static GeometryBuffer partialFacing(
            PartialModel model, BlockState state, Direction direction) {
        return new GeometryBuffer(CachedBuffers.partialFacing(model, state, direction));
    }

    public static GeometryBuffer block(BlockState state) {
        return new GeometryBuffer(CachedBuffers.block(state));
    }
}
