package com.george_vi.electroenergetics.compat.jei;

import com.george_vi.electroenergetics.CreateElectroEnergetics;
import com.george_vi.electroenergetics.events.recipe.FluidCompactingRecipe;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.compat.jei.category.BasinCategory;
import com.zurrtum.create.client.compat.jei.renderer.TwoIconRenderer;
import com.zurrtum.create.client.foundation.gui.render.PressBasinRenderState;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.types.IRecipeType;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;

import org.joml.Matrix3x2f;

/** Mechanical pressing with liquid output, matching Create Fly's basin recipe presentation. */
public final class FluidCompactingCategory extends BasinCategory<FluidCompactingRecipe> {
    public static final IRecipeType<RecipeHolder<FluidCompactingRecipe>> TYPE = createType();

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static IRecipeType<RecipeHolder<FluidCompactingRecipe>> createType() {
        return (IRecipeType)
                IRecipeType.create(
                        CreateElectroEnergetics.rl("fluid_compacting"), RecipeHolder.class);
    }

    @Override
    public IRecipeType<RecipeHolder<FluidCompactingRecipe>> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("create.recipe.packing");
    }

    @Override
    public IDrawable getIcon() {
        return new TwoIconRenderer(AllItems.MECHANICAL_PRESS, AllItems.BASIN);
    }

    @Override
    public void setRecipe(
            IRecipeLayoutBuilder builder,
            RecipeHolder<FluidCompactingRecipe> holder,
            IFocusGroup focuses) {
        var recipe = holder.value();
        var delegate = recipe.delegate();
        addIngredientSlots(builder, recipe);
        int total = delegate.results().size() + delegate.fluidResults().size();
        boolean odd = total % 2 != 0;
        addResultSlots(builder, delegate.results(), 0, total - 1, 51, odd);
        addFluidResultSlots(
                builder, delegate.fluidResults(), delegate.results().size(), total - 1, 51, odd);
        addHeatSlots(builder, recipe);
    }

    @Override
    public void draw(
            RecipeHolder<FluidCompactingRecipe> holder,
            IRecipeSlotsView slots,
            GuiGraphicsExtractor graphics,
            double mouseX,
            double mouseY) {
        var recipe = holder.value();
        drawBackground(
                recipe,
                graphics,
                recipe.delegate().results().size() + recipe.delegate().fluidResults().size());
        graphics.guiRenderState.addPicturesInPictureState(
                new PressBasinRenderState(new Matrix3x2f(graphics.pose()), 91, -5));
    }
}
