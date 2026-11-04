package com.tom.createores.recipe;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidStackTemplate;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import com.tom.createores.CreateOreExcavation;

public class ExtractorRecipe extends ExcavatingRecipe {
	public FluidStackTemplate output;

	@Override
	public String group() {
		return "extractor";
	}

	@Override
	protected void fromNetwork(RegistryFriendlyByteBuf buffer) {
		output = FluidStackTemplate.STREAM_CODEC.decode(buffer);
	}

	@Override
	protected void toNetwork(RegistryFriendlyByteBuf buffer) {
		FluidStackTemplate.STREAM_CODEC.encode(buffer, output);
	}

	public FluidStack getOutput() {
		return output.create();
	}

	public static record ExtractorRecipeData(FluidStackTemplate output) {
	}

	public static class Serializer extends ExcavatingRecipe.Serializer<ExtractorRecipe> {

		public Serializer() {
			super(ExtractorRecipe::new);
		}

		private static final MapCodec<ExtractorRecipeData> CODEC2 = RecordCodecBuilder.mapCodec(
				b -> b.group(
						FluidStackTemplate.CODEC.fieldOf("output").forGetter(ExtractorRecipeData::output)
						)
				.apply(b, ExtractorRecipeData::new)
				);

		public static final MapCodec<ExtractorRecipe> CODEC = Codec.mapPair(
				ExcavatingRecipe.Serializer.CODEC,
				CODEC2
				).xmap((Pair<ExcavatingRecipeCommon, ExtractorRecipeData> p) -> {
					var r = new ExtractorRecipe();
					r.setFromCommon(p.getFirst());
					r.output = p.getSecond().output();
					return r;
				}, (ExtractorRecipe r) -> {
					return Pair.of(r.getCommon(), new ExtractorRecipeData(r.output));
				});

		@Override
		public MapCodec<ExtractorRecipe> codec() {
			return CODEC;
		}
	}

	@Override
	public RecipeSerializer<ExtractorRecipe> getSerializer() {
		return CreateOreExcavation.EXTRACTING_RECIPES.getSerializer();
	}

	@Override
	public RecipeType<ExtractorRecipe> getType() {
		return CreateOreExcavation.EXTRACTING_RECIPES.getRecipeType();
	}
}
