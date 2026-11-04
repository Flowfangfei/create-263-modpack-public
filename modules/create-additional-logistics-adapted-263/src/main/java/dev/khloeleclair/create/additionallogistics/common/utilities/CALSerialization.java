package dev.khloeleclair.create.additionallogistics.common.utilities;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

/** Encode full item components through the current registry-aware codec. */
public final class CALSerialization {
    private CALSerialization() {}
    public static Tag writeStack(ItemStack stack, HolderLookup.Provider registries) {
        return ItemStack.OPTIONAL_CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), stack).getOrThrow();
    }
    public static ItemStack readStack(Tag tag, HolderLookup.Provider registries) {
        return ItemStack.OPTIONAL_CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag).result().orElse(ItemStack.EMPTY);
    }
}