package com.simibubi.create.compat.rei;

import com.simibubi.create.AllItems;
import com.simibubi.create.compat.rei.animation.AnimatedKinetics;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.sequenced.IAssemblyRecipe;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2f;

public final class AdaptedCategory implements DisplayCategory<AdaptedDisplay> {
   final AdaptedReiPlugin.Spec spec;

   public AdaptedCategory(AdaptedReiPlugin.Spec spec) {
      this.spec = spec;
   }

   public CategoryIdentifier<AdaptedDisplay> getCategoryIdentifier() {
      return this.spec.category();
   }

   public Component getTitle() {
      return this.spec.name();
   }

   public Renderer getIcon() {
      return EntryStacks.of(BuiltInRegistries.ITEM.getValue(Identifier.parse(this.spec.machine())));
   }

   public int getDisplayWidth(AdaptedDisplay display) {
      return 185;
   }

   private int contentHeight() {
      return !this.spec.type().equals("create:sequenced_assembly")
            && !this.spec.type().equals("create:mechanical_crafting")
            && AdaptedAnimations.machine(this.spec.type(), null) == null
         ? 130
         : 140;
   }

   public int getDisplayHeight() {
      return Math.min(this.contentHeight(), Math.max(100, Minecraft.getInstance().getWindow().getGuiScaledHeight() - 110));
   }

   public List<Widget> setupDisplay(AdaptedDisplay display, Rectangle bounds) {
      int width = Math.max(185, display.columns * 18 + 75);
      Rectangle contentBounds = new Rectangle(bounds.x, bounds.y, width, this.contentHeight());
      List<Widget> widgets = this.setupContent(display, contentBounds);
      if (width == bounds.width && contentBounds.height == bounds.height) {
         return widgets;
      }

      Matrix3x2f transform = new Matrix3x2f()
         .translate(bounds.x, bounds.y)
         .scale((float)bounds.width / width, (float)bounds.height / contentBounds.height)
         .translate(-bounds.x, -bounds.y);
      return widgets.stream().<Widget>map(widget -> Widgets.withTranslate(widget, transform)).toList();
   }

   private List<Widget> setupContent(AdaptedDisplay display, Rectangle bounds) {
      List<Widget> widgets = new ArrayList<>();
      widgets.add(Widgets.createRecipeBase(bounds));
      if (this.spec.type().equals("create:item_application")) {
         this.manual(display, bounds, widgets);
         return widgets;
      }

      if (!display.assemblyRecipes.isEmpty()) {
         this.assembly(display, bounds, widgets);
         return widgets;
      }

      int arrowX = display.columns * 18 + 16;
      widgets.add(Widgets.createArrow(new Point(bounds.x + arrowX, bounds.y + 35)));

      for (int index = 0; index < display.getInputEntries().size(); index++) {
         widgets.add(
            Widgets.createSlot(new Point(bounds.x + 8 + index % display.columns * 18, bounds.y + 20 + index / display.columns * 18))
               .entries(display.getInputEntries().get(index))
               .markInput()
         );
      }

      for (int outputIndex = 0; outputIndex < display.getOutputEntries().size(); outputIndex++) {
         int outputX = bounds.x + arrowX + 35 + outputIndex % 3 * 18;
         int outputY = bounds.y + 20 + outputIndex / 3 * 18;
         widgets.add(Widgets.createSlot(new Point(outputX, outputY)).entries(display.getOutputEntries().get(outputIndex)).markOutput());
         if (outputIndex < display.chances.size() && display.chances.get(outputIndex) < 1.0F) {
            widgets.add(
               Widgets.createTooltip(
                  new Rectangle(outputX, outputY, 18, 18),
                  new Component[]{Component.literal(String.format(Locale.ROOT, "Chance: %.2f%%", display.chances.get(outputIndex) * 100.0F))}
               )
            );
         }
      }

      AnimatedKinetics animation = AdaptedAnimations.machine(this.spec.type(), display.processingRecipe);
      if (animation != null) {
         boolean heated = display.processingRecipe != null
            && display.processingRecipe.getRequiredHeat() != HeatCondition.NONE
            && (this.spec.type().equals("create:mixing") || this.spec.type().equals("create:compacting"));
         int machineX = bounds.x
            + (this.spec.type().equals("create:mechanical_crafting") ? arrowX + 47 : bounds.width / 2 - (this.spec.type().equals("create:crushing") ? 34 : 12));
         int machineY = bounds.y
            + (heated ? 59 : (this.spec.type().equals("create:crushing") ? 75 : (this.spec.type().equals("create:mechanical_crafting") ? 78 : 65)));
         widgets.add(Widgets.createDrawableWidget((graphics, mouseX, mouseY, delta) -> {
            if (heated) {
               AdaptedAnimations.drawHeated(animation, display.processingRecipe, graphics, machineX, machineY);
            } else {
               AdaptedAnimations.drawScaled(animation, graphics, machineX, machineY, 0, 0.75F);
            }
         }));
      }

      int notesY = bounds.y + (animation == null ? 83 : 119);

      for (Component note : display.notes) {
         widgets.add(Widgets.createLabel(new Point(bounds.x + bounds.width / 2, notesY), note).centered().noShadow().color(-12566464, -3355444));
         notesY += 12;
      }

      for (int stepIndex = 0; stepIndex < display.steps.size(); stepIndex++) {
         widgets.add(
            Widgets.createLabel(new Point(bounds.x + 12, notesY), Component.literal(stepIndex + 1 + ". " + display.steps.get(stepIndex)))
               .leftAligned()
               .noShadow()
               .color(-12566464, -3355444)
         );
         notesY += 14;
      }

      return widgets;
   }

   private void manual(AdaptedDisplay display, Rectangle bounds, List<Widget> widgets) {
      List<EntryIngredient> inputs = display.getInputEntries();

      for (int index = 0; index < inputs.size(); index++) {
         widgets.add(Widgets.createSlot(new Point(bounds.x + (index == 0 ? 22 : 70), bounds.y + (index == 0 ? 57 : 12))).entries(inputs.get(index)).markInput());
      }

      widgets.add(Widgets.createArrow(new Point(bounds.x + 91, bounds.y + 57)));

      for (int outputIndex = 0; outputIndex < display.getOutputEntries().size(); outputIndex++) {
         int outputX = bounds.x + 127 + outputIndex % 2 * 18;
         int outputY = bounds.y + 57 + outputIndex / 2 * 18;
         widgets.add(Widgets.createSlot(new Point(outputX, outputY)).entries(display.getOutputEntries().get(outputIndex)).markOutput());
         if (outputIndex < display.chances.size() && display.chances.get(outputIndex) < 1.0F) {
            widgets.add(
               Widgets.createTooltip(
                  new Rectangle(outputX, outputY, 18, 18),
                  new Component[]{Component.literal(String.format(Locale.ROOT, "Chance: %.2f%%", display.chances.get(outputIndex) * 100.0F))}
               )
            );
         }
      }

      widgets.add(
         Widgets.createSlot(new Point(bounds.x + 70, bounds.y + 39)).entries(EntryIngredient.of(EntryStacks.of(AllItems.BRASS_HAND.asStack()))).disableBackground()
      );
      widgets.add(
         Widgets.createLabel(
               new Point(bounds.x + bounds.width / 2, bounds.y + 99), Component.translatableWithFallback("create.rei.manual_application", "Right-click placed block")
            )
            .centered()
            .noShadow()
            .color(-12566464, -3355444)
      );
      if (display.keepHeldItem && inputs.size() > 1) {
         widgets.add(
            Widgets.createTooltip(
               new Rectangle(bounds.x + 70, bounds.y + 12, 18, 18), new Component[]{Component.translatable("create.recipe.deploying.not_consumed")}
            )
         );
      }
   }

   private void assembly(AdaptedDisplay display, Rectangle bounds, List<Widget> widgets) {
      int stepWidth = Math.min(58, (bounds.width - 8) / display.assemblyRecipes.size());
      int startX = bounds.x + (bounds.width - display.assemblyRecipes.size() * stepWidth) / 2;

      for (int index = 0; index < display.assemblyRecipes.size(); index++) {
         ProcessingRecipe<?, ?> recipe = display.assemblyRecipes.get(index);
         int centerX = startX + index * stepWidth + stepWidth / 2;
         int animationOffset = index;
         List<EntryIngredient> ingredients = display.stepIngredients.get(index);

         for (int ingredientIndex = 0; ingredientIndex < ingredients.size(); ingredientIndex++) {
            widgets.add(Widgets.createSlot(new Point(centerX - ingredients.size() * 9 + ingredientIndex * 18, bounds.y + 6)).entries(ingredients.get(ingredientIndex)).markInput());
         }

         widgets.add(Widgets.createLabel(new Point(centerX, bounds.y + 80), Component.literal(index + 1 + "")).centered().noShadow().color(-12566464, -3355444));
         AnimatedKinetics animation = AdaptedAnimations.machine(AdaptedAnimations.type(recipe), recipe);
         if (animation != null) {
            widgets.add(
               Widgets.createDrawableWidget((graphics, mouseX, mouseY, delta) -> AdaptedAnimations.drawScaled(animation, graphics, centerX - 8, bounds.y + 34, animationOffset, 0.65F))
            );
         } else {
            widgets.add(Widgets.createLabel(new Point(centerX, bounds.y + 60), Component.literal(display.steps.get(index))).centered());
         }

         widgets.add(
            Widgets.createTooltip(
               new Rectangle(centerX - stepWidth / 2, bounds.y + 24, stepWidth, 65),
               new Component[]{
                  (recipe instanceof IAssemblyRecipe assembly ? assembly.getDescriptionForAssembly() : Component.literal(display.steps.get(index)))
               }
            )
         );
      }

      widgets.add(Widgets.createSlot(new Point(bounds.x + 12, bounds.y + 100)).entries(display.getInputEntries().getFirst()).markInput());
      widgets.add(Widgets.createArrow(new Point(bounds.x + 49, bounds.y + 100)));
      int resultsX = bounds.x + bounds.width - 100;

      for (int outputIndex = 0; outputIndex < display.getOutputEntries().size(); outputIndex++) {
         int outputX = resultsX + outputIndex % 5 * 18;
         int outputY = bounds.y + 100 + outputIndex / 5 * 18;
         widgets.add(Widgets.createSlot(new Point(outputX, outputY)).entries(display.getOutputEntries().get(outputIndex)).markOutput());
         if (outputIndex < display.chances.size() && display.chances.get(outputIndex) < 1.0F) {
            widgets.add(
               Widgets.createTooltip(
                  new Rectangle(outputX, outputY, 18, 18),
                  new Component[]{
                     Component.translatable(
                        "create.recipe.processing.chance", new Object[]{String.format(Locale.ROOT, "%.2f", display.chances.get(outputIndex) * 100.0F)}
                     )
                  }
               )
            );
         }
      }

      widgets.add(Widgets.createLabel(new Point(bounds.x + 49, bounds.y + 122), Component.literal("× " + display.loops)).centered().noShadow().color(-12566464, -3355444));
      widgets.add(
         Widgets.createTooltip(
            new Rectangle(bounds.x + 24, bounds.y + 118, 50, 18),
            new Component[]{Component.translatable("create.recipe.assembly.repeat", new Object[]{display.loops})}
         )
      );
   }
}
