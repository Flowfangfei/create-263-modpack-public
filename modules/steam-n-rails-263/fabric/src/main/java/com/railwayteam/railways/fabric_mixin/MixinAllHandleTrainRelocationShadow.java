package com.railwayteam.railways.fabric_mixin;
import com.railwayteam.railways.content.shadow_realm.ShadowRealm;
import com.railwayteam.railways.content.shadow_realm.ShadowRealm.RestorationTarget;
import com.simibubi.create.content.trains.entity.TrainRelocationPacket;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=TrainRelocationPacket.class,priority=500,remap=false)
public abstract class MixinAllHandleTrainRelocationShadow {
 @Inject(method="handle",at=@At("HEAD"),cancellable=true)
 private void railways$restore(ServerPlayer player,CallbackInfo ci) {
  var packet=(TrainRelocationPacket)(Object)this;
  var target=new RestorationTarget(player.level(),packet.pos(),packet.hoveredBezier(),packet.direction(),packet.lookAngle());
  ShadowRealm.handleTrainRelocationPacket(player,packet.trainId(),target,ci);
 }
}
