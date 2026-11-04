package com.george_vi.electroenergetics.content.variac;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.george_vi.electroenergetics.CEEPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.george_vi.electroenergetics.client.render.ElectricalRenderer;
import com.george_vi.electroenergetics.client.render.ModelBuffers;
import com.zurrtum.create.catnip.theme.Color;
import com.george_vi.electroenergetics.client.render.RenderCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

public class RedstoneVariacRenderer extends ElectricalRenderer<RedstoneVariacBlockEntity> {

    public RedstoneVariacRenderer(BlockEntityRendererProvider.Context context) {
        super();
    }

    @Override
    protected void extractModels(RedstoneVariacBlockEntity be, float partialTicks, PoseStack ms, RenderCollector buffer, int light, int overlay) {
        int rot = 0;

        Direction facing = be.getBlockState().getValue(VariacBlock.HORIZONTAL_FACING);

        if (facing == Direction.NORTH)
            rot = -135;
        else if (facing == Direction.SOUTH)
            rot = 45;
        else if (facing == Direction.EAST)
            rot = 135;
        else if (facing == Direction.WEST)
            rot = -45;

        float progress = be.smoothProgress.getValue(partialTicks);
        ModelBuffers.partial(CEEPartialModels.VARIAC_DIAL, be.getBlockState())
                .center()
                .rotateYDegrees(progress * 270 + rot)
                .uncenter()
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));

        if (progress < 0.01)
            return;

        ModelBuffers.partial(CEEPartialModels.REDSTONE_VARIAC_REDSTONE, be.getBlockState())
                .center()
                .rotateYDegrees(-facing.toYRot() + 180)
                .uncenter()
                .light(light)
                .color(Color.mixColors(0x650101, 0xCD0000, progress))
                .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));
    }
}
