package com.railwayteam.railways.fabric_mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.railwayteam.railways.content.animated_flywheel.FlywheelMovementBehaviour;
import com.railwayteam.railways.content.animated_flywheel.FlywheelMovementRender;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.render.ContraptionEntityRenderer;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import com.simibubi.create.foundation.render.CreateVisualizationManager;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.LightCoordsUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=ContraptionEntityRenderer.class,remap=false)
public abstract class MixinRailwaysFlywheelActors {
    @Inject(method="submitActors",at=@At("TAIL"))
    private static void railways$submitFlywheels(AbstractContraptionEntity entity,VirtualRenderWorld world,
        Contraption contraption,PoseStack poses,SubmitNodeCollector collector,float tick,CallbackInfo ci) {
        if(CreateVisualizationManager.supportsVisualization(entity.level()))return;
        for(var actor:contraption.getActors()) {
            var block=actor.getLeft();var context=actor.getRight();
            if(context==null || contraption.isHiddenInPortal(block.pos())
                || MovementBehaviour.REGISTRY.get(block.state())!=FlywheelMovementBehaviour.INSTANCE)continue;
            poses.pushPose();poses.translate(block.pos().getX(),block.pos().getY(),block.pos().getZ());
            try { FlywheelMovementRender.submit(context,poses,collector,LightCoordsUtil.getLightCoords(world,block.pos()),tick); }
            finally { poses.popPose(); }
        }
    }
}
