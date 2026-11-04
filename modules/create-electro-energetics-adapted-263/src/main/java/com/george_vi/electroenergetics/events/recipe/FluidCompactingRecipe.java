package com.george_vi.electroenergetics.events.recipe;

import com.george_vi.electroenergetics.CreateElectroEnergetics;
import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.content.kinetics.mixer.MixingRecipe;
import com.zurrtum.create.content.processing.basin.BasinInput;
import com.zurrtum.create.content.processing.basin.BasinRecipe;
import com.zurrtum.create.content.processing.recipe.HeatCondition;
import com.zurrtum.create.content.processing.recipe.SizedIngredient;
import com.zurrtum.create.foundation.fluid.FluidIngredient;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import java.util.List;

/** Create Fly's press supports BasinRecipe but its native compacting serializer has no fluid output. */
public record FluidCompactingRecipe(MixingRecipe delegate) implements BasinRecipe {
    public static final RecipeType<FluidCompactingRecipe> TYPE = new RecipeType<>() {
        @Override public String toString() { return "electroenergetics:fluid_compacting"; }
    };
    public static final MapCodec<FluidCompactingRecipe> CODEC = MixingRecipe.MAP_CODEC.xmap(
        FluidCompactingRecipe::new, FluidCompactingRecipe::delegate);
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidCompactingRecipe> STREAM_CODEC =
        MixingRecipe.SERIALIZER.streamCodec().map(FluidCompactingRecipe::new, FluidCompactingRecipe::delegate);
    public static final RecipeSerializer<FluidCompactingRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

    public static void register() {
        Registry.register(BuiltInRegistries.RECIPE_TYPE, CreateElectroEnergetics.rl("fluid_compacting"), TYPE);
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, CreateElectroEnergetics.rl("fluid_compacting"), SERIALIZER);
        net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization.synchronizeRecipeSerializer(SERIALIZER);
    }

    @Override public int getIngredientSize() { return delegate.getIngredientSize(); }
    @Override public List<SizedIngredient> ingredients() { return delegate.ingredients(); }
    @Override public List<FluidIngredient> fluidIngredients() { return delegate.fluidIngredients(); }
    @Override public HeatCondition heat() { return delegate.heat(); }
    @Override public boolean matches(BasinInput input, Level level) { return delegate.matches(input, level); }
    @Override public boolean apply(BasinInput input) { return delegate.apply(input); }
    @Override public RecipeSerializer<FluidCompactingRecipe> getSerializer() { return SERIALIZER; }
    @Override public RecipeType<FluidCompactingRecipe> getType() { return TYPE; }
}
