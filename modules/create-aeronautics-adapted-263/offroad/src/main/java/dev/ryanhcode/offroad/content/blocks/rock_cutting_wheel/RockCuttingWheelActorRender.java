package dev.ryanhcode.offroad.content.blocks.rock_cutting_wheel;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import dev.ryanhcode.offroad.index.OffroadPartialModels;
import dev.simulated_team.simulated.compat.render.CachedBuffers;
import net.createmod.catnip.api.animation.LerpedFloat;
import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.core.Direction;
public final class RockCuttingWheelActorRender {
    public static void render(MovementContext context,VirtualRenderWorld world,ContraptionMatrices matrices,MultiBufferSource buffers){
        if(dev.engine_room.flywheel.api.visualization.VisualizationManager.supportsVisualization(context.world))return;
        if(context.temporaryData==null)context.temporaryData=LerpedFloat.angular();
        var wheel=CachedBuffers.partial(OffroadPartialModels.ROCK_CUTTING_WHEEL_WHEEL,context.state);
        var facing=context.state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING);
        boolean first=context.state.getValue(RockCuttingWheelBlock.AXIS_ALONG_FIRST_COORDINATE);
        if((facing.getAxis()==Direction.Axis.Z||facing.getAxis()==Direction.Axis.Y)^first)wheel.rotateCentered(facing.getRotation()).rotateZCenteredDegrees(90).translate(.625,.5,0);
        else wheel.rotateCentered(facing.getRotation()).rotateXCenteredDegrees(90).translate(0,.5,-.625);
        float angle=((LerpedFloat)context.temporaryData).getValue(AnimationTickHolder.getPartialTicks());
        wheel.rotateYCenteredDegrees(angle).light(net.minecraft.util.LightCoordsUtil.getLightCoords(world,context.localPos))
            .useLevelLight(world,matrices.getLight()).renderInto(matrices.getModelViewProjection(),buffers.getBuffer(net.minecraft.client.renderer.rendertype.RenderTypes.solidMovingBlock()));
    }
}
