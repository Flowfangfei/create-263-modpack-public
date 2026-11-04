package dev.khloeleclair.create.additionallogistics.client.content.logistics.cashRegister;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import dev.khloeleclair.create.additionallogistics.client.registries.CALGuiTextures;
import dev.khloeleclair.create.additionallogistics.common.content.logistics.cashRegister.CashRegisterBlock;
import dev.khloeleclair.create.additionallogistics.common.content.logistics.cashRegister.CashRegisterMenu;
import net.createmod.catnip.api.client.gui.ILightingSettings;
import net.createmod.catnip.api.client.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Collections;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class CashRegisterScreen extends AbstractSimiContainerScreen<CashRegisterMenu> {

    private final CALGuiTextures background;
    private final BlockState icon;

    private List<Rect2i> extraAreas = Collections.emptyList();

    public CashRegisterScreen(CashRegisterMenu container, Inventory inv, Component title) {
        super(container, inv, title);

        background = CALGuiTextures.CASH_REGISTER_BG;
        icon = container.contentHolder.getBlockState().setValue(CashRegisterBlock.FACING, Direction.WEST).setValue(CashRegisterBlock.OPEN, true);
    }

    @Override
    protected void init() {
        setWindowSize(background.getWidth(), background.getHeight() + AllGuiTextures.PLAYER_INVENTORY.getHeight());
        super.init();

        int x = leftPos;
        int y = topPos;

        containerTick();

        extraAreas = ImmutableList.of(new Rect2i(x + background.getWidth(), y + background.getHeight() - 50, 70, 60));

    }

    @Override
    protected void renderBg(GuiGraphicsExtractor guiGraphics, float v, int i, int i1) {

        int x = leftPos;
        int y = topPos;

        background.render(guiGraphics, x, y);

        var title = getTitle();

        guiGraphics.text(font, title, Math.round(x + background.getWidth() / 2f - font.width(title) / 2f), y + 6, 0xFF442000, false);

        GuiGameElement.of(icon, menu.contentHolder).<GuiGameElement.GuiRenderBuilder>at(x + background.getWidth() + 16, y + background.getHeight() - 16, 100)
                .scale(32)
                .rotate(30, 45, 0)
                .lighting(ILightingSettings.ITEMS_3D)
                .submit(guiGraphics);

        int invX = leftPos + 30;
        int invY = topPos + 8 + imageHeight - AllGuiTextures.PLAYER_INVENTORY.getHeight();
        renderPlayerInventory(guiGraphics, invX, invY);

    }

    @Override
    public List<Rect2i> getExtraAreas() {
        return extraAreas;
    }
}
