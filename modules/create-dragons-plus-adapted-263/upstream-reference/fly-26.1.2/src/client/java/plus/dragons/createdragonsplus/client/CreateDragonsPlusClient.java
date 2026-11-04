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

package plus.dragons.createdragonsplus.client;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.client.AllItemTooltips;
import com.zurrtum.create.client.content.logistics.itemHatch.HatchFilterSlot;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.client.ponder.foundation.PonderIndex;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import plus.dragons.createdragonsplus.client.fluid.CDPFluidRenderers;
import plus.dragons.createdragonsplus.client.model.CDPPartialModels;
import plus.dragons.createdragonsplus.client.ponder.CDPPonderPlugin;
import plus.dragons.createdragonsplus.client.processing.blaze.BlazeClientHandler;
import plus.dragons.createdragonsplus.config.CDPClientConfig;
import plus.dragons.createdragonsplus.data.map.CDPColoringCatalysts;
import plus.dragons.createdragonsplus.network.ColoringCatalystSyncPayload;
import plus.dragons.createdragonsplus.registry.CDPBlockEntities;
import plus.dragons.createdragonsplus.registry.CDPItems;

public final class CreateDragonsPlusClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CDPPartialModels.register();
        BlazeClientHandler.register();
        CDPClientConfig.register();
        AllItemTooltips.register(CDPItems.FLUID_HATCH.get());
        CDPFluidRenderers.register();
        BlockEntityBehaviour.addClient(
                CDPBlockEntities.FLUID_HATCH.get(),
                blockEntity -> new FilteringBehaviour<>(blockEntity, new HatchFilterSlot()));
        BlockEntityRenderers.register(
                CDPBlockEntities.FLUID_HATCH.get(), SmartBlockEntityRenderer::new);
        PonderIndex.addPlugin(new CDPPonderPlugin());
        ClientConfigurationNetworking.registerGlobalReceiver(
                ColoringCatalystSyncPayload.TYPE,
                (payload, context) -> context.client()
                        .execute(() -> CDPColoringCatalysts.applyClientSnapshot(payload)));
        ClientPlayNetworking.registerGlobalReceiver(
                ColoringCatalystSyncPayload.TYPE,
                (payload, context) -> CDPColoringCatalysts.applyClientSnapshot(payload));
    }
}
