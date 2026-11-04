/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package plus.dragons.createenchantmentindustry.common.fluids.printer;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.sequenced.IAssemblyRecipe;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.UniformFloat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.enchantment.effects.PlaySoundEffect;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;
import plus.dragons.createenchantmentindustry.common.registry.CEIRecipes;

import plus.dragons.createenchantmentindustry.util.CEILang;

public class PrintingRecipe extends ProcessingRecipe<PrintingInput, PrintingRecipeParams> implements IAssemblyRecipe {
    public PrintingRecipe(PrintingRecipeParams params) {
        super(CEIRecipes.PRINTING, params);
    }

    public static Builder builder(Identifier id, PlaySoundEffect sound) {
        return new Builder(id, sound);
    }

    public static Builder builder(Identifier id) {
        return new Builder(id, new PlaySoundEffect(
                List.of(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.ENCHANTMENT_TABLE_USE)),
                ConstantFloat.of(1),
                UniformFloat.of(.9f, 1f)));
    }

    public void playSound(Level level, BlockPos pos, SoundSource source) {
        var sound = this.params.sound;
        level.playSound(null,
                pos,
                sound.soundEvents().get(level.getRandom().nextInt(sound.soundEvents().size())).value(),
                source,
                sound.volume().sample(level.getRandom()),
                sound.pitch().sample(level.getRandom()));
    }

    @Override
    protected int getMaxInputCount() {
        return 2;
    }

    @Override
    protected int getMaxFluidInputCount() {
        return 1;
    }

    @Override
    protected int getMaxOutputCount() {
        return 1;
    }

    @Override
    public boolean matches(PrintingInput input, Level level) {
        return ingredients.get(0).test(input.base()) &&
                ingredients.get(1).test(input.template()) &&
                (input.fluid().isEmpty() || fluidIngredients.getFirst().test(input.fluid()));
    }

    @Override
    public Component getDescriptionForAssembly() {
        ItemStack[] matchingStacks = ingredients.get(1).items().map(holder -> new ItemStack(holder)).toArray(ItemStack[]::new);
        List<FluidStack> matchingFluidStacks = fluidIngredients.getFirst().ingredient().fluids().stream().map(holder -> new FluidStack(holder, fluidIngredients.getFirst().amount())).toList();
        if (matchingStacks.length == 0 || matchingFluidStacks.isEmpty()) {
            return Component.literal("Invalid");
        }
        return CEILang.translate("recipe.assembly.printing",
                matchingStacks[0].getHoverName(),
                matchingFluidStacks.getFirst().getHoverName()).component();
    }

    @Override
    public void addRequiredMachines(Set<ItemLike> required) {
        required.add(CEIBlocks.PRINTER);
    }

    @Override
    public void addAssemblyIngredients(List<Ingredient> list) {
        list.add(getIngredients().get(1));
    }

    @Override
    public void addAssemblyFluidIngredients(List<SizedFluidIngredient> list) {
        list.add(getFluidIngredients().getFirst());
    }

    @Override
    public Supplier<Supplier<SequencedAssemblySubCategory>> getJEISubCategory() {
        return () -> () -> {
            try { return (SequencedAssemblySubCategory) Class.forName("plus.dragons.createenchantmentindustry.integration.jei.category.assembly.AssemblyPrintingCategory").getConstructor().newInstance(); }
            catch (ReflectiveOperationException exception) { throw new IllegalStateException("JEI integration is not enabled in this runtime", exception); }
        };
    }

    public static class Builder extends ProcessingRecipeBuilder<PrintingRecipeParams, PrintingRecipe, Builder> {
        protected Builder(Identifier id, PlaySoundEffect sound) {
            super(PrintingRecipe::new, id);
            PrintingRecipeParams params = (PrintingRecipeParams) this.params;
            params.sound = sound;
        }

        @Override
        protected PrintingRecipeParams createParams() {
            return new PrintingRecipeParams();
        }

        @Override
        public Builder self() {
            return this;
        }
    }

    public static class Serializer<R extends PrintingRecipe> {
        private final MapCodec<R> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, R> streamCodec;

        public Serializer(ProcessingRecipe.Factory<PrintingRecipeParams, R> factory) {
            this.codec = ProcessingRecipe.codec(factory, PrintingRecipeParams.CODEC);
            this.streamCodec = ProcessingRecipe.streamCodec(factory, PrintingRecipeParams.STREAM_CODEC);
        }

        public RecipeSerializer<R> asRecipeSerializer() { return new RecipeSerializer<>(codec, streamCodec); }

        public MapCodec<R> codec() {
            return codec;
        }

        public StreamCodec<RegistryFriendlyByteBuf, R> streamCodec() {
            return streamCodec;
        }
    }
}
