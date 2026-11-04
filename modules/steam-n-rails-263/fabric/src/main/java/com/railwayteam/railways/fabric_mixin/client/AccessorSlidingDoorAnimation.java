package com.railwayteam.railways.fabric_mixin.client;

import com.simibubi.create.content.decoration.slidingDoor.SlidingDoorBlockEntity;
import net.createmod.catnip.api.animation.LerpedFloat;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value=SlidingDoorBlockEntity.class,remap=false)
public interface AccessorSlidingDoorAnimation {
    @Accessor("animation") LerpedFloat railways$animation();
    @Invoker("shouldRenderSpecial") boolean railways$shouldRenderSpecial(BlockState state);
    @Invoker("isVisible") boolean railways$isVisible(BlockState state);
}
