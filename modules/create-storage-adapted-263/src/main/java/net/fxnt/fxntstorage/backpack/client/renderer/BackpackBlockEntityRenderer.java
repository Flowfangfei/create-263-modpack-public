package net.fxnt.fxntstorage.backpack.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fxnt.fxntstorage.backpack.BackpackBlock;
import net.fxnt.fxntstorage.backpack.BackpackEntity;
import net.fxnt.fxntstorage.backpack.upgrade.UpgradeHelper;
import net.fxnt.fxntstorage.backpack.upgrade.UpgradeType;
import net.fxnt.fxntstorage.backpack.upgrade.workshop.WorkshopFlywheelRenderer;
import net.fxnt.fxntstorage.config.ConfigManager;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BackpackBlockEntityRenderer implements BlockEntityRenderer<BackpackEntity, net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState> {

    public BackpackBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    public void collect(BackpackEntity backpack, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        if (!ConfigManager.ClientConfig.WORKSHOP_FLYWHEEL_VISUALS.get()) return;
        if (!UpgradeHelper.hasActiveUpgrade(backpack.getItemHandler(), UpgradeType.WORKSHOP)) return;

        float angle = backpack.advanceClientFlywheelAngle(
                WorkshopFlywheelRenderer.spinDegPerTick(backpack.isWorkshopProcessing()));

        Direction facing = backpack.getBlockState().getValue(BackpackBlock.FACING);
        VertexConsumer consumer = buffer.getBuffer(RenderTypes.solidMovingBlock());

        poseStack.pushPose();
        // Rotate into the backpack model's space the same way the blockstate does
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.rotate(Axis.YP.rotationDegrees(180.0F - facing.toYRot()));
        poseStack.translate(-0.5, -0.5, -0.5);
        WorkshopFlywheelRenderer.render(backpack.getBlockState().getBlock().asItem(), false,
                poseStack, consumer, packedLight, angle);
        poseStack.popPose();
    }

    private static final class NativeState extends net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState {
        BackpackEntity blockEntity; float partialTicks;
    }
    @Override public net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState createRenderState() { return new NativeState(); }
    @Override public void extractRenderState(BackpackEntity be, net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state,
            float pt, net.minecraft.world.phys.Vec3 camera, net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay overlay) {
        net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState.extractBase(be,state,overlay);
        var nativeState=(NativeState)state; nativeState.blockEntity=be; nativeState.partialTicks=pt;
    }
    @Override public void submit(net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state, PoseStack pose,
            net.minecraft.client.renderer.SubmitNodeCollector collector, net.minecraft.client.renderer.state.level.CameraRenderState camera) {
        var nativeState=(NativeState)state;
        if(nativeState.blockEntity==null || nativeState.blockEntity.isRemoved()) return;
        var geometry=new net.fxnt.fxntstorage.client.StorageRenderBuffers.GeometryCollector(collector);
        collect(nativeState.blockEntity,nativeState.partialTicks,pose,geometry,state.lightCoords,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        geometry.submit();
        
    }
}
