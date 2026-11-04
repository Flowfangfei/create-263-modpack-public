package com.george_vi.electroenergetics.mixins;

import com.george_vi.electroenergetics.client.render.RenderCollector;
import com.george_vi.electroenergetics.client.render.WorldGeometry;

import net.minecraft.client.renderer.state.level.LevelRenderState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LevelRenderState.class)
public class LevelRendererAccessor implements WorldGeometry {
    @Unique private RenderCollector electroenergetics$geometry = new RenderCollector();

    @Override
    public RenderCollector electroenergetics$getGeometry() {
        return electroenergetics$geometry;
    }

    @Override
    public void electroenergetics$setGeometry(RenderCollector geometry) {
        electroenergetics$geometry = geometry;
    }
}
