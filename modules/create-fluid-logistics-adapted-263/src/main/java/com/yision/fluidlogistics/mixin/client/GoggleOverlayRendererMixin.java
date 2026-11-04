package com.yision.fluidlogistics.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.equipment.goggles.GoggleOverlayRenderer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

@Mixin(GoggleOverlayRenderer.class)
public class GoggleOverlayRendererMixin {

    @ModifyExpressionValue(method = "renderOverlay(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V",
        at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;hitResult:Lnet/minecraft/world/phys/HitResult;"))
    private static HitResult fluidlogistics$entityOverlayHit(HitResult hitResult,
        @Share("fluidlogistics$entityHitResult") LocalRef<EntityHitResult> result) {
        if (hitResult instanceof EntityHitResult entityResult && entityResult.getType() != HitResult.Type.MISS
            && entityResult.getEntity() instanceof IHaveGoggleInformation) {
            result.set(entityResult);
            // Current Create stores the field result in an unnamed compiler temporary
            // before its pattern check/cast. Replace the read itself, not an unavailable local.
            return new BlockHitResult(entityResult.getLocation(), Direction.getApproximateNearest(entityResult.getLocation()),
                BlockPos.containing(entityResult.getLocation()), true);
        }
        return hitResult;
    }

	@Definition(id = "IHaveGoggleInformation", type = IHaveGoggleInformation.class)
	@Expression("? instanceof IHaveGoggleInformation")
	@ModifyExpressionValue(method = "renderOverlay(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", at = @At("MIXINEXTRAS:EXPRESSION"), remap = false)
	private static boolean fluidlogistics$modifyBlockEntityInstanceOfCheck(boolean original,
		@Share("fluidlogistics$entityHitResult") LocalRef<EntityHitResult> result) {
		if (result.get() != null && result.get().getEntity() instanceof IHaveGoggleInformation) {
			return true;
		}
		return original;
	}

	@Definition(id = "IHaveGoggleInformation", type = IHaveGoggleInformation.class)
	@Expression("(IHaveGoggleInformation) ?")
	@WrapOperation(method = "renderOverlay(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", at = @At("MIXINEXTRAS:EXPRESSION"))
	private static IHaveGoggleInformation fluidlogistics$wrapBlockEntityCast(Object object,
		Operation<IHaveGoggleInformation> original,
		@Share("fluidlogistics$entityHitResult") LocalRef<EntityHitResult> result) {
		if (result.get() != null && result.get().getEntity() instanceof IHaveGoggleInformation gte) {
			return gte;
		}
		return original.call(object);
	}
}
