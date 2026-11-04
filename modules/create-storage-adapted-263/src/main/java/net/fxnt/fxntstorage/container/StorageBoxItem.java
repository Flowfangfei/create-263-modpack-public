package net.fxnt.fxntstorage.container;

import net.fxnt.fxntstorage.backpack.client.tooltip.BackpackTooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Optional;

public class StorageBoxItem extends BlockItem {

    public StorageBoxItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> output, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, output, tooltipFlag);
        List<Component> tooltipComponents = new java.util.ArrayList<>();
        tooltipComponents.add(Component.translatable("tooltip.fxntstorage.holdForContents", (net.minecraft.client.Minecraft.getInstance().hasControlDown()) ? "§fCtrl" : "§7Ctrl").withStyle(ChatFormatting.DARK_GRAY));

    
        tooltipComponents.forEach(output);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        if (net.minecraft.client.Minecraft.getInstance().hasControlDown() && !net.minecraft.client.Minecraft.getInstance().hasShiftDown()) {
            return Optional.of(new BackpackTooltip(stack));
        }
        return Optional.empty();
    }
}
