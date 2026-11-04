package com.george_vi.electroenergetics.client;

import com.george_vi.electroenergetics.CEEPartialModels.Bufferer;
import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.CEETags;
import com.george_vi.electroenergetics.content.wire.WireAttachment;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import dev.engine_room.flywheel.lib.transform.PoseTransformStack;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import java.util.List;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.banner.BannerFlagModel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/** Original wire attachment geometry is loaded only by client rendering callbacks. */
public final class CEEWireAttachmentRenderers {
    private CEEWireAttachmentRenderers() {}

    public static void renderBanner(PoseStack pose, MultiBufferSource buffer, WireAttachment attachment, Vec3 pos, int light, float pitch) {
        var geometry=(com.george_vi.electroenergetics.CEEPartialModels.GeometryCollector)buffer;
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
            com.george_vi.electroenergetics.CEEPartialModels.Bufferer.partial(
                            com.george_vi.electroenergetics.CEEPartialModels.ATTACHMENT_CHAIN, Blocks.ANDESITE.defaultBlockState())
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
                                    0);
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
                                    patterns);
                        });
                TransformStack.of(pose)
                        .translate(0, 0, -0.07)
                        .rotateYDegrees(180)
                        .translate(0, 0, 0.07);
            }
    
    }

    public static void renderDamper(PoseStack pose, MultiBufferSource buffer, WireAttachment attachment, Vec3 pos, int light, float pitch) {
        Bufferer.partial(CEEPartialModels.WIRE_DAMPER_ATTACHMENT, Blocks.ANDESITE.defaultBlockState())
                .light(light)
                .rotateZDegrees(-pitch + 180)
                .renderInto(pose, buffer.getBuffer(RenderTypes.solidMovingBlock()));
    
    }

    public static void renderChain(PoseStack pose, MultiBufferSource buffer, int light) {
        Bufferer.partial(CEEPartialModels.ATTACHMENT_CHAIN, Blocks.ANDESITE.defaultBlockState())
                .light(light)
                .renderInto(pose, buffer.getBuffer(net.minecraft.client.renderer.rendertype.RenderTypes.cutoutMovingBlock()));
    
    }
}
