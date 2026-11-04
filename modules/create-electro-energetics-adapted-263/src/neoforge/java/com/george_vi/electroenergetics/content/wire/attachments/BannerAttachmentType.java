package com.george_vi.electroenergetics.content.wire.attachments;

import com.george_vi.electroenergetics.content.wire.WireAttachment;
import com.george_vi.electroenergetics.content.wire.WireAttachmentType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import dev.engine_room.flywheel.lib.transform.PoseTransformStack;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

public class BannerAttachmentType extends WireAttachmentType {
    @Override
    public float getWidth(WireAttachment attachment) {
        return 1f;
    }

    @Override
    public float getHeight(WireAttachment attachment) {
        return 2f;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(PoseStack pose, MultiBufferSource buffer, WireAttachment attachment, Vec3 pos, int light, float pitch) {
        com.george_vi.electroenergetics.client.CEEWireAttachmentRenderers.renderBanner(pose, buffer, attachment, pos, light, pitch);
    }

    @Override
    public List<ItemStack> getDrops(WireAttachment attachment, Level level) {
        BannerPatternLayers pattern = null;
        if (attachment.data.contains("Pattern"))
            pattern = BannerPatternLayers.CODEC
                    .parse(level.registryAccess().createSerializationContext(NbtOps.INSTANCE), attachment.data.get("Pattern"))
                    .resultOrPartial(s -> LogUtils.getLogger().error("Failed to parse banner patterns: '{}'", s))
                    .orElse(null);
        if (pattern == null)
            pattern = BannerPatternLayers.EMPTY;

        DyeColor color = DyeColor.byName(attachment.data.getStringOr("BaseColor","white"), DyeColor.WHITE);
        ItemStack stack = Blocks.BANNER.pick(color).asItem().getDefaultInstance();
        stack.set(DataComponents.BANNER_PATTERNS, pattern);
        return List.of(stack);
    }

    @Override
    public List<ItemStack> getItemRequirements(WireAttachment attachment) {
        BannerPatternLayers pattern = null;
        if (attachment.data.contains("Pattern"))
            pattern = BannerPatternLayers.CODEC
                    .parse(NbtOps.INSTANCE, attachment.data.get("Pattern"))
                    .resultOrPartial(s -> LogUtils.getLogger().error("Failed to parse banner patterns: '{}'", s))
                    .orElse(null);
        if (pattern == null)
            pattern = BannerPatternLayers.EMPTY;

        DyeColor color = DyeColor.byName(attachment.data.getStringOr("BaseColor","white"), DyeColor.WHITE);
        ItemStack stack = Blocks.BANNER.pick(color).asItem().getDefaultInstance();
        stack.set(DataComponents.BANNER_PATTERNS, pattern);
        return List.of(stack);
    }
}
