/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2025 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.railwayteam.railways.fabric_mixin.client;

import com.railwayteam.railways.content.switches.TrainHUDSwitchExtension;
import com.simibubi.create.content.trains.TrainHUD;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TrainHUD.class, remap = false)
public class MixinTrainHUD {
    @Inject(method = "tick", at = @At("HEAD"))
    private static void railways$tickHook(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && !(mc.player.getVehicle() instanceof CarriageContraptionEntity))
            TrainHUDSwitchExtension.clearSwitchState();
        TrainHUDSwitchExtension.tickAnimation();
    }

    @Inject(method = "renderOverlay", at = @At("HEAD"))
    private static void railways$renderOverlayHook(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        TrainHUDSwitchExtension.renderOverlay(graphics, deltaTracker.getGameTimeDeltaPartialTick(false),
                mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
    }
}
