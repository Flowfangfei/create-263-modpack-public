package com.railwayteam.railways.fabric_mixin.client;
import com.simibubi.create.content.kinetics.flywheel.FlywheelBlockEntity;
import net.createmod.catnip.api.animation.LerpedFloat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(value=FlywheelBlockEntity.class,remap=false)
public interface AccessorFlywheelAnimation {
 @Accessor("angle") float railways$angle();
 @Accessor("visualSpeed") LerpedFloat railways$visualSpeed();
}
