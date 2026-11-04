package com.forsteri.createliquidfuel.mixin;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity.FuelType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = BlazeBurnerBlockEntity.class, remap = false)
public interface BlazeBurnerAccessor {
    @Accessor("remainingBurnTime")
    void createliquidfuel$setRemainingBurnTime(int time);
    @Accessor("activeFuel")
    void createliquidfuel$setActiveFuel(FuelType type);
}
