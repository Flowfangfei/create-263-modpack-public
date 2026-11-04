package dev.simulated_team.simulated.content.blocks.redstone.redstone_inductor;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.simulated_team.simulated.compat.create.SmartBlockEntityRenderer;
import dev.simulated_team.simulated.index.SimPartialModels;
import dev.simulated_team.simulated.util.SimColors;
import net.createmod.catnip.api.math.AngleHelper;
import dev.simulated_team.simulated.compat.render.CachedBuffers;
import dev.simulated_team.simulated.compat.render.SuperByteBuffer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class RedstoneInductorRenderer extends SmartBlockEntityRenderer<RedstoneInductorBlockEntity> {

    public RedstoneInductorRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(RedstoneInductorBlockEntity be,float partialTicks,PoseStack poseStack,
            SubmitNodeCollector collector,int light,int overlay){
        super.renderSafe(be,partialTicks,poseStack,collector,light,overlay);
        renderInSubLevel(be,partialTicks,poseStack,collector,light,overlay);
    }

    public static void renderInSubLevel(final RedstoneInductorBlockEntity be, final float partialTicks,
                                        final PoseStack poseStack, final SubmitNodeCollector bufferSource,
                                        final int light, final int overlay) {
        final Direction facing = be.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        CachedBuffers.partial(SimPartialModels.REDSTONE_INDUCTOR_INDICATOR, be.getBlockState())
                .translate(0.5, 0, 0.5)
                .rotateYDegrees(AngleHelper.horizontalAngle(facing))
                .color(getIndicatorColor(be, partialTicks))
                .light(light)
                .submit(poseStack, bufferSource);
    }

    private static int getIndicatorColor(final RedstoneInductorBlockEntity be, final float partialTicks) {
        final float state = be.lerpedState.getValue(partialTicks);
        return SimColors.redstone(state / 15F);
    }
}
