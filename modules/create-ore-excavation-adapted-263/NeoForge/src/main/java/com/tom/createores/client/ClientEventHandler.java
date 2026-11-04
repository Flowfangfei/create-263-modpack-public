package com.tom.createores.client;

import static net.minecraft.ChatFormatting.GRAY;

import java.util.List;

import net.createmod.catnip.api.lang.Lang;
import net.createmod.catnip.api.lang.LangBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import com.simibubi.create.content.equipment.goggles.GogglesItem;
import com.simibubi.create.content.kinetics.base.IRotate.StressImpact;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.CreateLang;

import com.tom.createores.CreateOreExcavation;
import com.tom.createores.Registration;

@EventBusSubscriber(value = Dist.CLIENT, modid = CreateOreExcavation.MODID)
public class ClientEventHandler {

	@SubscribeEvent
	public static void addToItemTooltip(ItemTooltipEvent event) {
		if (event.getEntity() == null)
			return;

		ItemStack stack = event.getItemStack();
		if(stack.getItem() == Registration.DRILL_BLOCK.get().asItem() || stack.getItem() == Registration.EXTRACTOR_BLOCK.get().asItem()) {
			appendVariableStress(event.getToolTip());
		}
	}

    public static void appendVariableStress(List<Component> tooltip) {
        if (!StressImpact.isEnabled()) return;
        tooltip.add(CreateLang.translate("tooltip.stressImpact").style(GRAY).component());
        LangBuilder builder = CreateLang.builder().add(CreateLang.text(TooltipHelper.makeProgressBar(3, StressImpact.HIGH.ordinal() + 1)).style(StressImpact.HIGH.getAbsoluteColor()));
        if (GogglesItem.isWearingGoggles(Minecraft.getInstance().player))
            builder.add(CreateLang.text("1-?")).text("x ").add(CreateLang.translate("generic.unit.rpm")).add(Component.literal(" ").append(Component.translatable("tooltip.coe.variableImpact")));
        else builder.translate("tooltip.stressImpact." + Lang.asId(StressImpact.HIGH.name()));
        tooltip.add(builder.component());
    }
}
