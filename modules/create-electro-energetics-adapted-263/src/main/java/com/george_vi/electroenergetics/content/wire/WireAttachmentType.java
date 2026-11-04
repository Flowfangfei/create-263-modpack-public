package com.george_vi.electroenergetics.content.wire;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public abstract class WireAttachmentType {
    public abstract float getWidth(WireAttachment attachment);

    public abstract float getHeight(WireAttachment attachment);

    public abstract List<ItemStack> getDrops(WireAttachment attachment, Level level);

    public List<ItemStack> getItemRequirements(WireAttachment attachment) {
        return List.of(ItemStack.EMPTY);
    }

    public static class Empty extends WireAttachmentType {
        @Override
        public float getWidth(WireAttachment attachment) {
            return 0;
        }

        @Override
        public float getHeight(WireAttachment attachment) {
            return 0;
        }

        @Override
        public List<ItemStack> getDrops(WireAttachment attachment, Level level) {
            return List.of();
        }
    }
}
