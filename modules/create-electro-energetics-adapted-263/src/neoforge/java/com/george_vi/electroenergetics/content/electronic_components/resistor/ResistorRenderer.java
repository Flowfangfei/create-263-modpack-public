package com.george_vi.electroenergetics.content.electronic_components.resistor;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.foundation.base.DirectionalRolledDeviceBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.george_vi.electroenergetics.CEEPartialModels.Bufferer;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

import java.util.Map;

public class ResistorRenderer extends SmartBlockEntityRenderer<ResistorBlockEntity> {
    public ResistorRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(ResistorBlockEntity blockEntity, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        double resistance = blockEntity.resistance.value / 1000d;
        if (resistance > 1000000 || resistance < 0.1)
            return;

        int[] digitColor = {
                0x101010, // black
                0x8B4513, // brown
                0xEE1010, // red
                0xFF7F00, // orange
                0xFFFF00, // yellow
                0x00FF00, // green
                0x3030EE, // blue
                0x8B00FF, // violet
                0x808080, // grey
                0xFFFFFF  // white
        };

        Map<Integer, Integer> multiplierColor = Map.of(
               -2, 0xC0C0C0, // silver
               -1, 0xD6CB84, // gold
               0, 0x101010, // black
               1, 0x8B4513, // brown
               2, 0xEE1010, // red
               3, 0xFF7F00, // orange
               4, 0xFFFF00, // yellow
               5, 0x00FF00, // green
               6, 0x3030EE  // blue
        );

        int exponent = 0;
        while (resistance >= 100) {
            resistance = resistance / 10;
            exponent++;
        }
        while (resistance < 10) {
            resistance = resistance * 10;
            exponent--;
        }

        int digits = (int) Math.round(resistance);
        int d1 = (int) Math.floor(digits / 10d);
        int d2 = digits % 10;

        boolean roll = blockEntity.getBlockState().getValue(DirectionalRolledDeviceBlock.ROLL);
        Direction facing = blockEntity.getBlockState().getValue(DirectionalRolledDeviceBlock.FACING);

        Bufferer.partial(CEEPartialModels.RESISTOR_STRIP, blockEntity.getBlockState())
                .center()
                .rotateYDegrees(facing.getAxis().isHorizontal() ? (int) facing.toYRot() : 0)
                .rotateXDegrees(facing == Direction.DOWN ? 180 : facing.getAxis().isHorizontal() ? 270 : 0)
                .rotateZDegrees(facing.getAxis() == Direction.Axis.Z ? 180 : 0)
                .rotateYDegrees(roll ? 90 : 0)
                .uncenter()
                .color(digitColor[d1])
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));

        Bufferer.partial(CEEPartialModels.RESISTOR_STRIP, blockEntity.getBlockState())
                .center()
                .rotateYDegrees(facing.getAxis().isHorizontal() ? (int) facing.toYRot() : 0)
                .rotateXDegrees(facing == Direction.DOWN ? 180 : facing.getAxis().isHorizontal() ? 270 : 0)
                .rotateZDegrees(facing.getAxis() == Direction.Axis.Z ? 180 : 0)
                .rotateYDegrees(roll ? 90 : 0)
                .uncenter()
                .translate(-2/16f, 0, 0)
                .color(digitColor[d2])
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));

        Bufferer.partial(CEEPartialModels.RESISTOR_STRIP, blockEntity.getBlockState())
                .center()
                .rotateYDegrees(facing.getAxis().isHorizontal() ? (int) facing.toYRot() : 0)
                .rotateXDegrees(facing == Direction.DOWN ? 180 : facing.getAxis().isHorizontal() ? 270 : 0)
                .rotateZDegrees(facing.getAxis() == Direction.Axis.Z ? 180 : 0)
                .rotateYDegrees(roll ? 90 : 0)
                .uncenter()
                .color(multiplierColor.get(exponent))
                .translate(-4/16f, 0, 0)
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));
    }

    private static final class CEENativeState extends net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState {
        ResistorBlockEntity blockEntity;
        float partialTicks;
    }
    @Override public net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState createRenderState() { return new CEENativeState(); }
    @Override public void extractRenderState(ResistorBlockEntity be, net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state,
            float partialTicks, net.minecraft.world.phys.Vec3 cameraPos,
            net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay overlay) {
        net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState.extractBase(be,state,overlay);
        CEENativeState nativeState=(CEENativeState)state; nativeState.blockEntity=be; nativeState.partialTicks=partialTicks;
    }
    @Override public void submit(net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state,
            PoseStack pose, net.minecraft.client.renderer.SubmitNodeCollector collector,
            net.minecraft.client.renderer.state.level.CameraRenderState camera) {
        if (!(state instanceof CEENativeState nativeState)) return;
        ResistorBlockEntity be=nativeState.blockEntity;
        if (be==null || isInvalid(be)) return;
        submitBehaviours(be, nativeState.partialTicks, pose, collector, state.lightCoords);
        var geometry=new com.george_vi.electroenergetics.CEEPartialModels.GeometryCollector(collector);
        renderSafe(be,nativeState.partialTicks,pose,geometry,state.lightCoords,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        geometry.submit();
    }
}
