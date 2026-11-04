package com.george_vi.electroenergetics.client.render;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.content.fuse.fuse_held.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.foundation.render.CreateRenderTypes;

import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;

public final class FuseHeldRenderers {
    public static void render(
            FuseHoldable type,
            CompoundTag data,
            PoseStack pose,
            RenderCollector buffer,
            int light) {
        if (type instanceof FuseHeldFuse) render$FuseHeldFuse(data, pose, buffer, light);
        else if (type instanceof FuseHeldSwitch) render$FuseHeldSwitch(data, pose, buffer, light);
        else if (type instanceof FuseHeldIndicatorBulb)
            render$FuseHeldIndicatorBulb(data, pose, buffer, light);
        else
            ModelBuffers.partial(
                            CEEPartialModels.FUSE_HOLDER_COPPER_CONDUCTOR,
                            Blocks.ANDESITE.defaultBlockState())
                    .light(light)
                    .renderInto(pose, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));
    }

    private static void render$FuseHeldFuse(
            CompoundTag data, PoseStack pose, RenderCollector buffer, int light) {
        ModelBuffers.partial(
                        data.getBooleanOr("Broken", false)
                                ? CEEPartialModels.FUSE_HOLDER_BROKEN_FUSE
                                : CEEPartialModels.FUSE_HOLDER_FUSE,
                        Blocks.ANDESITE.defaultBlockState())
                .light(light)
                .renderInto(pose, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));
    }

    private static void render$FuseHeldSwitch(
            CompoundTag data, PoseStack pose, RenderCollector buffer, int light) {
        ModelBuffers.partial(
                        data.getBooleanOr("Closed", false)
                                ? CEEPartialModels.FUSE_HOLDER_SWITCH_CLOSED
                                : CEEPartialModels.FUSE_HOLDER_SWITCH,
                        Blocks.ANDESITE.defaultBlockState())
                .light(light)
                .renderInto(pose, buffer.getBuffer(RenderTypes.solidMovingBlock()));
    }

    private static void render$FuseHeldIndicatorBulb(
            CompoundTag data, PoseStack pose, RenderCollector buffer, int light) {
        float lightStrength = data.getFloatOr("Light", 0f);
        DyeColor color = DyeColor.byName(data.getStringOr("Color", "white"), DyeColor.WHITE);
        ModelBuffers.partial(
                        CEEPartialModels.FUSE_HOLDER_INDICATOR_BULB,
                        Blocks.ANDESITE.defaultBlockState())
                .light(light)
                .renderInto(pose, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));

        ModelBuffers.partial(
                        CEEPartialModels.INDICATOR_BULB_TUBE, Blocks.ANDESITE.defaultBlockState())
                .color(color.getTextColor())
                .translate(-2 / 16f, 1.5 / 16f, 4 / 16f)
                .light(light)
                .disableDiffuse()
                .scale(0.5f, 0.75f, 0.5f)
                .renderInto(
                        pose,
                        buffer.getBuffer(
                                lightStrength > 0.01f
                                        ? CreateRenderTypes.additive()
                                        : RenderTypes.translucentMovingBlock()));

        if (lightStrength > 0.01) {
            float factor = Mth.clamp(lightStrength * 2, 0, 1);
            int newColor =
                    ((int) (((color.getFireworkColor() >> 16) & 0xFF) * factor) << 16)
                            | ((int) (((color.getFireworkColor() >> 8) & 0xFF) * factor) << 8)
                            | ((int) ((color.getFireworkColor() & 0xFF) * factor));

            ModelBuffers.partial(
                            CEEPartialModels.INDICATOR_BULB_GLOW,
                            Blocks.ANDESITE.defaultBlockState())
                    .color(newColor)
                    .light(0xf000f0)
                    .disableDiffuse()
                    .translate(0 / 16f, 7 / 16f, 8 / 16f)
                    .scale(1.25f, 2.55f, 1.25f)
                    .translate(-4 / 16f, -5 / 16f, -8 / 16f)
                    .renderInto(pose, buffer.getBuffer(CreateRenderTypes.additive()));

            int cubeColor =
                    ((int) (255 * factor) << 16)
                            | ((int) (255 * factor) << 8)
                            | ((int) (255 * factor));

            ModelBuffers.partial(
                            CEEPartialModels.INDICATOR_BULB_CUBE,
                            Blocks.ANDESITE.defaultBlockState())
                    .color(cubeColor)
                    .light(0xf000f0)
                    .disableDiffuse()
                    .translate(0 / 16f, 7 / 16f, 8 / 16f)
                    .scale(1, 2.5f, 1)
                    .translate(-4 / 16f, -5 / 16f, -8 / 16f)
                    .renderInto(pose, buffer.getBuffer(CreateRenderTypes.additive()));

            int glowColor =
                    ((int) (255 * factor * 3) << 16)
                            | ((int) (255 * factor * 3) << 8)
                            | ((int) (255 * factor * 3));

            ModelBuffers.partial(
                            CEEPartialModels.INDICATOR_BULB_CUBE,
                            Blocks.ANDESITE.defaultBlockState())
                    .color(glowColor)
                    .light(0xf000f0)
                    .disableDiffuse()
                    .translate(0 / 16f, 7 / 16f, 8 / 16f)
                    .scale(0.75f, 2.15f, 0.75f)
                    .translate(-4 / 16f, -5 / 16f, -8 / 16f)
                    .renderInto(pose, buffer.getBuffer(CreateRenderTypes.additive()));
        }
    }
}
