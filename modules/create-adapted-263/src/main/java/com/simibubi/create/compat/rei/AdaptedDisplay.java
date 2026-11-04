package com.simibubi.create.compat.rei;

import com.simibubi.create.content.kinetics.crafter.MechanicalCraftingRecipe;
import com.simibubi.create.content.kinetics.deployer.ItemApplicationRecipe;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedRecipe;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.DataComponentFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public final class AdaptedDisplay extends BasicDisplay {
   public final AdaptedReiPlugin.Spec spec;
   public final List<Component> notes = new ArrayList<>();
   public final List<Float> chances = new ArrayList<>();
   public final List<String> steps = new ArrayList<>();
   public final List<ProcessingRecipe<?, ?>> assemblyRecipes = new ArrayList<>();
   public final List<List<EntryIngredient>> stepIngredients = new ArrayList<>();
   public ProcessingRecipe<?, ?> processingRecipe;
   public int loops;
   public int columns = 4;
   public boolean keepHeldItem;

   public static AdaptedDisplay encasing(AdaptedReiPlugin.Spec spec, Block base, Block casing, Block result) {
      Identifier id = Identifier.fromNamespaceAndPath(
         "create", "rei/encasing/" + BuiltInRegistries.BLOCK.getKey(result).getNamespace() + "/" + BuiltInRegistries.BLOCK.getKey(result).getPath()
      );
      AdaptedDisplay display = new AdaptedDisplay(
         spec,
         id,
         List.of(EntryIngredients.of(new ItemStack(base)), EntryIngredients.of(new ItemStack(casing))),
         List.of(EntryIngredients.of(new ItemStack(result)))
      );
      display.keepHeldItem = true;
      return display;
   }

   private AdaptedDisplay(AdaptedReiPlugin.Spec spec, Identifier id, List<EntryIngredient> inputs, List<EntryIngredient> outputs) {
      super(inputs, outputs, Optional.of(id));
      this.spec = spec;
   }

   public CategoryIdentifier<?> getCategoryIdentifier() {
      return this.spec.category();
   }

   public DisplaySerializer<AdaptedDisplay> getSerializer() {
      return null;
   }

   private static EntryIngredient fluid(FluidStack stack) {
      return EntryIngredients.of(dev.architectury.fluid.FluidStack.create(stack.getFluid(), stack.getAmount(), stack.getComponentsPatch()));
   }

   private static void inputs(ProcessingRecipe<?, ?> recipe, List<EntryIngredient> ingredients, int startIndex) {
      for (int index = startIndex; index < recipe.getIngredients().size(); index++) {
         ingredients.add(EntryIngredients.ofIngredient(recipe.getIngredients().get(index)));
      }

      for (SizedFluidIngredient fluidIngredient : recipe.getFluidIngredients()) {
         ingredients.add(
            EntryIngredients.from(
               fluidIngredient.ingredient().fluids(),
               fluidHolder -> {
                  DataComponentPatch components = fluidIngredient.ingredient() instanceof DataComponentFluidIngredient componentIngredient
                     ? componentIngredient.components().asPatch()
                     : DataComponentPatch.EMPTY;
                  return EntryStacks.of(dev.architectury.fluid.FluidStack.create(fluidHolder, fluidIngredient.amount(), components));
               }
            )
         );
      }
   }

   public static AdaptedDisplay processing(AdaptedReiPlugin.Spec spec, Identifier id, ProcessingRecipe<?, ?> recipe) {
      List<EntryIngredient> inputs = new ArrayList<>();
      List<EntryIngredient> outputs = new ArrayList<>();
      inputs(recipe, inputs, 0);

      for (ProcessingOutput result : recipe.getRollableResults()) {
         outputs.add(EntryIngredients.of(result.getStack()));
      }

      for (FluidStack fluidResult : recipe.getFluidResults()) {
         outputs.add(fluid(fluidResult));
      }

      AdaptedDisplay display = new AdaptedDisplay(spec, id, inputs, outputs);
      display.processingRecipe = recipe;
      if (recipe instanceof ItemApplicationRecipe application) {
         display.keepHeldItem = application.shouldKeepHeldItem();
      }

      recipe.getRollableResults().forEach(result -> display.chances.add(result.getChance()));
      if (recipe.getProcessingDuration() > 0) {
         display.notes.add(Component.literal(recipe.getProcessingDuration() + " ticks"));
      }

      if (recipe.getRequiredHeat() != HeatCondition.NONE) {
         display.notes.add(Component.translatable("create." + recipe.getRequiredHeat().getTranslationKey()));
      }

      for (String method : List.of("getEnergy", "getBurnTime")) {
         try {
            Object value = recipe.getClass().getMethod(method).invoke(recipe);
            display.notes.add(Component.literal(value + (method.equals("getEnergy") ? " FE" : " ticks of fuel")));
         } catch (NoSuchMethodException missingMethod) {
         } catch (ReflectiveOperationException reflectionError) {
            throw new IllegalStateException(reflectionError);
         }
      }

      try {
         if (Boolean.TRUE.equals(recipe.getClass().getMethod("isSuperheated").invoke(recipe))) {
            display.notes.add(Component.literal("Superheated"));
         }
      } catch (NoSuchMethodException missingMethod) {
      } catch (ReflectiveOperationException reflectionError) {
         throw new IllegalStateException(reflectionError);
      }

      return display;
   }

   public static AdaptedDisplay assembly(AdaptedReiPlugin.Spec spec, Identifier id, SequencedAssemblyRecipe recipe) {
      List<EntryIngredient> inputs = new ArrayList<>();
      List<EntryIngredient> outputs = new ArrayList<>();
      inputs.add(EntryIngredients.ofIngredient(recipe.getIngredient()));

      for (SequencedRecipe<?> step : recipe.getSequence()) {
         inputs(step.getRecipe(), inputs, 1);
      }

      for (ProcessingOutput result : recipe.resultPool) {
         outputs.add(EntryIngredients.of(result.getStack()));
      }

      AdaptedDisplay display = new AdaptedDisplay(spec, id, inputs, outputs);
      display.loops = recipe.getLoops();

      for (SequencedRecipe<?> step : recipe.getSequence()) {
         display.assemblyRecipes.add(step.getRecipe());
         List<EntryIngredient> stepInputs = new ArrayList<>();
         inputs(step.getRecipe(), stepInputs, 1);
         display.stepIngredients.add(stepInputs);
      }

      float totalChance = 0.0F;

      for (ProcessingOutput result : recipe.resultPool) {
         totalChance += result.getChance();
      }

      for (ProcessingOutput result : recipe.resultPool) {
         display.chances.add(totalChance == 0.0F ? 0.0F : result.getChance() / totalChance);
      }

      display.notes.add(Component.literal("Repeat sequence " + recipe.getLoops() + " times"));

      for (SequencedRecipe<?> step : recipe.getSequence()) {
         Identifier recipeType = BuiltInRegistries.RECIPE_TYPE.getKey(step.getRecipe().getType());
         display.steps
            .add(AdaptedReiPlugin.SPECS.stream().filter(candidate -> candidate.id().equals(recipeType)).map(AdaptedReiPlugin.Spec::title).findFirst().orElse(recipeType.toString()));
      }

      return display;
   }

   public static AdaptedDisplay crafting(AdaptedReiPlugin.Spec spec, Identifier id, MechanicalCraftingRecipe recipe) {
      ShapedRecipePattern pattern = recipe.getPattern();
      List<EntryIngredient> inputs = new ArrayList<>();

      for (Optional<Ingredient> ingredient : pattern.ingredients()) {
         inputs.add(ingredient.map(EntryIngredients::ofIngredient).orElseGet(EntryIngredient::empty));
      }

      AdaptedDisplay display = new AdaptedDisplay(spec, id, inputs, List.of(EntryIngredients.of(recipe.getResultStack())));
      display.columns = pattern.width();
      return display;
   }

   public static AdaptedDisplay cooking(AdaptedReiPlugin.Spec spec, Identifier id, AbstractCookingRecipe recipe, List<SmokingRecipe> smokingRecipes) {
      EntryIngredient ingredient = EntryIngredients.ofIngredient(recipe.input());
      List<EntryStack<?>> alternatives = ingredient.stream().filter(entry -> {
         if (!spec.type().equals("create:fan_blasting")) {
            return true;
         }

         ItemStack stack = (ItemStack)entry.getValue();
         SingleRecipeInput cookingInput = new SingleRecipeInput(stack);
         return smokingRecipes.stream().noneMatch(smoking -> smoking.input().test(stack) && ItemStack.isSameItem(smoking.assemble(cookingInput), recipe.assemble(cookingInput)));
      }).toList();
      ItemStack firstStack = alternatives.isEmpty() ? ItemStack.EMPTY : (ItemStack)alternatives.getFirst().getValue();
      return new AdaptedDisplay(spec, id, List.of(EntryIngredient.of(alternatives)), List.of(EntryIngredients.of(recipe.assemble(new SingleRecipeInput(firstStack)))));
   }
}
