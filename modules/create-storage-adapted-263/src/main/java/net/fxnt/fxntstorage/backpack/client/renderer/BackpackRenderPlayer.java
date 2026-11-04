package net.fxnt.fxntstorage.backpack.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fxnt.fxntstorage.FXNTStorage;
import net.fxnt.fxntstorage.backpack.BackpackItem;
import net.fxnt.fxntstorage.backpack.upgrade.UpgradeType;
import net.fxnt.fxntstorage.backpack.upgrade.workshop.WorkshopClientState;
import net.fxnt.fxntstorage.backpack.upgrade.workshop.WorkshopFlywheelRenderer;
import net.fxnt.fxntstorage.backpack.util.BackpackHelper;
import net.fxnt.fxntstorage.config.ConfigManager;
import net.fxnt.fxntstorage.init.ModDataComponents;
import net.fxnt.fxntstorage.item.upgrades.UpgradeItem;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class BackpackRenderPlayer extends RenderLayer<AvatarRenderState, PlayerModel> {
    private final RandomSource random = RandomSource.create();

    public BackpackRenderPlayer(RenderLayerParent<AvatarRenderState, PlayerModel> pRenderer) {
        super(pRenderer);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int packedLight, AvatarRenderState state, float yaw, float pitch) {
        var level=net.minecraft.client.Minecraft.getInstance().level;
        if(level==null || !(level.getEntity(state.id) instanceof AbstractClientPlayer livingEntity)) return;
        var buffer=new net.fxnt.fxntstorage.client.StorageRenderBuffers.GeometryCollector(collector);
        ItemStack backpack = BackpackHelper.getEquippedBackpackStack(livingEntity);
        if (backpack.isEmpty()) return;

        if (FXNTStorage.CURIOS_LOADED) {
            boolean isBackpackVisible = BackpackHelper.isBackpackCuriosSlotVisible(livingEntity);

            if (!(livingEntity.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof BackpackItem)) {
                // Is the Curios slot visibility toggled
                if (!isBackpackVisible) return;
            }
        }

        poseStack.pushPose();

        poseStack.rotate(Axis.YP.rotationDegrees(180.0F));

        poseStack.translate(0F, 0.65F, -0.3F);
        poseStack.scale(0.85F, 0.85F, 0.85F);

        if (livingEntity.isCrouching()) {
            poseStack.rotate(Axis.XP.rotationDegrees(-58.0F));
            poseStack.translate(0D, 0.15D, -0.1D);
        }

        renderBackpackModel(poseStack, collector, packedLight, BackpackWornModels.get(backpack));

        if (hasActiveWorkshop(backpack) && ConfigManager.ClientConfig.WORKSHOP_FLYWHEEL_VISUALS.get()) {
            renderFlywheels(poseStack, buffer, packedLight, livingEntity.getId(), backpack);
        }

        poseStack.popPose();
        buffer.submit();
    }

    private void renderBackpackModel(PoseStack poseStack,SubmitNodeCollector collector,int packedLight,net.minecraft.client.renderer.block.dispatch.BlockStateModelPart wornModel) {
        if(wornModel==null) return;
        poseStack.pushPose();applyModelSpace(poseStack);
        collector.submitBlockModel(poseStack,RenderTypes.cutoutMovingBlock(),java.util.List.of(wornModel),net.minecraft.client.renderer.block.BlockModelRenderState.EMPTY_TINTS,packedLight,OverlayTexture.NO_OVERLAY,0);
        poseStack.popPose();
    }

    private void applyModelSpace(PoseStack poseStack) {
        this.getParentModel().body.translateAndRotate(poseStack);
        poseStack.rotate(Axis.ZP.rotationDegrees(180.0F));
        poseStack.translate(-0.5F, 0.0F, -0.5F);
    }

    private static final String WORKSHOP_ACTIVE_NAME =
            ((UpgradeItem) UpgradeType.WORKSHOP.getActiveItem()).getUpgradeName();

    private static boolean hasActiveWorkshop(ItemStack backpack) {
        List<String> upgrades = backpack.get(ModDataComponents.BACKPACK_UPGRADES);
        return upgrades != null && upgrades.contains(WORKSHOP_ACTIVE_NAME) && !UpgradeType.WORKSHOP.isDisabled();
    }

    private void renderFlywheels(PoseStack poseStack, MultiBufferSource buffer, int packedLight, int entityId, ItemStack backpack) {
        boolean processing = WorkshopClientState.isProcessing(entityId);
        float angle = WorkshopClientState.advanceAngle(entityId, WorkshopFlywheelRenderer.spinDegPerTick(processing));

        VertexConsumer consumer = buffer.getBuffer(RenderTypes.solidMovingBlock());

        poseStack.pushPose();
        // Same space as the worn model, so layouts line up with it and follow the body as it bends
        applyModelSpace(poseStack);
        WorkshopFlywheelRenderer.render(backpack.getItem(), true, poseStack, consumer, packedLight, angle);
        poseStack.popPose();
    }
}
