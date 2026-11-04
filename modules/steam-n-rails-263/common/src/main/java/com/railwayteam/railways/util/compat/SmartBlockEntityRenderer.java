package com.railwayteam.railways.util.compat;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public abstract class SmartBlockEntityRenderer<T extends SmartBlockEntity>
    extends com.railwayteam.railways.internal.compat.client.render.SmartBlockEntityRenderer<T,
    com.railwayteam.railways.internal.compat.client.render.SmartBlockEntityRenderer.SmartRenderState> {
    protected SmartBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }
    @Override public com.railwayteam.railways.internal.compat.client.render.SmartBlockEntityRenderer.SmartRenderState createRenderState() { return new com.railwayteam.railways.internal.compat.client.render.SmartBlockEntityRenderer.SmartRenderState(); }
}
