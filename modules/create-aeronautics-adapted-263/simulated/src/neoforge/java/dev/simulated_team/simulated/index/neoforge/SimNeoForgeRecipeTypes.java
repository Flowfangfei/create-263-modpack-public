package dev.simulated_team.simulated.index.neoforge;

import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.recipe.PortableEngineDyeingRecipe;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class SimNeoForgeRecipeTypes {
    private static final PortableEngineDyeingRecipe RECIPE=new PortableEngineDyeingRecipe();
    public static final RecipeSerializer<PortableEngineDyeingRecipe> PORTABLE_ENGINE_DYEING=
        new RecipeSerializer<>(MapCodec.unit(RECIPE),StreamCodec.unit(RECIPE));
    public static void register(IEventBus bus){
        var serializers=DeferredRegister.create(Registries.RECIPE_SERIALIZER,Simulated.MOD_ID);
        serializers.register("portable_engine_dyeing",()->PORTABLE_ENGINE_DYEING);serializers.register(bus);
    }
}
