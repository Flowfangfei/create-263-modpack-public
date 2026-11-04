package dev.ryanhcode.sable.mixin.entity.entity_swimming;

import com.llamalad7.mixinextras.sugar.Local;
import dev.ryanhcode.sable.mixinhelpers.entity.entity_swimming.SubLevelFluidHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** NeoForge moves the swimming fluid lookup into its entity extension predicate. */
@Mixin(targets="net.neoforged.neoforge.common.extensions.IEntityExtension")
public interface EntityMixin {
    @Redirect(method="lambda$canStartSwimming$0", at=@At(value="INVOKE",
        target="Lnet/minecraft/world/level/Level;getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;"))
    private static FluidState sable$subLevelFluidAtBlockPos(Level level, BlockPos pos,
            @Local(argsOnly=true) Entity entity) {
        return SubLevelFluidHelper.getFluidStateAt(entity,pos);
    }
}
