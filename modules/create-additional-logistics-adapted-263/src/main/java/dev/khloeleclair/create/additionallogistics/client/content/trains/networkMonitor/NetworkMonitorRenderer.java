package dev.khloeleclair.create.additionallogistics.client.content.trains.networkMonitor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.trains.track.TrackTargetingClient;
import com.simibubi.create.content.trains.track.TrackTargetingBehaviour;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.simibubi.create.foundation.render.CreateVisualizationManager;
import dev.khloeleclair.create.additionallogistics.common.content.trains.networkMonitor.NetworkMonitorBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;

public class NetworkMonitorRenderer extends SmartBlockEntityRenderer<NetworkMonitorBlockEntity> {
    private static class State extends SmartRenderState { NetworkMonitorBlockEntity monitor; }
    public NetworkMonitorRenderer(BlockEntityRendererProvider.Context context) { super(context); }
    @Override public BlockEntityRenderState createRenderState() { return new State(); }
    @Override public void extractRenderState(NetworkMonitorBlockEntity be, BlockEntityRenderState state, float partial, Vec3 camera, ModelFeatureRenderer.CrumblingOverlay overlay) {
        super.extractRenderState(be, state, partial, camera, overlay); ((State)state).monitor = be;
    }
    @Override public void submit(BlockEntityRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        super.submit(state, pose, collector, camera);
        var be = ((State)state).monitor;
        if (be == null || isInvalid(be) || CreateVisualizationManager.supportsVisualization(be.getLevel())) return;
        var target = be.edgePoint;
        pose.pushPose();
        var offset = target.getGlobalPosition().subtract(be.getBlockPos());
        pose.translate(offset.getX(), offset.getY(), offset.getZ());
        TrackTargetingClient.submitOverlay(be.getLevel(), target.getGlobalPosition(), target.getTargetDirection(), target.getTargetBezier(), pose, collector,
            state.lightCoords, TrackTargetingBehaviour.RenderedTrackOverlayType.OBSERVER, 1);
        pose.popPose();
    }
}
