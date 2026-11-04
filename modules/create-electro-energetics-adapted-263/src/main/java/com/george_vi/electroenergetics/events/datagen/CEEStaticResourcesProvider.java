package com.george_vi.electroenergetics.events.datagen;

import com.george_vi.electroenergetics.CEEDamageTypes;
import com.george_vi.electroenergetics.CreateElectroEnergetics;
import com.google.common.hash.Hashing;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/** Keeps the checked-in resource sources in the generator's cache while other providers regenerate their files. */
public final class CEEStaticResourcesProvider implements DataProvider {
    private static final List<String> RECIPE_FOLDERS = List.of("crafting", "stonecutting", "sequenced_assembly",
        "mixing", "compacting", "mechanical_crafting");
    private final Path output;

    public CEEStaticResourcesProvider(PackOutput output) {
        this.output = output.getOutputFolder();
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        if (!Files.isDirectory(output)) return CompletableFuture.completedFuture(null);
        Set<String> regenerated = new HashSet<>();
        for (CEEAdvancement advancement : CEEAdvancements.ENTRIES)
            regenerated.add("data/" + CreateElectroEnergetics.ID + "/advancement/" + advancement.getId() + ".json");
        for (var damage : List.of(CEEDamageTypes.ELECTROCUTION, CEEDamageTypes.HV_ELECTROCUTION))
            regenerated.add("data/" + damage.identifier().getNamespace() + "/damage_type/" + damage.identifier().getPath() + ".json");
        String recipeRoot = "data/" + CreateElectroEnergetics.ID + "/recipe/";
        try (var files = Files.walk(output)) {
            for (Path file : files.filter(Files::isRegularFile).toList()) {
                String path = output.relativize(file).toString().replace('\\', '/');
                if (path.startsWith(".cache/") || regenerated.contains(path)
                    || RECIPE_FOLDERS.stream().anyMatch(folder -> path.startsWith(recipeRoot + folder + "/"))) continue;
                byte[] contents = Files.readAllBytes(file);
                cache.writeIfNeeded(file, contents, Hashing.sha1().hashBytes(contents));
            }
            return CompletableFuture.completedFuture(null);
        } catch (IOException exception) {
            return CompletableFuture.failedFuture(exception);
        }
    }

    @Override public String getName() { return "CEE checked-in static resources"; }
}
