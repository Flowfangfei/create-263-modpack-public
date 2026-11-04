package com.enxv.aerouniversaljoint.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import dev.simulated_team.simulated.compat.create.SmartBlockEntityRenderer.RenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;

/** Keeps the real Create behaviour overlays, with link meshes fully extracted before submission. */
public class FrozenKineticBlockEntityRenderer<T extends com.simibubi.create.content.kinetics.base.KineticBlockEntity> extends dev.simulated_team.simulated.compat.create.KineticBlockEntityRenderer<T> {
    public FrozenKineticBlockEntityRenderer(BlockEntityRendererProvider.Context context) { super(context); }
    @Override public RenderState<T> createRenderState() { return new FrozenState<>(); }
    @Override public void extractRenderState(T be, RenderState<T> state, float partialTicks, Vec3 camera,
            ModelFeatureRenderer.CrumblingOverlay crumbling) {
        super.extractRenderState(be, state, partialTicks, camera, crumbling);
        ((FrozenState<T>) state).draws = ExtractedGeometry.capture(collector ->
                renderSafe(be, partialTicks, new PoseStack(), collector, state.lightCoords, OverlayTexture.NO_OVERLAY));
    }
    @Override protected boolean shouldSkipLegacyRender(T be) { return true; }
    @Override public void submit(RenderState<T> state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, pose, collector, camera);
        for (var draw : ((FrozenState<T>) state).draws) draw.submit(pose, collector);
    }
    private static class FrozenState<T extends com.simibubi.create.content.kinetics.base.KineticBlockEntity> extends RenderState<T> {
        private List<ExtractedGeometry.Draw> draws = List.of();
    }
}
