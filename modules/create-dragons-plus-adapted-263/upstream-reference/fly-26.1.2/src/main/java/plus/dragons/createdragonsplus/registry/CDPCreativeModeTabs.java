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

package plus.dragons.createdragonsplus.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import plus.dragons.createdragonsplus.CreateDragonsPlus;
import plus.dragons.createdragonsplus.config.CDPConfig;
import plus.dragons.createdragonsplus.fluid.dye.DyeVariant;
import plus.dragons.createdragonsplus.fluid.dye.DyeVariantRegistry;

public final class CDPCreativeModeTabs {
    public static final ResourceKey<CreativeModeTab> BASE = ResourceKey.create(Registries.CREATIVE_MODE_TAB, CreateDragonsPlus.id("base"));

    private CDPCreativeModeTabs() {}

    public static void register() {
        Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                BASE,
                CreativeModeTab.builder(null, -1)
                        .title(Component.translatable("itemGroup.create_dragons_plus.base"))
                        .icon(() -> new ItemStack(CDPItems.RARE_MARBLE_GATE_PACKAGE.get()))
                        .displayItems(CDPCreativeModeTabs::addItems)
                        .build());
    }

    private static void addItems(
            CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        if (CDPConfig.features().fluidHatch.get()) {
            output.accept(CDPItems.FLUID_HATCH.get());
        }
        if (CDPConfig.features().blazeUpgradeSmithingTemplate.get()) {
            output.accept(CDPItems.BLAZE_UPGRADE_SMITHING_TEMPLATE.get());
        }
        if (CDPConfig.features().dyeFluids.get()) {
            for (DyeVariant variant : DyeVariantRegistry.all()) {
                if (variant.isAvailable()) {
                    output.accept(CDPFluids.DYES_BY_VARIANT.get(variant.id()).getBucket());
                }
            }
        }
        if (CDPConfig.features().dragonBreathFluid.get()) {
            output.accept(CDPFluids.DRAGON_BREATH.getBucket());
        }
        output.accept(CDPItems.RARE_BLAZE_PACKAGE.get(), CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
        output.accept(CDPItems.RARE_MARBLE_GATE_PACKAGE.get(), CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
    }
}
