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

import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.item.BottleItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import plus.dragons.createdragonsplus.registry.CDPAttachments;

@Mixin(BottleItem.class)
public abstract class BottleItemMixin {
    @Inject(method = "lambda$use$0", at = @At("HEAD"), cancellable = true)
    private static void createDragonsPlus$acceptVaporizedDragonBreath(
            AreaEffectCloud cloud, CallbackInfoReturnable<Boolean> callback) {
        if (Boolean.TRUE.equals(cloud.getAttached(CDPAttachments.DRAGON_BREATH_CLOUD))) {
            callback.setReturnValue(cloud.isAlive());
        }
    }
}
