package com.yision.fluidlogistics.foundation;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.fluids.FluidStack;

/** Registry-aware codecs preserve arbitrary item/fluid data components. */
public final class FluidLogisticsNbt {
 private static final org.slf4j.Logger LOG=org.slf4j.LoggerFactory.getLogger("fluidlogistics/nbt");
 private FluidLogisticsNbt() {}
 public static Tag save(ItemStack stack,HolderLookup.Provider registries){return ItemStack.OPTIONAL_CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE),stack).getOrThrow();}
 public static Tag save(FluidStack stack,HolderLookup.Provider registries){return FluidStack.OPTIONAL_CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE),stack).getOrThrow();}
 public static ItemStack item(HolderLookup.Provider registries,Tag tag){return ItemStack.OPTIONAL_CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE),tag).getOrThrow();}
 public static FluidStack fluid(HolderLookup.Provider registries,Tag tag){return FluidStack.OPTIONAL_CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE),tag).getOrThrow();}
 public static CompoundTag save(ValueIOSerializable value,HolderLookup.Provider registries){
  try(var reporter=new ProblemReporter.ScopedCollector(LOG)){var output=TagValueOutput.createWithContext(reporter,registries);value.serialize(output);return output.buildResult();}
 }
 public static void load(ValueIOSerializable value,HolderLookup.Provider registries,CompoundTag tag){
  try(var reporter=new ProblemReporter.ScopedCollector(LOG)){value.deserialize(TagValueInput.create(reporter,registries,tag));}
 }
}
