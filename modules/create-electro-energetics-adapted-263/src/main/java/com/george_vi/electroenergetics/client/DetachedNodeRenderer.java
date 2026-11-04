package com.george_vi.electroenergetics.client;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.LightCoordsUtil;
import com.george_vi.electroenergetics.CEEPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.george_vi.electroenergetics.client.render.ModelBuffers;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import com.george_vi.electroenergetics.client.render.RenderCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class DetachedNodeRenderer {
    public static void render(ClientLevel level, PoseStack pose, RenderCollector buffer, Camera camera) {
        Minecraft mc = Minecraft.getInstance();

        pose.pushPose();
        pose.translate(-camera.position().x(), -camera.position().y(), -camera.position().z());

        for (ClientNodeData nodeData : WireRenderer.NODE_DATA.values()) {
            Vec3 pos = nodeData.getPos(AnimationTickHolder.getPartialTicks());
            if (pos == null)
                continue;
            ModelBuffers.partial(CEEPartialModels.WIRE_TIE, Blocks.ANDESITE.defaultBlockState())
                    .translate(pos)
                    .light(LightCoordsUtil.getLightCoords(level, BlockPos.containing(pos)))
                    .renderInto(pose, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));
        }

        pose.popPose();
    }
}
