package com.george_vi.electroenergetics.mixins;

import com.george_vi.electroenergetics.events.GameEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpawnPlacements.class)
public class SpawnPlacementsMixin {
    @Inject(method = "checkSpawnRules", at = @At("HEAD"), cancellable = true)
    private static void electroenergetics$preventNaturalSpawns(EntityType<?> type, ServerLevelAccessor level,
        EntitySpawnReason reason, BlockPos pos, RandomSource random, CallbackInfoReturnable<Boolean> callback) {
        if (reason == EntitySpawnReason.NATURAL && GameEvents.preventsNaturalSpawn(level.getLevel(), pos))
            callback.setReturnValue(false);
    }
}
