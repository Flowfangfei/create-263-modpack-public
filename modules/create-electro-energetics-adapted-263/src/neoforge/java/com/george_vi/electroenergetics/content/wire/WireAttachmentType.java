package com.george_vi.electroenergetics.content.wire;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.george_vi.electroenergetics.CEEPartialModels.Bufferer;
import net.minecraft.client.renderer.LevelRenderer;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

public abstract class WireAttachmentType {
    public abstract float getWidth(WireAttachment attachment);

    public abstract float getHeight(WireAttachment attachment);

    @OnlyIn(Dist.CLIENT)
    public abstract void render(PoseStack pose, MultiBufferSource buffer, WireAttachment attachment, Vec3 pos, int light, float pitch);

    @OnlyIn(Dist.CLIENT)
    public void render(PoseStack pose, MultiBufferSource buffer, LevelRenderer levelRenderer, WireAttachment attachment, Vec3 pos, int light, float pitch) {
        render(pose, buffer, attachment, pos, light, pitch);
    };

    @OnlyIn(Dist.CLIENT)
    public void renderChain(PoseStack pose, MultiBufferSource buffer, int light) {
        com.george_vi.electroenergetics.client.CEEWireAttachmentRenderers.renderChain(pose, buffer, light);
    }

    public abstract List<ItemStack> getDrops(WireAttachment attachment, Level level);

    public List<ItemStack> getItemRequirements(WireAttachment attachment) {
        return List.of(ItemStack.EMPTY);
    }

    public static class Empty extends WireAttachmentType {
        @Override
        public float getWidth(WireAttachment attachment) {
            return 0;
        }

        @Override
        public float getHeight(WireAttachment attachment) {
            return 0;
        }

        @Override
        public void render(PoseStack pose, MultiBufferSource buffer, WireAttachment attachment, Vec3 pos, int light, float pitch) {

        }

        @Override
        public List<ItemStack> getDrops(WireAttachment attachment, Level level) {
            return List.of();
        }
    }
}
