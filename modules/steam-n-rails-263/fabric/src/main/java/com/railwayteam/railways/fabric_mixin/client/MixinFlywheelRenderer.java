package com.railwayteam.railways.fabric_mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.railwayteam.railways.content.animated_flywheel.FlywheelMovementRender;
import com.railwayteam.railways.content.palettes.PalettesFlywheelBlock;
import com.railwayteam.railways.internal.compat.client.render.CachedBuffers;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.flywheel.FlywheelBlockEntity;
import com.simibubi.create.content.kinetics.flywheel.FlywheelRenderer;
import com.simibubi.create.foundation.render.CreateVisualizationManager;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=FlywheelRenderer.class,remap=false)
public abstract class MixinFlywheelRenderer extends KineticBlockEntityRenderer<FlywheelBlockEntity> {
    protected MixinFlywheelRenderer(BlockEntityRendererProvider.Context context) { super(context); }
    @Inject(method="submit",at=@At("HEAD"),cancellable=true)
    private void railways$paletteWheel(BlockEntityRenderState state,PoseStack poses,SubmitNodeCollector collector,
        CameraRenderState camera,CallbackInfo ci) {
        if(!(state instanceof KineticRenderState kinetic) || !(kinetic.blockEntity instanceof FlywheelBlockEntity be)
            || !(be.getBlockState().getBlock() instanceof PalettesFlywheelBlock))return;
        super.submit(state,poses,collector,camera);
        if(!CreateVisualizationManager.supportsVisualization(be.getLevel())) {
            var direction=Direction.fromAxisAndDirection(getRotationAxisOf(be),Direction.AxisDirection.POSITIVE);
            float angle=((AccessorFlywheelAnimation)be).railways$angle()+((AccessorFlywheelAnimation)be).railways$visualSpeed().getValue(kinetic.partialTicks)*3/10*kinetic.partialTicks;
            CachedBuffers.partial(FlywheelMovementRender.getWheelModel(be.getBlockState()),be.getBlockState())
                .light(state.lightCoords).center()
                .rotate(new Quaternionf().rotateTo(0,1,0,direction.getStepX(),direction.getStepY(),direction.getStepZ()))
                .rotateYDegrees(angle).uncenter()
                .submit(net.minecraft.client.renderer.rendertype.RenderTypes.cutoutMovingBlock(),poses,collector);
        }
        ci.cancel();
    }
}
