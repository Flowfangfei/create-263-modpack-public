package com.yision.fluidlogistics.mixin.client;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelScreen;
import com.simibubi.create.foundation.gui.widget.ScrollInput;
import com.yision.fluidlogistics.client.gui.FluidClientLang;
import com.yision.fluidlogistics.api.packager.PackageResourceDisplay;
import com.yision.fluidlogistics.api.packager.PackageResources;

import net.createmod.catnip.api.client.gui.AbstractSimiScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@Mixin(FactoryPanelScreen.class)
public abstract class FactoryPanelScreenMixin extends AbstractSimiScreen {

	@Shadow(remap = false)
	private FactoryPanelBehaviour behaviour;

	@Shadow(remap = false)
	private boolean restocker;
	@Shadow(remap = false) private BigItemStack outputConfig;
	@Shadow private int left;
	@Shadow private int top;

	protected FactoryPanelScreenMixin(Component title) {
		super(title);
	}

	@WrapOperation(method = { "renderInput",
		"renderWindow" }, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;item("
			+ "Lnet/minecraft/world/item/ItemStack;II)V", remap = true), remap = false)
	private void fluidlogistics$renderResourceIcon(
		GuiGraphicsExtractor graphics, ItemStack stack, int x, int y, Operation<Void> original) {
		original.call(graphics, PackageResources.iconOf(stack)
			.orElse(stack), x, y);
	}

	@WrapOperation(method = "renderInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;itemDecorations("
		+ "Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V", remap = true), remap = false)
	private void fluidlogistics$renderResourceAmount(
		GuiGraphicsExtractor graphics, net.minecraft.client.gui.Font font, ItemStack stack, int x, int y, String text,
		Operation<Void> original, @Local(argsOnly = true) BigItemStack itemStack) {
		String amountText = PackageResources.formatAmount(itemStack.stack, itemStack.count,
			PackageResourceDisplay.Format.COMPACT)
			.orElse(text);
		original.call(graphics, font, stack, x, y, amountText);
	}

	@Inject(method = "renderInput", at = @At("TAIL"))
    private void fluidlogistics$renderResourceTooltip(GuiGraphicsExtractor graphics, int slot,
        BigItemStack itemStack, CallbackInfo ci) {
        var accessor = (GuiGraphicsExtractorAccessor) (Object) graphics;
        int mouseX = accessor.fluidlogistics$mouseX(), mouseY = accessor.fluidlogistics$mouseY();
        int x = left + (restocker ? 88 : 68 + slot % 3 * 20);
        int y = top + (restocker ? 12 : 28) + slot / 3 * 20;
        if (mouseX < x || mouseX >= x + 16 || mouseY < y || mouseY >= y + 16) return;
		Component resourceName = PackageResources.nameOf(itemStack.stack)
			.orElse(null);
		if (resourceName == null) {
			
			return;
		}

		String amountText = PackageResources.formatAmount(itemStack.stack, itemStack.count,
			PackageResourceDisplay.Format.PRECISE)
			.orElse(Integer.toString(itemStack.count));
		List<Component> resourceTooltips = new ArrayList<>();
		String header = restocker ? resourceName.getString() : resourceName.getString() + " x" + amountText;
		resourceTooltips.add(FluidClientLang.translate("gui.factory_panel.sending_item", header)
			.component().withStyle(style -> style.withColor(ScrollInput.HEADER_RGB.getRGB())));
		if (restocker) {
			resourceTooltips.add(FluidClientLang.translate("gui.factory_panel.sending_item_tip")
				.style(ChatFormatting.GRAY)
				.component());
			resourceTooltips.add(FluidClientLang.translate("gui.factory_panel.sending_item_tip_1")
				.style(ChatFormatting.GRAY)
				.component());
		} else {
			resourceTooltips.add(FluidClientLang.translate("gui.factory_panel.scroll_to_change_amount")
				.style(ChatFormatting.DARK_GRAY)
				.style(ChatFormatting.ITALIC)
				.component());
			resourceTooltips.add(FluidClientLang.translate("gui.factory_panel.left_click_disconnect")
				.style(ChatFormatting.DARK_GRAY)
				.style(ChatFormatting.ITALIC)
				.component());
		}
		graphics.setComponentTooltipForNextFrame(font, resourceTooltips, mouseX, mouseY);
	}

    @WrapOperation(method = "mouseScrolled", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(III)I", ordinal = 0, remap = true), remap = false)
    private int fluidlogistics$adjustInputResourceAmount(int value, int min, int max, Operation<Integer> original,
        @Local(name = "input") BigItemStack itemStack) {
        return fluidlogistics$adjustResourceAmount(value, min, max, original, itemStack);
    }

    @WrapOperation(method = "mouseScrolled", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(III)I", ordinal = 1, remap = true), remap = false)
    private int fluidlogistics$adjustOutputResourceAmount(int value, int min, int max, Operation<Integer> original) {
        return fluidlogistics$adjustResourceAmount(value, min, max, original, outputConfig);
    }

    @Unique
    private int fluidlogistics$adjustResourceAmount(int value, int min, int max, Operation<Integer> original,
        BigItemStack itemStack) {
		PackageResourceDisplay display = PackageResources.displayOf(itemStack.stack)
			.orElse(null);
		if (display == null)
			return original.call(value, min, max);

		int maximum = display.factoryPanelRestockPolicy(itemStack.stack)
			.maxRequestPerBatch();
		int adjusted = PackageResources.adjustAmount(itemStack.stack,
			new PackageResourceDisplay.Adjustment(
				itemStack.count,
				value > itemStack.count,
				com.yision.fluidlogistics.client.gui.GuiKeys.shift(),
				com.yision.fluidlogistics.client.gui.GuiKeys.control(),
				1,
				maximum,
				1,
				PackageResourceDisplay.Interaction.FACTORY_PANEL))
			.orElse(Integer.MIN_VALUE);
		return adjusted == Integer.MIN_VALUE ? original.call(value, min, max) : adjusted;
	}
}
