package com.george_vi.electroenergetics.mixins;

import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.ElectricTrainSounds;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.client.foundation.entity.behaviour.CarriageAudioBehaviour;

import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CarriageAudioBehaviour.class)
public class CarriageSoundsMixin {
    @WrapOperation(
            method = "tick",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lcom/zurrtum/create/AllSoundEvents$SoundEntry;playAt(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/phys/Vec3;FFZ)V"),
            remap = false)
    private void electroenergetics$steam(
            AllSoundEvents.SoundEntry sound,
            Level level,
            Vec3 pos,
            float volume,
            float pitch,
            boolean fade,
            Operation<Void> original) {
        var carriage = ((CarriageAudioBehaviour) (Object) this).entity.getCarriage();
        if (sound != AllSoundEvents.STEAM
                || carriage == null
                || ElectricTrainSounds.soundProperties.keySet().stream()
                        .noneMatch(key -> key.getFirst().equals(carriage.train.id)))
            original.call(sound, level, pos, volume, pitch, fade);
    }
}
