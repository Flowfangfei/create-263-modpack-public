package com.yision.fluidlogistics.api.packager.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@FunctionalInterface
@OnlyIn(Dist.CLIENT)
public interface StockKeeperAmountRenderer {
	void render(GuiGraphicsExtractor graphics, int amount);
}
