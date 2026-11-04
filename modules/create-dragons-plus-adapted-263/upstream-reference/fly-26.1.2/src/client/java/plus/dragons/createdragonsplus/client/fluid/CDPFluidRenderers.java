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

package plus.dragons.createdragonsplus.client.fluid;

import com.zurrtum.create.client.AllFluidConfigs;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createdragonsplus.CreateDragonsPlus;
import plus.dragons.createdragonsplus.api.registry.CDPFluidEntry;
import plus.dragons.createdragonsplus.config.CDPClientConfig;
import plus.dragons.createdragonsplus.fluid.dye.DyeVariant;
import plus.dragons.createdragonsplus.fluid.dye.DyeVariantRegistry;
import plus.dragons.createdragonsplus.registry.CDPFluids;

public final class CDPFluidRenderers {
    private static final Identifier DYE_STILL = CreateDragonsPlus.id("fluid/dye_still");
    private static final Identifier DYE_FLOW = CreateDragonsPlus.id("fluid/dye_flow");
    private static final Identifier DRAGON_STILL = CreateDragonsPlus.id("fluid/dragon_breath_still");
    private static final Identifier DRAGON_FLOW = CreateDragonsPlus.id("fluid/dragon_breath_flow");

    private CDPFluidRenderers() {}

    public static void register() {
        for (DyeVariant variant : DyeVariantRegistry.all()) {
            register(
                    CDPFluids.DYES_BY_VARIANT.get(variant.id()),
                    DYE_STILL,
                    DYE_FLOW,
                    variant.color(),
                    variant.color(),
                    () -> 96.0F * CDPClientConfig.get().dyeVisionMultiplier.getF() / 256.0F);
        }
        register(
                CDPFluids.DRAGON_BREATH,
                DRAGON_STILL,
                DRAGON_FLOW,
                null,
                0xDE9DC5,
                () -> 96.0F * CDPClientConfig.get().dragonBreathVisionMultiplier.getF() / 256.0F);
    }

    private static void register(
            CDPFluidEntry<? extends Fluid> entry,
            Identifier stillTexture,
            Identifier flowingTexture,
            @Nullable Integer tintColor,
            int fogColor,
            java.util.function.Supplier<Float> fogDistance) {
        Fluid source = entry.getSource();
        Fluid flowing = entry.getFlowing();
        BlockTintSource tint = tintColor == null ? null : state -> 0xFF000000 | tintColor;
        FluidModel.Unbaked model = new FluidModel.Unbaked(
                new Material(stillTexture, true),
                new Material(flowingTexture, true),
                null,
                tint);
        FluidRenderingRegistry.register(source, flowing, model);

        AllFluidConfigs.FOG_COLOR.put(source, fogColor);
        AllFluidConfigs.FOG_COLOR.put(flowing, fogColor);
        AllFluidConfigs.FOG_DISTANCE.put(source, fogDistance::get);
        AllFluidConfigs.FOG_DISTANCE.put(flowing, fogDistance::get);
        if (tintColor != null) {
            AllFluidConfigs.TINT.put(source, (fluid, components) -> 0xFF000000 | tintColor);
            AllFluidConfigs.TINT.put(flowing, (fluid, components) -> 0xFF000000 | tintColor);
        }
    }
}
