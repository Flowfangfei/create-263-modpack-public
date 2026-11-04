package dev.ryanhcode.sable.mixin.compatibility.create;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Preserve Create's permission and Manhattan range checks in moving plot coordinates. */
@Mixin(targets="com.simibubi.create.foundation.networking.BlockEntityConfigurationPacket")
public class BlockEntityConfigurationPacketMixin {
    @WrapOperation(method="handle(Lnet/minecraft/server/level/ServerPlayer;)V",
        at=@At(value="INVOKE", target="Lnet/minecraft/core/BlockPos;distManhattan(Lnet/minecraft/core/Vec3i;)I"))
    private int sable$plotDistance(BlockPos playerPos, Vec3i targetPos, Operation<Integer> original,
            @Local(argsOnly=true) ServerPlayer player) {
        var subLevel = Sable.HELPER.getContaining(player.level(), BlockPos.containing(targetPos.getX(), targetPos.getY(), targetPos.getZ()));
        if (subLevel == null) return original.call(playerPos, targetPos);
        var local = subLevel.logicalPose().transformPositionInverse(JOMLConversion.toJOML(Vec3.atLowerCornerOf(playerPos)));
        return original.call(BlockPos.containing(local.x(), local.y(), local.z()), targetPos);
    }
}
