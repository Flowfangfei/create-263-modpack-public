package com.george_vi.electroenergetics.content.railway_electrification.sound_effects.sound_types;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.function.Supplier;

public class ElectricTrainSoundType {
    @Environment(EnvType.CLIENT)
    public Supplier<ElectricTrainSoundBehaviour> soundBehaviour;
    public final Identifier id;
    public final Supplier<Object2IntMap<Block>> validBlocks;
    public ElectricTrainSoundType(Supplier<Supplier<ElectricTrainSoundBehaviour>> soundBehaviour, Identifier id, Supplier<Object2IntMap<Block>> validBlocks) {
        this.validBlocks = validBlocks;
        com.george_vi.electroenergetics.foundation.ClientExecution.run(() -> () -> this.soundBehaviour = soundBehaviour.get());
        this.id = id;
    }

    public String translationKey() {
        return id.getNamespace() + ".electric_train_sound." + id.getPath();
    }
}
