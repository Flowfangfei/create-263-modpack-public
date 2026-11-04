package com.tom.createores.recipe;

import java.util.Optional;
import java.util.function.Supplier;

import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import com.tom.createores.util.FluidIngredient;

public abstract class ExcavatingRecipe implements Recipe<RecipeInput> {
	public Identifier veinId;
	public Ingredient drill;
	public int priority, ticks, stressMul;
	public Optional<FluidIngredient> drillingFluid;
	protected boolean isNet;

	protected void setFromCommon(ExcavatingRecipeCommon c) {
		veinId = c.veinId();
		drill = c.drill();
		priority = c.priority();
		ticks = c.ticks();
		stressMul = c.stressMul();
		drillingFluid = c.drillingFluid();
	}

	protected ExcavatingRecipeCommon getCommon() {
		return new ExcavatingRecipeCommon(veinId, drill, priority, ticks, stressMul, drillingFluid);
	}

	@Override
	public boolean matches(RecipeInput pContainer, Level pLevel) {
		return false;
	}

	@Override
	public ItemStack assemble(RecipeInput p_44001_) {
		return getResultItem(null);
	}

	public boolean showNotification() { return false; }

    @Override
    public boolean isSpecial() { return true; }

    public boolean canCraftInDimensions(int pWidth, int pHeight) {
		return true;
	}

	public ItemStack getResultItem(HolderLookup.Provider p_267052_) {
		return ItemStack.EMPTY;
	}

	@Override
	public RecipeBookCategory recipeBookCategory() {
		return RecipeBookCategories.CRAFTING_MISC;
	}

	@Override
	public PlacementInfo placementInfo() {
		return PlacementInfo.NOT_PLACEABLE;
	}

	public Ingredient getDrill() {
		return drill;
	}

	public int getTicks() {
		return ticks;
	}

	public abstract String group();

	public int getStress() {
		return stressMul;
	}

	public Optional<FluidIngredient> getDrillingFluid() {
		return drillingFluid;
	}

	protected abstract void fromNetwork(RegistryFriendlyByteBuf buffer);
	protected abstract void toNetwork(RegistryFriendlyByteBuf buffer);

	public static record ExcavatingRecipeCommon(Identifier veinId, Ingredient drill, int priority, int ticks, int stressMul, Optional<FluidIngredient> drillingFluid) {
	}

	public static abstract class Serializer<T extends ExcavatingRecipe> {
		private final Supplier<T> create;
        public abstract MapCodec<T> codec();
        public final RecipeSerializer<T> serializer() { return new RecipeSerializer<>(codec(), STREAM_CODEC); }

		public Serializer(Supplier<T> create) {
			this.create = create;
		}

		private T fromNetwork(RegistryFriendlyByteBuf buffer) {
			T r = create.get();
			r.drill = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
			r.ticks = buffer.readVarInt();
			r.stressMul = buffer.readVarInt();
			r.priority = buffer.readVarInt();
			r.veinId = buffer.readIdentifier();
			r.fromNetwork(buffer);
			r.drillingFluid = FluidIngredient.read(buffer);
			r.isNet = true;
			return r;
		}

		private void toNetwork(RegistryFriendlyByteBuf buffer, T recipe) {
			Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.drill);
			buffer.writeVarInt(recipe.ticks);
			buffer.writeVarInt(recipe.stressMul);
			buffer.writeVarInt(recipe.priority);
			buffer.writeIdentifier(recipe.veinId);
			recipe.toNetwork(buffer);
			FluidIngredient.write(buffer, recipe.drillingFluid);
		}

		public static final MapCodec<ExcavatingRecipeCommon> CODEC = RecordCodecBuilder.mapCodec(
				b -> b.group(
						Identifier.CODEC.fieldOf("veinId").forGetter(ExcavatingRecipeCommon::veinId),
						Ingredient.CODEC.fieldOf("drill").forGetter(ExcavatingRecipeCommon::drill),
						Codec.INT.fieldOf("priority").forGetter(ExcavatingRecipeCommon::priority),
						Codec.INT.fieldOf("ticks").forGetter(ExcavatingRecipeCommon::ticks),
						Codec.INT.fieldOf("stress").forGetter(ExcavatingRecipeCommon::stressMul),
						FluidIngredient.CODEC.optionalFieldOf("fluid").forGetter(ExcavatingRecipeCommon::drillingFluid)
						)
				.apply(b, ExcavatingRecipeCommon::new)
				);

		public final StreamCodec<RegistryFriendlyByteBuf, T> STREAM_CODEC = StreamCodec.of(
				this::toNetwork, this::fromNetwork
				);

		public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
			return STREAM_CODEC;
		}
	}

	@FunctionalInterface
	public static interface RecipeFactory<T extends ExcavatingRecipe> {
		T create(RecipeType<?> type, RecipeSerializer<?> serializer);
	}
}
