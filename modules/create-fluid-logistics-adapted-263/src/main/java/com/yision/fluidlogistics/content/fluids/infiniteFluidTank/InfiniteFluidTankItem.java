package com.yision.fluidlogistics.content.fluids.infiniteFluidTank;

import java.util.List;

import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.api.client.lang.FontHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;

import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.fluids.FluidStack;

public class InfiniteFluidTankItem extends BlockItem {

	public InfiniteFluidTankItem(Block block, Properties properties) {
		super(block, properties);
	}

	public static FluidStack getContainedFluid(ItemStack stack, HolderLookup.Provider registries) {
		var blockEntityData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
		if (blockEntityData == null)
			return FluidStack.EMPTY;

		CompoundTag tag = blockEntityData.copyTagWithoutId();
		if (!tag.contains("TankContent"))
			return FluidStack.EMPTY;

		return readTankContent(tag.getCompoundOrEmpty("TankContent"), registries);
	}

	private static FluidStack readTankContent(CompoundTag tankContent, HolderLookup.Provider registries) {
		return com.yision.fluidlogistics.foundation.FluidLogisticsNbt.fluid(registries, tankContent);
	}

	public static boolean isInfiniteSupply(FluidStack fluid) {
		return InfiniteFluidSupplyRules.isInfiniteSupply(fluid);
	}
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> consumer, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack,context,display,consumer,tooltipFlag);
        java.util.List<Component> tooltipComponents=new java.util.ArrayList<>();
        appendRetainedTooltip(stack,context,tooltipComponents,tooltipFlag);
        tooltipComponents.forEach(consumer);
    }
    private void appendRetainedTooltip(ItemStack stack,TooltipContext context,List<Component> tooltipComponents,TooltipFlag tooltipFlag) {
		

		FluidStack fluid = getContainedFluid(stack, context.registries());
		if (fluid.isEmpty())
			return;

		tooltipComponents.add(fluid.getHoverName()
			.copy()
			.withStyle(ChatFormatting.GRAY));

		if (isInfiniteSupply(fluid)) {
			tooltipComponents.add(CreateLang.translateDirect("hint.hose_pulley.title")
				.withStyle(ChatFormatting.GOLD));
			return;
		}

		tooltipComponents.add(Component.empty()
			.append(Component.literal(InfiniteFluidSupplyRules.formatBuckets(fluid.getAmount()))
				.withStyle(ChatFormatting.GOLD))
			.append(Component.literal(" / ")
				.withStyle(ChatFormatting.GRAY))
			.append(Component.literal(InfiniteFluidSupplyRules.getRequiredAmountText())
				.withStyle(ChatFormatting.DARK_GRAY)));
	}


    public static class TooltipModifier extends ItemDescription.Modifier {
        private final Item tankItem;
        private final FontHelper.Palette colors;
        public TooltipModifier(Item item, FontHelper.Palette palette) {
            super(item,palette);tankItem=item;colors=palette;
        }
        @Override public void modify(ItemTooltipEvent context) {
            var tooltip=context.getToolTip();
            tooltip.add(Component.empty());
            if(!com.simibubi.create.AllKeys.shiftDown()) {
                tooltip.add(com.simibubi.create.foundation.item.TooltipHelper.holdShift(colors,false));
                return;
            }
            tooltip.addAll(com.simibubi.create.foundation.item.TooltipHelper.cutTextComponent(
                Component.translatable(tankItem.getDescriptionId()+".tooltip.summary",InfiniteFluidSupplyRules.getRequiredAmountText()),colors));
        }
    }
}
