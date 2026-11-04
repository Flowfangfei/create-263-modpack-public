package com.george_vi.electroenergetics.client;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import net.createmod.catnip.api.client.animation.AnimationTickHolder;
import com.george_vi.electroenergetics.CEEPartialModels.Bufferer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class DetachedNodeRenderer {
    @OnlyIn(Dist.CLIENT)
    public static void render(ClientLevel level, PoseStack pose, MultiBufferSource buffer, Camera camera) {
        Minecraft mc = Minecraft.getInstance();

        pose.pushPose();
        pose.translate(-camera.position().x(), -camera.position().y(), -camera.position().z());

        for (ClientNodeData nodeData : WireRenderer.NODE_DATA.values()) {
            Vec3 pos = nodeData.getPos(AnimationTickHolder.getPartialTicks());
            if (pos == null)
                continue;
            Bufferer.partial(CEEPartialModels.WIRE_TIE, Blocks.ANDESITE.defaultBlockState())
                    .translate(pos)
                    .light(net.minecraft.util.LightCoordsUtil.getLightCoords(level, BlockPos.containing(pos)))
                    .renderInto(pose, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));
        }

        pose.popPose();
    }
}
