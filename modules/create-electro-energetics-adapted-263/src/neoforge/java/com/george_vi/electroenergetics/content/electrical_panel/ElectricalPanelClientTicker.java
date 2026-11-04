package com.george_vi.electroenergetics.content.electrical_panel;

import com.george_vi.electroenergetics.CEETags;
import com.george_vi.electroenergetics.content.electrical_panel.attachments.PanelAttachmentType;
import net.createmod.catnip.api.animation.LerpedFloat;
import net.createmod.catnip.api.math.VecHelper;
import net.createmod.catnip.api.client.outliner.Outliner;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class ElectricalPanelClientTicker {

    public static float prevCoverAlpha;
    public static float coverAlpha;

    @OnlyIn(Dist.CLIENT)
    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || !(mc.hitResult instanceof BlockHitResult result))
            return;

        ItemStack heldItemStack = mc.player.getMainHandItem();

        // Cover See Through:
        float coverAlphaTarget = heldItemStack.is(CEETags.ELECTRICAL_PANEL_SEE_THROUGH) ? 0 : 1;

        if (!Float.isFinite(coverAlpha))
            coverAlpha = 1;

        prevCoverAlpha = coverAlpha;
        coverAlpha = LerpedFloat.Chaser.LINEAR.chase(coverAlpha, 0.3, coverAlphaTarget);

        if (!heldItemStack.is(CEETags.ELECTRICAL_PANEL_ATTACHMENT))
            return;

        Level level = mc.level;
        BlockPos targetPos = result.getBlockPos();
        BlockState targetState = level.getBlockState(targetPos);

        if (!(targetState.getBlock() instanceof ElectricalPanelBlock) ||
                result.getDirection() != targetState.getValue(ElectricalPanelBlock.FACING) ||
                !(level.getBlockEntity(targetPos) instanceof ElectricalPanelBlockEntity be))
            return;

        Direction facing = targetState.getValue(ElectricalPanelBlock.FACING);

        PanelAttachmentType attachmentType = PanelAttachmentType.getForItem(heldItemStack);
        if (attachmentType == null)
            return;

        Vec3 localClickPos = result.getLocation().subtract(Vec3.atLowerCornerOf(targetPos));
        ElectricalPanelSlot slot = be.getHoveringAttachmentIndex(localClickPos);
        if (slot != null)
            return;
        slot = attachmentType.mode.getSlot(facing, localClickPos, be.getAttachments());
        if (slot == null)
            return;

        AABB shape = slot.shape;

        Vec3 lowerCorner = VecHelper.rotateCentered(shape.getMinPosition(), -facing.toYRot() + 180, Direction.Axis.Y);
        Vec3 higherCorner = VecHelper.rotateCentered(shape.getMaxPosition(), -facing.toYRot() + 180, Direction.Axis.Y);

        shape = new AABB(
                lowerCorner.add(Vec3.atLowerCornerOf(targetPos)),
                higherCorner.add(Vec3.atLowerCornerOf(targetPos)));

        Outliner.getInstance().showAABB("CEEPanelPlacementOutline", shape, 3)
                .disableLineNormals();

    }

    /** Snapshots the hovered attachment shape during level extraction. */
    @OnlyIn(Dist.CLIENT)
    @Nullable
    public static VoxelShape getHighlightShape(Level level, BlockHitResult hitResult, BlockState state) {
        BlockPos pos = hitResult.getBlockPos();
        Direction facing = state.getValue(ElectricalPanelBlock.FACING);
        if (hitResult.getDirection() != facing || !(level.getBlockEntity(pos) instanceof ElectricalPanelBlockEntity be))
            return null;

        Vec3 localClickPos = hitResult.getLocation().subtract(Vec3.atLowerCornerOf(pos));
        ElectricalPanelSlot slot = be.getHoveringAttachmentIndex(localClickPos);
        if (slot == null)
            return null;

        Vec3 rotatedClickPos = VecHelper.rotateCentered(localClickPos, facing.toYRot() + 180, Direction.Axis.Y);
        double x = rotatedClickPos.x;
        double y = rotatedClickPos.y;
        if (x < 2/16f || x > 14/16f || y < 2/16f || y > 14/16f)
            return null;

        AABB shape = slot.shape;
        Vec3 lowerCorner = VecHelper.rotateCentered(shape.getMinPosition(), -facing.toYRot() + 180, Direction.Axis.Y);
        Vec3 higherCorner = VecHelper.rotateCentered(shape.getMaxPosition(), -facing.toYRot() + 180, Direction.Axis.Y);
        return Shapes.create(new AABB(lowerCorner, higherCorner));
    }
}
