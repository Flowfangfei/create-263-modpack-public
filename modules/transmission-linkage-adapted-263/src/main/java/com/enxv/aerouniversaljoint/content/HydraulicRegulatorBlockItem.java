package com.enxv.aerouniversaljoint.content;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

public class HydraulicRegulatorBlockItem extends BlockItem {
    public HydraulicRegulatorBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipComponents, tooltipFlag);

        tooltipComponents.accept(Component.translatable("tooltip.aeronautics_utility_objects.hydraulic_regulator_hint")
                .withStyle(ChatFormatting.GRAY));
        tooltipComponents.accept(Component.translatable("tooltip.aeronautics_utility_objects.hydraulic_regulator_signal")
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltipComponents.accept(Component.translatable("tooltip.aeronautics_utility_objects.hydraulic_regulator_speed")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
