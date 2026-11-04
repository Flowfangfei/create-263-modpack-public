package com.george_vi.electroenergetics.mixins;

import com.george_vi.electroenergetics.CEEPantographTypes;
import com.george_vi.electroenergetics.CEERegistries;
import com.george_vi.electroenergetics.content.railway_electrification.ElectricTrainData;
import com.george_vi.electroenergetics.content.railway_electrification.pantograph.PantographType;
import com.george_vi.electroenergetics.content.railway_electrification.pantograph.TrainPantographEntry;
import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.TrainSoundModifier;
import com.george_vi.electroenergetics.mixin_interfaces.ICEETrainExtension;
import com.george_vi.electroenergetics.mixin_interfaces.IPantographList;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.CarriageContraption;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.graph.DimensionPalette;
import com.zurrtum.create.content.trains.graph.TrackGraph;
import com.zurrtum.create.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mixin(Carriage.class)
public class CarriageMixin implements IPantographList {
    @Shadow @Final @Mutable
    public static StreamCodec<RegistryFriendlyByteBuf, Carriage> STREAM_CODEC;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void electroenergetics$networkCodec(CallbackInfo callback) {
        STREAM_CODEC = com.george_vi.electroenergetics.events.TrainSerialization.networkCodec(STREAM_CODEC,
            carriage -> com.george_vi.electroenergetics.events.TrainSerialization.writeCarriage((IPantographList) carriage),
            (carriage, tag) -> com.george_vi.electroenergetics.events.TrainSerialization.readCarriage((IPantographList) carriage, tag));
    }

    @Unique
    public List<TrainPantographEntry> electroEnergetics$pantographs = new ArrayList<>();

    @Unique
    public Set<TrainSoundModifier> electroEnergetics$soundModifyingBlocks = new HashSet<>();

    @Unique
    public boolean electroEnergetics$hasMotor = false;

    @Shadow
    public Train train;

    @Override
    public int electroEnergetics$getAccumulators() {
        return 0;
    }

    @Override
    public void electroEnergetics$setAccumulators(int value) {
    }

    @Override
    public boolean electroEnergetics$hasCreativeElectricalSource() {
        return false;
    }

    @Inject(method = "setContraption", at=@At("TAIL"), remap = false)
    public void electroEnergetics$setContraption(Level level, CarriageContraption contraption, CallbackInfo ci) {
        IPantographList contraptionExtension = (IPantographList) contraption;
        this.electroEnergetics$pantographs = contraptionExtension.electroEnergetics$getPantographList();
        this.electroEnergetics$hasMotor = contraptionExtension.electroEnergetics$hasElectricMotor();
        this.electroEnergetics$soundModifyingBlocks = contraptionExtension.electroEnergetics$getSoundModifyingBlocks();

        if (train == null)
            return;
        ICEETrainExtension trainExtension = (ICEETrainExtension) train;
        ElectricTrainData trainData = trainExtension.getElectricTrainData();
        trainData.accumulators += contraptionExtension.electroEnergetics$getAccumulators();
        trainData.hasCreativeSource |= contraptionExtension.electroEnergetics$hasCreativeElectricalSource();
        trainData.pantographs.addAll(this.electroEnergetics$pantographs);

        if (electroEnergetics$soundModifyingBlocks.isEmpty())
            return;
        Set<TrainSoundModifier> trainSMBlocks = trainExtension.getSoundModifyingBlocks();
        trainSMBlocks.addAll(electroEnergetics$soundModifyingBlocks);
        TrainSoundModifier choice = null;
        for (TrainSoundModifier sm : trainSMBlocks) {
            if (choice == null || choice.priority() < sm.priority())
                choice = sm;
        }

        if (choice != null)
            trainExtension.setSoundType(choice.soundType());
    }

    @Inject(method = "setTrain", at=@At("TAIL"), remap = false)
    public void setElectroEnergetics$setTrain(Train train, CallbackInfo ci) {
        ICEETrainExtension trainExtension = (ICEETrainExtension) train;
        ElectricTrainData trainData = trainExtension.getElectricTrainData();
        List<TrainPantographEntry> toAdd = new ArrayList<>();
        for (TrainPantographEntry pantograph : this.electroEnergetics$pantographs) {
            if (!trainData.pantographs.contains(pantograph))
                toAdd.add(pantograph);
        }
        trainData.pantographs.addAll(toAdd);
    }


    @Override
    public void electroEnergetics$setPantographList(List<TrainPantographEntry> newPantographList) {
        electroEnergetics$pantographs = newPantographList;
    }

    @Override
    public List<TrainPantographEntry> electroEnergetics$getPantographList() {
        return electroEnergetics$pantographs;
    }

    @Override
    public boolean electroEnergetics$hasElectricMotor() {
        return electroEnergetics$hasMotor;
    }

    @Override
    public void electroEnergetics$setElectricMotor(boolean v) {
        electroEnergetics$hasMotor = v;
    }


    @Override
    public Set<TrainSoundModifier> electroEnergetics$getSoundModifyingBlocks() {
        return electroEnergetics$soundModifyingBlocks;
    }

    @Inject(method = "write", at = @At("TAIL"))
    private void electroenergetics$write(net.minecraft.world.level.storage.ValueOutput view, DimensionPalette dimensions, CallbackInfo callback) {
        com.george_vi.electroenergetics.foundation.NbtView.write(view, com.george_vi.electroenergetics.events.TrainSerialization.writeCarriage(this));
    }

    @Inject(method = "read", at = @At("RETURN"))
    private static void electroenergetics$read(net.minecraft.world.level.storage.ValueInput view, TrackGraph graph,
        DimensionPalette dimensions, CallbackInfoReturnable<Carriage> callback) {
        com.george_vi.electroenergetics.events.TrainSerialization.readCarriage((IPantographList) callback.getReturnValue(),
            com.george_vi.electroenergetics.foundation.NbtView.read(view));
    }

    @Inject(method = "encode", at = @At("RETURN"), cancellable = true)
    private static <T> void electroenergetics$encode(Carriage carriage, com.mojang.serialization.DynamicOps<T> ops, T prefix,
        DimensionPalette dimensions, CallbackInfoReturnable<com.mojang.serialization.DataResult<T>> callback) {
        callback.setReturnValue(com.george_vi.electroenergetics.events.TrainSerialization.merge(ops, callback.getReturnValue(),
            com.george_vi.electroenergetics.events.TrainSerialization.writeCarriage((IPantographList) carriage)));
    }

    @Inject(method = "decode", at = @At("RETURN"))
    private static <T> void electroenergetics$decode(com.mojang.serialization.DynamicOps<T> ops, T input, TrackGraph graph,
        DimensionPalette dimensions, CallbackInfoReturnable<Carriage> callback) {
        com.george_vi.electroenergetics.events.TrainSerialization.readCarriage((IPantographList) callback.getReturnValue(),
            com.george_vi.electroenergetics.events.TrainSerialization.asTag(ops, input));
    }
}
