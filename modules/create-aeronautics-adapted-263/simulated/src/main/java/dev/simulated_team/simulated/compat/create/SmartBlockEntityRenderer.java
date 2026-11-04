package dev.simulated_team.simulated.compat.create;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class SmartBlockEntityRenderer<T extends net.minecraft.world.level.block.entity.BlockEntity>
        implements BlockEntityRenderer<T, SmartBlockEntityRenderer.RenderState<T>> {
    private final com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer<com.simibubi.create.foundation.blockEntity.SmartBlockEntity> createRenderer;
    private boolean renderingInSubLevel;
    private static boolean smokeExtractionLogged;

    public SmartBlockEntityRenderer(final BlockEntityRendererProvider.Context context) {
        createRenderer = new com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer<>(context);
    }

    @Override
    public RenderState<T> createRenderState() {
        return new RenderState<>();
    }

    @Override
    public void extractRenderState(final T blockEntity, final RenderState<T> state, final float partialTicks, final Vec3 cameraPos,
                                   @Nullable final ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(blockEntity, state, crumblingOverlay);
        if (blockEntity instanceof final com.simibubi.create.foundation.blockEntity.SmartBlockEntity smartBlockEntity) {
            state.createState = createRenderer.createRenderState();
            createRenderer.extractRenderState(smartBlockEntity, state.createState, partialTicks, cameraPos, crumblingOverlay);
        }
        state.blockEntity = blockEntity;
        // Vanilla Sable extracts plot render states separately from submission.
        state.renderingInSubLevel = dev.ryanhcode.sable.Sable.HELPER.getContainingClient(blockEntity.getBlockPos()) != null;
        state.partialTicks = partialTicks;
        if (Boolean.getBoolean("simulated.clientSmokeTest") && !smokeExtractionLogged && blockEntity.getClass().getSimpleName().contains("Propeller")) {
            smokeExtractionLogged = true;
            dev.simulated_team.simulated.Simulated.LOGGER.info("Propeller extraction probe: pos={}, sublevel={}", blockEntity.getBlockPos(), state.renderingInSubLevel);
        }
    }

    @Override
    public void submit(final RenderState<T> state, final PoseStack poseStack, final SubmitNodeCollector queue, final CameraRenderState cameraState) {
        if (state.blockEntity instanceof com.simibubi.create.foundation.blockEntity.SmartBlockEntity) {
            createRenderer.submit(state.createState, poseStack, queue, cameraState);
        }
        if (state.blockEntity == null || state.blockEntity.isRemoved() || shouldSkipLegacyRender(state.blockEntity)) {
            return;
        }

        final boolean previous = renderingInSubLevel;
        renderingInSubLevel = state.renderingInSubLevel;
        try {
            renderSafe(state.blockEntity, state.partialTicks, poseStack, queue, state.lightCoords, OverlayTexture.NO_OVERLAY);
        } finally {
            renderingInSubLevel = previous;
        }
    }

    protected boolean shouldSkipLegacyRender(final T blockEntity) {
        return false;
    }

    protected void renderSafe(final T blockEntity, final float partialTicks, final PoseStack poseStack,
                              final SubmitNodeCollector bufferSource, final int light, final int overlay) {
    }

    /**
     * Runs only when an individual block-entity type has opted into Sable's
     * immediate moving-sublevel renderer.
     */
    public final void renderExplicitlyInSubLevel(final T blockEntity, final float partialTicks, final PoseStack poseStack,
                                                 final SubmitNodeCollector bufferSource, final int light, final int overlay) {
        final boolean previous = renderingInSubLevel;
        renderingInSubLevel = true;
        try {
            renderSafe(blockEntity, partialTicks, poseStack, bufferSource, light, overlay);
        } finally {
            renderingInSubLevel = previous;
        }
    }

    protected final boolean isRenderingInSubLevel() {
        return renderingInSubLevel;
    }

    /** Keep Flywheel in normal worlds; use real BER geometry for this port's moving-plot wrappers. */
    public static boolean shouldSkipVanillaRender(final net.minecraft.world.level.block.entity.BlockEntity blockEntity) {
        if (dev.ryanhcode.sable.Sable.HELPER.getContainingClient(blockEntity.getBlockPos()) == null) return true;
        final Object renderer = net.minecraft.client.Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(blockEntity);
        return !(renderer instanceof SmartBlockEntityRenderer<?>);
    }

    public boolean shouldRenderOffScreen(final T blockEntity) {
        return shouldRenderOffScreen();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    public static class RenderState<T extends net.minecraft.world.level.block.entity.BlockEntity> extends BlockEntityRenderState {
        private BlockEntityRenderState createState;
        private T blockEntity;
        private float partialTicks;
        private boolean renderingInSubLevel;
    }
}
