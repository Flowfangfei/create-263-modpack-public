package com.george_vi.electroenergetics.content.wire.attachments;

import com.george_vi.electroenergetics.CEETags;
import com.george_vi.electroenergetics.content.wire.WireAttachment;
import com.george_vi.electroenergetics.content.wire.WireAttachmentType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class DamperAttachmentType extends WireAttachmentType {
    @Override
    public float getWidth(WireAttachment attachment) {
        return 1f;
    }

    @Override
    public float getHeight(WireAttachment attachment) {
        return 0.5f;
    }

    @Override
    public List<ItemStack> getDrops(WireAttachment attachment, Level level) {
        Item item = CEETags.itemFromTag(CEETags.WIRE_DAMPER_ITEM);
        return List.of(item.getDefaultInstance());
    }

    @Override
    public List<ItemStack> getItemRequirements(WireAttachment attachment) {
        Item item = CEETags.itemFromTag(CEETags.WIRE_DAMPER_ITEM);
        return List.of(item.getDefaultInstance());
    }
}
