package com.railwayteam.railways.fabric_mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.railwayteam.railways.content.custom_tracks.monorail.CustomTrackBlockOutline;
import com.railwayteam.railways.registry.CRTrackMaterials;
import com.simibubi.create.content.trains.track.BezierConnection;
import com.simibubi.create.content.trains.track.TrackBlock;
import com.simibubi.create.content.trains.track.TrackBlockOutline;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=TrackBlockOutline.class,remap=false)
public abstract class MixinTrackBlockOutline {
 @ModifyExpressionValue(method="pickFromTurns",at=@At(value="INVOKE",target="Lnet/createmod/catnip/api/math/VoxelShaper;get(Lnet/minecraft/core/Direction;)Lnet/minecraft/world/phys/shapes/VoxelShape;"))
 private static VoxelShape railways$curveBounds(VoxelShape shape,@Local BezierConnection connection) { return CustomTrackBlockOutline.convert(shape,connection.getMaterial()); }
 @ModifyExpressionValue(method="lambda$submitCurveSelection$0",at=@At(value="INVOKE",target="Lnet/createmod/catnip/api/math/VoxelShaper;get(Lnet/minecraft/core/Direction;)Lnet/minecraft/world/phys/shapes/VoxelShape;"))
 private static VoxelShape railways$curveOutline(VoxelShape shape) {
  var selection=TrackBlockOutline.result;if(selection==null)return shape;
  var connection=selection.blockEntity().getConnections().get(selection.loc().curveTarget());
  return connection==null?shape:CustomTrackBlockOutline.convert(shape,connection.getMaterial());
 }
 @Inject(method="extractDiagonalTrackSelection",at=@At("HEAD"),cancellable=true)
 private static void railways$materialBlockShape(ExtractBlockOutlineRenderStateEvent event,CallbackInfo ci) {
  if(event.getBlockState().getBlock() instanceof TrackBlock block) {
   var type=CRTrackMaterials.getType(block.getMaterial());
   if(type==CRTrackMaterials.CRTrackType.MONORAIL || type==CRTrackMaterials.CRTrackType.NARROW_GAUGE)ci.cancel();
  }
 }
}
