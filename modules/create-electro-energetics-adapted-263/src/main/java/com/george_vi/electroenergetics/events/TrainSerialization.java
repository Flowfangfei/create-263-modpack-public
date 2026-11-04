package com.george_vi.electroenergetics.events;

import com.george_vi.electroenergetics.*;
import com.george_vi.electroenergetics.content.railway_electrification.ElectricTrainData;
import com.george_vi.electroenergetics.content.railway_electrification.pantograph.PantographType;
import com.george_vi.electroenergetics.content.railway_electrification.pantograph.TrainPantographEntry;
import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.sound_types.ElectricTrainSoundType;
import com.george_vi.electroenergetics.mixin_interfaces.ICEETrainExtension;
import com.george_vi.electroenergetics.mixin_interfaces.IPantographList;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.zurrtum.create.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/** The same CEE fields are shared by Create Fly's value views and its generic codecs. */
public final class TrainSerialization {
    private TrainSerialization() {}

    public static <T> StreamCodec<RegistryFriendlyByteBuf, T> networkCodec(
        StreamCodec<RegistryFriendlyByteBuf, T> original, Function<T, CompoundTag> writer, BiConsumer<T, CompoundTag> reader) {
        return new StreamCodec<>() {
            @Override public T decode(RegistryFriendlyByteBuf buffer) {
                T value = original.decode(buffer);
                reader.accept(value, ByteBufCodecs.COMPOUND_TAG.decode(buffer));
                return value;
            }

            @Override public void encode(RegistryFriendlyByteBuf buffer, T value) {
                original.encode(buffer, value);
                ByteBufCodecs.COMPOUND_TAG.encode(buffer, writer.apply(value));
            }
        };
    }

    public static CompoundTag writeTrain(ICEETrainExtension train) {
        CompoundTag tag = new CompoundTag();
        Identifier id = CEERegistries.ELECTRIC_TRAIN_SOUND_TYPE.getKey(train.getSoundType());
        if (id != null) tag.putString("CEETrainSoundType", id.toString());
        ElectricTrainData data = train.getElectricTrainData();
        tag.putInt("CEEAccumulators", data.accumulators);
        tag.putDouble("CEEAccumulatorCharge", data.accumulatorCharge);
        tag.putBoolean("CEECreativeSource", data.hasCreativeSource);
        tag.putDouble("CEELastVoltage", data.lastVoltage);
        return tag;
    }

    public static void readTrain(ICEETrainExtension train, CompoundTag tag) {
        Identifier id = Identifier.tryParse(tag.getStringOr("CEETrainSoundType", ""));
        ElectricTrainSoundType type = id == null ? null : CEERegistries.ELECTRIC_TRAIN_SOUND_TYPE.getValue(id);
        train.setSoundType(type == null ? CEEElectricTrainSoundTypes.MODERN.get() : type);
        ElectricTrainData data = train.getElectricTrainData();
        data.accumulators = tag.getIntOr("CEEAccumulators", 0);
        data.accumulatorCharge = tag.getDoubleOr("CEEAccumulatorCharge", 0);
        data.hasCreativeSource = tag.getBooleanOr("CEECreativeSource", false);
        data.lastVoltage = tag.getDoubleOr("CEELastVoltage", 0);
    }

    public static CompoundTag writeCarriage(IPantographList carriage) {
        CompoundTag tag = new CompoundTag();
        ListTag entries = new ListTag();
        for (TrainPantographEntry entry : List.copyOf(carriage.electroEnergetics$getPantographList())) {
            CompoundTag value = new CompoundTag();
            value.store("Pos", BlockPos.CODEC, entry.rotatedPos);
            value.store("OriginalPos", BlockPos.CODEC, entry.originalPos);
            value.putBoolean("Forward", entry.facingForward);
            value.putBoolean("Active", entry.active);
            Identifier type = CEERegistries.PANTOGRAPH_TYPE.getKey(entry.type);
            if (type != null) value.putString("TypeID", type.toString());
            entries.add(value);
        }
        tag.put("CEEPantographs", entries);
        tag.putBoolean("CEEHasElectricMotor", carriage.electroEnergetics$hasElectricMotor());
        return tag;
    }

    public static void readCarriage(IPantographList carriage, CompoundTag tag) {
        List<TrainPantographEntry> entries = new ArrayList<>();
        for (Tag raw : tag.getListOrEmpty("CEEPantographs")) {
            if (!(raw instanceof CompoundTag value)) continue;
            Identifier typeId = Identifier.tryParse(value.getStringOr("TypeID", ""));
            PantographType type = typeId == null ? null : CEERegistries.PANTOGRAPH_TYPE.getValue(typeId);
            entries.add(new TrainPantographEntry(NBTHelper.readBlockPos(value, "OriginalPos"),
                NBTHelper.readBlockPos(value, "Pos"), type == null ? CEEPantographTypes.STANDARD.get() : type,
                value.getBooleanOr("Active", false), value.getBooleanOr("Forward", false)));
        }
        carriage.electroEnergetics$setPantographList(entries);
        carriage.electroEnergetics$setElectricMotor(tag.getBooleanOr("CEEHasElectricMotor", false));
    }

    public static <T> DataResult<T> merge(DynamicOps<T> ops, DataResult<T> encoded, CompoundTag fields) {
        T extra = NbtOps.INSTANCE.convertTo(ops, fields);
        return encoded.flatMap(value -> ops.getMap(extra).flatMap(map -> ops.mergeToMap(value, map)));
    }

    public static <T> CompoundTag asTag(DynamicOps<T> ops, T input) {
        Tag tag = ops.convertTo(NbtOps.INSTANCE, input);
        return tag instanceof CompoundTag compound ? compound : new CompoundTag();
    }
}
