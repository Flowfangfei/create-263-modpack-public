package dev.simulated_team.simulated.neoforge.service;

import com.simibubi.create.api.data.datamaps.BlazeBurnerFuel;
import com.simibubi.create.api.registry.CreateDataMaps;
import dev.simulated_team.simulated.service.SimItemService;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

public class NeoForgeSimItemService implements SimItemService {

    public int getBurnTime(final ItemStack stack) {
        if(stack.isEmpty())return 0;
        var fuel=stack.get(net.minecraft.core.component.DataComponents.COOKING_FUEL);
        if(fuel==null)return 0;
        if(fuel.burnTime() instanceof net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt.Constant constant)return constant.value();
        var server=net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if(server==null)return 0;
        var params=new net.minecraft.world.level.storage.loot.LootParams.Builder(server.overworld()).create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.EMPTY);
        var context=new net.minecraft.world.level.storage.loot.LootContext.Builder(params).create(java.util.Optional.empty());
        return fuel.burnTime().get(context,0);
    }

    @Override
    public int getSuperheatedBurnTime(final ItemStack stack) {
        final BlazeBurnerFuel fuel = stack.getItem().builtInRegistryHolder().getData(CreateDataMaps.SUPERHEATED_BLAZE_BURNER_FUELS);
        if(fuel != null) {
            return fuel.burnTime();
        }
        return 0;
    }
}