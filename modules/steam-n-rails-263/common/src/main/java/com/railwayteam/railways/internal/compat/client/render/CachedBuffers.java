package com.railwayteam.railways.internal.compat.client.render;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.api.client.render.SuperBufferFactory;
import net.createmod.catnip.api.client.render.SuperByteBufferCache;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.world.level.block.state.BlockState;

public final class CachedBuffers {
    private record Key(PartialModel model,BlockState state) { }
    private static final SuperByteBufferCache.Compartment<Key> PARTIALS=new SuperByteBufferCache.Compartment<>();
    static { SuperByteBufferCache.getInstance().registerCompartment(PARTIALS); }
    private CachedBuffers() { }
    public static SuperByteBuffer partial(PartialModel model,BlockState state) {
        return new SuperByteBuffer(()->SuperByteBufferCache.getInstance().get(PARTIALS,new Key(model,state),
            ()->SuperBufferFactory.getInstance().createForBlock(new SingleVariant(model.get()),state)));
    }
    public static SuperByteBuffer block(BlockState state) {
        return new SuperByteBuffer(()->net.createmod.catnip.api.client.render.CachedBuffers.block(state));
    }
    public static SuperByteBuffer blockModel(BlockStateModel model,BlockState state) {
        return new SuperByteBuffer(()->SuperBufferFactory.getInstance().createForBlock(model,state));
    }
}
