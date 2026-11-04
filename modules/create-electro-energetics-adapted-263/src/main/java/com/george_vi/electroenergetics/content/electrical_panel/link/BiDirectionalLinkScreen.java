package com.george_vi.electroenergetics.content.electrical_panel.link;

import com.george_vi.electroenergetics.CEEGuiTextures;
import com.george_vi.electroenergetics.content.electrical_panel.special_interaction.SetMenuPanelAttachmentOptionsPacket;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.gui.widget.IconButton;
import com.george_vi.electroenergetics.events.network.CEENetwork;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import static com.zurrtum.create.client.foundation.gui.AllGuiTextures.PLAYER_INVENTORY;

public class BiDirectionalLinkScreen extends AbstractLinkScreen<BiDirectionalLinkMenu> {
    private final CEEGuiTextures background = CEEGuiTextures.BIDIRECTIONAL_LINK;
    private IconButton shouldReturn;

    public BiDirectionalLinkScreen(BiDirectionalLinkMenu container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @Override
    protected void init() {
        assert minecraft != null;
        assert minecraft.player != null;

        setWindowOffset(1, 0);
        super.init();

        int x = leftPos;
        int y = topPos;

        IconButton confirmButton = new IconButton(x + background.getWidth() - 33, y + background.getHeight() - 24, AllIcons.I_CONFIRM);
        confirmButton.withCallback(() -> minecraft.player.closeContainer());

        addRenderableWidget(confirmButton);

        shouldReturn = new IconButton(x + 7, y + background.getHeight() - 24, CEEGuiTextures.LINK_RETURN_TO_ORIGINAL);
        shouldReturn.withCallback(() -> {
            shouldReturn.green ^= true;
            CEENetwork.NETWORK.sendToServer(
                    new SetMenuPanelAttachmentOptionsPacket(SetMenuPanelAttachmentOptionsPacket.RETURN_TO_ORIGINAL, shouldReturn.green));

        });
        shouldReturn.green = menu.contentHolder.getReturnToZero();
        shouldReturn.setToolTip(CEELang.translateDirect("electrical_panel_linking.return_to_zero"));
        if (menu.contentHolder.hasReturnToZeroOption())
            addRenderableWidget(shouldReturn);
    }

    @Override
    public void extractBackground(@NotNull GuiGraphicsExtractor graphics,int mouseX,int mouseY,float partialTick) {
        int invX = getLeftOfCentered(PLAYER_INVENTORY.getWidth());
        int invY = topPos + background.getHeight() + 4;
        renderPlayerInventory(graphics, invX, invY);

        int x = leftPos;
        int y = topPos;

        background.render(graphics, x, y);
        graphics.text(font, title, x + 15, y + 4, 0xFF592424, false);
    }

    @Override
    public int slots() {
        return 4;
    }
}
