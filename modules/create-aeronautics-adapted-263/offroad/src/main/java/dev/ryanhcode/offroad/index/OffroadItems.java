package dev.ryanhcode.offroad.index;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.data.AssetLookup;
import dev.simulated_team.simulated.registrate.SimulatedRegistrate;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import dev.ryanhcode.offroad.Offroad;
import dev.ryanhcode.offroad.content.components.TireLike;
import dev.ryanhcode.offroad.content.items.tire.TireItem;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public class OffroadItems {
    private static final SimulatedRegistrate REGISTRATE = Offroad.getRegistrate();

    static {
        REGISTRATE.item("small_tire", TireItem::new)
                .properties(x -> x.component(OffroadDataComponents.TIRE, TireLike.SMALL_TIRE))


                .register();

        REGISTRATE.item("tire", TireItem::new)
                .properties(x -> x.component(OffroadDataComponents.TIRE, TireLike.TIRE))


                .register();

        REGISTRATE.item("large_tire", TireItem::new)
                .properties(x -> x.component(OffroadDataComponents.TIRE, TireLike.LARGE_TIRE))


                .register();

        REGISTRATE.item("monstrous_tire", TireItem::new)
                .properties(x -> x.component(OffroadDataComponents.TIRE, TireLike.MONSTROUS_TIRE))


                .register();
    }

    public static void init() {
    }
}
