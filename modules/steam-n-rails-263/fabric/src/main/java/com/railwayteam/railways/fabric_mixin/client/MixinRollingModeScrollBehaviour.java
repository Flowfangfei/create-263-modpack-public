package com.railwayteam.railways.fabric_mixin.client;
import com.railwayteam.railways.content.roller_extensions.RailwaysRollerModeBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=ScrollOptionBehaviour.class,remap=false)
public abstract class MixinRollingModeScrollBehaviour {
 @Inject(method="getIconForSelected",at=@At("HEAD"),cancellable=true)
 private void railways$fourthRollerIcon(CallbackInfoReturnable<INamedIconOptions> ci) {
  if((Object)this instanceof RailwaysRollerModeBehaviour mode)ci.setReturnValue(mode.selectedIcon());
 }
}
