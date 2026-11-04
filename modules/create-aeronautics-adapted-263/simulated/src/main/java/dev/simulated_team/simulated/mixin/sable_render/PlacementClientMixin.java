package dev.simulated_team.simulated.mixin.sable_render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.createmod.catnip.impl.client.placement.PlacementClient;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = PlacementClient.class, remap = false)
public abstract class PlacementClientMixin {
    // Catnip's direction indicator calls its own private projection helper.
    @WrapOperation(
            method = "drawDirectionIndicator(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;FFF)V",
            at = @At(value = "INVOKE", target = "Lnet/createmod/catnip/impl/client/placement/PlacementClient;projectToPlayerView(Lnet/minecraft/world/phys/Vec3;F)Lnet/minecraft/world/phys/Vec3;", remap = false))
    private static Vec3 simulated$projectPhysicalPlacementTarget(final Vec3 target, final float partialTick,
                                                                final Operation<Vec3> original) {
        final var level = Minecraft.getInstance().level;
        if (level != null && Sable.HELPER.getContaining(level, target) instanceof final ClientSubLevel subLevel) {
            return original.call(subLevel.renderPose(partialTick).transformPosition(target), partialTick);
        }
        return original.call(target, partialTick);
    }
}