package com.george_vi.electroenergetics.content.bulb;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CEEPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.george_vi.electroenergetics.client.render.ElectricalRenderer;
import com.zurrtum.create.client.foundation.render.CreateRenderTypes;
import com.zurrtum.create.client.flywheel.lib.transform.PoseTransformStack;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.george_vi.electroenergetics.client.render.ModelBuffers;
import net.minecraft.util.LightCoordsUtil;
import com.george_vi.electroenergetics.client.render.RenderCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class BulbBlockEntityRenderer extends ElectricalRenderer<BulbBlockEntity> {
    public BulbBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void extractModels(BulbBlockEntity blockEntity, float partialTicks, PoseStack ms, RenderCollector buffer, int light, int overlay) {
        BlockState state = blockEntity.getBlockState();
        PoseTransformStack transformStack = TransformStack.of(ms);
        transformStack.center()
                .rotateYDegrees(state.getValue(BulbBlock.FACING).getAxis().isHorizontal() ? (int) state.getValue(BulbBlock.FACING).toYRot() : 0)
                .rotateXDegrees(state.getValue(BulbBlock.FACING) == Direction.DOWN ? 180 : state.getValue(BulbBlock.FACING).getAxis().isHorizontal() ? 270 : 0)
                .rotateZDegrees(state.getValue(BulbBlock.FACING).getAxis() == Direction.Axis.Z ? 180 : 0)
                .rotateYDegrees(state.getValue(BulbBlock.ROLL) ? 90 : 0)
                .uncenter();

        if (CEEBlocks.BULB.has(state)) {
            float bulbLight = blockEntity.smoothLight.getValue(partialTicks);
            if (bulbLight > 0.5) {
                float factor = (bulbLight - 0.3f);
                int newColor = ((int)(255 * factor) << 16)
                        | ((int)(200 * factor) << 8)
                        | ((int)(140 * factor));
                if (bulbLight < 0.65)
                    ModelBuffers.partial(CEEPartialModels.BULB_GLASS, state)
                            .light(light)
                            .renderInto(ms, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));

                ModelBuffers.partial(CEEPartialModels.BULB_FILAMENT_BRIGHT, state)
                        .color(newColor)
                        .disableDiffuse()
                        .renderInto(ms, buffer.getBuffer(CreateRenderTypes.additive()));

                ModelBuffers.partial(CEEPartialModels.INDICATOR_BULB_GLOW, state)
                        .light(0xf000f0)
                        .color(newColor)
                        .disableDiffuse()
                        .translate(8/16f, 7/16f, 8/16f)
                        .scale(3.5f, 3.5f, 3.5f)
                        .translate(-4/16f, -5/16f, -8/16f)
                        .renderInto(ms, buffer.getBuffer(CreateRenderTypes.additive()));

                ModelBuffers.partial(CEEPartialModels.INDICATOR_BULB_GLOW, state)
                        .light(0xf000f0)
                        .color(newColor)
                        .disableDiffuse()
                        .translate(8/16f, 7/16f, 8/16f)
                        .scale(3.0625f, 3.0625f, 3.0625f)
                        .translate(-4/16f, -5/16f, -8/16f)
                        .renderInto(ms, buffer.getBuffer(CreateRenderTypes.additive()));
            } else {
                ModelBuffers.partial(CEEPartialModels.BULB_FILAMENT, state)
                        .light(bulbLight > 0.05 ? LightCoordsUtil.FULL_BRIGHT : light)
                        .color(bulbLight > 0.7 ? 255 : (int) (bulbLight * 330) + 22, bulbLight > 0.7 ? 255 : (int) (bulbLight * 330) + 22, bulbLight > 0.7 ? 255 : (int) (bulbLight * 330) + 22, 255)
                        .renderInto(ms, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));
                ModelBuffers.partial(CEEPartialModels.BULB_GLASS, state)
                        .light(light)
                        .renderInto(ms, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));
            }
        } else {
            ModelBuffers.partial(CEEPartialModels.BULB_BROKEN_FILAMENT, state)
                    .light(light)
                    .color(22, 22, 22, 255)
                    .renderInto(ms, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));
            ModelBuffers.partial(CEEPartialModels.BULB_GLASS, state)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));
        }
    }
}
