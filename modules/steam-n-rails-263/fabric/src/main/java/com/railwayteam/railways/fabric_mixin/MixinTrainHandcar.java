package com.railwayteam.railways.fabric_mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.railwayteam.railways.mixin_interfaces.IHandcarTrain;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.graph.DimensionPalette;
import com.simibubi.create.content.trains.graph.TrackGraph;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.UUID;

@Mixin(value = Train.class, remap = false)
public class MixinTrainHandcar implements IHandcarTrain {
    @Unique
    private boolean railways$isHandcar = false;

    @Override
    public boolean railways$isHandcar() {
        return railways$isHandcar;
    }

    @Override
    public void railways$setHandcar(boolean handcar) {
        railways$isHandcar = handcar;
    }

    @Inject(method = "write(Lcom/simibubi/create/content/trains/graph/DimensionPalette;Lnet/minecraft/core/HolderLookup$Provider;Lnet/minecraft/resources/RegistryOps;)Lnet/minecraft/nbt/CompoundTag;", at = @At("RETURN"))
    private void railways$writeExtras(DimensionPalette dimensions, HolderLookup.Provider registries,
                                     RegistryOps<Tag> ops, CallbackInfoReturnable<CompoundTag> cir) {
        cir.getReturnValue().putBoolean("IsHandcar", railways$isHandcar);
    }

    @Inject(method = "read(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/core/HolderLookup$Provider;Lnet/minecraft/resources/RegistryOps;Ljava/util/Map;Lcom/simibubi/create/content/trains/graph/DimensionPalette;)Lcom/simibubi/create/content/trains/entity/Train;", at = @At("RETURN"))
    private static void railways$readExtras(CompoundTag tag, HolderLookup.Provider registries,
            RegistryOps<Tag> ops, Map<UUID, TrackGraph> trackNetworks, DimensionPalette dimensions,
            CallbackInfoReturnable<Train> cir) {
        ((IHandcarTrain) cir.getReturnValue()).railways$setHandcar(tag.getBooleanOr("IsHandcar", false));
    }
}
