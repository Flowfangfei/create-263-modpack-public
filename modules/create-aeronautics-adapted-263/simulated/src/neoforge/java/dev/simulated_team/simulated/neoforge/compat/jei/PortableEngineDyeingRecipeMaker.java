package dev.simulated_team.simulated.neoforge.compat.jei;


import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.index.SimBlocks;
import net.minecraft.core.NonNullList;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.block.Block;

import java.util.Arrays;
import java.util.stream.Stream;

public final class PortableEngineDyeingRecipeMaker {

    // From JEI's ShulkerBoxColoringRecipeMaker
    public static Stream<RecipeHolder<CraftingRecipe>> createRecipes() {
        final String group = "simulated.portable_engine.color";
        final ItemStack base = SimBlocks.PORTABLE_ENGINES.get(DyeColor.RED)
                .asStack();
        final Ingredient baseIngredient = Ingredient.of(base.getItem());

        return Arrays.stream(DyeColor.values())
                .filter(dc -> dc != DyeColor.RED)
                .map(color -> {
                    final TagKey<Item> colorTag = color.getTag();
                    final Ingredient colorIngredient = Ingredient.of(net.minecraft.core.registries.BuiltInRegistries.ITEM.getOrThrow(colorTag));
                    final java.util.List<Ingredient> inputs = java.util.List.of(baseIngredient,colorIngredient);
                    final Block coloredShulkerBox = SimBlocks.PORTABLE_ENGINES.get(color)
                            .get();
                    final ItemStack output = new ItemStack(coloredShulkerBox);
                    final ShapelessRecipe recipe = new ShapelessRecipe(new net.minecraft.world.item.crafting.Recipe.CommonInfo(false), new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC,group), net.minecraft.world.item.ItemStackTemplate.fromNonEmptyStack(output), inputs);
                    return new RecipeHolder<>(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.RECIPE, Simulated.path(group + "/" + color)), recipe);
                });
    }

    private PortableEngineDyeingRecipeMaker() {}

}