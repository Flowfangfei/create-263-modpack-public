package com.railwayteam.railways.fabric_mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.railwayteam.railways.content.custom_tracks.phantom.PhantomTrackOverlays;
import com.simibubi.create.content.trains.track.TrackBlock;
import com.simibubi.create.content.trains.track.BezierTrackPointLocation;
import com.simibubi.create.content.trains.track.TrackTargetingBehaviour.RenderedTrackOverlayType;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.transform.Affine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value=TrackBlock.class,remap=false)
public class MixinStandardTrackBlockRendererPhantom {
 @Inject(method="prepareTrackOverlay(Ldev/engine_room/flywheel/lib/transform/Affine;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lcom/simibubi/create/content/trains/track/BezierTrackPointLocation;Lnet/minecraft/core/Direction$AxisDirection;Lcom/simibubi/create/content/trains/track/TrackTargetingBehaviour$RenderedTrackOverlayType;)Ldev/engine_room/flywheel/lib/model/baked/PartialModel;",at=@At("HEAD"),cancellable=true)
 private void railways$hideInstanced(Affine<?> pose,BlockGetter world,BlockPos pos,BlockState state,
  BezierTrackPointLocation bezier,AxisDirection direction,RenderedTrackOverlayType type,CallbackInfoReturnable<PartialModel> ci) {
  if(PhantomTrackOverlays.isHidden(world,pos,state,bezier)){pose.scale(0);ci.setReturnValue(null);}
 }
 @Inject(method="prepareTrackOverlay(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lcom/simibubi/create/content/trains/track/BezierTrackPointLocation;Lnet/minecraft/core/Direction$AxisDirection;Lcom/simibubi/create/content/trains/track/TrackTargetingBehaviour$RenderedTrackOverlayType;)Ldev/engine_room/flywheel/lib/model/baked/PartialModel;",at=@At("HEAD"),cancellable=true)
 private void railways$hideSubmitted(PoseStack pose,BlockGetter world,BlockPos pos,BlockState state,
  BezierTrackPointLocation bezier,AxisDirection direction,RenderedTrackOverlayType type,CallbackInfoReturnable<PartialModel> ci) {
  if(PhantomTrackOverlays.isHidden(world,pos,state,bezier))ci.setReturnValue(null);
 }
}
