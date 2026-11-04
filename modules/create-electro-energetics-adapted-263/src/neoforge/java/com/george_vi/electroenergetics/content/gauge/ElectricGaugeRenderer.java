package com.george_vi.electroenergetics.content.gauge;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.api.data.Iterate;
import com.george_vi.electroenergetics.CEEPartialModels.Bufferer;
import com.george_vi.electroenergetics.CEEPartialModels.ModelBuffer;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class ElectricGaugeRenderer extends SafeBlockEntityRenderer<ElectricGaugeBlockEntity> {
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
    protected void renderSafe(ElectricGaugeBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        BlockState gaugeState = be.getBlockState();

        PartialModel partialModel = (voltmeter ? AllPartialModels.GAUGE_HEAD_SPEED : AllPartialModels.GAUGE_HEAD_STRESS);
        ModelBuffer headBuffer =
                Bufferer.partial(partialModel, gaugeState);
        ModelBuffer dialBuffer = Bufferer.partial(AllPartialModels.GAUGE_DIAL, gaugeState);

        float dialPivot = 5.75f / 16;
        float progress = Mth.lerp(partialTicks, be.prevDialState, be.dialState);

        for (Direction facing : Iterate.directions) {
            if (!((ElectricGaugeBlock) gaugeState.getBlock()).shouldRenderHeadOnFace(be.getLevel(), be.getBlockPos(), gaugeState,
                    facing))
                continue;

            VertexConsumer vb = buffer.getBuffer(RenderTypes.solidMovingBlock());
            rotateBufferTowards(dialBuffer, facing).translate(0, dialPivot, dialPivot)
                    .rotate((float) (Math.PI / 2 * -progress), Direction.EAST)
                    .translate(0, -dialPivot, -dialPivot)
                    .light(light)
                    .renderInto(ms, vb);
            rotateBufferTowards(headBuffer, facing).light(light)
                    .renderInto(ms, vb);
        }
    }

    protected ModelBuffer rotateBufferTowards(ModelBuffer buffer, Direction target) {
        return buffer.rotateCentered((float) ((-target.toYRot() - 90) / 180 * Math.PI), Direction.UP);
    }

    private static final class CEENativeState extends net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState {
        ElectricGaugeBlockEntity blockEntity;
        float partialTicks;
    }
    @Override public net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState createRenderState() { return new CEENativeState(); }
    @Override public void extractRenderState(ElectricGaugeBlockEntity be, net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state,
            float partialTicks, net.minecraft.world.phys.Vec3 cameraPos,
            net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay overlay) {
        net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState.extractBase(be,state,overlay);
        CEENativeState nativeState=(CEENativeState)state; nativeState.blockEntity=be; nativeState.partialTicks=partialTicks;
    }
    @Override public void submit(net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state,
            PoseStack pose, net.minecraft.client.renderer.SubmitNodeCollector collector,
            net.minecraft.client.renderer.state.level.CameraRenderState camera) {
        if (!(state instanceof CEENativeState nativeState)) return;
        ElectricGaugeBlockEntity be=nativeState.blockEntity;
        if (be==null || isInvalid(be)) return;
        var geometry=new com.george_vi.electroenergetics.CEEPartialModels.GeometryCollector(collector);
        renderSafe(be,nativeState.partialTicks,pose,geometry,state.lightCoords,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        geometry.submit();
    }
}
