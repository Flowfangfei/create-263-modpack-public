/*
 * Steam 'n' Rails
 * Copyright (c) 2026 The Railways Team
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

package com.railwayteam.railways.fabric_mixin;

import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.content.contraptions.actors.roller.RollerBlockEntity;

import com.simibubi.create.content.contraptions.actors.roller.RollerMovementBehaviour;
import com.simibubi.create.content.trains.track.ITrackBlock;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = RollerBlockEntity.class, remap = false)
public class MixinRollerBlockEntity {
    @Shadow public ScrollOptionBehaviour<?> mode;
    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method="addBehaviours",at=@At(value="NEW",target="(Ljava/lang/Class;Lnet/minecraft/network/chat/Component;Lcom/simibubi/create/foundation/blockEntity/SmartBlockEntity;Lcom/simibubi/create/foundation/blockEntity/behaviour/ValueBoxTransform;)Lcom/simibubi/create/foundation/blockEntity/behaviour/scrollValue/ScrollOptionBehaviour;"))
    private ScrollOptionBehaviour<?> railways$rollerMode(Class<?> options,net.minecraft.network.chat.Component label,
      com.simibubi.create.foundation.blockEntity.SmartBlockEntity be,com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform slot,
      com.llamalad7.mixinextras.injector.wrapoperation.Operation<ScrollOptionBehaviour<?>> original) {
        return new com.railwayteam.railways.content.roller_extensions.RailwaysRollerModeBehaviour(label,be,slot);
    }

    @Inject(method = "addBehaviours", at = @At("RETURN"))
    private void railways$addTrackReplaceMode(List<BlockEntityBehaviour> behaviours, CallbackInfo ci) {
        mode.between(0, 3);
    }

    @Inject(method = "isValidMaterial", at = @At("HEAD"), cancellable = true)
    private void makeTracksValid(ItemStack newFilter, CallbackInfoReturnable<Boolean> cir) {
        if (newFilter.isEmpty())
            return;
        BlockState appliedState = RollerMovementBehaviour.getStateToPaveWith(newFilter);
        if (appliedState.getBlock() instanceof ITrackBlock)
            cir.setReturnValue(true);
    }
}
