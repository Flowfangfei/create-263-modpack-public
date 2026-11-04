package com.railwayteam.railways.internal.compat.client.model;

import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.ItemQuads;
import net.minecraft.world.item.ItemDisplayContext;
import java.util.List;

public final class ItemModelRenderHelper {
    private ItemModelRenderHelper() { }
    public static ItemStackRenderState.LayerRenderState submitQuads(ItemStackRenderState state,ModelRenderProperties settings,ItemDisplayContext context,List<BakedQuad> quads) {
        var layer=state.newLayer();settings.applyToLayer(layer,context);layer.setQuads(ItemQuads.split(quads));return layer;
    }
}
