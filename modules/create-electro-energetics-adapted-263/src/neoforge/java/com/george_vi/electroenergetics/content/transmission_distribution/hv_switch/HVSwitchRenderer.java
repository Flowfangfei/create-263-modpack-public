package com.george_vi.electroenergetics.content.transmission_distribution.hv_switch;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.content.connector.ConnectorBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.george_vi.electroenergetics.CEEPartialModels.Bufferer;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class HVSwitchRenderer extends SmartBlockEntityRenderer<HVSwitchBlockEntity> {
    public HVSwitchRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(HVSwitchBlockEntity blockEntity, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        float progress = Mth.lerp(partialTicks, blockEntity.prevProgress, blockEntity.progress);

        Bufferer.partial(CEEPartialModels.HV_SWITCH_ARM, blockEntity.getBlockState())
                .translate(0.5, 0.8, 0.5)
                .rotateYDegrees((blockEntity.getBlockState().getValue(HVSwitchBlock.FACING).toYRot()) +
                                (blockEntity.getBlockState().getValue(HVSwitchBlock.FACING).getAxis() == Direction.Axis.X ? 180 : 0))
                .translate(-1/32f, 0, 0)
                .rotateXDegrees(Mth.lerp(progress, -90, 0))
                .light(light)
                .renderInto(ms, buffer.getBuffer(net.minecraft.client.renderer.rendertype.RenderTypes.cutoutMovingBlock()));

        Bufferer.partial(CEEPartialModels.HV_SWITCH_PIVOT, blockEntity.getBlockState())
                .translate(0.5, 0.8, 0.5)
                .rotateYDegrees((blockEntity.getBlockState().getValue(HVSwitchBlock.FACING).toYRot()) +
                        (blockEntity.getBlockState().getValue(HVSwitchBlock.FACING).getAxis() == Direction.Axis.X ? 180 : 0))
                .translate(0, Mth.sin(Mth.lerp(progress, Mth.HALF_PI, 0)), Mth.cos(Mth.lerp(progress, Mth.HALF_PI, 0)))
                .rotateXDegrees(Mth.lerp(progress, 120, 0))
                .light(light)
                .renderInto(ms, buffer.getBuffer(net.minecraft.client.renderer.rendertype.RenderTypes.cutoutMovingBlock()));

        Bufferer.partial(CEEPartialModels.HV_SWITCH_ARM, blockEntity.getBlockState())
                .translate(0.5, 0.8, 0.5)
                .rotateYDegrees((blockEntity.getBlockState().getValue(HVSwitchBlock.FACING).toYRot()) +
                        (blockEntity.getBlockState().getValue(HVSwitchBlock.FACING).getAxis() == Direction.Axis.X ? 180 : 0))
                .translate(-1/32f, Mth.sin(Mth.lerp(progress, Mth.HALF_PI, 0)), Mth.cos(Mth.lerp(progress, Mth.HALF_PI, 0)))
                .rotateXDegrees(Mth.lerp(progress, 70, 0))
                .light(light)
                .renderInto(ms, buffer.getBuffer(net.minecraft.client.renderer.rendertype.RenderTypes.cutoutMovingBlock()));

        BlockState state = blockEntity.getLevel().getBlockState(blockEntity.getBlockPos().relative(blockEntity.getBlockState().getValue(HVSwitchBlock.FACING), 2));
        if (!CEEBlocks.CONNECTOR.has(state) ||
                state.getValue(ConnectorBlock.STYLE) != ConnectorBlock.Style.LONG)
            return;

        Bufferer.partial(CEEPartialModels.HV_SWITCH_PIVOT, blockEntity.getBlockState())
                .translate(0.5, 14/16f, 0.5)
                .rotateYDegrees((blockEntity.getBlockState().getValue(HVSwitchBlock.FACING).toYRot()) +
                        (blockEntity.getBlockState().getValue(HVSwitchBlock.FACING).getAxis() == Direction.Axis.X ? 180 : 0))
                .translate(0, 0, 2)
                .light(light)
                .renderInto(ms, buffer.getBuffer(net.minecraft.client.renderer.rendertype.RenderTypes.cutoutMovingBlock()));

    }

    private static final class CEENativeState extends net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState {
        HVSwitchBlockEntity blockEntity;
        float partialTicks;
    }
    @Override public net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState createRenderState() { return new CEENativeState(); }
    @Override public void extractRenderState(HVSwitchBlockEntity be, net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state,
            float partialTicks, net.minecraft.world.phys.Vec3 cameraPos,
            net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay overlay) {
        net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState.extractBase(be,state,overlay);
        CEENativeState nativeState=(CEENativeState)state; nativeState.blockEntity=be; nativeState.partialTicks=partialTicks;
    }
    @Override public void submit(net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state,
            PoseStack pose, net.minecraft.client.renderer.SubmitNodeCollector collector,
            net.minecraft.client.renderer.state.level.CameraRenderState camera) {
        if (!(state instanceof CEENativeState nativeState)) return;
        HVSwitchBlockEntity be=nativeState.blockEntity;
        if (be==null || isInvalid(be)) return;
        submitBehaviours(be, nativeState.partialTicks, pose, collector, state.lightCoords);
        var geometry=new com.george_vi.electroenergetics.CEEPartialModels.GeometryCollector(collector);
        renderSafe(be,nativeState.partialTicks,pose,geometry,state.lightCoords,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        geometry.submit();
    }
}
