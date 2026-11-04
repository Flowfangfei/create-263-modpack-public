package com.railwayteam.railways.compat.jei;

import com.railwayteam.railways.Railways;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.kinetics.press.PressingRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;

@mezz.jei.api.JeiPlugin
public class RailwaysJeiClient implements IModPlugin {
 private static final RecipeType<RecipeHolder<PressingRecipe>> PRESSING=RecipeType.createRecipeHolderType(AllRecipeTypes.PRESSING.getId());
 public static void register() { Railways.LOGGER.debug("Sequenced cutting is provided by Create Adapted's native JEI category"); }
 @Override public Identifier getPluginUid() { return Railways.asResource("jei_plugin"); }
 @Override public void onRuntimeAvailable(IJeiRuntime runtime) {
  var hidden=runtime.getRecipeManager().createRecipeLookup(PRESSING).includeHidden().get()
   .filter(holder->isTrackStep(holder)).toList();
  if(!hidden.isEmpty())runtime.getRecipeManager().hideRecipes(PRESSING,hidden);
 }
 private static boolean isTrackStep(RecipeHolder<PressingRecipe> holder) {
  var id=holder.id().identifier();
  return id.getNamespace().equals("create") && id.getPath().startsWith("sequenced_assembly_")
   && holder.value().getRollableResultsAsItemStacks().stream().anyMatch(stack->{
    var output=BuiltInRegistries.ITEM.getKey(stack.getItem());
    return output.getNamespace().equals(Railways.MOD_ID) && output.getPath().startsWith("track_");
   });
 }
}
