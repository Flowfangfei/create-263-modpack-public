package net.fxnt.fxntstorage.util;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.JsonOps;
import com.google.gson.JsonParser;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

/** Registry-aware bridge for Storage-owned inventories; vanilla codecs remain unchanged. */
public final class StorageSerialization {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("fxntstorage/serialization");
    private static final MapCodec<CompoundTag> COMPOUND = MapCodec.assumeMapUnsafe(CompoundTag.CODEC);
    private StorageSerialization() {}
    public static CompoundTag readCompound(ValueInput input) { return input.read(COMPOUND).orElseGet(CompoundTag::new); }
    public static CompoundTag write(ValueIOSerializable storage, HolderLookup.Provider registries) {
        if (storage instanceof com.simibubi.create.compat.neoforge263.items.IItemHandler handler) {
            var contents = net.fxnt.fxntstorage.inventory.StorageContents.fromHandler(handler);
            if (storage instanceof net.fxnt.fxntstorage.inventory.StorageInventoryHandler privateHandler)
                privateHandler.validateContents(contents);
            return (CompoundTag) net.fxnt.fxntstorage.inventory.StorageContents.CODEC.encodeStart(
                    registries.createSerializationContext(NbtOps.INSTANCE), contents).getOrThrow();
        }
        try (var problems = new ProblemReporter.ScopedCollector(LOGGER)) {
            var output = TagValueOutput.createWithContext(problems, registries);
            storage.serialize(output);
            return output.buildResult();
        }
    }
    public static void read(ValueIOSerializable storage, HolderLookup.Provider registries, CompoundTag tag) {
        if (storage instanceof com.simibubi.create.compat.neoforge263.items.ItemStackHandler handler) {
            // Metadata-only block items omit the inventory tag; never erase live slots on absent data.
            if (tag.isEmpty()) return;
            // Decode the entire immutable document before changing a live slot.
            var contents=net.fxnt.fxntstorage.inventory.StorageContents.CODEC.parse(
                    registries.createSerializationContext(NbtOps.INSTANCE),tag).getOrThrow();
            if (handler instanceof net.fxnt.fxntstorage.inventory.StorageInventoryHandler privateHandler) {
                privateHandler.replaceContents(contents);
            } else {
                handler.setSize(contents.getSlots());
                for(int i=0;i<contents.getSlots();i++)handler.setStackInSlot(i,contents.getStackInSlot(i));
            }
            return;
        }
        try (var problems = new ProblemReporter.ScopedCollector(LOGGER)) {
            storage.deserialize(TagValueInput.create(problems, registries, tag));
        }
    }
    public static Tag writeStack(ItemStack stack, HolderLookup.Provider registries) {
        return ItemStack.OPTIONAL_CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE),stack).getOrThrow();
    }
    public static ItemStack readStack(HolderLookup.Provider registries, Tag tag) {
        return ItemStack.OPTIONAL_CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE),tag)
            .getOrThrow();
    }
    public static String writeComponentJson(Component component, HolderLookup.Provider registries) {
        return ComponentSerialization.CODEC.encodeStart(registries.createSerializationContext(JsonOps.INSTANCE),component).getOrThrow().toString();
    }
    public static Component readComponentJson(String json, HolderLookup.Provider registries) {
        try {
            return ComponentSerialization.CODEC.parse(registries.createSerializationContext(JsonOps.INSTANCE),JsonParser.parseString(json))
                .resultOrPartial(error -> LOGGER.error("Invalid stored name: {}",error)).orElse(null);
        } catch (RuntimeException invalidJson) { LOGGER.error("Invalid stored name JSON",invalidJson); return null; }
    }
}