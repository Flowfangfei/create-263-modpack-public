package com.george_vi.electroenergetics.content.fuse;

import com.george_vi.electroenergetics.content.fuse.fuse_held.FuseHoldable;
import com.george_vi.electroenergetics.foundation.base.DirectionalRolledDeviceBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.api.data.Pair;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

public class FuseHolderRenderer extends SmartBlockEntityRenderer<FuseHolderBlockEntity> {
    public FuseHolderRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(FuseHolderBlockEntity blockEntity, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        Pair<FuseHoldable, CompoundTag> firstFuse = blockEntity.firstFuse;
        Pair<FuseHoldable, CompoundTag> secondFuse = blockEntity.secondFuse;

        boolean roll = blockEntity.getBlockState().getValue(DirectionalRolledDeviceBlock.ROLL);
        Direction facing = blockEntity.getBlockState().getValue(DirectionalRolledDeviceBlock.FACING);

        if (facing.getAxis().isVertical()) {
            Pair<FuseHoldable, CompoundTag> temp = firstFuse;
            firstFuse = secondFuse;
            secondFuse = temp;
        }


        if (firstFuse != null) {
            ms.pushPose();
            TransformStack.of(ms)
                    .center()
                    .rotateYDegrees(facing.getAxis().isHorizontal() ? (int) facing.toYRot() : 0)
                    .rotateXDegrees(facing == Direction.DOWN ? 180 : facing.getAxis().isHorizontal() ? 270 : 0)
                    .rotateZDegrees(facing.getAxis() == Direction.Axis.Z ? 180 : 0)
                    .rotateYDegrees(roll ? 90 : 0)
                    .uncenter()
                    .translate(5.5/16f, 0, 0);
            firstFuse.getFirst().render(firstFuse.getSecond(), ms, buffer, light);
            ms.popPose();
        }

        if (secondFuse != null) {
            ms.pushPose();
            TransformStack.of(ms)
                    .center()
                    .rotateYDegrees(facing.getAxis().isHorizontal() ? (int) facing.toYRot() : 0)
                    .rotateXDegrees(facing == Direction.DOWN ? 180 : facing.getAxis().isHorizontal() ? 270 : 0)
                    .rotateZDegrees(facing.getAxis() == Direction.Axis.Z ? 180 : 0)
                    .rotateYDegrees(roll ? 90 : 0)
                    .uncenter()
                    .translate(10.5/16f, 0, 0);
            secondFuse.getFirst().render(secondFuse.getSecond(), ms, buffer, light);
            ms.popPose();
        }

    }

    private static final class CEENativeState extends net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState {
        FuseHolderBlockEntity blockEntity;
        float partialTicks;
    }
    @Override public net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState createRenderState() { return new CEENativeState(); }
    @Override public void extractRenderState(FuseHolderBlockEntity be, net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state,
            float partialTicks, net.minecraft.world.phys.Vec3 cameraPos,
            net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay overlay) {
        net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState.extractBase(be,state,overlay);
        CEENativeState nativeState=(CEENativeState)state; nativeState.blockEntity=be; nativeState.partialTicks=partialTicks;
    }
    @Override public void submit(net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state,
            PoseStack pose, net.minecraft.client.renderer.SubmitNodeCollector collector,
            net.minecraft.client.renderer.state.level.CameraRenderState camera) {
        if (!(state instanceof CEENativeState nativeState)) return;
        FuseHolderBlockEntity be=nativeState.blockEntity;
        if (be==null || isInvalid(be)) return;
        submitBehaviours(be, nativeState.partialTicks, pose, collector, state.lightCoords);
        var geometry=new com.george_vi.electroenergetics.CEEPartialModels.GeometryCollector(collector);
        renderSafe(be,nativeState.partialTicks,pose,geometry,state.lightCoords,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        geometry.submit();
    }
}
