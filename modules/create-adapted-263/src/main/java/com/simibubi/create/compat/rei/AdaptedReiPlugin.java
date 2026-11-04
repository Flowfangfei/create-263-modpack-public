package com.simibubi.create.compat.rei;

import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.decoration.encasing.EncasedBlock;
import com.simibubi.create.content.decoration.encasing.EncasingRegistry;
import com.simibubi.create.content.kinetics.crafter.MechanicalCraftingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import java.util.ArrayList;
import java.util.List;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.screen.DisplayBoundsProvider;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.forge.REIPluginClient;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.block.Block;
import org.apache.logging.log4j.LogManager;

@REIPluginClient
public final class AdaptedReiPlugin implements REIClientPlugin {
   public static final List<Spec> SPECS = List.of(
      new Spec("create:conversion", "Conversion", "create:mechanical_crafter"),
      new Spec("create:crushing", "Crushing", "create:crushing_wheel"),
      new Spec("create:cutting", "Sawing", "create:mechanical_saw"),
      new Spec("create:milling", "Milling", "create:millstone"),
      new Spec("create:mixing", "Mixing", "create:mechanical_mixer"),
      new Spec("create:compacting", "Compacting", "create:mechanical_press"),
      new Spec("create:pressing", "Pressing", "create:mechanical_press"),
      new Spec("create:sandpaper_polishing", "Sandpaper Polishing", "create:sand_paper"),
      new Spec("create:splashing", "Bulk Washing", "create:encased_fan"),
      new Spec("create:haunting", "Bulk Haunting", "create:encased_fan"),
      new Spec("create:fan_smoking", "Bulk Smoking", "create:encased_fan"),
      new Spec("create:fan_blasting", "Bulk Blasting", "create:encased_fan"),
      new Spec("create:deploying", "Deploying", "create:deployer"),
      new Spec("create:filling", "Filling", "create:spout"),
      new Spec("create:emptying", "Emptying", "create:item_drain"),
      new Spec("create:item_application", "Item Application", "create:brass_hand"),
      new Spec("create:sequenced_assembly", "Sequenced Assembly", "create:precision_mechanism"),
      new Spec("create:mechanical_crafting", "Mechanical Crafting", "create:mechanical_crafter"),
      new Spec("createaddition:rolling", "Rolling", "createaddition:rolling_mill"),
      new Spec("createaddition:charging", "Charging", "createaddition:tesla_coil"),
      new Spec("createaddition:liquid_burning", "Liquid Fuel", "create:blaze_burner")
   );

   public void registerScreens(ScreenRegistry registry) {
      registry.registerDecider(new DisplayBoundsProvider<AbstractSimiContainerScreen<?>>() {
         @Override
         public <R extends Screen> boolean isHandingScreen(Class<R> screenClass) {
            return AbstractSimiContainerScreen.class.isAssignableFrom(screenClass);
         }
         @Override public double getPriority() { return 100; }
         @Override
         public Rectangle getScreenBounds(AbstractSimiContainerScreen<?> screen) {
            return new Rectangle(screen.getLeftPos(), screen.getTopPos(), screen.getImageWidth(), screen.getImageHeight());
         }
      });
   }

   public void registerExclusionZones(ExclusionZones registry) {
      registry.register(AbstractSimiContainerScreen.class, AdaptedReiPlugin::exclusionAreas);
   }

   private static List<Rectangle> exclusionAreas(AbstractSimiContainerScreen<?> screen) {
      List<Rectangle> areas = new ArrayList<>();
      areas.add(new Rectangle(screen.getLeftPos(), screen.getTopPos(), screen.getImageWidth(), screen.getImageHeight()));
      for (Rect2i area : screen.getExtraAreas())
         areas.add(new Rectangle(area.getX(), area.getY(), area.getWidth(), area.getHeight()));
      return areas;
   }

   public void registerCategories(CategoryRegistry registry) {
      for (Spec spec : SPECS) {
         if (BuiltInRegistries.RECIPE_TYPE.containsKey(spec.id()) || spec.type().startsWith("create:fan_")) {
            registry.add(new AdaptedCategory(spec));
            Item machine = BuiltInRegistries.ITEM.getValue(Identifier.parse(spec.machine()));
            if (machine != null && machine != Items.AIR) {
               registry.addWorkstations(spec.category(), EntryStacks.of(machine));
            }
         }
      }
   }

   public void registerDisplays(DisplayRegistry registry) {
      int count = 0;
      List<RecipeHolder<?>> recipes = AdaptedReiCompat.recipes();
      List<SmokingRecipe> smokingRecipes = recipes.stream()
         .filter(AllRecipeTypes.CAN_BE_AUTOMATED)
         .map(RecipeHolder::value)
         .filter(SmokingRecipe.class::isInstance)
         .map(SmokingRecipe.class::cast)
         .toList();

      for (RecipeHolder<?> holder : recipes) {
         Recipe<?> recipe = holder.value();
         Identifier type = recipe instanceof MechanicalCraftingRecipe
            ? Identifier.parse("create:mechanical_crafting")
            : (
               recipe instanceof SmokingRecipe
                  ? Identifier.parse("create:fan_smoking")
                  : (
                     !(recipe instanceof SmeltingRecipe) && !(recipe instanceof BlastingRecipe)
                        ? BuiltInRegistries.RECIPE_TYPE.getKey(recipe.getType())
                        : Identifier.parse("create:fan_blasting")
                  )
            );
         Spec spec = SPECS.stream().filter(candidate -> candidate.id().equals(type)).findFirst().orElse(null);
         if (spec != null) {
            if (recipe instanceof ProcessingRecipe<?, ?> processing) {
               registry.add(AdaptedDisplay.processing(spec, holder.id().identifier(), processing), holder);
            } else if (recipe instanceof SequencedAssemblyRecipe assembly) {
               registry.add(AdaptedDisplay.assembly(spec, holder.id().identifier(), assembly), holder);
            } else if (recipe instanceof MechanicalCraftingRecipe crafting) {
               registry.add(AdaptedDisplay.crafting(spec, holder.id().identifier(), crafting), holder);
            } else {
               if (!(recipe instanceof AbstractCookingRecipe cooking) || !AllRecipeTypes.CAN_BE_AUTOMATED.test(holder)) {
                  continue;
               }

               AdaptedDisplay display = AdaptedDisplay.cooking(spec, holder.id().identifier(), cooking, smokingRecipes);
               if (display.getInputEntries().getFirst().isEmpty()) {
                  continue;
               }

               registry.add(display, holder);
            }

            count++;
         }
      }

      Spec manualSpec = SPECS.stream().filter(candidate -> candidate.type().equals("create:item_application")).findFirst().orElseThrow();

      for (Block base : BuiltInRegistries.BLOCK) {
         if (base.asItem() != Items.AIR) {
            for (Block result : EncasingRegistry.getVariants(base)) {
               if (result instanceof EncasedBlock encased && result.asItem() != Items.AIR && encased.getCasing().asItem() != Items.AIR) {
                  registry.add(AdaptedDisplay.encasing(manualSpec, base, encased.getCasing(), result));
                  count++;
               }
            }
         }
      }

      LogManager.getLogger("Create Adapted REI").info("Registered {} synchronized and encasing recipes", count);
   }

   public record Spec(String type, String title, String machine) {
      public Identifier id() {
         return Identifier.parse(this.type);
      }

      public CategoryIdentifier<AdaptedDisplay> category() {
         return CategoryIdentifier.of(this.id());
      }

      public Component name() {
         return Component.translatableWithFallback(this.id().getNamespace() + ".recipe." + this.id().getPath(), this.title);
      }
   }
}
