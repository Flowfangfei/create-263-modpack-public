package com.yision.fluidlogistics.content.schematics.cannon;

import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity;
import com.simibubi.create.content.schematics.cannon.SchematicannonMenu;
import com.simibubi.create.content.schematics.cannon.SchematicannonScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.yision.fluidlogistics.client.gui.FluidClientLang;
import com.yision.fluidlogistics.client.FluidLogisticsGuiTextures;
import com.yision.fluidlogistics.content.logistics.fluidPackage.FluidPackageResourceType;
import com.yision.fluidlogistics.content.schematics.client.FluidSchematicColors;
import com.yision.fluidlogistics.foundation.render.GuiFluidBlockRenderer;
import com.yision.fluidlogistics.registry.AllBlocks;

import net.createmod.catnip.api.client.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.fluids.FluidStack;

public class CopperSchematicannonScreen extends SchematicannonScreen {

	private static final FluidLogisticsGuiTextures BG_BOTTOM = FluidLogisticsGuiTextures.COPPER_SCHEMATICANNON_BOTTOM;
	private static final FluidLogisticsGuiTextures BG_TOP = FluidLogisticsGuiTextures.COPPER_SCHEMATICANNON_TOP;

	private final ItemStack renderedItem = AllBlocks.COPPER_SCHEMATICANNON.asStack();

	public CopperSchematicannonScreen(SchematicannonMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
	}

	@Override
	protected void renderBg(GuiGraphicsExtractor graphics, float partialTicks, int mouseX, int mouseY) {
		int invX = getLeftOfCentered(AllGuiTextures.PLAYER_INVENTORY.getWidth());
		int invY = topPos + BG_TOP.getHeight() + BG_BOTTOM.getHeight() + 2;
		renderPlayerInventory(graphics, invX, invY);

		int x = leftPos;
		int y = topPos;

		BG_TOP.render(graphics, x, y);
		BG_BOTTOM.render(graphics, x, y + BG_TOP.getHeight());
		FluidLogisticsGuiTextures.COPPER_SCHEMATICANNON_TITLE.render(graphics, x, y - 2);

		SchematicannonBlockEntity be = menu.contentHolder;
		renderPrintingProgress(graphics, x, y, be.schematicProgress);
		float amount = be.remainingFuel / (float) be.getShotsPerGunpowder();
		renderFuelBar(graphics, x, y, amount);
		renderChecklistPrinterProgress(graphics, x, y, be.bookPrintingProgress);

		if (!be.inventory.getStackInSlot(0).isEmpty()) {
			renderBlueprintHighlight(graphics, x, y);
		}

		GuiGameElement.of(renderedItem).<GuiGameElement.GuiRenderBuilder>at(
			x + BG_TOP.getWidth(), y + BG_TOP.getHeight() + BG_BOTTOM.getHeight() - 48, -200)
			.scale(5)
			.submit(graphics);

		graphics.text(font, title, x + (BG_TOP.getWidth() - 8 - font.width(title)) / 2, y + 2, 0xFF505050,
			false);

		Component msg = FluidClientLang.translateDirect("schematicannon.status." + be.statusMsg);
		int stringWidth = font.width(msg);

		if (be.missingItem != null) {
			stringWidth += 16;
			FluidStack missingFluid = FluidPackageResourceType.getFluid(be.missingItem);
			if (missingFluid.isEmpty()) {
				GuiGameElement.of(be.missingItem).<GuiGameElement.GuiRenderBuilder>at(x + 128, y + 49, 100)
					.scale(1)
					.submit(graphics);
			} else {
				GuiFluidBlockRenderer.render(graphics, missingFluid, x + 128, y + 49, 1);
			}
		}

		graphics.text(font, msg, x + 103 - stringWidth / 2, y + 53, 0xFF000000 | FluidSchematicColors.TEXT_HIGHLIGHT);

		if ("schematicErrored".equals(be.statusMsg)) {
			graphics.text(font,
				FluidClientLang.translateDirect("schematicannon.status.schematicErroredCheckLogs"),
				x + 103 - stringWidth / 2, y + 65, 0xFF000000 | FluidSchematicColors.TEXT_HIGHLIGHT);
		}
	}

	@Override
	protected void renderBlueprintHighlight(GuiGraphicsExtractor graphics, int x, int y) {
		FluidLogisticsGuiTextures.COPPER_SCHEMATICANNON_HIGHLIGHT.render(graphics, x + 10, y + 60);
	}

	@Override
	protected void renderPrintingProgress(GuiGraphicsExtractor graphics, int x, int y, float progress) {
		progress = Math.min(progress, 1);
		FluidLogisticsGuiTextures sprite = FluidLogisticsGuiTextures.COPPER_SCHEMATICANNON_PROGRESS;
		graphics.blit(sprite.location, x + 44, y + 64, sprite.getStartX(), sprite.getStartY(),
			(int) (sprite.getWidth() * progress), sprite.getHeight(), 256, 256);
	}

	@Override
	protected void renderChecklistPrinterProgress(GuiGraphicsExtractor graphics, int x, int y, float progress) {
		FluidLogisticsGuiTextures sprite = FluidLogisticsGuiTextures.COPPER_SCHEMATICANNON_CHECKLIST_PROGRESS;
		graphics.blit(sprite.location, x + 154, y + 20, sprite.getStartX(), sprite.getStartY(),
			(int) (sprite.getWidth() * progress), sprite.getHeight(), 256, 256);
	}

	@Override
	protected void renderFuelBar(GuiGraphicsExtractor graphics, int x, int y, float amount) {
		FluidLogisticsGuiTextures sprite = FluidLogisticsGuiTextures.COPPER_SCHEMATICANNON_FUEL;
		if (menu.contentHolder.hasCreativeCrate) {
			FluidLogisticsGuiTextures.COPPER_SCHEMATICANNON_FUEL_CREATIVE.render(graphics, x + 36, y + 19);
			return;
		}
		graphics.blit(sprite.location, x + 36, y + 19, sprite.getStartX(), sprite.getStartY(),
			(int) (sprite.getWidth() * amount), sprite.getHeight(), 256, 256);
	}
}
