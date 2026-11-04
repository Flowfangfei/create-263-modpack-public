/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package plus.dragons.createdragonsplus.data.map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.fabric.api.resource.v1.DataResourceLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createdragonsplus.CreateDragonsPlus;
import plus.dragons.createdragonsplus.fluid.dye.DyeVariantRegistry;
import plus.dragons.createdragonsplus.network.ColoringCatalystSyncPayload;

public final class CDPColoringCatalysts {
    private static final Identifier RELOADER_ID = CreateDragonsPlus.id("coloring_catalysts");
    private static final Identifier FLUID_DATA = CreateDragonsPlus.id("data_maps/fluid/fan_processing_catalysts/coloring.json");
    private static final Identifier BLOCK_DATA = CreateDragonsPlus.id("data_maps/block/fan_processing_catalysts/coloring.json");
    private static volatile Snapshot snapshot = Snapshot.EMPTY;
    private static volatile RawData pending = RawData.EMPTY;
    private static boolean registered;

    private CDPColoringCatalysts() {}

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        DataResourceLoader.get().registerReloadListener(RELOADER_ID, registries -> new Reloader());
    }

    public static Snapshot snapshot() {
        return snapshot;
    }

    public static @Nullable Identifier fluidColor(Fluid fluid) {
        return snapshot.fluids().get(BuiltInRegistries.FLUID.getKey(fluid));
    }

    public static @Nullable Identifier blockColor(Block block) {
        return snapshot.blocks().get(BuiltInRegistries.BLOCK.getKey(block));
    }

    public static void applyClientSnapshot(ColoringCatalystSyncPayload payload) {
        snapshot = new Snapshot(payload.fluids(), payload.blocks());
    }

    public static void applyAfterTags() {
        Snapshot resolved = resolve(pending.fluids(), pending.blocks());
        snapshot = resolved;
        CreateDragonsPlus.LOGGER.info(
                "Loaded {} fluid and {} block coloring catalysts",
                resolved.fluids().size(),
                resolved.blocks().size());
    }

    public static Layer mergeLayers(List<Layer> layers) {
        LinkedHashMap<String, Identifier> values = new LinkedHashMap<>();
        for (Layer layer : layers) {
            if (layer.replace()) {
                values.clear();
            }
            layer.values().forEach((selector, color) -> {
                values.remove(selector);
                values.put(selector, color);
            });
        }
        return new Layer(false, values);
    }

    private static Layer readStack(ResourceManager manager, Identifier id) {
        List<Resource> resources = manager.getResourceStack(id);
        java.util.ArrayList<Layer> layers = new java.util.ArrayList<>(resources.size());
        for (Resource resource : resources) {
            try (var reader = resource.openAsReader()) {
                JsonElement parsed = JsonParser.parseReader(reader);
                if (!parsed.isJsonObject()) {
                    CreateDragonsPlus.LOGGER.error("Ignoring non-object data map {} from {}", id, resource.sourcePackId());
                    continue;
                }
                Layer layer = parseLayer(parsed.getAsJsonObject(), id + " from " + resource.sourcePackId());
                if (layer != null) {
                    layers.add(layer);
                }
            } catch (IOException | RuntimeException exception) {
                CreateDragonsPlus.LOGGER.error("Failed to read data map {} from {}", id, resource.sourcePackId(), exception);
            }
        }
        return mergeLayers(layers);
    }

    static @Nullable Layer parseLayer(JsonObject root, String source) {
        if (!conditionsPass(root, source)) {
            return null;
        }
        boolean replace = root.has("replace") && root.get("replace").getAsBoolean();
        JsonElement valuesElement = root.get("values");
        if (valuesElement == null || !valuesElement.isJsonObject()) {
            CreateDragonsPlus.LOGGER.error("Ignoring {} because its values field is not an object", source);
            return null;
        }
        LinkedHashMap<String, Identifier> values = new LinkedHashMap<>();
        for (var entry : valuesElement.getAsJsonObject().entrySet()) {
            Identifier color = readValue(entry.getValue(), source + " entry " + entry.getKey());
            if (color == null || DyeVariantRegistry.get(color).isEmpty()) {
                if (color != null) {
                    CreateDragonsPlus.LOGGER.error("Ignoring unknown dye variant {} in {}", color, source);
                }
                continue;
            }
            String selector = entry.getKey();
            String idText = selector.startsWith("#") ? selector.substring(1) : selector;
            if (Identifier.tryParse(idText) == null) {
                CreateDragonsPlus.LOGGER.error("Ignoring invalid selector {} in {}", selector, source);
                continue;
            }
            values.put(selector, color);
        }
        return new Layer(replace, values);
    }

    private static @Nullable Identifier readValue(JsonElement element, String source) {
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            return Identifier.tryParse(element.getAsString());
        }
        if (!element.isJsonObject()) {
            CreateDragonsPlus.LOGGER.error("Ignoring invalid value in {}", source);
            return null;
        }
        JsonObject object = element.getAsJsonObject();
        if (!conditionsPass(object, source)) {
            return null;
        }
        JsonElement value = object.has("value") ? object.get("value") : object.get("fabric:value");
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            CreateDragonsPlus.LOGGER.error("Ignoring conditional value without a string value field in {}", source);
            return null;
        }
        return Identifier.tryParse(value.getAsString());
    }

    private static boolean conditionsPass(JsonObject object, String source) {
        JsonElement conditions = object.get(ResourceConditions.CONDITIONS_KEY);
        if (conditions == null) {
            return true;
        }
        return ResourceCondition.CONDITION_CODEC
                .parse(JsonOps.INSTANCE, conditions)
                .resultOrPartial(message -> CreateDragonsPlus.LOGGER.error(
                        "Invalid Fabric resource condition in {}: {}", source, message))
                .map(condition -> condition.test(null))
                .orElse(false);
    }

    private static Snapshot resolve(Layer fluids, Layer blocks) {
        LinkedHashMap<Identifier, Identifier> resolvedFluids = resolve(BuiltInRegistries.FLUID, Registries.FLUID, fluids);
        LinkedHashMap<Identifier, Identifier> resolvedBlocks = resolve(BuiltInRegistries.BLOCK, Registries.BLOCK, blocks);
        return new Snapshot(resolvedFluids, resolvedBlocks);
    }

    private static <T> LinkedHashMap<Identifier, Identifier> resolve(
            Registry<T> registry,
            net.minecraft.resources.ResourceKey<? extends Registry<T>> registryKey,
            Layer layer) {
        LinkedHashMap<Identifier, Identifier> resolved = new LinkedHashMap<>();
        for (var entry : layer.values().entrySet()) {
            String selector = entry.getKey();
            Identifier id = Identifier.tryParse(selector.startsWith("#") ? selector.substring(1) : selector);
            if (id == null) {
                continue;
            }
            if (selector.startsWith("#")) {
                registry.getTagOrEmpty(TagKey.create(registryKey, id)).forEach(
                        holder -> resolved.put(registry.getKey(holder.value()), entry.getValue()));
            } else if (registry.containsKey(id)) {
                resolved.put(id, entry.getValue());
            } else {
                CreateDragonsPlus.LOGGER.error("Ignoring missing {} registry entry {} in coloring catalysts", registryKey.identifier(), id);
            }
        }
        return resolved;
    }

    public record Layer(boolean replace, Map<String, Identifier> values) {
        public Layer {
            values = Collections.unmodifiableMap(new LinkedHashMap<>(values));
        }
    }

    public record Snapshot(Map<Identifier, Identifier> fluids, Map<Identifier, Identifier> blocks) {
        private static final Snapshot EMPTY = new Snapshot(Map.of(), Map.of());

        public Snapshot {
            fluids = Map.copyOf(fluids);
            blocks = Map.copyOf(blocks);
        }

        public ColoringCatalystSyncPayload payload() {
            return new ColoringCatalystSyncPayload(fluids, blocks);
        }
    }

    private record RawData(Layer fluids, Layer blocks) {
        private static final RawData EMPTY = new RawData(new Layer(false, Map.of()), new Layer(false, Map.of()));
    }

    private static final class Reloader extends SimplePreparableReloadListener<RawData> {
        @Override
        protected RawData prepare(ResourceManager manager, ProfilerFiller profiler) {
            return new RawData(readStack(manager, FLUID_DATA), readStack(manager, BLOCK_DATA));
        }

        @Override
        protected void apply(RawData prepared, ResourceManager manager, ProfilerFiller profiler) {
            pending = prepared;
        }
    }
}
