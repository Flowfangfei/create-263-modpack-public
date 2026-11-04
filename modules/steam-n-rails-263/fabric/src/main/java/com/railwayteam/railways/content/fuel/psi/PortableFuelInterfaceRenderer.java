/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2026 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.content.fuel.psi;

import com.railwayteam.railways.Railways;
import com.simibubi.create.content.contraptions.actors.psi.PortableStorageInterfaceRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import net.neoforged.neoforge.client.model.standalone.UnbakedStandaloneModel;

/** Uses Create Adapted's submit renderer, with Railway partials selected by the PSI renderer mixin. */
public class PortableFuelInterfaceRenderer extends PortableStorageInterfaceRenderer {
    public static final StandaloneModelKey<BlockStateModelPart> MIDDLE =
        new StandaloneModelKey<>(() -> "railways:portable_fuel_interface_middle");
    public static final StandaloneModelKey<BlockStateModelPart> MIDDLE_POWERED =
        new StandaloneModelKey<>(() -> "railways:portable_fuel_interface_middle_powered");
    public static final StandaloneModelKey<BlockStateModelPart> TOP =
        new StandaloneModelKey<>(() -> "railways:portable_fuel_interface_top");

    public PortableFuelInterfaceRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    public static void registerModels(ModelEvent.RegisterStandalone event) {
        registerModel(event, MIDDLE, Railways.asResource("block/portable_fuel_interface/block_middle"));
        registerModel(event, MIDDLE_POWERED, Railways.asResource("block/portable_fuel_interface/block_middle_powered"));
        registerModel(event, TOP, Railways.asResource("block/portable_fuel_interface/block_top"));
    }

    @SuppressWarnings("unchecked")
    private static <T> void registerModel(ModelEvent.RegisterStandalone event, StandaloneModelKey<T> key,
                                         Identifier location) {
        event.register(key, (UnbakedStandaloneModel<T>) SimpleUnbakedStandaloneModel.simpleModelWrapper(location));
    }
}
