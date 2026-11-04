package com.forsteri.createliquidfuel.core;

import com.forsteri.createliquidfuel.CreateLiquidFuel;
import com.forsteri.createliquidfuel.util.Triplet;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.IdentifierException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

/**
 * One-version compatibility loader for {@code data/<namespace>/compat/*.json}.
 * New packs should use the {@code createliquidfuel:liquid_fuel} data map.
 */
public class LiquidBurnerFuelJsonLoader extends SimpleJsonResourceReloadListener<JsonElement> {
    public static final Identifier IDENTIFIER = Identifier.fromNamespaceAndPath("createliquidfuel", "drainable_fuel_loader");

    public static final LiquidBurnerFuelJsonLoader INSTANCE = new LiquidBurnerFuelJsonLoader();

    public LiquidBurnerFuelJsonLoader() {
        super(ExtraCodecs.JSON, FileToIdConverter.json("compat"));
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> entries, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        BurnerStomachHandler.LIQUID_BURNER_FUEL_MAP.entrySet()
                .removeIf(entry -> IDENTIFIER.equals(entry.getValue().getFirst()));

        for (Map.Entry<Identifier, JsonElement> entry : entries.entrySet()) {
            JsonElement element = entry.getValue();
            if (!element.isJsonObject()) {
                continue;
            }

            Identifier id = entry.getKey();
            JsonObject object = element.getAsJsonObject();
            JsonElement fluidElement = object.get("fluid");

            if (fluidElement == null) {
                CreateLiquidFuel.LOGGER.warn("Skipping {}: not a liquid burner fuel definition (no \"fluid\" field)", id);
                continue;
            }

            final Fluid fluid;
            try {
                Optional<Fluid> value = BuiltInRegistries.FLUID.getOptional(Identifier.parse(fluidElement.getAsString()));
                if (value.isEmpty()) {
                    CreateLiquidFuel.LOGGER.warn("Skipping liquid burner fuel {}: unknown fluid {}", id, fluidElement.getAsString());
                    continue;
                }
                fluid = value.get();
            } catch (IdentifierException e) {
                CreateLiquidFuel.LOGGER.warn("Skipping liquid burner fuel {}: invalid fluid {}", id, fluidElement.getAsString());
                continue;
            }

            CreateLiquidFuel.LOGGER.warn(
                    "Liquid burner fuel {} is using the deprecated data/<namespace>/compat/ format. Move it to data/createliquidfuel/data_maps/fluid/liquid_fuel.json",
                    id
            );

            boolean superHeat = object.has("superHeat") && object.get("superHeat").getAsBoolean();
            int burnTime = object.has("burnTime")
                    ? object.get("burnTime").getAsInt()
                    : superHeat ? 32 : 20;
            int amountConsumedPerTick = object.has("amountConsumedPerTick")
                    ? object.get("amountConsumedPerTick").getAsInt()
                    : superHeat ? 10 : 1;

            if (burnTime <= 0 || amountConsumedPerTick <= 0) {
                CreateLiquidFuel.LOGGER.warn("Skipping liquid burner fuel {}: fuel values must be positive", id);
                continue;
            }
            BurnerStomachHandler.LIQUID_BURNER_FUEL_MAP.put(
                    fluid,
                    Pair.of(IDENTIFIER, Triplet.of(burnTime, superHeat, amountConsumedPerTick))
            );
        }
    }
}
