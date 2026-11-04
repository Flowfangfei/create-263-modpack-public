package com.railwayteam.railways.content.conductor.toolbox;

import com.railwayteam.railways.content.conductor.ConductorEntity;
import com.railwayteam.railways.registry.CRContainerTypes;
import com.simibubi.create.content.equipment.toolbox.ToolboxBlockEntity;
import com.simibubi.create.content.equipment.toolbox.ToolboxMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

public class MountedToolboxContainer extends ToolboxMenu {
    public MountedToolboxContainer(int id,Inventory inventory,MountedToolbox toolbox) { super(CRContainerTypes.MOUNTED_TOOLBOX,id,inventory,toolbox); }
    public MountedToolboxContainer(int id,Inventory inventory,RegistryFriendlyByteBuf data) { super(CRContainerTypes.MOUNTED_TOOLBOX,id,inventory,data); }
    @Override protected ToolboxBlockEntity createOnClient(RegistryFriendlyByteBuf data) {
        int conductorId=data.readVarInt();var tag=data.readNbt();var level=Minecraft.getInstance().level;
        if (level==null || !(level.getEntity(conductorId) instanceof ConductorEntity conductor))
            throw new IllegalStateException("Missing conductor for mounted toolbox " + conductorId);
        var toolbox=conductor.getOrCreateToolboxHolder();toolbox.read(tag,true);return toolbox;
    }
}
