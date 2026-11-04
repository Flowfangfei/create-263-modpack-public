package com.george_vi.electroenergetics.mixins;

import com.george_vi.electroenergetics.events.recipe.FluidCompactingRecipe;
import com.zurrtum.create.content.kinetics.press.MechanicalPressBlockEntity;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MechanicalPressBlockEntity.class)
public class MechanicalPressMixin {
    @Inject(method = "matchStaticFilters", at = @At("HEAD"), cancellable = true)
    private void electroenergetics$fluidCompacting(RecipeHolder<? extends Recipe<?>> recipe,
                                                   CallbackInfoReturnable<Boolean> callback) {
        if (recipe.value() instanceof FluidCompactingRecipe) callback.setReturnValue(true);
    }
}
