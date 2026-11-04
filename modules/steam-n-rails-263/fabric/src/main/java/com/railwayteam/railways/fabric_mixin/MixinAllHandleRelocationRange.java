package com.railwayteam.railways.fabric_mixin;
import com.railwayteam.railways.config.CRConfigs;
import com.simibubi.create.content.trains.entity.TrainRelocationPacket;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(value=TrainRelocationPacket.class,remap=false)
public abstract class MixinAllHandleRelocationRange {
 @WrapOperation(method="handle",at=@At(value="INVOKE",target="Lnet/minecraft/server/level/ServerPlayer;distanceToSqr(Lnet/minecraft/world/phys/Vec3;)D"))
 private double railways$creativeRange(ServerPlayer player,Vec3 position,Operation<Double> original) { return player.isCreative() && CRConfigs.server().unlimitedCreativeRelocation.get()?0:original.call(player,position); }
 @WrapOperation(method="handle",at=@At(value="INVOKE",target="Lnet/minecraft/server/level/ServerPlayer;distanceToSqr(Lnet/minecraft/world/entity/Entity;)D"))
 private double railways$creativeEntityRange(ServerPlayer player,Entity entity,Operation<Double> original) { return player.isCreative() && CRConfigs.server().unlimitedCreativeRelocation.get()?0:original.call(player,entity); }
}
