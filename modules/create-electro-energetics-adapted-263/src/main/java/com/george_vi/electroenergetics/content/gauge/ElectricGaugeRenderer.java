package com.george_vi.electroenergetics.content.gauge;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.AllPartialModels;
import com.george_vi.electroenergetics.client.render.ElectricalRenderer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.catnip.data.Iterate;
import com.george_vi.electroenergetics.client.render.ModelBuffers;
import com.george_vi.electroenergetics.client.render.GeometryBuffer;
import com.george_vi.electroenergetics.client.render.RenderCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class ElectricGaugeRenderer extends ElectricalRenderer<ElectricGaugeBlockEntity> {
    public final boolean voltmeter;

    ElectricGaugeRenderer(boolean voltmeter) {
        this.voltmeter = voltmeter;
    }

    public static ElectricGaugeRenderer voltmeter(BlockEntityRendererProvider.Context context) {
        return new ElectricGaugeRenderer(true);
    }

    public static ElectricGaugeRenderer ammeter(BlockEntityRendererProvider.Context context) {
        return new ElectricGaugeRenderer(false);
    }

    @Override
    protected void extractModels(ElectricGaugeBlockEntity be, float partialTicks, PoseStack ms, RenderCollector buffer, int light, int overlay) {
        BlockState gaugeState = be.getBlockState();

        PartialModel partialModel = (voltmeter ? AllPartialModels.GAUGE_HEAD_SPEED : AllPartialModels.GAUGE_HEAD_STRESS);
        GeometryBuffer headBuffer =
                ModelBuffers.partial(partialModel, gaugeState);
        GeometryBuffer dialBuffer = ModelBuffers.partial(AllPartialModels.GAUGE_DIAL, gaugeState);

        float dialPivot = 5.75f / 16;
        float progress = Mth.lerp(partialTicks, be.prevDialState, be.dialState);

        for (Direction facing : Iterate.directions) {
            if (!((ElectricGaugeBlock) gaugeState.getBlock()).shouldRenderHeadOnFace(be.getLevel(), be.getBlockPos(), gaugeState,
                    facing))
                continue;

            RenderCollector.Target vb = buffer.getBuffer(RenderTypes.solidMovingBlock());
            rotateBufferTowards(dialBuffer, facing).translate(0, dialPivot, dialPivot)
                    .rotate((float) (Math.PI / 2 * -progress), Direction.EAST)
                    .translate(0, -dialPivot, -dialPivot)
                    .light(light)
                    .renderInto(ms, vb);
            rotateBufferTowards(headBuffer, facing).light(light)
                    .renderInto(ms, vb);
        }
    }

    protected GeometryBuffer rotateBufferTowards(GeometryBuffer buffer, Direction target) {
        return buffer.rotateCentered((float) ((-target.toYRot() - 90) / 180 * Math.PI), Direction.UP);
    }
}
