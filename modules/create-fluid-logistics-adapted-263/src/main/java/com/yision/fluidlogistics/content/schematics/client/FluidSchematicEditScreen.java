package com.yision.fluidlogistics.content.schematics.client;

import com.simibubi.create.CreateClient;
import com.simibubi.create.content.schematics.client.SchematicEditScreen;
import com.yision.fluidlogistics.client.FluidLogisticsGuiTextures;
import com.yision.fluidlogistics.registry.AllItems;

import net.createmod.catnip.api.client.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FluidSchematicEditScreen extends SchematicEditScreen {

	private static final FluidLogisticsGuiTextures BACKGROUND = FluidLogisticsGuiTextures.FLUID_SCHEMATIC;
	private final ItemStack renderedItem = AllItems.FLUID_SCHEMATIC.asStack();

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		int x = (width - BACKGROUND.getWidth()) / 2;
		int y = (height - BACKGROUND.getHeight()) / 2;

		BACKGROUND.render(graphics, x, y);

		String title = CreateClient.SCHEMATIC_HANDLER.getCurrentSchematicName();
		graphics.text(font, title, x + (BACKGROUND.getWidth() - 8 - font.width(title)) / 2, y + 4, 0xFF505050,
			false);

		GuiGameElement.of(renderedItem).<GuiGameElement.GuiRenderBuilder>at(x + BACKGROUND.getWidth() + 6,
			y + BACKGROUND.getHeight() - 40, -200)
			.scale(3)
			.submit(graphics);
        super.extractRenderState(graphics,mouseX,mouseY,partialTicks);
	}
}
