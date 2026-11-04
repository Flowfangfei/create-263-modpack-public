package com.yision.fluidlogistics.api.packager.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@FunctionalInterface
@OnlyIn(Dist.CLIENT)
public interface FactoryPanelPreviewRenderer {
	void render(GuiGraphicsExtractor graphics, ItemStack resourceKey, int x, int y);
}
