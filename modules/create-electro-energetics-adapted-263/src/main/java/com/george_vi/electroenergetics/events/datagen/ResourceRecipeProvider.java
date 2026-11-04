package com.george_vi.electroenergetics.events.datagen;

import com.george_vi.electroenergetics.CreateElectroEnergetics;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/** The checked-in recipe JSON is the editable source of truth for Fabric data generation. */
public abstract class ResourceRecipeProvider implements DataProvider {
    private final PackOutput output;
    private final List<String> folders;

    protected ResourceRecipeProvider(PackOutput output, String... folders) {
        this.output = output;
        this.folders = List.of(folders);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Path root = FabricLoader.getInstance().getModContainer(CreateElectroEnergetics.ID).orElseThrow()
            .findPath("data/" + CreateElectroEnergetics.ID + "/recipe").orElseThrow();
        PackOutput.PathProvider paths = output.createRegistryElementsPathProvider(Registries.RECIPE);
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (String folder : folders) {
            Path directory = root.resolve(folder);
            if (!Files.isDirectory(directory)) continue;
            try (var stream = Files.walk(directory)) {
                for (Path source : stream.filter(path -> path.toString().endsWith(".json")).toList()) {
                    String name = root.relativize(source).toString().replace('\\', '/');
                    Identifier id = CreateElectroEnergetics.rl(name.substring(0, name.length() - 5));
                    try (Reader reader = Files.newBufferedReader(source, StandardCharsets.UTF_8)) {
                        JsonElement recipe = JsonParser.parseReader(reader);
                        futures.add(DataProvider.saveStable(cache, recipe, paths.json(id)));
                    }
                }
            } catch (IOException exception) {
                return CompletableFuture.failedFuture(exception);
            }
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override public String getName() { return "CEE recipes: " + String.join(", ", folders); }
}
