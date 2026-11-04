package dev.eriksonn.aeronautics.content.blocks.propeller.small;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.contraptions.bearing.BearingBlock;
import dev.simulated_team.simulated.compat.create.KineticBlockEntityRenderer;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.api.math.AngleHelper;
import dev.simulated_team.simulated.compat.render.CachedBuffers;
import dev.simulated_team.simulated.compat.render.SuperByteBuffer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public abstract class SimplePropellerRenderer<T extends BasePropellerBlockEntity> extends KineticBlockEntityRenderer<T> {

    private static boolean smokeLogged;

    public SimplePropellerRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void renderSafe(final T be, final float partialTicks, final PoseStack ms, final SubmitNodeCollector buffer, final int light, final int overlay) {
        final boolean diagnose = Boolean.getBoolean("simulated.clientSmokeTest") && !smokeLogged;
        if (diagnose) {
            smokeLogged = true;
            dev.eriksonn.aeronautics.Aeronautics.LOGGER.info("Propeller render probe: pos={}, sublevel={}, visualization={}, speed={}", be.getBlockPos(), isRenderingInSubLevel(), VisualizationManager.supportsVisualization(be.getLevel()), be.getSpeed());
        }
        if (VisualizationManager.supportsVisualization(be.getLevel()) && !isRenderingInSubLevel()) {
            return;
        }

        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

        final BlockState state = be.getBlockState();
        final Direction dir = state.getValue(BlockStateProperties.FACING);


        final SuperByteBuffer propeller = CachedBuffers.partial(this.getCurrentModel(be), state);

        if (diagnose) dev.eriksonn.aeronautics.Aeronautics.LOGGER.info("Propeller geometry probe: model={}, empty={}, facing={}", getCurrentModel(be), propeller.isEmpty(), dir);
        final float angle = this.getAngle(partialTicks, dir, be);
        kineticRotationTransform(propeller, be, dir.getAxis(), angle, light);

        if (dir.getAxis().isHorizontal()) {
            propeller.rotateCentered(AngleHelper.rad(AngleHelper.horizontalAngle(dir.getOpposite())), Direction.UP);
        }
        if (dir.getAxis().isVertical()) {
            propeller.rotateCentered(AngleHelper.rad(AngleHelper.verticalAngle(dir.getOpposite())), Direction.EAST);
        }

        propeller.translate(0, 0, -3 / 16f).rotateCentered(AngleHelper.rad(-90 - AngleHelper.verticalAngle(dir)), Direction.EAST);

        propeller.submit(RenderTypes.solidMovingBlock(), ms, buffer);
    }

    public abstract PartialModel getCurrentModel(T be);

    public float getAngle(final float partialTicks, final Direction dir, final T be) {
        float angle = be.getPreviousAngle() * (1f - partialTicks) + be.getAngle() * partialTicks;

        angle = angle / 180f * (float) Math.PI;

        angle *= 2;

        return angle;
    }

    @Override
    protected SuperByteBuffer getRotatedModel(final T be, final BlockState state) {
        return CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state, state
                .getValue(BearingBlock.FACING)
                .getOpposite());
    }
}
