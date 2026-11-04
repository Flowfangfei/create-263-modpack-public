package com.george_vi.electroenergetics.content.railway_electrification.catenary;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import com.george_vi.electroenergetics.CEEPartialModels.Bufferer;
import com.george_vi.electroenergetics.CEEPartialModels.ModelBuffer;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class CatenaryHolderRenderer extends SmartBlockEntityRenderer<CatenaryHolderBlockEntity> {
    public CatenaryHolderRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(CatenaryHolderBlockEntity blockEntity, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        BlockPos attachedTo = blockEntity.getAttachedTo();
        if (blockEntity.poleWidth == 0 || attachedTo == null)
            return;

        CatenaryHolderBlock.Style style = blockEntity.getBlockState().getValue(CatenaryHolderBlock.STYLE);

        attachedTo = attachedTo.offset(blockEntity.getBlockPos());


        ModelBuffer grip;
        if (blockEntity.poleWidth == 4)
            grip = Bufferer.partial(style.isWeathered() ? CEEPartialModels.CATENARY_HOLDER_MOUNT_4_WEATHERED : CEEPartialModels.CATENARY_HOLDER_MOUNT_4, blockEntity.getBlockState());
        else if (blockEntity.poleWidth == 6)
            grip = Bufferer.partial(style.isWeathered() ? CEEPartialModels.CATENARY_HOLDER_MOUNT_6_WEATHERED : CEEPartialModels.CATENARY_HOLDER_MOUNT_6, blockEntity.getBlockState());
        else if (blockEntity.poleWidth == 8)
            grip = Bufferer.partial(style.isWeathered() ? CEEPartialModels.CATENARY_HOLDER_MOUNT_8_WEATHERED : CEEPartialModels.CATENARY_HOLDER_MOUNT_8, blockEntity.getBlockState());
        else
            grip = Bufferer.partial(style.isWeathered() ? CEEPartialModels.CATENARY_HOLDER_MOUNT_10_WEATHERED: CEEPartialModels.CATENARY_HOLDER_MOUNT_10, blockEntity.getBlockState());


        Vec3 difference = Vec3.atLowerCornerOf(attachedTo).subtract(Vec3.atLowerCornerOf(blockEntity.getBlockPos()));

        float yaw = (float) Math.toDegrees(Mth.atan2(difference.x, difference.z)) + 180;

        PartialModel longRodModel = style.isWeathered() ? CEEPartialModels.CATENARY_HOLDER_LONG_ROD_WEATHERED : CEEPartialModels.CATENARY_HOLDER_LONG_ROD;
        PartialModel shortRodModel = style.isWeathered() ? CEEPartialModels.CATENARY_HOLDER_SHORT_ROD_WEATHERED : CEEPartialModels.CATENARY_HOLDER_SHORT_ROD;
        PartialModel insulatorModel = style.isWeathered() ? CEEPartialModels.CATENARY_HOLDER_INSULATOR_WEATHERED : CEEPartialModels.CATENARY_HOLDER_INSULATOR;

        if (style.isLow()) {
            grip.translate(attachedTo.subtract(blockEntity.getBlockPos()))
                    .translate(0.5, 11/32f, 0.5)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));

            Bufferer.partial(insulatorModel, blockEntity.getBlockState())
                    .translate(attachedTo.subtract(blockEntity.getBlockPos()))
                    .translate(0.5, 8/16f, 0.5)
                    .rotateYDegrees(yaw)
                    .translate(0, 0, 1/16f + blockEntity.poleWidth / 32f)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));

            float shortLength = 17 / 16f;
            float longLength = 28 / 16f;

            float horizontalDistance = (float) difference.horizontalDistance();
            boolean isLong = horizontalDistance > shortLength;
            float size = (horizontalDistance + 0.5f) / (isLong ? longLength : shortLength);
            Bufferer.partial(isLong ? longRodModel : CEEPartialModels.CATENARY_HOLDER_SHORT_ROD, blockEntity.getBlockState())
                    .translate(attachedTo.subtract(blockEntity.getBlockPos()))
                    .translate(0.5, 8/16f, 0.5)
                    .rotateYDegrees(yaw)
                    .scaleZ(size)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));

            Bufferer.partial(CEEPartialModels.CATENARY_HOLDER_CONNECTOR, blockEntity.getBlockState())
                    .translate(attachedTo.subtract(blockEntity.getBlockPos()))
                    .translate(0.5, -8 / 16f, 0.5)
                    .rotateYDegrees(yaw)
                    .translate(0, 10/16f, horizontalDistance)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));
            return;
        }

        grip.translate(attachedTo.subtract(blockEntity.getBlockPos()))
                .translate(0.5, 43/32f, 0.5)
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));

        Bufferer.partial(insulatorModel, blockEntity.getBlockState())
                .translate(attachedTo.subtract(blockEntity.getBlockPos()))
                .translate(0.5, 24/16f, 0.5)
                .rotateYDegrees(yaw)
                .translate(0, 0, 1/16f + blockEntity.poleWidth / 32f)
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));
        float shortLength = 17 / 16f;
        float longLength = 28 / 16f;

        float horizontalDistance = (float) difference.horizontalDistance();
        boolean isLong = horizontalDistance > shortLength;
        float size = horizontalDistance / (isLong ? longLength : shortLength);
        Bufferer.partial(isLong ? longRodModel : shortRodModel, blockEntity.getBlockState())
                .translate(attachedTo.subtract(blockEntity.getBlockPos()))
                .translate(0.5, 24/16f, 0.5)
                .rotateYDegrees(yaw)
                .scale(0.95f, 0.95f, size)
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));

        float angle = -(float) Mth.atan2(2, horizontalDistance);
        // Thick rod
        Bufferer.partial(longRodModel, blockEntity.getBlockState())
                .translate(attachedTo.subtract(blockEntity.getBlockPos()))
                .translate(0.5, -8 / 16f, 0.5)
                .rotateYDegrees(yaw)
                .rotateX(angle)
                .scaleZ(Mth.sqrt(4f + horizontalDistance * horizontalDistance) / longLength)
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));

        // Bottom insulator
        Bufferer.partial(insulatorModel, blockEntity.getBlockState())
                .translate(attachedTo.subtract(blockEntity.getBlockPos()))
                .translate(0.5, -8 / 16f, 0.5)
                .rotateYDegrees(yaw)
                .rotateX(angle)
                .translate(0, 0, 0.25f)
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));

        // Holding?? rod
        Bufferer.partial(longRodModel, blockEntity.getBlockState())
                .translate(attachedTo.subtract(blockEntity.getBlockPos()))
                .translate(0.5, -8 / 16f, 0.5)
                .rotateYDegrees(yaw)
                .translate(0, 15/16f, horizontalDistance / 2 - 0.1)
                .scale(0.95f, 0.95f, (horizontalDistance + 1.1f) / (2 * longLength))
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));

        // Connector
        Bufferer.partial(CEEPartialModels.CATENARY_HOLDER_CONNECTOR, blockEntity.getBlockState())
                .translate(attachedTo.subtract(blockEntity.getBlockPos()))
                .translate(0.5, -8 / 16f, 0.5)
                .rotateYDegrees(yaw)
                .translate(0, 10/16f, horizontalDistance)
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));

        grip.translate(attachedTo.subtract(blockEntity.getBlockPos()))
                .translate(0.5, -10/16f, 0.5)
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    private static final class CEENativeState extends net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState {
        CatenaryHolderBlockEntity blockEntity;
        float partialTicks;
    }
    @Override public net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState createRenderState() { return new CEENativeState(); }
    @Override public void extractRenderState(CatenaryHolderBlockEntity be, net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state,
            float partialTicks, net.minecraft.world.phys.Vec3 cameraPos,
            net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay overlay) {
        net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState.extractBase(be,state,overlay);
        CEENativeState nativeState=(CEENativeState)state; nativeState.blockEntity=be; nativeState.partialTicks=partialTicks;
    }
    @Override public void submit(net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state,
            PoseStack pose, net.minecraft.client.renderer.SubmitNodeCollector collector,
            net.minecraft.client.renderer.state.level.CameraRenderState camera) {
        if (!(state instanceof CEENativeState nativeState)) return;
        CatenaryHolderBlockEntity be=nativeState.blockEntity;
        if (be==null || isInvalid(be)) return;
        submitBehaviours(be, nativeState.partialTicks, pose, collector, state.lightCoords);
        var geometry=new com.george_vi.electroenergetics.CEEPartialModels.GeometryCollector(collector);
        renderSafe(be,nativeState.partialTicks,pose,geometry,state.lightCoords,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        geometry.submit();
    }
}
