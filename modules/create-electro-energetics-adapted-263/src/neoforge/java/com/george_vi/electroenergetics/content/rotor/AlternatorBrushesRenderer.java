package com.george_vi.electroenergetics.content.rotor;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.george_vi.electroenergetics.CEEPartialModels.Bufferer;
import com.george_vi.electroenergetics.CEEPartialModels.ModelBuffer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class AlternatorBrushesRenderer extends KineticBlockEntityRenderer<AlternatorBrushesBlockEntity> {
    public AlternatorBrushesRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    protected ModelBuffer getRotatedModel(AlternatorBrushesBlockEntity be, BlockState state) {
        return Bufferer.partialFacing(CEEPartialModels.ALTERNATOR_BRUSHES_SHAFT, state);
    }
    private static final class CEENativeState extends net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState {
        AlternatorBrushesBlockEntity blockEntity;
        float partialTicks;
    }
    @Override public net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState createRenderState() { return new CEENativeState(); }
    @Override public void extractRenderState(AlternatorBrushesBlockEntity be, net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state,
            float partialTicks, net.minecraft.world.phys.Vec3 cameraPos,
            net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay overlay) {
        net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState.extractBase(be,state,overlay);
        CEENativeState nativeState=(CEENativeState)state; nativeState.blockEntity=be; nativeState.partialTicks=partialTicks;
    }
    @Override public void submit(net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state,
            com.mojang.blaze3d.vertex.PoseStack pose, net.minecraft.client.renderer.SubmitNodeCollector collector,
            net.minecraft.client.renderer.state.level.CameraRenderState camera) {
        if (!(state instanceof CEENativeState nativeState)) return;
        AlternatorBrushesBlockEntity be=nativeState.blockEntity;
        if (be==null || isInvalid(be)) return;
        var geometry=new com.george_vi.electroenergetics.CEEPartialModels.GeometryCollector(collector);
        renderRotatingBuffer(be,getRotatedModel(be,be.getBlockState()),pose,geometry.getBuffer(net.minecraft.client.renderer.rendertype.RenderTypes.solidMovingBlock()),state.lightCoords);
        geometry.submit();
    }
}
