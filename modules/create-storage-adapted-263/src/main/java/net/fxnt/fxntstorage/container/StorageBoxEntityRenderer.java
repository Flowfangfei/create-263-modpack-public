package net.fxnt.fxntstorage.container;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.createmod.catnip.api.client.gui.AbstractSimiScreen;
import net.fxnt.fxntstorage.compat.sable.SableCompat;
import net.fxnt.fxntstorage.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import static net.fxnt.fxntstorage.util.RendererHelper.*;

public class StorageBoxEntityRenderer extends SmartBlockEntityRenderer<StorageBoxEntity> {

    public StorageBoxEntityRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    public static void renderFromContraptionContext(MovementContext context, ContraptionMatrices matrices, MultiBufferSource buffer) {
        BlockState state = context.state;
        CompoundTag tag = context.blockEntityData;
        if (tag == null || state == null) return;

        int amount = tag.getInt("StoredAmount").orElse(0);
        int percentUsed = Math.round(tag.getFloat("PercentageUsed").orElse(0f));

        String line1 = Util.formatNumber(amount);
        String line2 = tag.getBoolean("VoidUpgrade").orElse(false)
                ? Component.translatable("container.fxntstorage.void_mode").getString()
                : Component.translatable("container.fxntstorage.percent_used", percentUsed).getString();

        Direction side = state.getValue(HorizontalDirectionalBlock.FACING);

        PoseStack poseStack = matrices.getModelViewProjection();

        poseStack.pushPose();
        poseStack.translate(context.localPos.getX() + 0.5f, context.localPos.getY() + 0.5f, context.localPos.getZ() + 0.5f);
        poseStack.rotate(Axis.YP.rotationDegrees(-side.toYRot()));
        poseStack.translate(0f, 0f, 0.5f - 0.95f / 16f);

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            poseStack.popPose();
            return;
        }

        double distance = context.position != null
                ? Math.sqrt(player.distanceToSqr(context.position))
                : Math.sqrt(player.distanceToSqr(context.contraption.entity.toGlobalVector(Vec3.atCenterOf(context.localPos), 1f)));

        if (distance >= getMaxDistance()) {
            poseStack.popPose();
            return;
        }

        int color = getColorForDistance(distance);

        BlockPos lightPos = context.contraption.entity.blockPosition().offset(context.localPos).relative(side);
        int envLight = net.minecraft.util.LightCoordsUtil.getLightCoords(context.world, lightPos);
        int textLight = emissiveLight(envLight);
        int itemLight = emissiveItemLight(envLight);

        renderLine(line1, -1f, poseStack, buffer, color, textLight);
        renderLine(line2, -4f, poseStack, buffer, color, textLight);

        if (tag.contains("Filter")) {
            ItemStack filterItem = tag.getCompound("Filter").orElse(new CompoundTag()).isEmpty()
                    ? ItemStack.EMPTY
                    : net.fxnt.fxntstorage.util.RendererHelper.parseStack(context.contraption.entity.level().registryAccess(), tag.getCompound("Filter").orElse(new CompoundTag()));
            if (!filterItem.isEmpty()) {
                renderItem(mc.getItemModelResolver(), filterItem, poseStack, buffer, itemLight, ItemStack.EMPTY);
            }
        }

        poseStack.popPose();
    }

    @Override
    protected void renderSafe(StorageBoxEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Minecraft mc = Minecraft.getInstance();
        Screen currentScreen = mc.gui.screen();
        boolean isPonderScene = currentScreen instanceof AbstractSimiScreen;
        boolean inSubLevel = SableCompat.isInPlotGrid(blockEntity);

        Player player = mc.player;
        if (player == null) return;

        Level level = blockEntity.getLevel();
        if (level == null) return;

        int amount = blockEntity.getStoredAmount();
        int percentUsed = blockEntity.getPercentageUsed();

        String line1 = Util.formatNumber(amount);
        String line2 = blockEntity.voidUpgrade
                ? Component.translatable("container.fxntstorage.void_mode").getString()
                : Component.translatable("container.fxntstorage.percent_used", percentUsed).getString();

        float distance = (float) Math.sqrt(blockEntity.getBlockPos().distToCenterSqr(player.position()));

        if (distance > getMaxDistance() && !isPonderScene && !inSubLevel) return;
        if (isPonderScene || inSubLevel) distance = 5f;

        BlockState blockState = blockEntity.getBlockState();
        Direction side = blockState.getValue(HorizontalDirectionalBlock.FACING);

        int envLight = net.minecraft.util.LightCoordsUtil.getLightCoords(level, blockEntity.getBlockPos());
        int textLight = emissiveLight(envLight);
        int itemLight = emissiveItemLight(envLight);

        FilteringRenderer.renderOnBlockEntity(blockEntity, partialTick, poseStack, buffer, itemLight, packedOverlay);

        poseStack.pushPose();
        poseStack.translate(0.5f, 0, 0.5f);
        poseStack.rotate(Axis.YP.rotationDegrees(-side.toYRot()));
        poseStack.translate(0f, 0f, 0.5f - 0.95f / 16f);

        int color = getColorForDistance(distance);

        renderLine(line1, 7f, poseStack, buffer, color, textLight);
        renderLine(line2, 4f, poseStack, buffer, color, textLight);

        poseStack.popPose();
    }

    private static final class NativeState extends net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState {
        StorageBoxEntity blockEntity; float partialTicks;
    }
    @Override public net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState createRenderState() { return new NativeState(); }
    @Override public void extractRenderState(StorageBoxEntity be, net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state,
            float pt, net.minecraft.world.phys.Vec3 camera, net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay overlay) {
        net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState.extractBase(be,state,overlay);
        var nativeState=(NativeState)state; nativeState.blockEntity=be; nativeState.partialTicks=pt;
    }
    @Override public void submit(net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState state, PoseStack pose,
            net.minecraft.client.renderer.SubmitNodeCollector collector, net.minecraft.client.renderer.state.level.CameraRenderState camera) {
        var nativeState=(NativeState)state;
        if(nativeState.blockEntity==null || nativeState.blockEntity.isRemoved()) return;
        var geometry=new net.fxnt.fxntstorage.client.StorageRenderBuffers.GeometryCollector(collector);
        renderSafe(nativeState.blockEntity,nativeState.partialTicks,pose,geometry,state.lightCoords,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        geometry.submit();
        submitBehaviours(nativeState.blockEntity,nativeState.partialTicks,pose,collector,state.lightCoords);
    }
}
