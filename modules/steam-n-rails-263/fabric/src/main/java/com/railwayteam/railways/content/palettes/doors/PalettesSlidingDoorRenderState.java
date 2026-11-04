package com.railwayteam.railways.content.palettes.doors;

import com.mojang.blaze3d.vertex.PoseStack;
import com.railwayteam.railways.fabric_mixin.client.AccessorSlidingDoorAnimation;
import com.railwayteam.railways.internal.compat.client.render.CachedBuffers;
import com.railwayteam.railways.internal.compat.client.render.SuperByteBufferRenderState;
import com.railwayteam.railways.registry.CRBlockPartials;
import com.simibubi.create.content.decoration.slidingDoor.SlidingDoorBlockEntity;
import net.createmod.catnip.api.math.AngleHelper;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import java.util.ArrayList;
import java.util.List;

/** Captures both animated halves for NeoForge's native submit renderer. */
public record PalettesSlidingDoorRenderState(List<SuperByteBufferRenderState> parts) {
    public interface Holder {
        PalettesSlidingDoorRenderState railways$paletteDoor();
        void railways$paletteDoor(PalettesSlidingDoorRenderState state);
    }
    public static PalettesSlidingDoorRenderState create(SlidingDoorBlockEntity be,float tick,int light) {
        var state=be.getBlockState();
        if (!(state.getBlock() instanceof PalettesSlidingDoorBlock block)) return null;
        var accessor=(AccessorSlidingDoorAnimation)be;
        if (!accessor.railways$shouldRenderSpecial(state)) return new PalettesSlidingDoorRenderState(List.of());
        var facing=state.getValue(DoorBlock.FACING);
        boolean flip=state.getValue(DoorBlock.HINGE)==DoorHingeSide.RIGHT;
        boolean windowed=state.getValue(PalettesSlidingDoorBlock.WINDOWED);
        float value=accessor.railways$animation().getValue(tick);
        float value2=Mth.clamp(value*10,0,1);
        float bridge=accessor.railways$isVisible(state)?1f/256:0;
        var offset=net.minecraft.world.phys.Vec3.atLowerCornerOf(facing.getUnitVec3i()).scale(value2/32+bridge);
        var result=new ArrayList<SuperByteBufferRenderState>();
        if (block.isFoldingDoor()) {
            var models=CRBlockPartials.FOLDING_DOORS.get(block.color).get(windowed);
            for (boolean left:new boolean[]{true,false}) {
                float sign=flip?-1:1;
                var buffer=CachedBuffers.partial(models.get(left^flip),state)
                    .light(light).translate(0,-1f/512,0).translate(offset)
                    .rotateCenteredDegrees(AngleHelper.horizontalAngle(facing.getClockWise()),Direction.Axis.Y);
                if(flip)buffer.translate(0,0,1);
                buffer.rotateYDegrees(91*sign*value*value);
                if(!left)buffer.translate(0,0,sign/2).rotateYDegrees(-181*sign*value*value);
                if(flip)buffer.translate(0,0,-.5f);
                result.add(buffer.extractRenderState());
            }
        } else {
            var models=CRBlockPartials.SLIDING_DOORS.get(block.color).get(windowed);
            Direction movement=flip?facing.getClockWise():facing.getCounterClockWise();
            var movementOffset=net.minecraft.world.phys.Vec3.atLowerCornerOf(movement.getUnitVec3i()).scale(value*value*13/16f);
            for(boolean upper:new boolean[]{false,true}) {
                var buffer=CachedBuffers.partial(models.get(upper),state).light(light)
                    .translate(offset.add(movementOffset)).translate(0,upper?1-1f/512:-1f/512,0)
                    .rotateCenteredDegrees(AngleHelper.horizontalAngle(facing.getClockWise()),Direction.Axis.Y);
                result.add(buffer.extractRenderState());
            }
        }
        return new PalettesSlidingDoorRenderState(List.copyOf(result));
    }
    public void submit(PoseStack poses,SubmitNodeCollector collector) { for(var part:parts)part.submit(poses,collector); }
}
