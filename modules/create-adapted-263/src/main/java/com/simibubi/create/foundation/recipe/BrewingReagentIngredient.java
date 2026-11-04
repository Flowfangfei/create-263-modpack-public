package com.simibubi.create.foundation.recipe;

import java.util.stream.Stream;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PotionIngredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

public record BrewingReagentIngredient(PotionIngredient reagent) implements ICustomIngredient {
	public static final MapCodec<BrewingReagentIngredient> CODEC = PotionIngredient.MAP_CODEC
		.xmap(BrewingReagentIngredient::new, BrewingReagentIngredient::reagent);

	public static Ingredient of(PotionIngredient reagent) {
		return reagent.potions().isEmpty() ? reagent.ingredient() : new BrewingReagentIngredient(reagent).toVanilla();
	}

	@Override
	public boolean test(ItemStack stack) {
		return reagent.test(stack);
	}

	@Override
	public Stream<Holder<Item>> items() {
		return reagent.ingredient().items();
	}

	@Override
	public boolean isSimple() {
		return false;
	}

	@Override
	public IngredientType<?> getType() {
		return AllIngredients.BREWING_REAGENT.get();
	}

	@Override
	public SlotDisplay display() {
		return reagent.ingredient().display();
	}
}
