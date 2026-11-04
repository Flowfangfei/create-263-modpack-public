package com.enxv.aerouniversaljoint.content;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

public class HydraulicConnectionHeadBlockItem extends BlockItem {
    private final String hintKey;
    private final String rangeKey;

    public HydraulicConnectionHeadBlockItem(Block block, Properties properties) {
        this(block, properties,
                "tooltip.aeronautics_utility_objects.hydraulic_head_hint",
                "tooltip.aeronautics_utility_objects.hydraulic_head_range");
    }

    public HydraulicConnectionHeadBlockItem(Block block, Properties properties, String hintKey, String rangeKey) {
        super(block, properties);
        this.hintKey = hintKey;
        this.rangeKey = rangeKey;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipComponents, tooltipFlag);

        tooltipComponents.accept(Component.translatable(this.hintKey)
                .withStyle(ChatFormatting.GRAY));
        tooltipComponents.accept(Component.translatable(this.rangeKey)
                .withStyle(ChatFormatting.GRAY));
    }
}
