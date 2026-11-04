package com.george_vi.electroenergetics.client.render;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.content.wire.WireAttachment;
import com.george_vi.electroenergetics.content.wire.attachments.BannerAttachmentType;
import com.george_vi.electroenergetics.content.wire.attachments.DamperAttachmentType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.banner.BannerFlagModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.phys.Vec3;

public final class WireAttachmentRenderers {
    private WireAttachmentRenderers() {}

    public static void render(
            PoseStack pose,
            RenderCollector geometry,
            WireAttachment attachment,
            Vec3 pos,
            int light,
            float pitch) {
        if (attachment.type instanceof DamperAttachmentType) {
            ModelBuffers.partial(
                            CEEPartialModels.WIRE_DAMPER_ATTACHMENT,
                            Blocks.ANDESITE.defaultBlockState())
                    .light(light)
                    .rotateZDegrees(-pitch + 180)
                    .renderInto(pose, geometry.getBuffer(RenderTypes.solidMovingBlock()));
        } else if (attachment.type instanceof BannerAttachmentType) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) return;
            BannerPatternLayers patterns =
                    attachment.data.contains("Pattern")
                            ? BannerPatternLayers.CODEC
                                    .parse(
                                            mc.level
                                                    .registryAccess()
                                                    .createSerializationContext(NbtOps.INSTANCE),
                                            attachment.data.get("Pattern"))
                                    .result()
                                    .orElse(BannerPatternLayers.EMPTY)
                            : BannerPatternLayers.EMPTY;
            DyeColor color =
                    DyeColor.byName(
                            attachment.data.getStringOr("BaseColor", "white"), DyeColor.WHITE);
            BannerFlagModel flag =
                    new BannerFlagModel(
                            mc.getEntityModels().bakeLayer(ModelLayers.STANDING_BANNER_FLAG));
            pose.pushPose();
            TransformStack.of(pose).rotateXDegrees(180).rotateYDegrees(90.1f);
            ModelBuffers.partial(
                            CEEPartialModels.ATTACHMENT_CHAIN, Blocks.ANDESITE.defaultBlockState())
                    .light(light)
                    .renderInto(pose, geometry.getBuffer(RenderTypes.cutoutMovingBlock()));
            pose.popPose();
            TransformStack.of(pose).scale(0.66f).translate(0, 0.375, 0.07);
            for (int face = 0; face < 2; face++) {
                geometry.defer(
                        pose,
                        (matrices, queue) -> {
                            queue.submitModel(
                                    flag,
                                    0f,
                                    matrices,
                                    light,
                                    OverlayTexture.NO_OVERLAY,
                                    -1,
                                    Sheets.BANNER_BASE,
                                    mc.getAtlasManager(),
                                    0,
                                    null);
                            BannerRenderer.submitPatterns(
                                    mc.getAtlasManager(),
                                    matrices,
                                    queue,
                                    light,
                                    OverlayTexture.NO_OVERLAY,
                                    flag,
                                    0f,
                                    true,
                                    color,
                                    patterns,
                                    null);
                        });
                TransformStack.of(pose)
                        .translate(0, 0, -0.07)
                        .rotateYDegrees(180)
                        .translate(0, 0, 0.07);
            }
        }
    }
}
