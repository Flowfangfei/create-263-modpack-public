package dev.simulated_team.simulated.mixin.extra_kinetics.goggle_tooltips;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import dev.simulated_team.simulated.compat.create.ExtraKineticsTooltip;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = KineticBlockEntity.class, remap = false)
public class KineticBlockEntityMixin {

    @Inject(method = "addToGoggleTooltip", at = @At("RETURN"), cancellable = true)
    public void simulated$addExtraKineticsInfo(final List<Component> tooltip, final boolean isPlayerSneaking, final CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(ExtraKineticsTooltip.append((KineticBlockEntity)(Object)this, tooltip, isPlayerSneaking, cir.getReturnValue()));
    }
}
