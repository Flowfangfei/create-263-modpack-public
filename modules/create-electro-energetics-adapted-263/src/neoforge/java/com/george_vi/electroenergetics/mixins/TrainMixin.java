package com.george_vi.electroenergetics.mixins;

import com.george_vi.electroenergetics.CEEElectricTrainSoundTypes;
import com.george_vi.electroenergetics.CEERegistries;
import com.george_vi.electroenergetics.content.railway_electrification.ElectricTrainData;
import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.TrainSoundModifier;
import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.sound_types.ElectricTrainSoundType;
import com.george_vi.electroenergetics.mixin_interfaces.ICEETrainExtension;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.graph.DimensionPalette;
import com.simibubi.create.content.trains.graph.TrackGraph;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mixin(Train.class)
public class TrainMixin implements ICEETrainExtension {
    @Unique
    ElectricTrainSoundType electroenergetics$soundType;

    @Unique
    ElectricTrainData electroenergetics$electricTrainData = new ElectricTrainData();

    @Unique
    public Set<TrainSoundModifier> electroEnergetics$soundModifyingBlocks = new HashSet<>();

    @Override
    public ElectricTrainSoundType getSoundType() {
        return electroenergetics$soundType;
    }

    @Override
    public void setSoundType(ElectricTrainSoundType type) {
        electroenergetics$soundType = type;
    }

    @Override
    public Set<TrainSoundModifier> getSoundModifyingBlocks() {
        return electroEnergetics$soundModifyingBlocks;
    }

    @Override
    public ElectricTrainData getElectricTrainData() {
        return electroenergetics$electricTrainData;
    }

    // Wrap method so its compatible with Create: Power Loader
    @WrapMethod(method = "write(Lcom/simibubi/create/content/trains/graph/DimensionPalette;Lnet/minecraft/core/HolderLookup$Provider;Lnet/minecraft/resources/RegistryOps;)Lnet/minecraft/nbt/CompoundTag;")
    public CompoundTag electroEnergetics$write(DimensionPalette dimensions, HolderLookup.Provider registries, RegistryOps<Tag> ops, @NotNull Operation<CompoundTag> original) {
        CompoundTag tag = original.call(dimensions, registries, ops);
        Identifier id = CEERegistries.ELECTRIC_TRAIN_SOUND_TYPE.getKey(electroenergetics$soundType);
        if (id != null)
            tag.putString("CEETrainSoundType", id.toString());
        tag.putInt("CEEAccumulators", electroenergetics$electricTrainData.accumulators);
        tag.putDouble("CEEAccumulatorCharge", electroenergetics$electricTrainData.accumulatorCharge);
        tag.putBoolean("CEECreativeSource", electroenergetics$electricTrainData.hasCreativeSource);
        tag.putDouble("CEELastVoltage", electroenergetics$electricTrainData.lastVoltage);
        return tag;
    }

    // Wrap method so its compatible with Create: Power Loader
    @WrapMethod(method = "read(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;Lnet/minecraft/resources/RegistryOps;Ljava/util/Map;Lcom/simibubi/create/content/trains/graph/DimensionPalette;)Lcom/simibubi/create/content/trains/entity/Train;")
    private static Train electroEnergetics$read(CompoundTag tag, HolderLookup.Provider registries, RegistryOps<Tag> ops, Map<UUID, TrackGraph> trackNetworks, DimensionPalette dimensions, Operation<Train> original) {
        Train originalTrain = original.call(tag, registries, ops, trackNetworks, dimensions);
        String id = tag.getStringOr("CEETrainSoundType", "");
        Identifier location = Identifier.tryParse(id);
        ElectricTrainSoundType soundType = null;
        if (location != null)
            soundType = CEERegistries.ELECTRIC_TRAIN_SOUND_TYPE.getValue(location);
        if (soundType == null)
            soundType = CEEElectricTrainSoundTypes.MODERN.get();
        ICEETrainExtension train = (ICEETrainExtension) originalTrain;
        train.setSoundType(soundType);
        ElectricTrainData electricTrainData = train.getElectricTrainData();
        electricTrainData.accumulators = tag.getIntOr("CEEAccumulators", 0);
        electricTrainData.accumulatorCharge = tag.getDoubleOr("CEEAccumulatorCharge", 0d);
        electricTrainData.hasCreativeSource = tag.getBooleanOr("CEECreativeSource", false);
        electricTrainData.lastVoltage = tag.getDoubleOr("CEELastVoltage", 0d);
        return originalTrain;
    }
}
