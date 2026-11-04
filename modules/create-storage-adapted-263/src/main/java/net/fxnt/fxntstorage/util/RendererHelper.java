package net.fxnt.fxntstorage.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.content.kinetics.simpleRelays.AbstractSimpleShaftBlock;
import com.simibubi.create.infrastructure.config.AllConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;

public class RendererHelper {
    // Use Create config for max render distance - read at call time so config changes take effect without restart
    public static double getMaxDistance() {
        return AllConfigs.client().filterItemRenderDistance.get();
    }

    // Minimum packed-light value per component so text/items look emissive; 128 = level 8 (out of 15).
    private static final int EMISSIVE_BASE = 224;
    private static final int EMISSIVE_BASE_ITEM = 192;

    public static int emissiveLight(int envPackedLight) {
        return emissiveLight(envPackedLight, EMISSIVE_BASE);
    }

    public static int emissiveLight(int envPackedLight, int base) {
        int sky = (envPackedLight >> 16) & 0xFFFF;
        int block = envPackedLight & 0xFFFF;
        // Use the strongest source for both components so shader packs see neutral white
        int value = Math.max(Math.max(sky, block), base);
        return (value << 16) | value;
    }

    public static int emissiveItemLight(int envPackedLight) {
        return emissiveLight(envPackedLight, EMISSIVE_BASE_ITEM);
    }

    public static void renderLine(String text, float yOffset, PoseStack poseStack, MultiBufferSource buffer, int color, int packedLight) {
        renderLine(text, yOffset, poseStack, buffer, color, packedLight, 1f);
    }

    public static void renderLine(String text, float yOffset, PoseStack poseStack, MultiBufferSource buffer, int color, int packedLight, float scale) {
        Font font = Minecraft.getInstance().font;

        poseStack.pushPose();
        poseStack.translate(0f, yOffset / 16f, 0f);
        poseStack.scale(scale / 64f, -scale / 64f, 1f);

        float x = -font.width(text) / 2f;

        ((net.fxnt.fxntstorage.client.StorageRenderBuffers.GeometryCollector)buffer).text(poseStack,text,x,0,color|0xff000000,packedLight);
        poseStack.popPose();
    }

    public static void renderLine(Component text, float yOffset, PoseStack poseStack, MultiBufferSource buffer, int packedLight, float scale) {
        Font font = Minecraft.getInstance().font;

        poseStack.pushPose();
        poseStack.translate(0f, yOffset / 16f, 0f);
        poseStack.scale(scale / 64f, -scale / 64f, 1f);

        float x = -font.width(text) / 2f;

        ((net.fxnt.fxntstorage.client.StorageRenderBuffers.GeometryCollector)buffer).text(poseStack,text,x,0,0xffffffff,packedLight);
        poseStack.popPose();
    }

    public static void renderItem(ItemModelResolver itemRenderer, ItemStack stack, PoseStack poseStack, MultiBufferSource buffer, int packedLight, ItemStack upgrade) {
        Level level = Minecraft.getInstance().level;
        poseStack.pushPose();
        poseStack.translate(0f, 0.175f, 0f);
        poseStack.rotate(Axis.YP.rotationDegrees(180));

        ItemStackRenderState itemState=new ItemStackRenderState();
        itemRenderer.updateForTopItem(itemState,stack,ItemDisplayContext.FIXED,Minecraft.getInstance().level,Minecraft.getInstance().player,0);
        boolean flatItem = !itemState.usesBlockLight();

        float scale = (flatItem ? 0.25f : 0.50f) + 0.01f;
        float zOffset = (flatItem ? -.025f : .068f) + customZOffset(stack.getItem());

        poseStack.scale(scale, scale, scale);
        poseStack.translate(0, 0, zOffset);

        ((net.fxnt.fxntstorage.client.StorageRenderBuffers.GeometryCollector)buffer).defer(poseStack,(matrices,queue)->itemState.submit(matrices,queue,packedLight,OverlayTexture.NO_OVERLAY,0));
        poseStack.popPose();

        if (!upgrade.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0f, 0.5f, 0f);
            poseStack.rotate(Axis.YP.rotationDegrees(180));
            poseStack.translate(-0.3f, -0.2f, 0f);

            scale = 0.25f + (1 / 64f);
            poseStack.scale(scale, scale, scale);

            ItemStackRenderState upgradeState=new ItemStackRenderState();
            itemRenderer.updateForTopItem(upgradeState,upgrade,ItemDisplayContext.FIXED,Minecraft.getInstance().level,Minecraft.getInstance().player,0);
            ((net.fxnt.fxntstorage.client.StorageRenderBuffers.GeometryCollector)buffer).defer(poseStack,(matrices,queue)->upgradeState.submit(matrices,queue,packedLight,OverlayTexture.NO_OVERLAY,0));

            poseStack.popPose();
        }
    }

    public static int getColorForDistance(double distance) {
        final int START = 192;
        final int END = 32;

        double maxDistance = getMaxDistance();
        double clamped = Math.min(distance, maxDistance);
        double factor = clamped / maxDistance;

        int val = (int) (START + (END - START) * factor);

        return (val << 16) | (val << 8) | val;
    }

    private static float customZOffset(Item item) {
        if (item instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            if (block instanceof AbstractSimpleShaftBlock
                    || block instanceof FenceBlock
                    || block.defaultBlockState().is(BlockTags.BUTTONS)
                    || block == Blocks.END_ROD) {
                return -0.1f;
            }
        }
        return 0f;
    }

    public static ItemStack parseStack(net.minecraft.core.HolderLookup.Provider registries, net.minecraft.nbt.CompoundTag tag) {
        return ItemStack.CODEC.parse(registries.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE),tag).result().orElse(ItemStack.EMPTY);
    }
    /** Called from the real native contraption renderer tail, including when Flywheel owns the blocks. */
    public static void submitMounted(com.simibubi.create.content.contraptions.AbstractContraptionEntity entity, float partialTicks,
            PoseStack pose, net.minecraft.client.renderer.SubmitNodeCollector collector) {
        if(entity==null || entity.getContraption()==null) return;
        var matrices=new com.simibubi.create.content.contraptions.render.ContraptionMatrices();
        matrices.getModelViewProjection().last().set(pose.last());
        entity.applyLocalTransforms(matrices.getModelViewProjection(),partialTicks);
        var geometry=new net.fxnt.fxntstorage.client.StorageRenderBuffers.GeometryCollector(collector);
        for(var actor : entity.getContraption().getActors()) {
            var context=actor.getRight(); if(context==null) continue;
            if(context.world==null) context.world=entity.level();
            var block=actor.getLeft().state().getBlock();
            if(block instanceof net.fxnt.fxntstorage.container.StorageBox)
                net.fxnt.fxntstorage.container.StorageBoxEntityRenderer.renderFromContraptionContext(context,matrices,geometry);
            else if(block instanceof net.fxnt.fxntstorage.simple_storage.SimpleStorageBox)
                net.fxnt.fxntstorage.simple_storage.SimpleStorageBoxEntityRenderer.renderFromContraptionContext(context,matrices,geometry);
            else if(block instanceof net.fxnt.fxntstorage.reserve_storage.ReserveStorageBox)
                net.fxnt.fxntstorage.reserve_storage.ReserveStorageBoxEntityRenderer.renderFromContraptionContext(context,matrices,geometry);
        }
        geometry.submit();
    }
}
