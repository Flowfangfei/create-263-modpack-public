package com.railwayteam.railways.fabric_mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.railwayteam.railways.content.palettes.doors.PalettesSlidingDoorRenderState;
import com.simibubi.create.content.decoration.slidingDoor.SlidingDoorBlockEntity;
import com.simibubi.create.content.decoration.slidingDoor.SlidingDoorRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=SlidingDoorRenderer.class,remap=false)
public abstract class MixinSlidingDoorRenderer {
    @Inject(method="extractRenderState(Lcom/simibubi/create/content/decoration/slidingDoor/SlidingDoorBlockEntity;Lnet/minecraft/client/renderer/blockentity/state/BlockEntityRenderState;FLnet/minecraft/world/phys/Vec3;Lnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V",at=@At("TAIL"))
    private void railways$extract(SlidingDoorBlockEntity be,BlockEntityRenderState state,float tick,Vec3 camera,
        ModelFeatureRenderer.CrumblingOverlay crumbling,CallbackInfo ci) {
        if(state instanceof PalettesSlidingDoorRenderState.Holder holder)
            holder.railways$paletteDoor(PalettesSlidingDoorRenderState.create(be,tick,state.lightCoords));
    }
    @Inject(method="submit",at=@At("HEAD"),cancellable=true)
    private void railways$submit(BlockEntityRenderState state,PoseStack poses,SubmitNodeCollector collector,
        CameraRenderState camera,CallbackInfo ci) {
        if(state instanceof PalettesSlidingDoorRenderState.Holder holder && holder.railways$paletteDoor()!=null) {
            holder.railways$paletteDoor().submit(poses,collector);ci.cancel();
        }
    }
}
