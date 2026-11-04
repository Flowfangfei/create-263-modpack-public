package com.railwayteam.railways.fabric_mixin.client;

import com.railwayteam.railways.content.palettes.doors.PalettesSlidingDoorRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(targets="com.simibubi.create.content.decoration.slidingDoor.SlidingDoorRenderer$SlidingDoorRenderState",remap=false)
public abstract class MixinSlidingDoorRenderState implements PalettesSlidingDoorRenderState.Holder {
    @Unique private PalettesSlidingDoorRenderState railways$paletteDoor;
    @Override public PalettesSlidingDoorRenderState railways$paletteDoor() { return railways$paletteDoor; }
    @Override public void railways$paletteDoor(PalettesSlidingDoorRenderState state) { railways$paletteDoor=state; }
}
