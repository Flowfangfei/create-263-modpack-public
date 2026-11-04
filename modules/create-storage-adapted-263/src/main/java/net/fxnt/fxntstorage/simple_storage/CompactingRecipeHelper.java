package net.fxnt.fxntstorage.simple_storage;

import net.fxnt.fxntstorage.FXNTStorage;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CompactingRecipeHelper {

    private static final int MAX_TIERS = 3;

    private static final Map<Level, Map<Item, Optional<CompactingChain>>> SERVER_CACHE = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<Item, Optional<CompactingChain>> CLIENT_CACHE = new ConcurrentHashMap<>();
    private static final Map<Item, Long> CLIENT_REQUESTS = new ConcurrentHashMap<>();
    private static volatile boolean built = false;

    private record Step(Item item, int ratio) {}

    public static void rebuild() {
        CLIENT_CACHE.clear();
        CLIENT_REQUESTS.clear();
        SERVER_CACHE.clear();
        built = true;
    }

    public static boolean isEmpty() {
        return !built;
    }

    @Nullable
    public static CompactingChain buildChain(Level level, Item filterItem) {
        if (level.isClientSide()) {
            Optional<CompactingChain> cached = CLIENT_CACHE.get(filterItem);
            if (cached != null) return cached.orElse(null);
            long now = System.currentTimeMillis();
            Long previous = CLIENT_REQUESTS.putIfAbsent(filterItem, now);
            if (previous == null || now - previous >= 2000) {
                CLIENT_REQUESTS.put(filterItem, now);
                net.createmod.catnip.api.client.network.ClientNetworkHelper.INSTANCE.sendToServer(
                        net.fxnt.fxntstorage.network.packet.CompactingChainSyncPacket.request(filterItem));
            }
            return null;
        }
        Map<Item, Optional<CompactingChain>> cache = SERVER_CACHE.computeIfAbsent(level, ignored -> new ConcurrentHashMap<>());
        Optional<CompactingChain> cached = cache.get(filterItem);
        if (cached != null) return cached.orElse(null);
        CompactingChain chain = resolveChain(level, filterItem);
        cache.putIfAbsent(filterItem, Optional.ofNullable(chain));
        FXNTStorage.LOGGER.debug("Compacting recipe chain has been rebuilt!");
        return chain;
    }

    @Nullable
    private static CompactingChain resolveChain(Level level, Item filterItem) {
        List<Item> items = new ArrayList<>();
        List<Integer> ratios = new ArrayList<>();
        Set<Item> seen = new HashSet<>();
        items.add(filterItem);
        seen.add(filterItem);

        Item cursor = filterItem;
        while (items.size() < MAX_TIERS) {
            Step up = findUpperTier(level, cursor);
            if (up == null || !seen.add(up.item())) break;
            items.addLast(up.item());
            ratios.addLast(up.ratio());
            cursor = up.item();
        }

        cursor = filterItem;
        while (items.size() < MAX_TIERS) {
            Step down = findLowerTier(level, cursor);
            if (down == null || !seen.add(down.item())) break;
            items.addFirst(down.item());
            ratios.addFirst(down.ratio());
            cursor = down.item();
        }

        if (items.size() < 2) return null;

        Item t2 = items.size() > 2 ? items.get(2) : null;
        int t1ToT2 = items.size() > 2 ? ratios.get(1) : 1;
        Set<Item> t0Variants = lowerTierVariants(level, items, ratios, 0);
        Set<Item> t1Variants = t2 != null ? lowerTierVariants(level, items, ratios, 1) : Set.of();
        return new CompactingChain(items.get(0), items.get(1), ratios.get(0), t2, t1ToT2, t0Variants, t1Variants);
    }

    // What a full grid of this item crafts into, 3x3 preferred over 2x2.
    @Nullable
    private static Step findUpperTier(Level level, Item item) {
        Step step = matchSquare(level, item, 3);
        return step != null ? step : matchSquare(level, item, 2);
    }

    @Nullable
    private static Step matchSquare(Level level, Item item, int size) {
        int ratio = size * size;
        CraftingInput input = filledGrid(item, size, size);

        List<Item> candidates = new ArrayList<>();
        for (RecipeHolder<CraftingRecipe> holder : matchingRecipes(level, input)) {
            ItemStack result = holder.value().assemble(input);
            if (result.isEmpty() || result.getCount() != 1 || result.getItem() == item) continue;

            // Every slot holds our item, so any ingredient accepts it; the first one stands for the rest.
            List<Ingredient> ingredients = ingredients(holder.value());
            if (ingredients.isEmpty()) continue;
            if (!trustworthy(level, ingredients.getFirst(), result.getItem(), ratio)) continue;

            if (!candidates.contains(result.getItem())) candidates.add(result.getItem());
        }

        return pick(candidates, item, ratio);
    }

    // Other items that pack into the tier above at the same ratio, so they can stand in for this tier
    private static Set<Item> lowerTierVariants(Level level, List<Item> items, List<Integer> ratios, int tier) {
        LowerTierCandidates found = lowerTierCandidates(level, items.get(tier + 1));
        if (found.ratio() != ratios.get(tier)) return Set.of();

        Set<Item> variants = new HashSet<>(found.items());
        items.forEach(variants::remove); // never let a variant shadow another tier of the chain
        return Set.copyOf(variants);
    }

    // What this item is a packed form of, found by scanning for recipes that produce it
    @Nullable
    private static Step findLowerTier(Level level, Item item) {
        LowerTierCandidates found = lowerTierCandidates(level, item);
        return pick(found.items(), item, found.ratio());
    }

    private record LowerTierCandidates(List<Item> items, int ratio) {}

    private static LowerTierCandidates lowerTierCandidates(Level level, Item item) {
        List<Item> candidates = new ArrayList<>();
        int bestRatio = 0;

        for (RecipeHolder<CraftingRecipe> holder : craftingRecipes(level)) {
            CraftingRecipe recipe = holder.value();
            ItemStack result = displayResult(recipe, level);
            if (result.isEmpty() || result.getCount() != 1 || result.getItem() != item) continue;

            List<Ingredient> ingredients = ingredients(recipe);
            int ratio = ingredients.size();
            // Mirrors the 3x3-before-2x2 preference on the way up
            if ((ratio != 4 && ratio != 9) || ratio < bestRatio) continue;

            List<Item> variants = uniformIngredientItems(ingredients);
            variants.remove(item);
            if (variants.isEmpty()) continue;
            if (!trustworthy(level, ingredients.getFirst(), item, ratio)) continue;

            if (ratio > bestRatio) {
                candidates.clear();
                bestRatio = ratio;
            }
            for (Item variant : variants) {
                if (!candidates.contains(variant)) candidates.add(variant);
            }
        }

        return new LowerTierCandidates(candidates, bestRatio);
    }

    private static List<Item> uniformIngredientItems(List<Ingredient> ingredients) {
        List<Item> result = new ArrayList<>();
        // Ingredient.EMPTY resolves to nothing, so shaped recipes with holes drop out here
        for (ItemStack reference : ingredients.getFirst().items().map(holder -> new ItemStack(holder.value())).toList()) {
            if (reference.isEmpty()) continue;

            boolean acceptedEverywhere = true;
            for (int i = 1; i < ingredients.size(); i++) {
                if (!ingredients.get(i).test(reference)) {
                    acceptedEverywhere = false;
                    break;
                }
            }

            if (acceptedEverywhere && !result.contains(reference.getItem())) result.add(reference.getItem());
        }
        return result;
    }

    private static boolean trustworthy(Level level, Ingredient ingredient, Item packed, int count) {
        if (ingredient.items().count() <= 1) return true;

        CraftingInput input = filledGrid(packed, 1, 1);
        for (RecipeHolder<CraftingRecipe> holder : matchingRecipes(level, input)) {
            ItemStack result = holder.value().assemble(input);
            if (result.getCount() == count && ingredient.test(result)) return true;
        }
        return false;
    }

    public static void receiveClientChain(Item filter, @Nullable CompactingChain chain) {
        CLIENT_CACHE.put(filter, Optional.ofNullable(chain));
        CLIENT_REQUESTS.remove(filter);
        built = true;
    }

    @SuppressWarnings("unchecked")
    private static List<RecipeHolder<CraftingRecipe>> craftingRecipes(Level level) {
        if (!(level.recipeAccess() instanceof RecipeManager manager)) return List.of();
        List<RecipeHolder<CraftingRecipe>> recipes = new ArrayList<>();
        for (RecipeHolder<?> holder : manager.getRecipes())
            if (holder.value() instanceof CraftingRecipe) recipes.add((RecipeHolder<CraftingRecipe>) (RecipeHolder<?>) holder);
        return recipes;
    }

    private static List<RecipeHolder<CraftingRecipe>> matchingRecipes(Level level, CraftingInput input) {
        return craftingRecipes(level).stream().filter(holder -> holder.value().matches(input, level)).toList();
    }

    private static List<Ingredient> ingredients(CraftingRecipe recipe) {
        if (recipe instanceof ShapedRecipe shaped) {
            if (shaped.getIngredients().stream().anyMatch(Optional::isEmpty)) return List.of();
            return shaped.getIngredients().stream().map(Optional::orElseThrow).toList();
        }
        return recipe.placementInfo().ingredients();
    }

    private static ItemStack displayResult(CraftingRecipe recipe, Level level) {
        var displays = recipe.display();
        return displays.isEmpty() ? ItemStack.EMPTY : displays.getFirst().result().resolveForFirstStack(
                net.minecraft.world.item.crafting.display.SlotDisplayContext.fromLevel(level));
    }

    // Prefer a candidate from the same mod as the item being resolved, then lowest id
    @Nullable
    private static Step pick(List<Item> candidates, Item reference, int ratio) {
        if (candidates.isEmpty() || ratio <= 0) return null;

        String preferred = idOf(reference).getNamespace();
        Item best = candidates.stream()
                .min(Comparator.comparingInt((Item candidate) -> idOf(candidate).getNamespace().equals(preferred) ? 0 : 1)
                        .thenComparing(candidate -> idOf(candidate).toString()))
                .orElseThrow();
        return new Step(best, ratio);
    }

    private static Identifier idOf(Item item) {
        return BuiltInRegistries.ITEM.getKey(item);
    }

    private static CraftingInput filledGrid(Item item, int width, int height) {
        List<ItemStack> slots = new ArrayList<>(width * height);
        for (int i = 0; i < width * height; i++) slots.add(new ItemStack(item));
        return CraftingInput.of(width, height, slots);
    }
}
