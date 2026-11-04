package com.george_vi.electroenergetics.content.fuse;

import com.george_vi.electroenergetics.content.fuse.fuse_held.FuseHoldable;
import com.george_vi.electroenergetics.foundation.base.DirectionalRolledDeviceBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.george_vi.electroenergetics.client.render.ElectricalRenderer;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.catnip.data.Pair;
import com.george_vi.electroenergetics.client.render.RenderCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

public class FuseHolderRenderer extends ElectricalRenderer<FuseHolderBlockEntity> {
    public FuseHolderRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void extractModels(FuseHolderBlockEntity blockEntity, float partialTicks, PoseStack ms, RenderCollector buffer, int light, int overlay) {
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
            com.george_vi.electroenergetics.client.render.FuseHeldRenderers.render(firstFuse.getFirst(),firstFuse.getSecond(), ms, buffer, light);
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
            com.george_vi.electroenergetics.client.render.FuseHeldRenderers.render(secondFuse.getFirst(),secondFuse.getSecond(), ms, buffer, light);
            ms.popPose();
        }

    }
}
