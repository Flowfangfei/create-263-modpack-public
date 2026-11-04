package dev.khloeleclair.create.additionallogistics.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.api.client.render.CachedBuffers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import java.util.List;

/** Submit the same partial geometry and facing transform through the 26.3 collector. */
public final class CALRenderModels {
    private CALRenderModels() {}
    public static void submit(PartialModel partial, Direction facing, boolean vertical, PoseStack pose,
                              SubmitNodeCollector collector, RenderType layer, int light) {
        var part = partial.get();
        if (part == null) return;
        pose.pushPose();
        if (facing != null) pose.mulPose((vertical ? CachedBuffers.rotateToFaceVertical(facing) : CachedBuffers.rotateToFace(facing)).get().last().pose());
        collector.submitBlockModel(pose, layer, List.of(part), BlockModelRenderState.EMPTY_TINTS, light, OverlayTexture.NO_OVERLAY, 0);
        pose.popPose();
    }
}
