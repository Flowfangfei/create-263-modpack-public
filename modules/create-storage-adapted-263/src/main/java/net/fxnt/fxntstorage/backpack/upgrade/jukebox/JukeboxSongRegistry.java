package net.fxnt.fxntstorage.backpack.upgrade.jukebox;

import com.google.gson.*;
import net.fxnt.fxntstorage.FXNTStorage;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.fxnt.fxntstorage.FXNTStorage.MAX_EFFECTS_PER_SONG;

@EventBusSubscriber(modid = FXNTStorage.MOD_ID)
public class JukeboxSongRegistry {
    private static final Map<Identifier, JukeboxSongData> SONGS = new HashMap<>();

    public static void clear() {
        SONGS.clear();
    }

    public static void register(JukeboxSongData data) {
        SONGS.put(data.song(), data);
    }

    @Nullable
    public static JukeboxSongData get(Identifier songId) {
        return SONGS.get(songId);
    }

    public record JukeboxSongData(Identifier song, boolean playerOnly, List<SongEffectData> effects) {
    }

    public record SongEffectData(Identifier effect, int amplifier) {
    }

    public static class JukeboxSongReloadListener extends SimpleJsonResourceReloadListener<JsonElement> {
        private static final Gson GSON = new GsonBuilder().create();

        public JukeboxSongReloadListener() {
            super(com.mojang.serialization.Codec.PASSTHROUGH.xmap(
                    value -> value.convert(com.mojang.serialization.JsonOps.INSTANCE).getValue(),
                    value -> new com.mojang.serialization.Dynamic<>(com.mojang.serialization.JsonOps.INSTANCE, value)),
                    net.minecraft.resources.FileToIdConverter.json("jukebox_songs"));
        }

        @Override
        protected void apply(Map<Identifier, JsonElement> jsons, @NotNull ResourceManager manager, @NotNull ProfilerFiller profiler) {
            JukeboxSongRegistry.clear();

            jsons.forEach((id, json) -> {
                try {
                    JukeboxSongData data = parse(json.getAsJsonObject());
                    JukeboxSongRegistry.register(data);
                } catch (Exception e) {
                    FXNTStorage.LOGGER.error("Failed to load jukebox song data {}", id, e);
                }
            });
        }

        private static JukeboxSongData parse(JsonObject obj) {
            Identifier song = Identifier.parse(obj.get("song").getAsString());
            boolean playerOnly = obj.has("player_only") && obj.get("player_only").getAsBoolean();

            JsonArray effectsArray = obj.getAsJsonArray("effects");

            if (effectsArray.size() > MAX_EFFECTS_PER_SONG) {
                FXNTStorage.LOGGER.warn(
                        "[Jukebox] Song '{}' defines {} effects (max {}). Extra effects will be ignored.",
                        song, effectsArray.size(), MAX_EFFECTS_PER_SONG);
            }

            List<SongEffectData> effects = new ArrayList<>();
            Set<Identifier> seen = new HashSet<>();

            for (int i = 0; i < Math.min(effectsArray.size(), MAX_EFFECTS_PER_SONG); i++) {
                JsonObject effectObj = effectsArray.get(i).getAsJsonObject();
                Identifier effectId = Identifier.parse(effectObj.get("effect").getAsString());

                if (!seen.add(effectId)) {
                    FXNTStorage.LOGGER.warn("[Jukebox] Duplicate effect '{}' in song '{}'. Skipping.", effectId, song);
                    continue;
                }

                effects.add(new SongEffectData(
                        Identifier.parse(effectObj.get("effect").getAsString()),
                        effectObj.has("amplifier")
                                ? effectObj.get("amplifier").getAsInt()
                                : 0
                ));
            }

            return new JukeboxSongData(song, playerOnly, effects);
        }
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddServerReloadListenersEvent event) {
        event.addListener(FXNTStorage.modLoc("jukebox_songs"), new JukeboxSongReloadListener());
    }
}
