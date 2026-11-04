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

package plus.dragons.createdragonsplus.fluid;

import com.zurrtum.create.api.effect.OpenPipeEffectHandler;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import plus.dragons.createdragonsplus.config.CDPConfig;
import plus.dragons.createdragonsplus.fluid.dye.DyeVariant;
import plus.dragons.createdragonsplus.registry.CDPFanProcessingTypes;
import plus.dragons.createdragonsplus.registry.CDPFluids;

public final class CDPOpenPipeEffects {
    private CDPOpenPipeEffects() {}

    public static void register() {
        for (DyeVariant variant : plus.dragons.createdragonsplus.fluid.dye.DyeVariantRegistry.all()) {
            var fluid = CDPFluids.DYES_BY_VARIANT.get(variant.id());
            OpenPipeEffectHandler handler = (level, area, stack) -> colorEntities(level, area, variant);
            OpenPipeEffectHandler.REGISTRY.register(fluid.getSource(), handler);
            OpenPipeEffectHandler.REGISTRY.register(fluid.getFlowing(), handler);
        }
        OpenPipeEffectHandler.REGISTRY.register(CDPFluids.DRAGON_BREATH.getSource(), CDPOpenPipeEffects::harmEntities);
        OpenPipeEffectHandler.REGISTRY.register(CDPFluids.DRAGON_BREATH.getFlowing(), CDPOpenPipeEffects::harmEntities);
    }

    private static void colorEntities(Level level, AABB area, DyeVariant variant) {
        if (level.isClientSide()) {
            return;
        }
        boolean colorItems = CDPConfig.dyeFluid().dyeFluidOpenPipeColorsItems.get();
        boolean colorLiving = CDPConfig.dyeFluid().dyeFluidOpenPipeColorsLivingEntities.get();
        if (!colorItems && !colorLiving) {
            return;
        }
        var type = CDPFanProcessingTypes.COLORING.get(variant.id());
        if (type == null) {
            return;
        }
        for (Entity entity : level.getEntities(
                (Entity) null,
                area,
                candidate -> colorItems && candidate instanceof ItemEntity
                        || colorLiving && candidate instanceof LivingEntity)) {
            if (entity instanceof ItemEntity item) {
                type.applyContactColoring(item, level);
            } else if (entity instanceof LivingEntity living) {
                type.applyContactColoring(living, level);
            }
        }
    }

    private static void harmEntities(Level level, AABB area, FluidStack stack) {
        if (level.isClientSide() || level.getGameTime() % 5 != 0) {
            return;
        }
        for (LivingEntity entity : level.getEntitiesOfClass(
                LivingEntity.class, area, LivingEntity::isAffectedByPotions)) {
            entity.addEffect(new MobEffectInstance(MobEffects.INSTANT_DAMAGE, 1, 1, false, false, false));
        }
    }
}
