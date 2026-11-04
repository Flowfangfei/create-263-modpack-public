package com.george_vi.electroenergetics.content.wire.attachments;

import com.george_vi.electroenergetics.content.wire.WireAttachment;
import com.george_vi.electroenergetics.content.wire.WireAttachmentType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.fabricmc.loader.api.FabricLoader;

import java.util.Collections;
import java.util.List;

public class BuntingAttachmentType extends WireAttachmentType {
    @Override
    public float getWidth(WireAttachment attachment) {
        return 0.5f;
    }

    @Override
    public float getHeight(WireAttachment attachment) {
        return 0.5f;
    }

    @Override
    public List<ItemStack> getDrops(WireAttachment attachment, Level level) {
        if (!FabricLoader.getInstance().isModLoaded("supplementaries"))
            return Collections.emptyList();
        DyeColor color = DyeColor.byName(attachment.data.getStringOr("BaseColor", ""), DyeColor.WHITE);
        ItemStack stack = buntingStack(attachment, color);
        return stack.isEmpty() ? List.of() : List.of(stack);
    }

    @Override
    public List<ItemStack> getItemRequirements(WireAttachment attachment) {
        if (!FabricLoader.getInstance().isModLoaded("supplementaries"))
            return Collections.emptyList();
        DyeColor color = DyeColor.byName(attachment.data.getStringOr("BaseColor", ""), DyeColor.WHITE);
        ItemStack stack = buntingStack(attachment, color);
        return stack.isEmpty() ? List.of() : List.of(stack);
    }
    private static ItemStack buntingStack(WireAttachment attachment, DyeColor color) {
        String stored = attachment.data.getStringOr("ItemId", "");
        if (!stored.isEmpty()) {
            net.minecraft.resources.Identifier id = net.minecraft.resources.Identifier.tryParse(stored);
            if (id != null) {
                net.minecraft.world.item.Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(id);
                if (item != null) return item.getDefaultInstance();
            }
        }
        for (String path : List.of("bunting_" + color.getSerializedName(), color.getSerializedName() + "_bunting")) {
            net.minecraft.world.item.Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(
                    net.minecraft.resources.Identifier.fromNamespaceAndPath("supplementaries", path));
            if (item != null && item != net.minecraft.world.item.Items.AIR) return item.getDefaultInstance();
        }
        return ItemStack.EMPTY;
    }
}
