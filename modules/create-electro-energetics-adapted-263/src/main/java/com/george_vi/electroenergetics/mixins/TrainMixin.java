package com.george_vi.electroenergetics.mixins;

import com.george_vi.electroenergetics.CEEElectricTrainSoundTypes;
import com.george_vi.electroenergetics.CEERegistries;
import com.george_vi.electroenergetics.content.railway_electrification.ElectricTrainData;
import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.TrainSoundModifier;
import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.sound_types.ElectricTrainSoundType;
import com.george_vi.electroenergetics.mixin_interfaces.ICEETrainExtension;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.graph.DimensionPalette;
import com.zurrtum.create.content.trains.graph.TrackGraph;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mixin(Train.class)
public class TrainMixin implements ICEETrainExtension {
    @Shadow @Final @Mutable
    public static StreamCodec<RegistryFriendlyByteBuf, Train> STREAM_CODEC;

    @org.spongepowered.asm.mixin.injection.Inject(method = "<clinit>", at = @org.spongepowered.asm.mixin.injection.At("TAIL"))
    private static void electroenergetics$networkCodec(org.spongepowered.asm.mixin.injection.callback.CallbackInfo callback) {
        STREAM_CODEC = com.george_vi.electroenergetics.events.TrainSerialization.networkCodec(STREAM_CODEC,
            train -> com.george_vi.electroenergetics.events.TrainSerialization.writeTrain((ICEETrainExtension) train),
            (train, tag) -> com.george_vi.electroenergetics.events.TrainSerialization.readTrain((ICEETrainExtension) train, tag));
    }

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

    @org.spongepowered.asm.mixin.injection.Inject(method = "write", at = @org.spongepowered.asm.mixin.injection.At("TAIL"))
    private void electroenergetics$write(net.minecraft.world.level.storage.ValueOutput view, DimensionPalette dimensions,
        org.spongepowered.asm.mixin.injection.callback.CallbackInfo callback) {
        com.george_vi.electroenergetics.foundation.NbtView.write(view, com.george_vi.electroenergetics.events.TrainSerialization.writeTrain(this));
    }

    @org.spongepowered.asm.mixin.injection.Inject(method = "read", at = @org.spongepowered.asm.mixin.injection.At("RETURN"))
    private static void electroenergetics$read(net.minecraft.world.level.storage.ValueInput view,
        Map<UUID, TrackGraph> graphs, DimensionPalette dimensions,
        org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Train> callback) {
        com.george_vi.electroenergetics.events.TrainSerialization.readTrain((ICEETrainExtension) callback.getReturnValue(),
            com.george_vi.electroenergetics.foundation.NbtView.read(view));
    }

    @org.spongepowered.asm.mixin.injection.Inject(method = "encode", at = @org.spongepowered.asm.mixin.injection.At("RETURN"), cancellable = true)
    private static <T> void electroenergetics$encode(Train train, com.mojang.serialization.DynamicOps<T> ops, T prefix,
        DimensionPalette dimensions, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<com.mojang.serialization.DataResult<T>> callback) {
        callback.setReturnValue(com.george_vi.electroenergetics.events.TrainSerialization.merge(ops, callback.getReturnValue(),
            com.george_vi.electroenergetics.events.TrainSerialization.writeTrain((ICEETrainExtension) train)));
    }

    @org.spongepowered.asm.mixin.injection.Inject(method = "decode", at = @org.spongepowered.asm.mixin.injection.At("RETURN"))
    private static <T> void electroenergetics$decode(com.mojang.serialization.DynamicOps<T> ops, T input,
        Map<UUID, TrackGraph> graphs, DimensionPalette dimensions,
        org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Train> callback) {
        com.george_vi.electroenergetics.events.TrainSerialization.readTrain((ICEETrainExtension) callback.getReturnValue(),
            com.george_vi.electroenergetics.events.TrainSerialization.asTag(ops, input));
    }
}
