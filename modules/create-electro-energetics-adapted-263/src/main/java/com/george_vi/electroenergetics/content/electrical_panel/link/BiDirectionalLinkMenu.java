package com.george_vi.electroenergetics.content.electrical_panel.link;

import com.george_vi.electroenergetics.CEEMenuTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import com.zurrtum.create.foundation.gui.menu.MenuType;
import net.minecraft.world.inventory.Slot;

public class BiDirectionalLinkMenu extends AbstractLinkMenu {

    public BiDirectionalLinkMenu(MenuType<ElectricalPanelLink> type, int id, Inventory inv, RegistryFriendlyByteBuf extraData) {
        super(type, id, inv, ElectricalPanelLink.createOnClient(extraData));
    }

    public BiDirectionalLinkMenu(MenuType<ElectricalPanelLink> type, int id, Inventory inv, ElectricalPanelLink contentHolder) {
        super(type, id, inv, contentHolder);
        this.contentHolder = contentHolder;
    }

    public static BiDirectionalLinkMenu create(int id, Inventory inv, ElectricalPanelLink contentHolder) {
        return new BiDirectionalLinkMenu(CEEMenuTypes.BIDIRECTIONAL_LINK_MENU.get(), id, inv, contentHolder);
    }

    @Override
    protected void addSlots() {
        addPlayerSlots(-26, 131);

        addSlot(new Slot(ghostInventory, 0, 24, 34));
        addSlot(new Slot(ghostInventory, 1, 24, 52));
        addSlot(new Slot(ghostInventory, 2, 62, 34));
        addSlot(new Slot(ghostInventory, 3, 62, 52));
    }

    @Override
    public int slots() {
        return 4;
    }
}
