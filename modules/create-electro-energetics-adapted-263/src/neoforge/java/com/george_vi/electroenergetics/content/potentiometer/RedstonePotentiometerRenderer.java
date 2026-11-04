package com.george_vi.electroenergetics.content.potentiometer;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.george_vi.electroenergetics.CEEPartialModels.Bufferer;
import com.george_vi.electroenergetics.CEEPartialModels.ModelBuffer;
import net.createmod.catnip.api.theme.Color;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class RedstonePotentiometerRenderer extends SafeBlockEntityRenderer<RedstonePotentiometerBlockEntity> {

    public RedstonePotentiometerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void renderSafe(RedstonePotentiometerBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {

        int rot = 0;

        Direction facing = be.getBlockState().getValue(PotentiometerBlock.HORIZONTAL_FACING);

        if (facing == Direction.NORTH)
            rot = -135;
        else if (facing == Direction.SOUTH)
            rot = 45;
        else if (facing == Direction.EAST)
            rot = 135;
        else if (facing == Direction.WEST)
            rot = -45;

        float progress = be.smoothProgress.getValue(partialTicks);
        Bufferer.partial(CEEPartialModels.REDSTONE_POTENTIOMETER_DIAL, be.getBlockState())
                .center()
                .rotateYDegrees(progress * 270 + rot)
                .uncenter()
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));


        if (progress < 0.01)
            return;

        Bufferer.partial(CEEPartialModels.REDSTONE_POTENTIOMETER_REDSTONE, be.getBlockState())
                .center()
                .rotateYDegrees(-facing.toYRot() + 180)
                .uncenter()
                .light(light)
                .color(Color.mixColors(0x650101, 0xCD0000, progress))
                .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));
    }

    private static final class CEENativeState extends net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState {
        RedstonePotentiometerBlockEntity blockEntity;
        float partialTicks;
    }
    @Override public net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState createRenderState() { return new CEENativeState(); }
    @Override public void extractRenderState(RedstonePotentiometerBlockEntity be, net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state,
            float partialTicks, net.minecraft.world.phys.Vec3 cameraPos,
            net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay overlay) {
        net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState.extractBase(be,state,overlay);
        CEENativeState nativeState=(CEENativeState)state; nativeState.blockEntity=be; nativeState.partialTicks=partialTicks;
    }
    @Override public void submit(net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state,
            PoseStack pose, net.minecraft.client.renderer.SubmitNodeCollector collector,
            net.minecraft.client.renderer.state.level.CameraRenderState camera) {
        if (!(state instanceof CEENativeState nativeState)) return;
        RedstonePotentiometerBlockEntity be=nativeState.blockEntity;
        if (be==null || isInvalid(be)) return;
        var geometry=new com.george_vi.electroenergetics.CEEPartialModels.GeometryCollector(collector);
        renderSafe(be,nativeState.partialTicks,pose,geometry,state.lightCoords,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        geometry.submit();
    }
}
