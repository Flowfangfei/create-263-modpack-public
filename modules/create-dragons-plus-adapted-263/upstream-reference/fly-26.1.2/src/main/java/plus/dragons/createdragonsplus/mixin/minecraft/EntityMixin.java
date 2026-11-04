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

package plus.dragons.createdragonsplus.mixin.minecraft;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityFluidInteraction;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import plus.dragons.createdragonsplus.registry.CDPTags;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    @Final
    private EntityFluidInteraction fluidInteraction;

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityFluidInteraction;<init>(Ljava/util/Set;)V"), index = 0)
    private Set<TagKey<Fluid>> createDragonsPlus$trackSwimmableFluid(Set<TagKey<Fluid>> trackedFluids) {
        Set<TagKey<Fluid>> expanded = new HashSet<>(trackedFluids);
        expanded.add(CDPTags.SWIMMABLE_FLUIDS);
        return expanded;
    }

    @ModifyExpressionValue(method = "updateFluidInteraction", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityFluidInteraction;isInFluid(Lnet/minecraft/tags/TagKey;)Z", ordinal = 0))
    private boolean createDragonsPlus$treatSwimmableFluidAsWater(boolean touchingWater) {
        return touchingWater || fluidInteraction.isInFluid(CDPTags.SWIMMABLE_FLUIDS);
    }

    @Inject(method = "updateFluidInteraction", at = @At("RETURN"))
    private void createDragonsPlus$applySwimmableFluidCurrent(CallbackInfoReturnable<Boolean> callback) {
        Entity entity = (Entity) (Object) this;
        if (entity.isPushedByFluid() && fluidInteraction.isInFluid(CDPTags.SWIMMABLE_FLUIDS)) {
            fluidInteraction.applyCurrentTo(CDPTags.SWIMMABLE_FLUIDS, entity, 0.014);
        }
    }

    @Inject(method = "getFluidHeight", at = @At("HEAD"), cancellable = true)
    private void createDragonsPlus$includeSwimmableFluidHeight(
            TagKey<Fluid> tag, CallbackInfoReturnable<Double> callback) {
        if (tag.equals(FluidTags.WATER)) {
            callback.setReturnValue(Math.max(
                    fluidInteraction.getFluidHeight(FluidTags.WATER),
                    fluidInteraction.getFluidHeight(CDPTags.SWIMMABLE_FLUIDS)));
        }
    }
}
