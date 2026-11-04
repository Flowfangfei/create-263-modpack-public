package com.george_vi.electroenergetics.content.wire.attachments;

import com.george_vi.electroenergetics.CEEWireAttachments;
import com.george_vi.electroenergetics.content.wire.WireAttachment;
import com.george_vi.electroenergetics.content.wire.interaction.WireInteractionBehaviour;
import com.george_vi.electroenergetics.foundation.nodes.NodeConnectionPoint;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.fabricmc.loader.api.FabricLoader;

public class BuntingWireInteractionBehaviour extends WireInteractionBehaviour {
    @Override
    public void interactWire(NodeConnectionPoint point, Level level, Player player, ItemStack stack) {
        if (!FabricLoader.getInstance().isModLoaded("supplementaries"))
            return;

        WireAttachment attachment = new WireAttachment(CEEWireAttachments.BUNTING.get());
        net.minecraft.resources.Identifier id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
        net.minecraft.world.item.DyeColor color = net.minecraft.world.item.DyeColor.WHITE;
        for (net.minecraft.world.item.DyeColor candidate : net.minecraft.world.item.DyeColor.values()) {
            if (id.getPath().contains(candidate.getSerializedName())) color = candidate;
        }
        attachment.data.putString("BaseColor", color.getSerializedName());
        attachment.data.putString("ItemId", id.toString());

        attachToWire(point, level, player, stack, attachment);
    }

    @Override
    public boolean isActiveFor(ItemStack stack, Player player) {
        if (!FabricLoader.getInstance().isModLoaded("supplementaries"))
            return false;
        net.minecraft.resources.Identifier id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id.getNamespace().equals("supplementaries") &&
                (id.getPath().startsWith("bunting") || id.getPath().endsWith("_bunting"));
    }

    @Override
    public int getWireDisplayColor(NodeConnectionPoint point, Level level, Player player, ItemStack stack) {
        return attachmentColor();
    }
}
