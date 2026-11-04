package com.yision.fluidlogistics.content.logistics.copperFrogport;

import java.util.List;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.InputConstants;
import com.simibubi.create.content.logistics.packagePort.PackagePortConfigurationPacket;
import com.simibubi.create.content.logistics.packagePort.PackagePortMenu;
import com.simibubi.create.content.trains.station.NoShadowFontWrapper;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.yision.fluidlogistics.client.gui.FluidClientLang;
import com.yision.fluidlogistics.client.FluidLogisticsGuiTextures;

import net.createmod.catnip.api.client.gui.element.GuiGameElement;
import net.createmod.catnip.api.client.gui.widget.AbstractSimiWidget;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class CopperFrogportScreen extends AbstractSimiContainerScreen<PackagePortMenu> {

	private final FluidLogisticsGuiTextures background;

	private EditBox addressBox;
	private IconButton confirmButton;
	private IconButton dontAcceptPackages;
	private IconButton acceptPackages;

	private final ItemStack icon;

	private List<Rect2i> extraAreas = List.of();

	public CopperFrogportScreen(PackagePortMenu container, Inventory inv, Component title) {
		super(container, inv, title);
		background = FluidLogisticsGuiTextures.FROGPORT_BG;
		icon = new ItemStack(container.contentHolder.getBlockState()
			.getBlock()
			.asItem());
	}

	@Override
	protected void init() {
		setWindowSize(background.getWidth(), background.getHeight() + AllGuiTextures.PLAYER_INVENTORY.getHeight());
		super.init();
		clearWidgets();

		int x = getLeftPos();
		int y = getTopPos();

		Consumer<String> onTextChanged = s -> addressBox.setX(nameBoxX(s, addressBox));
		addressBox = new EditBox(new NoShadowFontWrapper(font), x + 23, y - 11, background.getWidth() - 20, 10,
			Component.empty());
		addressBox.setBordered(false);
		addressBox.setMaxLength(25);
		addressBox.setTextColor(0xFF3D3C48);
		addressBox.setValue(menu.contentHolder.addressFilter);
		addressBox.setFocused(false);
		addressBox.setFocused(true);
		addressBox.setResponder(onTextChanged);
		addressBox.setX(nameBoxX(addressBox.getValue(), addressBox));
		addRenderableWidget(addressBox);

		confirmButton = new IconButton(x + background.getWidth() - 33, y + background.getHeight() - 24,
			AllIcons.I_CONFIRM);
		confirmButton.withCallback(() -> minecraft.player.closeContainer());
		addRenderableWidget(confirmButton);

		acceptPackages = new IconButton(x + 37, y + background.getHeight() - 24, AllIcons.I_SEND_AND_RECEIVE);
		acceptPackages.withCallback(() -> {
			acceptPackages.green = true;
			dontAcceptPackages.green = false;
		});
		acceptPackages.green = menu.contentHolder.acceptsPackages;
		acceptPackages.setToolTip(FluidClientLang.translateDirect("gui.package_port.send_and_receive"));
		addRenderableWidget(acceptPackages);

		dontAcceptPackages = new IconButton(x + 55, y + background.getHeight() - 24, AllIcons.I_SEND_ONLY);
		dontAcceptPackages.withCallback(() -> {
			acceptPackages.green = false;
			dontAcceptPackages.green = true;
		});
		dontAcceptPackages.green = !menu.contentHolder.acceptsPackages;
		dontAcceptPackages.setToolTip(FluidClientLang.translateDirect("gui.package_port.send_only"));
		addRenderableWidget(dontAcceptPackages);

		containerTick();

		extraAreas = ImmutableList.of(new Rect2i(x + background.getWidth(), y + background.getHeight() - 50, 70, 60));
	}

	private int nameBoxX(String text, EditBox nameBox) {
		return getLeftPos() + background.getWidth() / 2 - (Math.min(font.width(text), nameBox.getWidth()) + 10) / 2;
	}

	@Override
	protected void containerTick() {
		acceptPackages.visible = menu.contentHolder.target != null;
		dontAcceptPackages.visible = menu.contentHolder.target != null;
		super.containerTick();
	}

	@Override
	protected void renderBg(GuiGraphicsExtractor graphics, float partialTick, int mouseX, int mouseY) {
		int x = getLeftPos();
		int y = getTopPos();

		FluidLogisticsGuiTextures.FROGPORT_HEADER.render(
			graphics,
			x,
			y - FluidLogisticsGuiTextures.FROGPORT_HEADER.getHeight());
		background.render(graphics, x, y);

		String text = addressBox.getValue();
		if (!addressBox.isFocused()) {
			if (text.isEmpty()) {
				text = icon.getHoverName().getString();
				graphics.text(font, text, nameBoxX(text, addressBox), y - 11, 0xFF3D3C48, false);
			}
			FluidLogisticsGuiTextures.FROGPORT_EDIT_NAME.render(
				graphics,
				nameBoxX(text, addressBox) + font.width(text) + 5,
				y - 14);
		}

		GuiGameElement.of(icon).<GuiGameElement.GuiRenderBuilder>at(
			x + background.getWidth() + 6,
			y + background.getHeight() - 56,
			-200)
			.scale(4)
			.submit(graphics);

		int invX = leftPos + 30;
		int invY = topPos + 8 + imageHeight - AllGuiTextures.PLAYER_INVENTORY.getHeight();
		renderPlayerInventory(graphics, invX, invY);

		if (menu.contentHolder.target == null) {
			return;
		}

		x += 13;
		y += 58;
		FluidLogisticsGuiTextures.FROGPORT_SLOT.render(graphics, x, y);
		graphics.item(menu.contentHolder.target.getIcon(), x + 1, y + 1);

		if (addressBox.isHovered()) {
			graphics.setComponentTooltipForNextFrame(
				font,
				List.of(
					FluidClientLang.translate("gui.package_port.catch_packages")
						.color(AbstractSimiWidget.HEADER_RGB)
						.component(),
					FluidClientLang.translate("gui.package_port.catch_packages_empty")
						.style(ChatFormatting.GRAY)
						.component(),
					FluidClientLang.translate("gui.package_port.catch_packages_wildcard")
						.style(ChatFormatting.GRAY)
						.component()),
				mouseX,
				mouseY);
		}
	}

	@Override
	public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        int keyCode=event.key(), scanCode=event.keycode(), modifiers=event.modifiers();
		boolean hitEnter = getFocused() instanceof EditBox
			&& (keyCode == InputConstants.KEY_RETURN || keyCode == InputConstants.KEY_NUMPADENTER);

		if (hitEnter && addressBox.isFocused()) {
			addressBox.setFocused(false);
			return true;
		}

		return super.keyPressed(event);
	}

	@Override
	public void removed() {
		net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(new PackagePortConfigurationPacket(
			menu.contentHolder.getBlockPos(),
			addressBox.getValue(),
			acceptPackages.green));
		super.removed();
	}

	@Override
	public List<Rect2i> getExtraAreas() {
		return extraAreas;
	}
}
