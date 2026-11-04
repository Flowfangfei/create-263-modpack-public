package com.railwayteam.railways.internal.compat.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import com.simibubi.create.content.redstone.link.LinkRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

/** Typed 26.3 render-state bridge retaining Create's behaviour render passes. */
public abstract class SmartBlockEntityRenderer<T extends SmartBlockEntity, S extends SmartBlockEntityRenderer.SmartRenderState>
    implements BlockEntityRenderer<T, S> {
    public static class SmartRenderState extends BlockEntityRenderState {
        public SmartBlockEntity blockEntity;
        public float partialTicks;
    }
    protected SmartBlockEntityRenderer(BlockEntityRendererProvider.Context context) { }
    @Override public void extractRenderState(T blockEntity, S state, float partialTicks, Vec3 cameraPos,
        ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(blockEntity, state, crumblingOverlay);
        state.blockEntity = blockEntity;
        state.partialTicks = partialTicks;
    }
    @Override public void submit(S state, PoseStack poses, SubmitNodeCollector collector, CameraRenderState camera) {
        var be = state.blockEntity;
        if (be == null || !be.hasLevel() || be.isRemoved()) return;
        FilteringRenderer.submitOnBlockEntity(be, poses, collector, state.lightCoords);
        LinkRenderer.submitOnBlockEntity(be, state.partialTicks, poses, collector, state.lightCoords);
    }
    protected Level getCardinalLighting(Level level) { return level; }
}
