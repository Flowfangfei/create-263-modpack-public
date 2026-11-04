package plus.dragons.createdragonsplus.common.recipe;

import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

/** Reads the server RecipeManager and the real synchronized client snapshot on the same API. */
public final class CDPRecipeAccess {
    private static final Map<ResourceKey<Recipe<?>>, RecipeHolder<?>> CLIENT_RECIPES = new LinkedHashMap<>();
    private final Level level;
    private CDPRecipeAccess(Level level) { this.level = level; }
    public static CDPRecipeAccess of(Level level) { return new CDPRecipeAccess(level); }
    public static void applyClientSnapshot(boolean reset, List<RecipeHolder<?>> recipes) {
        if (reset) CLIENT_RECIPES.clear();
        recipes.forEach(holder -> CLIENT_RECIPES.put(holder.id(), holder));
    }
    public Collection<RecipeHolder<?>> getRecipes() {
        if (level.recipeAccess() instanceof RecipeManager manager) return manager.getRecipes();
        return List.copyOf(CLIENT_RECIPES.values());
    }
    @SuppressWarnings("unchecked")
    public <R extends Recipe<?>> List<RecipeHolder<R>> getAllRecipesFor(RecipeType<R> type) {
        return getRecipes().stream().filter(holder -> holder.value().getType() == type)
            .map(holder -> (RecipeHolder<R>) holder).toList();
    }
    public <I extends RecipeInput, R extends Recipe<I>> List<RecipeHolder<R>> getRecipesFor(RecipeType<R> type, I input, Level world) {
        return getAllRecipesFor(type).stream().filter(holder -> holder.value().matches(input, world)).toList();
    }
    public <I extends RecipeInput, R extends Recipe<I>> Optional<RecipeHolder<R>> getRecipeFor(RecipeType<R> type, I input, Level world) {
        return getRecipesFor(type, input, world).stream().findFirst();
    }
    public <I extends RecipeInput, R extends Recipe<I>> Optional<RecipeHolder<R>> getRecipeFor(RecipeType<R> type, I input, Level world, ResourceKey<Recipe<?>> previous) {
        var recipe = getAllRecipesFor(type).stream().filter(holder -> holder.id().equals(previous) && holder.value().matches(input, world)).findFirst();
        return recipe.isPresent() ? recipe : getRecipeFor(type, input, world);
    }
    public <I extends RecipeInput, R extends Recipe<I>> Optional<RecipeHolder<R>> getRecipeFor(RecipeType<R> type, I input, Level world, RecipeHolder<R> previous) {
        return previous == null ? getRecipeFor(type, input, world) : getRecipeFor(type, input, world, previous.id());
    }
    public Optional<RecipeHolder<?>> byKey(Identifier id) {
        var key = ResourceKey.create(Registries.RECIPE, id);
        return getRecipes().stream().filter(holder -> holder.id().equals(key)).findFirst();
    }
}
