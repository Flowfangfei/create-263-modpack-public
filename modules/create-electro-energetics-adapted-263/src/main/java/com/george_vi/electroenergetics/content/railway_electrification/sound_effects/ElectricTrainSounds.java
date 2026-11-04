package com.george_vi.electroenergetics.content.railway_electrification.sound_effects;

import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.sound_types.ElectricTrainSoundBehaviour;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.catnip.data.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
public class ElectricTrainSounds {
    public static Map<Pair<UUID, Integer>, ElectricTrainSoundEntry> soundProperties = new HashMap<>();

    @Environment(EnvType.CLIENT)
    static Map<Pair<UUID, Integer>, ElectricTrainSoundBehaviour> sounds = new HashMap<>();

    @Environment(EnvType.CLIENT)
    public static void tick() {
        for (Map.Entry<Pair<UUID, Integer>, ElectricTrainSoundEntry> e : Set.copyOf(soundProperties.entrySet())) {
            Pair<UUID, Integer> carriagePair = e.getKey();
            UUID trainID = carriagePair.getFirst();
            int carriageID = carriagePair.getSecond();
            Train train = Create.RAILWAYS.trains.get(trainID);
            if (train == null) {
                sounds.remove(carriagePair);
                soundProperties.remove(carriagePair);
                continue;
            }
            ElectricTrainSoundBehaviour soundBehaviour = sounds.get(carriagePair);
            if (soundBehaviour == null)
                sounds.put(carriagePair, soundBehaviour = e.getValue().type().soundBehaviour.get());

            soundBehaviour.trainSpeed = e.getValue().speed();
            soundBehaviour.acceleration = e.getValue().acceleration();
            soundBehaviour.pos = null;
            if (train.carriages.size() >= Math.abs(carriageID)) {
                Carriage.DimensionalCarriageEntity dimensional = train.carriages.get(Math.abs(carriageID) - 1).getDimensional(Minecraft.getInstance().level);
                Vec3 leading = dimensional.leadingAnchor();
                Vec3 trailing = dimensional.trailingAnchor();
                if (carriageID > 0) {
                    if (leading != null)
                        soundBehaviour.pos = leading;
                } else if (trailing != null)
                    soundBehaviour.pos = trailing;
            }

            if (e.getValue().ticks() > 0) {
                if (soundBehaviour.pos != null)
                    soundBehaviour.tick();
                soundProperties.replace(carriagePair, new ElectricTrainSoundEntry(e.getValue().speed(), e.getValue().acceleration(), e.getValue().active(), e.getValue().ticks() - 1, e.getValue().type()));
            } else
                soundProperties.remove(carriagePair);
        }
    }
}
