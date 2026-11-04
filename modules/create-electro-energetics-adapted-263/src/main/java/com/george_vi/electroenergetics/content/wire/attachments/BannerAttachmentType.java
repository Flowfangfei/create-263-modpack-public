package com.george_vi.electroenergetics.content.wire.attachments;

import com.george_vi.electroenergetics.content.wire.WireAttachment;
import com.george_vi.electroenergetics.content.wire.WireAttachmentType;
import com.mojang.logging.LogUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

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

        DyeColor color = DyeColor.byName(attachment.data.getStringOr("BaseColor", ""), DyeColor.WHITE);
        ItemStack stack = net.minecraft.world.level.block.Blocks.BANNER.pick(color).asItem().getDefaultInstance();
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

        DyeColor color = DyeColor.byName(attachment.data.getStringOr("BaseColor", ""), DyeColor.WHITE);
        ItemStack stack = net.minecraft.world.level.block.Blocks.BANNER.pick(color).asItem().getDefaultInstance();
        stack.set(DataComponents.BANNER_PATTERNS, pattern);
        return List.of(stack);
    }
}
