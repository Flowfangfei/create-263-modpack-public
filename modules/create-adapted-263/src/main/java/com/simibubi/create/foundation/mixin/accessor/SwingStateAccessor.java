package com.simibubi.create.foundation.mixin.accessor;

import net.minecraft.world.entity.LivingEntity.SwingDescription;
import net.minecraft.world.entity.LivingEntity.SwingState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SwingState.class)
public interface SwingStateAccessor {
   @Accessor("currentSwing")
   void create$setCurrentSwing(@Nullable SwingDescription var1);
}
