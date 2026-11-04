package com.simibubi.create.compat.rei;

import com.simibubi.create.compat.rei.animation.AnimatedBlazeBurner;
import com.simibubi.create.compat.rei.animation.AnimatedCrafter;
import com.simibubi.create.compat.rei.animation.AnimatedCrushingWheels;
import com.simibubi.create.compat.rei.animation.AnimatedDeployer;
import com.simibubi.create.compat.rei.animation.AnimatedItemDrain;
import com.simibubi.create.compat.rei.animation.AnimatedKinetics;
import com.simibubi.create.compat.rei.animation.AnimatedMillstone;
import com.simibubi.create.compat.rei.animation.AnimatedMixer;
import com.simibubi.create.compat.rei.animation.AnimatedPress;
import com.simibubi.create.compat.rei.animation.AnimatedSaw;
import com.simibubi.create.compat.rei.animation.AnimatedSpout;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.DataComponentFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public final class AdaptedAnimations {
   public static AnimatedKinetics machine(String type, ProcessingRecipe<?, ?> recipe) {
      return switch (type) {
         case "create:splashing", "create:haunting", "create:fan_smoking", "create:fan_blasting" -> new AnimatedFan(type);
         case "create:deploying" -> new AnimatedDeployer();
         case "create:pressing" -> new AnimatedPress(false);
         case "create:compacting" -> new AnimatedPress(true);
         case "create:mixing" -> new AnimatedMixer();
         case "create:milling" -> new AnimatedMillstone();
         case "create:crushing" -> new AnimatedCrushingWheels();
         case "create:cutting" -> new AnimatedSaw();
         case "create:emptying" -> new AnimatedItemDrain();
         case "create:mechanical_crafting" -> new AnimatedCrafter();
         case "create:filling" -> new AnimatedSpout().withFluids(fluids(recipe));
         default -> null;
      };
   }

   private static List<FluidStack> fluids(ProcessingRecipe<?, ?> recipe) {
      if (recipe == null) {
         return List.of();
      }

      List<FluidStack> fluids = new ArrayList<>();

      for (SizedFluidIngredient ingredient : recipe.getFluidIngredients()) {
         for (Holder<Fluid> fluid : ingredient.ingredient().fluids()) {
            FluidStack stack = new FluidStack(fluid.value(), ingredient.amount());
            if (ingredient.ingredient() instanceof DataComponentFluidIngredient components) {
               stack.applyComponents(components.components().asPatch());
            }

            fluids.add(stack);
         }
      }

      return fluids;
   }

   public static String type(ProcessingRecipe<?, ?> recipe) {
      return BuiltInRegistries.RECIPE_TYPE.getKey(recipe.getType()).toString();
   }

   public static void draw(AnimatedKinetics animation, GuiGraphicsExtractor graphics, int x, int y, int offset) {
      animation.offset = offset;
      animation.draw(graphics, x, y);
   }

   public static void drawHeated(AnimatedKinetics animation, ProcessingRecipe<?, ?> recipe, GuiGraphicsExtractor graphics, int x, int y) {
      graphics.pose().pushMatrix();

      try {
         graphics.pose().translate(x, y).scale(0.65F);
         new AnimatedBlazeBurner().withHeat(recipe.getRequiredHeat().visualizeAsBlazeBurner()).draw(graphics, 0, 21);
         animation.draw(graphics, 0, 0);
      } finally {
         graphics.pose().popMatrix();
      }
   }

   public static void drawScaled(AnimatedKinetics animation, GuiGraphicsExtractor graphics, int x, int y, int offset, float scale) {
      graphics.pose().pushMatrix();

      try {
         graphics.pose().translate(x, y).scale(scale);
         draw(animation, graphics, 0, 0, offset);
      } finally {
         graphics.pose().popMatrix();
      }
   }
}
