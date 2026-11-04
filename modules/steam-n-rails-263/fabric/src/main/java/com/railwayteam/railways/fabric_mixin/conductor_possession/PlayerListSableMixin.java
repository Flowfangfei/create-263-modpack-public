/* Steam 'n' Rails compatibility; LGPL-3.0-or-later, see module LICENSE. */
package com.railwayteam.railways.fabric_mixin.conductor_possession;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.railwayteam.railways.annotation.mixin.ConditionalMixin;
import com.railwayteam.railways.compat.Mods;
import com.railwayteam.railways.content.conductor.ConductorEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Keep Sable's sublevel distance calculation, substituting the possessed camera position. */
@ConditionalMixin(mods = Mods.SABLE)
@Mixin(value = PlayerList.class, priority = 1200)
public class PlayerListSableMixin {
    @WrapOperation(method = "broadcast", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;position()Lnet/minecraft/world/phys/Vec3;"))
    private Vec3 railways$cameraPositionForSableBroadcast(ServerPlayer player, Operation<Vec3> original) {
        Vec3 position = original.call(player);
        return player.getCamera() instanceof ConductorEntity conductor ? conductor.position() : position;
    }
}
