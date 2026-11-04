package com.george_vi.electroenergetics.client.render;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.CreateElectroEnergetics;
import com.george_vi.electroenergetics.content.electrical_panel.*;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelBlockEntity;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelClientTicker;
import com.george_vi.electroenergetics.content.electrical_panel.attachments.*;
import com.george_vi.electroenergetics.content.electrical_panel.attachments.PanelAttachment;
import com.george_vi.electroenergetics.content.electrical_panel.link.*;
import com.george_vi.electroenergetics.content.electrical_panel.link.ElectricalPanelLink;
import com.george_vi.electroenergetics.content.electrical_panel.special_interaction.*;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.foundation.lang.CreateLang;
import com.george_vi.electroenergetics.foundation.lang.Lang;
import com.george_vi.electroenergetics.foundation.lang.LangNumberFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.item.TooltipHelper;
import com.zurrtum.create.client.foundation.render.CreateRenderTypes;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.List;

public final class PanelAttachmentRenderers {
    private static PartialModel leverHead(String style, boolean dyed) {
        return switch (style) {
            case "THROTTLE" ->
                    dyed
                            ? CEEPartialModels.PANEL_ATTACHMENT_ANALOG_LEVER_THROTTLE_HEAD_DYEABLE
                            : CEEPartialModels.PANEL_ATTACHMENT_ANALOG_LEVER_THROTTLE_HEAD;
            case "BALL" ->
                    dyed
                            ? CEEPartialModels.PANEL_ATTACHMENT_ANALOG_LEVER_BALL_HEAD_DYEABLE
                            : CEEPartialModels.PANEL_ATTACHMENT_ANALOG_LEVER_BALL_HEAD;
            default ->
                    dyed
                            ? CEEPartialModels.PANEL_ATTACHMENT_ANALOG_LEVER_LEVER_DYEABLE
                            : CEEPartialModels.PANEL_ATTACHMENT_ANALOG_LEVER_LEVER;
        };
    }

    public static boolean addToTooltip(
            PanelAttachment a,
            ElectricalPanelBlockEntity be,
            List<Component> tooltip,
            boolean isPlayerSneaking) {

        return baseTooltip(a, be, tooltip, isPlayerSneaking);
    }

    public static boolean addToGoggleTooltip(
            PanelAttachment a,
            ElectricalPanelBlockEntity be,
            List<Component> tooltip,
            boolean isPlayerSneaking) {
        if (a instanceof VelocitySensorPanelAttachment v) {
            return addToGoggleTooltip$VelocitySensorPanelAttachment(
                    v, be, tooltip, isPlayerSneaking);
        }
        if (a instanceof TransformerAttachment v) {
            return addToGoggleTooltip$TransformerAttachment(v, be, tooltip, isPlayerSneaking);
        }
        if (a instanceof GaugePanelAttachment v) {
            return addToGoggleTooltip$GaugePanelAttachment(v, be, tooltip, isPlayerSneaking);
        }
        if (a instanceof AltitudeSensorPanelAttachment v) {
            return addToGoggleTooltip$AltitudeSensorPanelAttachment(
                    v, be, tooltip, isPlayerSneaking);
        }
        return baseTooltip(a, be, tooltip, isPlayerSneaking);
    }

    public static void render(
            PanelAttachment a,
            ElectricalPanelBlockEntity be,
            float partialTicks,
            PoseStack ms,
            RenderCollector buffer,
            int light,
            int overlay) {
        if (a instanceof ThrottleWheelPanelAttachment v) {
            render$ThrottleWheelPanelAttachment(v, be, partialTicks, ms, buffer, light, overlay);
            return;
        }
        if (a instanceof AnalogLeverPanelAttachment v) {
            render$AnalogLeverPanelAttachment(v, be, partialTicks, ms, buffer, light, overlay);
            return;
        }
        if (a instanceof VelocitySensorPanelAttachment v) {
            render$VelocitySensorPanelAttachment(v, be, partialTicks, ms, buffer, light, overlay);
            return;
        }
        if (a instanceof TriPolarEnergyMeterAttachment v) {
            render$TriPolarEnergyMeterAttachment(v, be, partialTicks, ms, buffer, light, overlay);
            return;
        }
        if (a instanceof TransformerAttachment v) {
            render$TransformerAttachment(v, be, partialTicks, ms, buffer, light, overlay);
            return;
        }
        if (a instanceof MomentarySwitchPanelAttachment v) {
            render$MomentarySwitchPanelAttachment(v, be, partialTicks, ms, buffer, light, overlay);
            return;
        }
        if (a instanceof MCBPanelAttachment v) {
            render$MCBPanelAttachment(v, be, partialTicks, ms, buffer, light, overlay);
            return;
        }
        if (a instanceof IndicatorBulbPanelAttachment v) {
            render$IndicatorBulbPanelAttachment(v, be, partialTicks, ms, buffer, light, overlay);
            return;
        }
        if (a instanceof GaugePanelAttachment v) {
            render$GaugePanelAttachment(v, be, partialTicks, ms, buffer, light, overlay);
            return;
        }
        if (a instanceof EStopPanelAttachment v) {
            render$EStopPanelAttachment(v, be, partialTicks, ms, buffer, light, overlay);
            return;
        }
        if (a instanceof EnergyMeterAttachment v) {
            render$EnergyMeterAttachment(v, be, partialTicks, ms, buffer, light, overlay);
            return;
        }
        if (a instanceof ElectricFanAttachment v) {
            render$ElectricFanAttachment(v, be, partialTicks, ms, buffer, light, overlay);
            return;
        }
        if (a instanceof CutOffSwitchPanelAttachment v) {
            render$CutOffSwitchPanelAttachment(v, be, partialTicks, ms, buffer, light, overlay);
            return;
        }
        if (a instanceof AltitudeSensorPanelAttachment v) {
            render$AltitudeSensorPanelAttachment(v, be, partialTicks, ms, buffer, light, overlay);
            return;
        }
    }

    private static void render$AltitudeSensorPanelAttachment(
            AltitudeSensorPanelAttachment a,
            ElectricalPanelBlockEntity be,
            float partialTicks,
            PoseStack ms,
            RenderCollector buffer,
            int light,
            int overlay) {
        if (!net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("simulated")) return;
        double visualHeight = Mth.lerp(partialTicks, a.prevVisualHeight, a.visualHeight);
        transformPose(a, ms, be);

        ModelBuffers.partial(
                        a.radial
                                ? CEEPartialModels.Simulated.RADIAL_ALTITUDE_SENSOR
                                : CEEPartialModels.Simulated.LINEAR_ALTITUDE_SENSOR,
                        be.getBlockState())
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));

        PartialModel dial =
                a.radial
                        ? CEEPartialModels.Simulated.RADIAL_HAND
                        : CEEPartialModels.Simulated.LINEAR_HAND;

        GeometryBuffer dialBuffer = ModelBuffers.partial(dial, be.getBlockState());

        if (a.radial) {
            dialBuffer.rotateZCentered((float) (visualHeight * Math.PI / 2.0));
        } else {
            double value =
                    (visualHeight - a.level.getMinY()) / (a.level.getMaxY() - a.level.getMinY());
            dialBuffer.translate(0, (value * 8f - 4f) / 16f, 0);
        }

        dialBuffer
                .translate(0, 0, 5 / 16f)
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));
    }

    private static boolean addToGoggleTooltip$AltitudeSensorPanelAttachment(
            AltitudeSensorPanelAttachment a,
            ElectricalPanelBlockEntity be,
            List<Component> tooltip,
            boolean isPlayerSneaking) {
        if (!net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("simulated"))
            return false;

        CEELang.builder()
                .add(
                        a.label == null
                                ? Component.translatable("block.simulated.altitude_sensor")
                                : Component.literal(a.label))
                .forGoggles(tooltip, 1);

        Lang.builder("simulated")
                .translate(
                        "altitude_sensor.height",
                        Lang.builder("simulated")
                                .text(String.format("%.2f", a.worldHeight))
                                .style(ChatFormatting.AQUA))
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);

        return true;
    }

    private static void render$CutOffSwitchPanelAttachment(
            CutOffSwitchPanelAttachment a,
            ElectricalPanelBlockEntity be,
            float partialTicks,
            PoseStack ms,
            RenderCollector buffer,
            int light,
            int overlay) {
        transformPose(a, ms, be);

        renderLinkAntenna(a, be, ms, buffer, light);

        ModelBuffers.partial(
                        (a.slot.fullWidth == 14)
                                ? (a.slot.isSixth()
                                        ? CEEPartialModels.PANEL_ATTACHMENT_TINY_SWITCH
                                        : (((Enum<?>) a.style).name().equals("LEVER")
                                                ? CEEPartialModels
                                                        .PANEL_ATTACHMENT_SMOL_CUT_OFF_SWITCH
                                                : CEEPartialModels
                                                        .PANEL_ATTACHMENT_SMOL_MOMENTARY_SWITCH))
                                : (((Enum<?>) a.style).name().equals("BUTTON")
                                        ? CEEPartialModels.PANEL_ATTACHMENT_MOMENTARY_SWITCH
                                        : CEEPartialModels.PANEL_ATTACHMENT_CUT_OFF_SWITCH),
                        be.getBlockState())
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));

        float angle = Mth.lerp(partialTicks, a.prevLeverAngle, a.leverAngle);
        switch (a.style) {
            case LEVER -> {
                if (a.color == null)
                    ModelBuffers.partial(
                                    CEEPartialModels.PANEL_ATTACHMENT_CUT_OFF_SWITCH_LEVER,
                                    be.getBlockState())
                            .translate(5 / 16f, 8 / 16f, 10 / 16f)
                            .rotateXDegrees(angle)
                            .translate(
                                    (a.slot.fullWidth == 14) ? -6 / 16f : -5 / 16f,
                                    -8 / 16f,
                                    -10 / 16f)
                            .light(light)
                            .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));
                else
                    ModelBuffers.partial(
                                    CEEPartialModels.PANEL_ATTACHMENT_CUT_OFF_SWITCH_LEVER_DYEABLE,
                                    be.getBlockState())
                            .translate(5 / 16f, 8 / 16f, 10 / 16f)
                            .rotateXDegrees(angle)
                            .translate(
                                    (a.slot.fullWidth == 14) ? -6 / 16f : -5 / 16f,
                                    -8 / 16f,
                                    -10 / 16f)
                            .color(a.color.getFireworkColor())
                            .light(light)
                            .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));
            }
            case BUTTON -> {
                if (a.color == null)
                    ModelBuffers.partial(
                                    (a.slot.fullWidth == 14)
                                            ? CEEPartialModels
                                                    .PANEL_ATTACHMENT_SMOL_MOMENTARY_SWITCH_BUTTON
                                            : CEEPartialModels
                                                    .PANEL_ATTACHMENT_MOMENTARY_SWITCH_BUTTON,
                                    be.getBlockState())
                            .translate(0, 0, angle)
                            .light(light)
                            .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));
                else
                    ModelBuffers.partial(
                                    (a.slot.fullWidth == 14)
                                            ? CEEPartialModels
                                                    .PANEL_ATTACHMENT_SMOL_MOMENTARY_SWITCH_BUTTON_DYEABLE
                                            : CEEPartialModels
                                                    .PANEL_ATTACHMENT_MOMENTARY_SWITCH_BUTTON_DYEABLE,
                                    be.getBlockState())
                            .translate(0, 0, angle)
                            .color(a.color.getFireworkColor())
                            .light(light)
                            .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));
            }
            case DIAL -> {
                if (a.color == null)
                    ModelBuffers.partial(
                                    CEEPartialModels.PANEL_ATTACHMENT_CUT_OFF_SWITCH_DIAL,
                                    be.getBlockState())
                            .translate(
                                    (a.slot.fullWidth == 14) ? 4 / 16f : 5 / 16f, 8 / 16f, 10 / 16f)
                            .rotateZDegrees(angle)
                            .translate(-5 / 16f, -8 / 16f, -10 / 16f)
                            .light(light)
                            .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));
                else
                    ModelBuffers.partial(
                                    CEEPartialModels.PANEL_ATTACHMENT_CUT_OFF_SWITCH_DIAL_DYEABLE,
                                    be.getBlockState())
                            .translate(
                                    (a.slot.fullWidth == 14) ? 4 / 16f : 5 / 16f, 8 / 16f, 10 / 16f)
                            .rotateZDegrees(angle)
                            .translate(-5 / 16f, -8 / 16f, -10 / 16f)
                            .color(a.color.getFireworkColor())
                            .light(light)
                            .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));
            }
        }
    }

    private static void render$ElectricFanAttachment(
            ElectricFanAttachment a,
            ElectricalPanelBlockEntity be,
            float partialTicks,
            PoseStack ms,
            RenderCollector buffer,
            int light,
            int overlay) {
        transformPose(a, ms, be);

        ModelBuffers.partial(CEEPartialModels.PANEL_ATTACHMENT_FAN_CONNECTORS, be.getBlockState())
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));

        float coverAlpha =
                Mth.lerp(
                        partialTicks,
                        ElectricalPanelClientTicker.prevCoverAlpha,
                        ElectricalPanelClientTicker.coverAlpha);
        coverAlpha = Mth.lerp(coverAlpha, 0.5f, 1f);

        if (coverAlpha != 0) {
            ModelBuffers.partial(CEEPartialModels.PANEL_ATTACHMENT_FAN_BODY, be.getBlockState())
                    .light(light)
                    .color(255, 255, 255, (int) (coverAlpha * 255))
                    .renderInto(
                            ms,
                            buffer.getBuffer(
                                    coverAlpha == 1
                                            ? RenderTypes.solidMovingBlock()
                                            : RenderTypes.translucentMovingBlock()));
        }
    }

    private static void render$EnergyMeterAttachment(
            EnergyMeterAttachment a,
            ElectricalPanelBlockEntity be,
            float partialTicks,
            PoseStack ms,
            RenderCollector buffer,
            int light,
            int overlay) {
        transformPose(a, ms, be);

        ModelBuffers.partial(
                        a.inverted
                                ? CEEPartialModels.PANEL_ATTACHMENT_ENERGY_METER_INVERTED
                                : CEEPartialModels.PANEL_ATTACHMENT_ENERGY_METER,
                        be.getBlockState())
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));
    }

    private static void render$EStopPanelAttachment(
            EStopPanelAttachment a,
            ElectricalPanelBlockEntity be,
            float partialTicks,
            PoseStack ms,
            RenderCollector buffer,
            int light,
            int overlay) {
        transformPose(a, ms, be);

        ModelBuffers.partial(
                        a.isClosed
                                ? CEEPartialModels.PANEL_ATTACHMENT_ESTOP_CLOSED
                                : CEEPartialModels.PANEL_ATTACHMENT_ESTOP_OPEN,
                        be.getBlockState())
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));
    }

    private static void render$GaugePanelAttachment(
            GaugePanelAttachment a,
            ElectricalPanelBlockEntity be,
            float partialTicks,
            PoseStack ms,
            RenderCollector buffer,
            int light,
            int overlay) {
        transformPose(a, ms, be);
        float progress = Mth.lerp(partialTicks, a.prevDialState, a.dialState);

        if ((a.slot.fullWidth == 14)) {
            ModelBuffers.partial(
                            a.voltmeter
                                    ? CEEPartialModels.PANEL_ATTACHMENT_SMOL_VOLTMETER
                                    : CEEPartialModels.PANEL_ATTACHMENT_SMOL_AMMETER,
                            be.getBlockState())
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));

            ModelBuffers.partial(CEEPartialModels.PANEL_ATTACHMENT_SMOL_DIAL, be.getBlockState())
                    .translate(0, progress * 0.35f, 0)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));
        } else {
            ModelBuffers.partial(
                            a.voltmeter
                                    ? CEEPartialModels.PANEL_ATTACHMENT_VOLTMETER
                                    : CEEPartialModels.PANEL_ATTACHMENT_AMMETER,
                            be.getBlockState())
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));

            ModelBuffers.partial(CEEPartialModels.PANEL_ATTACHMENT_DIAL, be.getBlockState())
                    .translate(3.5 / 16f, 6.5 / 16f, 8.75 / 16f)
                    .rotateZ(Mth.PI / 2 * progress)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));
        }
    }

    private static boolean addToGoggleTooltip$GaugePanelAttachment(
            GaugePanelAttachment a,
            ElectricalPanelBlockEntity be,
            List<Component> tooltip,
            boolean isPlayerSneaking) {
        if (a.label != null) CEELang.builder().text(a.label).forGoggles(tooltip);
        else CreateLang.translate("gui.gauge.info_header").forGoggles(tooltip);

        Lang.builder(CreateElectroEnergetics.ID)
                .translate(a.voltmeter ? "generic.voltage" : "generic.current")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        double v = Math.abs(a.value);
        if (v > 1) v = Math.round(v);
        Lang.builder(CreateElectroEnergetics.ID)
                .text(
                        TooltipHelper.makeProgressBar(
                                3,
                                a.dialState < 0.01
                                        ? 0
                                        : a.dialState < 0.33 ? 1 : a.dialState < 0.66 ? 2 : 3))
                .space()
                .add(CreateLang.number(v))
                .add(
                        Component.translatable(
                                a.voltmeter
                                        ? "electroenergetics.generic.volts"
                                        : "electroenergetics.generic.amps"))
                .style(
                        a.dialState < 0.01
                                ? ChatFormatting.DARK_GRAY
                                : a.dialState < 0.33f
                                        ? ChatFormatting.GREEN
                                        : a.dialState < 0.66f
                                                ? (a.voltmeter
                                                        ? ChatFormatting.AQUA
                                                        : ChatFormatting.GOLD)
                                                : (a.voltmeter
                                                        ? ChatFormatting.LIGHT_PURPLE
                                                        : ChatFormatting.RED))
                .forGoggles(tooltip);
        return true;
    }

    private static void render$IndicatorBulbPanelAttachment(
            IndicatorBulbPanelAttachment a,
            ElectricalPanelBlockEntity be,
            float partialTicks,
            PoseStack ms,
            RenderCollector buffer,
            int light,
            int overlay) {
        transformPose(a, ms, be);

        PartialModel partial =
                (a.slot.fullWidth == 14)
                        ? CEEPartialModels.PANEL_ATTACHMENT_SMOL_INDICATOR_BULB
                        : CEEPartialModels.PANEL_ATTACHMENT_INDICATOR_BULB;

        if (a.slot.isSixth()) partial = CEEPartialModels.PANEL_ATTACHMENT_TINY_INDICATOR_BULB;

        ModelBuffers.partial(partial, be.getBlockState())
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));
        float lightStrength = a.smoothLight.getValue(partialTicks);

        RenderType renderType =
                lightStrength > 0.05f
                        ? CreateRenderTypes.additive()
                        : RenderTypes.translucentMovingBlock();
        if ((a.slot.fullWidth == 14)) {
            ModelBuffers.partial(CEEPartialModels.INDICATOR_BULB_TUBE, be.getBlockState())
                    .color(a.color.getTextColor())
                    .rotateXCenteredDegrees(270)
                    .scale(3 / 6f, 2 / 6f, 3 / 6f)
                    .translate(4 / 16f, 16 / 16f, 8 / 16f)
                    .light(light)
                    .disableDiffuse()
                    .renderInto(ms, buffer.getBuffer(renderType));

            if (lightStrength > 0.05f) {
                int newColor =
                        ((int) (((a.color.getMapColor().col >> 16) & 0xFF) * lightStrength) << 16)
                                | ((int) (((a.color.getMapColor().col >> 8) & 0xFF) * lightStrength)
                                        << 8)
                                | ((int) ((a.color.getMapColor().col & 0xFF) * lightStrength));

                ModelBuffers.partial(CEEPartialModels.INDICATOR_BULB_GLOW, be.getBlockState())
                        .color(newColor)
                        .light(0xf000f0)
                        .disableDiffuse()
                        .translate(4 / 16f, 5 / 16f + 3 / 16f, 8 / 16f + 1 / 16f)
                        .scale(1f, 1f, 1.5f)
                        .translate(-4 / 16f, -5 / 16f, -8 / 16f)
                        .renderInto(ms, buffer.getBuffer(CreateRenderTypes.additive()));

                int cubeColor =
                        ((int) (((0xffffff >> 16) & 0xFF) * lightStrength) << 16)
                                | ((int) (((0xffffff >> 8) & 0xFF) * lightStrength) << 8)
                                | ((int) ((0xffffff & 0xFF) * lightStrength));

                ModelBuffers.partial(CEEPartialModels.INDICATOR_BULB_CUBE, be.getBlockState())
                        .color(cubeColor)
                        .light(0xf000f0)
                        .disableDiffuse()
                        .translate(4 / 16f, 5 / 16f + 3 / 16f, 8 / 16f + 1 / 16f)
                        .scale(0.75f, 0.75f, 1f)
                        .translate(-4 / 16f, -5 / 16f, -8 / 16f)
                        .renderInto(ms, buffer.getBuffer(CreateRenderTypes.additive()));
            }
        } else {

            ModelBuffers.partial(CEEPartialModels.INDICATOR_BULB_TUBE, be.getBlockState())
                    .color(a.color.getTextColor())
                    .rotateXCenteredDegrees(270)
                    .scale(4 / 6f, 2 / 6f, 4 / 6f)
                    .translate(3.5 / 16f, 16 / 16f, 4 / 16f)
                    .light(light)
                    .disableDiffuse()
                    .renderInto(ms, buffer.getBuffer(renderType));

            if (lightStrength > 0.05f) {
                int newColor =
                        ((int) (((a.color.getMapColor().col >> 16) & 0xFF) * lightStrength) << 16)
                                | ((int) (((a.color.getMapColor().col >> 8) & 0xFF) * lightStrength)
                                        << 8)
                                | ((int) ((a.color.getMapColor().col & 0xFF) * lightStrength));

                ModelBuffers.partial(CEEPartialModels.INDICATOR_BULB_GLOW, be.getBlockState())
                        .color(newColor)
                        .light(0xf000f0)
                        .disableDiffuse()
                        .translate(4 / 16f + 1 / 16f, 5 / 16f + 3 / 16f, 8 / 16f + 1 / 16f)
                        .scale(1.5f, 1.5f, 1.5f)
                        .translate(-4 / 16f, -5 / 16f, -8 / 16f)
                        .renderInto(ms, buffer.getBuffer(CreateRenderTypes.additive()));

                int cubeColor =
                        ((int) (((0xffffff >> 16) & 0xFF) * lightStrength) << 16)
                                | ((int) (((0xffffff >> 8) & 0xFF) * lightStrength) << 8)
                                | ((int) ((0xffffff & 0xFF) * lightStrength));

                ModelBuffers.partial(CEEPartialModels.INDICATOR_BULB_CUBE, be.getBlockState())
                        .color(cubeColor)
                        .light(0xf000f0)
                        .disableDiffuse()
                        .translate(4 / 16f + 1 / 16f, 5 / 16f + 3 / 16f, 8 / 16f + 1 / 16f)
                        .scale(1f, 1f, 1f)
                        .translate(-4 / 16f, -5 / 16f, -8 / 16f)
                        .renderInto(ms, buffer.getBuffer(CreateRenderTypes.additive()));
            }
        }
    }

    private static void render$MCBPanelAttachment(
            MCBPanelAttachment a,
            ElectricalPanelBlockEntity be,
            float partialTicks,
            PoseStack ms,
            RenderCollector buffer,
            int light,
            int overlay) {
        transformPose(a, ms, be);

        ModelBuffers.partial(
                        a.isClosed
                                ? CEEPartialModels.PANEL_ATTACHMENT_MCB_CLOSED
                                : CEEPartialModels.PANEL_ATTACHMENT_MCB_OPEN,
                        be.getBlockState())
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));
    }

    private static void render$MomentarySwitchPanelAttachment(
            MomentarySwitchPanelAttachment a,
            ElectricalPanelBlockEntity be,
            float partialTicks,
            PoseStack ms,
            RenderCollector buffer,
            int light,
            int overlay) {
        transformPose(a, ms, be);
        renderLinkAntenna(a, be, ms, buffer, light);

        ModelBuffers.partial(
                        (a.slot.fullWidth == 14)
                                ? (a.slot.isSixth()
                                        ? CEEPartialModels.PANEL_ATTACHMENT_TINY_INDICATOR_BULB
                                        : CEEPartialModels.PANEL_ATTACHMENT_SMOL_MOMENTARY_SWITCH)
                                : CEEPartialModels.PANEL_ATTACHMENT_MOMENTARY_SWITCH,
                        be.getBlockState())
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));

        float offset = Mth.lerp(partialTicks, a.prevOffsetState, a.offsetState);
        if (a.color == null)
            ModelBuffers.partial(
                            (a.slot.fullWidth == 14)
                                    ? CEEPartialModels.PANEL_ATTACHMENT_SMOL_MOMENTARY_SWITCH_BUTTON
                                    : CEEPartialModels.PANEL_ATTACHMENT_MOMENTARY_SWITCH_BUTTON,
                            be.getBlockState())
                    .translate(0, 0, offset)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));
        else
            ModelBuffers.partial(
                            (a.slot.fullWidth == 14)
                                    ? CEEPartialModels
                                            .PANEL_ATTACHMENT_SMOL_MOMENTARY_SWITCH_BUTTON_DYEABLE
                                    : CEEPartialModels
                                            .PANEL_ATTACHMENT_MOMENTARY_SWITCH_BUTTON_DYEABLE,
                            be.getBlockState())
                    .translate(0, 0, offset)
                    .color(a.color.getFireworkColor())
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));
    }

    private static void render$TransformerAttachment(
            TransformerAttachment a,
            ElectricalPanelBlockEntity be,
            float partialTicks,
            PoseStack ms,
            RenderCollector buffer,
            int light,
            int overlay) {
        transformPose(a, ms, be);
        ModelBuffers.partial(
                        a.blown
                                ? CEEPartialModels.PANEL_ATTACHMENT_SMOL_TRANSFORMER_BLOWN
                                : CEEPartialModels.PANEL_ATTACHMENT_SMOL_TRANSFORMER,
                        be.getBlockState())
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));
    }

    private static boolean addToGoggleTooltip$TransformerAttachment(
            TransformerAttachment a,
            ElectricalPanelBlockEntity be,
            List<Component> tooltip,
            boolean isPlayerSneaking) {
        if (a.label != null) CEELang.builder().text(a.label).forGoggles(tooltip);
        else CreateLang.translate("gui.gauge.info_header").forGoggles(tooltip);

        CEELang.builder().translate("gui.goggles.electric_stats").forGoggles(tooltip);
        CEELang.builder()
                .translate("gui.goggles.primary_voltage")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        CEELang.builder()
                .text(LangNumberFormat.format(Math.round(Math.abs(a.primaryVoltage))))
                .translate("generic.volts")
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);

        CEELang.builder()
                .translate("gui.goggles.secondary_voltage")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        CEELang.builder()
                .text(LangNumberFormat.format(Math.round(Math.abs(a.secondaryVoltage))))
                .translate("generic.volts")
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);

        Lang.builder(CreateElectroEnergetics.ID)
                .translate("gui.goggles.power")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        Lang.builder(CreateElectroEnergetics.ID)
                .text(LangNumberFormat.format(Math.round(a.power)))
                .translate("generic.watts")
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);
        return true;
    }

    private static void render$TriPolarEnergyMeterAttachment(
            TriPolarEnergyMeterAttachment a,
            ElectricalPanelBlockEntity be,
            float partialTicks,
            PoseStack ms,
            RenderCollector buffer,
            int light,
            int overlay) {
        transformPose(a, ms, be);

        ModelBuffers.partial(
                        a.inverted
                                ? CEEPartialModels.PANEL_ATTACHMENT_TRI_ENERGY_METER_INVERTED
                                : CEEPartialModels.PANEL_ATTACHMENT_TRI_ENERGY_METER,
                        be.getBlockState())
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));
    }

    private static void render$VelocitySensorPanelAttachment(
            VelocitySensorPanelAttachment a,
            ElectricalPanelBlockEntity be,
            float partialTicks,
            PoseStack ms,
            RenderCollector buffer,
            int light,
            int overlay) {
        transformPose(a, ms, be);
        float progress = Mth.lerp(partialTicks, a.prevDialState, a.dialState);

        if (a.wide) {
            ModelBuffers.partial(
                            CEEPartialModels.PANEL_ATTACHMENT_VELOCITY_SENSOR_WIDE,
                            be.getBlockState())
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));

            ModelBuffers.partial(CEEPartialModels.PANEL_ATTACHMENT_DIAL, be.getBlockState())
                    .translate(3.5 / 16f, 8 / 16f, 8.75 / 16f)
                    .rotateZ(Mth.PI * -progress + Mth.HALF_PI)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));
        } else {
            ModelBuffers.partial(
                            CEEPartialModels.PANEL_ATTACHMENT_VELOCITY_SENSOR, be.getBlockState())
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));

            ModelBuffers.partial(CEEPartialModels.PANEL_ATTACHMENT_DIAL, be.getBlockState())
                    .translate(3.5 / 16f, 6.5 / 16f, 8.75 / 16f)
                    .rotateZ(Mth.PI / 2 * progress)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));
        }
    }

    private static boolean addToGoggleTooltip$VelocitySensorPanelAttachment(
            VelocitySensorPanelAttachment a,
            ElectricalPanelBlockEntity be,
            List<Component> tooltip,
            boolean isPlayerSneaking) {
        if (a.label != null) CEELang.builder().text(a.label).forGoggles(tooltip);
        else CreateLang.translate("gui.gauge.info_header").forGoggles(tooltip);

        double v = a.value;
        Lang.builder(CreateElectroEnergetics.ID)
                .text(
                        TooltipHelper.makeProgressBar(
                                3,
                                a.dialState < 0.01
                                        ? 0
                                        : a.dialState < 0.33 ? 1 : a.dialState < 0.66 ? 2 : 3))
                .space()
                .add(CreateLang.number(v * a.speedUnit.multiplier))
                .text(a.speedUnit.suffix)
                .style(
                        a.dialState < 0.01
                                ? ChatFormatting.DARK_GRAY
                                : a.dialState < 0.33f
                                        ? ChatFormatting.GREEN
                                        : a.dialState < 0.66f
                                                ? (ChatFormatting.AQUA)
                                                : (ChatFormatting.LIGHT_PURPLE))
                .forGoggles(tooltip);
        return true;
    }

    private static void render$AnalogLeverPanelAttachment(
            AnalogLeverPanelAttachment a,
            ElectricalPanelBlockEntity be,
            float partialTicks,
            PoseStack ms,
            RenderCollector buffer,
            int light,
            int overlay) {
        transformPose(a, ms, be);

        renderLinkAntenna(a, be, ms, buffer, light);

        ModelBuffers.partial(
                        (a.slot.fullWidth == 14) || a.slot.isSixth()
                                ? CEEPartialModels.PANEL_ATTACHMENT_ANALOG_LEVER_SMOL
                                : CEEPartialModels.PANEL_ATTACHMENT_ANALOG_LEVER,
                        be.getBlockState())
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));

        float angle = Mth.lerp(partialTicks, a.prevLeverAngle, a.leverAngle);

        if (!((Enum<?>) a.style).name().equals("LEVER")) {
            ModelBuffers.partial(
                            CEEPartialModels.PANEL_ATTACHMENT_ANALOG_LEVER_METAL_THINGY,
                            be.getBlockState())
                    .translate(5 / 16f, 8 / 16f, 10 / 16f)
                    .rotateXDegrees(angle)
                    .translate((a.slot.fullWidth == 14) ? -6 / 16f : -5 / 16f, -8 / 16f, -10 / 16f)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));
        }

        if (a.color == null)
            ModelBuffers.partial(leverHead(((Enum<?>) a.style).name(), false), be.getBlockState())
                    .translate(5 / 16f, 8 / 16f, 10 / 16f)
                    .rotateXDegrees(angle)
                    .translate((a.slot.fullWidth == 14) ? -6 / 16f : -5 / 16f, -8 / 16f, -10 / 16f)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));
        else
            ModelBuffers.partial(leverHead(((Enum<?>) a.style).name(), true), be.getBlockState())
                    .translate(5 / 16f, 8 / 16f, 10 / 16f)
                    .rotateXDegrees(angle)
                    .translate((a.slot.fullWidth == 14) ? -6 / 16f : -5 / 16f, -8 / 16f, -10 / 16f)
                    .color(a.color.getFireworkColor())
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));
    }

    private static void render$ThrottleWheelPanelAttachment(
            ThrottleWheelPanelAttachment a,
            ElectricalPanelBlockEntity be,
            float partialTicks,
            PoseStack ms,
            RenderCollector buffer,
            int light,
            int overlay) {
        transformPose(a, ms, be);

        ModelBuffers.partial(CEEPartialModels.PANEL_ATTACHMENT_STEERING_WHEEL, be.getBlockState())
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));

        float angle = Mth.lerp(partialTicks, a.prevLeverAngle, a.leverAngle);
        ModelBuffers.partial(
                        CEEPartialModels.PANEL_ATTACHMENT_STEERING_WHEEL_WHEEL, be.getBlockState())
                .rotateZCenteredDegrees(angle)
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));

        if (a.color == null)
            ModelBuffers.partial(
                            CEEPartialModels.PANEL_ATTACHMENT_STEERING_WHEEL_WHEEL_HANDLE,
                            be.getBlockState())
                    .rotateZCenteredDegrees(angle)
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));
        else
            ModelBuffers.partial(
                            CEEPartialModels.PANEL_ATTACHMENT_STEERING_WHEEL_WHEEL_HANDLE_DYEABLE,
                            be.getBlockState())
                    .rotateZCenteredDegrees(angle)
                    .color(a.color.getFireworkColor())
                    .light(light)
                    .renderInto(ms, buffer.getBuffer(RenderTypes.solidMovingBlock()));

        ms.translate(4 / 16f, -2 / 16f, 0);
        renderLinkAntenna(a, be, ms, buffer, light);
    }

    public static boolean baseTooltip(
            PanelAttachment a,
            ElectricalPanelBlockEntity be,
            List<Component> tooltip,
            boolean sneaking) {
        if (a.label == null) return false;
        CEELang.builder().text(a.label).forGoggles(tooltip);
        return true;
    }

    public static void transformPose(
            PanelAttachment a, PoseStack ms, ElectricalPanelBlockEntity be) {
        Direction facing = be.getBlockState().getValue(ElectricalPanelBlock.FACING);
        TransformStack.of(ms)
                .rotateYCenteredDegrees(-facing.toYRot() + 180)
                .translateY(
                        a.slot == ElectricalPanelSlot.HALF_LOWER
                                ? -6 / 16f
                                : a.slot == ElectricalPanelSlot.HALF_UPPER ? 6 / 16f : 0)
                .rotateZCenteredDegrees(a.slot.isHorizontal ? 90 : 0)
                .translateX((float) a.slot.leftOffset)
                .translateY((float) a.slot.topOffset);
    }

    private static void renderLinkAntenna(
            ElectricalPanelLink a,
            ElectricalPanelBlockEntity be,
            PoseStack ms,
            RenderCollector buffer,
            int light) {
        if (Arrays.stream(a.getLinkFrequencies()).allMatch(ItemStack::isEmpty)) return;
        boolean powered =
                Arrays.stream(a.getLinkables())
                        .anyMatch(link -> link.getTransmittedStrength() != 0);
        ModelBuffers.partial(
                        powered
                                ? CEEPartialModels.PANEL_ATTACHMENT_LINK_ANTENNA_POWERED
                                : CEEPartialModels.PANEL_ATTACHMENT_LINK_ANTENNA,
                        be.getBlockState())
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));
    }

    public static void renderLabel(
            PanelAttachment a,
            ElectricalPanelBlockEntity be,
            float partialTicks,
            PoseStack ms,
            RenderCollector buffer,
            int light,
            int overlay) {
        if (a.label == null) return;
        Direction facing = be.getBlockState().getValue(ElectricalPanelBlock.FACING);
        boolean horizontal = a.slot.isHorizontal;
        int fullWidth = horizontal ? 48 : a.slot.fullWidth;
        int width = Minecraft.getInstance().font.width(a.label);
        float scale = width > fullWidth * 2 ? 1 / 256f : 1 / 128f;
        float leftOffset = width > fullWidth * 2 ? 3.2f / 16 : 2 / 16f;
        float y =
                (float)
                        ((a.slot == ElectricalPanelSlot.FULL_SLOT ? 2 / 16f : 4 / 16f)
                                + a.slot.topOffset);
        TransformStack.of(ms)
                .rotateYCenteredDegrees(-facing.toYRot() + 180)
                .translate(
                        (horizontal ? 0 : (float) a.slot.leftOffset) + leftOffset,
                        horizontal ? -a.slot.leftOffset + 9 / 16f : y,
                        horizontal ? 9 / 16f : 11 / 16f)
                .scale(-scale, -scale, -scale);
        buffer.text(
                ms,
                Component.literal(a.label),
                -fullWidth - width / 2f,
                0,
                0xffffffff,
                LightCoordsUtil.FULL_BRIGHT);
    }
}
