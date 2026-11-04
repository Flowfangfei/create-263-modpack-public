package com.george_vi.electroenergetics.foundation;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.stream.Stream;

/** Preserves the established device NBT format at Minecraft's value-view boundary. */
public final class NbtView {
    private NbtView() {}

    public static final MapCodec<CompoundTag> CODEC = new MapCodec<>() {
        @Override
        public <T> DataResult<CompoundTag> decode(DynamicOps<T> ops, MapLike<T> input) {
            CompoundTag tag = new CompoundTag();
            input.entries().forEach(entry -> ops.getStringValue(entry.getFirst()).result()
                    .ifPresent(key -> tag.put(key, ops.convertTo(NbtOps.INSTANCE, entry.getSecond()))));
            return DataResult.success(tag);
        }

        @Override
        public <T> RecordBuilder<T> encode(CompoundTag input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            for (String key : input.keySet()) {
                prefix.add(key, NbtOps.INSTANCE.convertTo(ops, input.get(key)));
            }
            return prefix;
        }

        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.empty();
        }
    };

    public static CompoundTag read(ValueInput input) {
        return input.read(CODEC).orElseGet(CompoundTag::new);
    }

    public static void write(ValueOutput output, CompoundTag tag) {
        output.store(CODEC, tag);
    }
}
