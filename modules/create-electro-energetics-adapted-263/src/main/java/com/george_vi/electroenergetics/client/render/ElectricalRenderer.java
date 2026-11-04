package com.george_vi.electroenergetics.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;

import org.jspecify.annotations.Nullable;

public abstract class ElectricalRenderer<T extends SmartBlockEntity>
        implements BlockEntityRenderer<T, ElectricalRenderer.State> {
    public ElectricalRenderer() {}

    public ElectricalRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(
            T be,
            State state,
            float partialTick,
            Vec3 cameraPos,
            @Nullable CrumblingOverlay overlay) {
        SmartBlockEntityRenderer.extractBase(be, state, overlay);
        state.geometry = new RenderCollector();
        state.geometry.cardinalLighting =
                SmartBlockEntityRenderer.getCardinalLighting(be.getLevel());
        extractModels(
                be,
                partialTick,
                new PoseStack(),
                state.geometry,
                state.lightCoords,
                OverlayTexture.NO_OVERLAY);
    }

    protected abstract void extractModels(
            T be,
            float partialTick,
            PoseStack matrices,
            RenderCollector geometry,
            int light,
            int overlay);

    @Override
    public void submit(
            State state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState camera) {
        state.geometry.submit(matrices, queue);
    }

    public static class State extends BlockEntityRenderState {
        public RenderCollector geometry;
    }
}
