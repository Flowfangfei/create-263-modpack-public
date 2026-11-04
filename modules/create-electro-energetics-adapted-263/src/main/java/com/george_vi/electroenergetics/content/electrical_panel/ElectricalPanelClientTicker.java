package com.george_vi.electroenergetics.content.electrical_panel;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.george_vi.electroenergetics.CEETags;
import com.george_vi.electroenergetics.content.electrical_panel.attachments.PanelAttachmentType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.content.trains.track.TrackBlockOutline;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;

public class ElectricalPanelClientTicker {

    public static float prevCoverAlpha;
    public static float coverAlpha;
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
    public static net.minecraft.world.phys.shapes.VoxelShape getHighlightShape(BlockHitResult result,BlockState state) {
        Minecraft mc=Minecraft.getInstance();
        if(mc.level==null)return null;
        BlockPos pos=result.getBlockPos();
        Direction facing=state.getValue(ElectricalPanelBlock.FACING);
        if(result.getDirection()!=facing || !(mc.level.getBlockEntity(pos) instanceof ElectricalPanelBlockEntity be))return null;
        Vec3 local=result.getLocation().subtract(Vec3.atLowerCornerOf(pos));
        ElectricalPanelSlot slot=be.getHoveringAttachmentIndex(local);
        if(slot==null)return null;
        Vec3 rotated=VecHelper.rotateCentered(local,facing.toYRot()+180,Direction.Axis.Y);
        if(rotated.x<2/16f||rotated.x>14/16f||rotated.y<2/16f||rotated.y>14/16f)return null;
        Vec3 low=VecHelper.rotateCentered(slot.shape.getMinPosition(),-facing.toYRot()+180,Direction.Axis.Y);
        Vec3 high=VecHelper.rotateCentered(slot.shape.getMaxPosition(),-facing.toYRot()+180,Direction.Axis.Y);
        return Shapes.create(new AABB(low,high));
    }
}
