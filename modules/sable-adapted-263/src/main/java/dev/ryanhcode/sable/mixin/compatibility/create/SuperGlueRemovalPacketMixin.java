package dev.ryanhcode.sable.mixin.compatibility.create;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.ryanhcode.sable.Sable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Evaluate glue removal range across the world and moving plot coordinate systems. */
@Mixin(targets="com.simibubi.create.content.contraptions.glue.SuperGlueRemovalPacket")
public class SuperGlueRemovalPacketMixin {
    @WrapOperation(method="handle(Lnet/minecraft/server/level/ServerPlayer;)V",
        at=@At(value="INVOKE", target="Lnet/minecraft/server/level/ServerPlayer;distanceToSqr(Lnet/minecraft/world/phys/Vec3;)D"))
    private double sable$glueDistance(ServerPlayer player, Vec3 gluePos, Operation<Double> original) {
        return Sable.HELPER.distanceSquaredWithSubLevels(player.level(), player.position(), gluePos);
    }
}
