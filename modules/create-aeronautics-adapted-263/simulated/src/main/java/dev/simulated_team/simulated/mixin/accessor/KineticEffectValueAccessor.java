package dev.simulated_team.simulated.mixin.accessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(targets="com.simibubi.create.content.kinetics.base.KineticEffectHandler")
public interface KineticEffectValueAccessor{
    @Accessor("overStressedEffect") float simulated$getOverstress();
}
