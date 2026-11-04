package dev.ryanhcode.offroad.neoforge.mixin.client;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.content.contraptions.render.ActorVisual;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.ryanhcode.offroad.content.blocks.rock_cutting_wheel.RockCuttingWheelActor;
import dev.ryanhcode.offroad.content.blocks.rock_cutting_wheel.RockCuttingWheelActorRender;
import dev.ryanhcode.offroad.content.blocks.rock_cutting_wheel.RockCuttingWheelActorVisual;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.createmod.catnip.api.animation.LerpedFloat;
import org.spongepowered.asm.mixin.Mixin;
/** Client-only method descriptors never enter the dedicated server's actor class. */
@Mixin(RockCuttingWheelActor.class)
public abstract class RockCuttingWheelActorClientMixin implements MovementBehaviour {
    @Override public void renderInContraption(MovementContext context,VirtualRenderWorld world,ContraptionMatrices matrices,MultiBufferSource buffers){RockCuttingWheelActorRender.render(context,world,matrices,buffers);}
    @Override public ActorVisual createVisual(VisualizationContext visualization,VirtualRenderWorld world,MovementContext context){
        if(context.temporaryData==null)context.temporaryData=LerpedFloat.angular();return new RockCuttingWheelActorVisual(visualization,world,context);
    }
}
