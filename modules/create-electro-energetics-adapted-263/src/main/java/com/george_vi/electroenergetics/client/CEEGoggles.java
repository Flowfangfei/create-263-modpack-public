package com.george_vi.electroenergetics.client;

import com.george_vi.electroenergetics.CEEBlocks;
import com.george_vi.electroenergetics.CreateElectroEnergetics;
import com.george_vi.electroenergetics.client.render.PanelAttachmentRenderers;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.config.CRotor;
import com.george_vi.electroenergetics.content.accumulator.*;
import com.george_vi.electroenergetics.content.accumulator.AccumulatorBlockEntity;
import com.george_vi.electroenergetics.content.converter.*;
import com.george_vi.electroenergetics.content.converter.ConverterBlockEntity;
import com.george_vi.electroenergetics.content.electric_motor.*;
import com.george_vi.electroenergetics.content.electric_motor.ElectricMotorBlockEntity;
import com.george_vi.electroenergetics.content.electric_pump.*;
import com.george_vi.electroenergetics.content.electric_pump.ElectricPumpBlockEntity;
import com.george_vi.electroenergetics.content.electrical_panel.*;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelBlockEntity;
import com.george_vi.electroenergetics.content.frequency_meter.*;
import com.george_vi.electroenergetics.content.frequency_meter.FrequencyMeterBlockEntity;
import com.george_vi.electroenergetics.content.gauge.*;
import com.george_vi.electroenergetics.content.gauge.ElectricGaugeBlockEntity;
import com.george_vi.electroenergetics.content.rotor.*;
import com.george_vi.electroenergetics.content.rotor.AlternatorBrushesBlockEntity;
import com.george_vi.electroenergetics.content.synchroscope.SynchroscopeBlockEntity;
import com.george_vi.electroenergetics.content.transmission_distribution.transformer.*;
import com.george_vi.electroenergetics.content.transmission_distribution.transformer.TransformerBlockEntity;
import com.george_vi.electroenergetics.content.transmission_distribution.transformer.TransformerCoreBlockEntity;
import com.george_vi.electroenergetics.content.transmission_distribution.voltage_regulator.*;
import com.george_vi.electroenergetics.content.transmission_distribution.voltage_regulator.VoltageRegulatorBlockEntity;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.foundation.electrical_properties.AccumulatorProperties;
import com.george_vi.electroenergetics.foundation.lang.CreateLang;
import com.george_vi.electroenergetics.foundation.lang.Lang;
import com.george_vi.electroenergetics.foundation.lang.LangNumberFormat;
import com.zurrtum.create.client.api.goggles.*;
import com.zurrtum.create.client.catnip.lang.FontHelper;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.*;
import com.zurrtum.create.client.foundation.item.TooltipHelper;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.List;

public class CEEGoggles extends TooltipBehaviour<SmartBlockEntity>
        implements IHaveGoggleInformation, IHaveHoveringInformation {
    public CEEGoggles(SmartBlockEntity be) {
        super(be);
    }

    @Override
    public boolean addToTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (blockEntity instanceof SynchroscopeBlockEntity be)
            return addToTooltip$SynchroscopeBlockEntity(be, tooltip, isPlayerSneaking);
        if (blockEntity instanceof ElectricalPanelBlockEntity be)
            return addToTooltip$ElectricalPanelBlockEntity(be, tooltip, isPlayerSneaking);
        return false;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (blockEntity instanceof VoltageRegulatorBlockEntity be)
            return addToGoggleTooltip$VoltageRegulatorBlockEntity(be, tooltip, isPlayerSneaking);
        if (blockEntity instanceof TransformerCoreBlockEntity be)
            return addToGoggleTooltip$TransformerCoreBlockEntity(be, tooltip, isPlayerSneaking);
        if (blockEntity instanceof TransformerBlockEntity be)
            return addToGoggleTooltip$TransformerBlockEntity(be, tooltip, isPlayerSneaking);
        if (blockEntity instanceof AlternatorBrushesBlockEntity be)
            return addToGoggleTooltip$AlternatorBrushesBlockEntity(be, tooltip, isPlayerSneaking);
        if (blockEntity instanceof ElectricGaugeBlockEntity be)
            return addToGoggleTooltip$ElectricGaugeBlockEntity(be, tooltip, isPlayerSneaking);
        if (blockEntity instanceof FrequencyMeterBlockEntity be)
            return addToGoggleTooltip$FrequencyMeterBlockEntity(be, tooltip, isPlayerSneaking);
        if (blockEntity instanceof ElectricPumpBlockEntity be)
            return addToGoggleTooltip$ElectricPumpBlockEntity(be, tooltip, isPlayerSneaking);
        if (blockEntity instanceof ElectricMotorBlockEntity be)
            return addToGoggleTooltip$ElectricMotorBlockEntity(be, tooltip, isPlayerSneaking);
        if (blockEntity instanceof ElectricalPanelBlockEntity be)
            return addToGoggleTooltip$ElectricalPanelBlockEntity(be, tooltip, isPlayerSneaking);
        if (blockEntity instanceof ConverterBlockEntity be)
            return addToGoggleTooltip$ConverterBlockEntity(be, tooltip, isPlayerSneaking);
        if (blockEntity instanceof AccumulatorBlockEntity be)
            return addToGoggleTooltip$AccumulatorBlockEntity(be, tooltip, isPlayerSneaking);
        return false;
    }

    private static boolean addToGoggleTooltip$AccumulatorBlockEntity(
            AccumulatorBlockEntity be, List<Component> tooltip, boolean isPlayerSneaking) {
        Minecraft mc = Minecraft.getInstance();
        HitResult hitResult = mc.hitResult;
        if (hitResult == null) return false;

        BlockState state = be.getBlockState();
        Direction facing = state.getValue(AccumulatorBlock.FACING);
        boolean roll = state.getValue(AccumulatorBlock.ROLL);

        CEELang.builder().translate("gui.goggles.electric_stats").forGoggles(tooltip);

        Lang.builder(CreateElectroEnergetics.ID)
                .translate("gui.goggles.stored_energy")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        boolean clickedOnRight =
                AccumulatorBlock.clickedOnFirst(
                        roll,
                        facing,
                        hitResult.getLocation().subtract(Vec3.atLowerCornerOf(be.getBlockPos())));

        boolean isFirst = true;
        if (state.getValue(AccumulatorBlock.STACK).isDouble()) isFirst = clickedOnRight ^ roll;

        double charge = isFirst ? be.cell1Charge : be.cell2Charge;

        CEELang.builder()
                .add(
                        CEELang.formatEnergy(
                                AccumulatorProperties.energyFromSOC(
                                        charge, 24, AccumulatorProperties.getNominalCharge())))
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);

        Lang.builder(CreateElectroEnergetics.ID)
                .translate("gui.goggles.state_of_charge")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        CEELang.builder()
                .add(CreateLang.number(charge / AccumulatorProperties.getNominalCharge() * 100d))
                .add(Component.literal("%"))
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);
        return true;
    }

    private static boolean addToGoggleTooltip$ConverterBlockEntity(
            ConverterBlockEntity be, List<Component> tooltip, boolean isPlayerSneaking) {
        if (Math.abs(be.power) <= 1) return false;
        Lang.builder(CreateElectroEnergetics.ID)
                .translate("gui.goggles.energy_consumption")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        Lang.builder(CreateElectroEnergetics.ID)
                .add(
                        be.power < 0
                                ? CEELang.formatPower(Math.abs(be.power))
                                : CEELang.formatFEPerTick(
                                        Math.abs(
                                                be.power
                                                        / CEEConfigs.server()
                                                                .wattFeTConversionRate
                                                                .get())))
                .style(ChatFormatting.AQUA)
                .space()
                .add(
                        Component.translatable("electroenergetics.gui.goggles.at_current_load")
                                .withStyle(ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 1);

        Lang.builder(CreateElectroEnergetics.ID)
                .translate("gui.goggles.energy_generation")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        Lang.builder(CreateElectroEnergetics.ID)
                .add(
                        be.power > 0
                                ? CEELang.formatPower(Math.abs(be.power))
                                : CEELang.formatFEPerTick(
                                        Math.abs(
                                                be.power
                                                        / CEEConfigs.server()
                                                                .wattFeTConversionRate
                                                                .get())))
                .style(ChatFormatting.AQUA)
                .space()
                .add(
                        Component.translatable("electroenergetics.gui.goggles.at_current_load")
                                .withStyle(ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 1);
        return true;
    }

    private static boolean addToGoggleTooltip$ElectricalPanelBlockEntity(
            ElectricalPanelBlockEntity be, List<Component> tooltip, boolean isPlayerSneaking) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.hitResult == null) return false;
        Vec3 clickedPos =
                mc.hitResult.getLocation().subtract(Vec3.atLowerCornerOf(be.getBlockPos()));
        ElectricalPanelSlot slot = be.getHoveringAttachmentIndex(clickedPos);
        if (slot == null || be.beAttachments[slot.ordinal()] == null) return false;
        return PanelAttachmentRenderers.addToGoggleTooltip(
                be.beAttachments[slot.ordinal()], be, tooltip, isPlayerSneaking);
    }

    private static boolean addToTooltip$ElectricalPanelBlockEntity(
            ElectricalPanelBlockEntity be, List<Component> tooltip, boolean isPlayerSneaking) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.hitResult == null) return false;
        Vec3 clickedPos =
                mc.hitResult.getLocation().subtract(Vec3.atLowerCornerOf(be.getBlockPos()));
        ElectricalPanelSlot slot = be.getHoveringAttachmentIndex(clickedPos);
        if (slot == null || be.beAttachments[slot.ordinal()] == null) return false;
        return PanelAttachmentRenderers.addToTooltip(
                be.beAttachments[slot.ordinal()], be, tooltip, isPlayerSneaking);
    }

    private static boolean addToGoggleTooltip$ElectricMotorBlockEntity(
            ElectricMotorBlockEntity be, List<Component> tooltip, boolean isPlayerSneaking) {
        //        baseKinetic(be,tooltip,isPlayerSneaking);
        if (be.getSpeed() == 0) return false;

        // Maximum capacity
        float stressBase = be.calculateAddedStressCapacity();

        CreateLang.translate("gui.goggles.generator_stats").forGoggles(tooltip);
        CEELang.builder()
                .translate("gui.goggles.maximum_capacity")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        float speed = be.getTheoreticalSpeed();
        if (speed != be.getGeneratedSpeed() && speed != 0)
            stressBase *= be.getGeneratedSpeed() / speed;

        float stressTotal = Math.abs(stressBase * speed);

        CreateLang.number(stressTotal)
                .translate("generic.unit.stress")
                .style(ChatFormatting.AQUA)
                .space()
                .add(
                        CEELang.translate("gui.goggles.at_current_voltage")
                                .style(ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 1);

        // Current generation

        CEELang.builder()
                .translate("gui.goggles.current_load")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        CreateLang.number(be.load * stressTotal)
                .translate("generic.unit.stress")
                .style(ChatFormatting.AQUA)
                .space()
                .add(
                        CreateLang.translate("gui.goggles.at_current_speed")
                                .style(ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 1);

        // Energy consumption
        double load = Mth.clamp(be.load, 0.05, 1);
        float wattage =
                Math.round(
                        be.averageVoltage.get()
                                * be.averageVoltage.get()
                                / (0.8
                                        * Math.min(
                                                CEEConfigs.server()
                                                                .resistanceValues
                                                                .motorResistance
                                                                .get()
                                                        * 6,
                                                CEEConfigs.server()
                                                                .resistanceValues
                                                                .motorResistance
                                                                .get()
                                                        / load)));

        CEELang.builder()
                .translate("gui.goggles.energy_consumption")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        CEELang.builder()
                .add(CEELang.formatPower(wattage))
                .style(ChatFormatting.AQUA)
                .space()
                .add(
                        CEELang.translate("gui.goggles.at_current_load")
                                .style(ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 1);

        return true;
    }

    private static boolean addToGoggleTooltip$ElectricPumpBlockEntity(
            ElectricPumpBlockEntity be, List<Component> tooltip, boolean isPlayerSneaking) {
        if (Math.abs(be.voltage) < 0.1) return false;
        CEELang.builder()
                .translate("gui.goggles.energy_consumption")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        CEELang.builder()
                .text(
                        LangNumberFormat.format(
                                Math.round(
                                        be.voltage
                                                * be.voltage
                                                / CEEConfigs.server()
                                                        .resistanceValues
                                                        .pumpResistance
                                                        .get())))
                .translate("generic.watts")
                .style(ChatFormatting.AQUA)
                .space()
                .add(
                        Component.translatable("electroenergetics.gui.goggles.at_current_voltage")
                                .withStyle(ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 1);

        CEELang.builder()
                .translate("gui.goggles.rpm_equivalent")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        CreateLang.builder()
                .text(LangNumberFormat.format(Math.round(be.getSpeed())))
                .translate("generic.unit.rpm")
                .style(ChatFormatting.AQUA)
                .space()
                .add(
                        Component.translatable("electroenergetics.gui.goggles.at_current_voltage")
                                .withStyle(ChatFormatting.DARK_GRAY))
                .forGoggles(tooltip, 1);

        return true;
    }

    private static boolean addToGoggleTooltip$FrequencyMeterBlockEntity(
            FrequencyMeterBlockEntity be, List<Component> tooltip, boolean isPlayerSneaking) {
        CreateLang.translate("gui.gauge.info_header").forGoggles(tooltip);

        CEELang.builder()
                .translate("gui.goggles.frequency")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        float v = Math.round(be.frequency * 100) / 100f;

        CEELang.builder()
                .add(CreateLang.number(v))
                .add(CEELang.translateDirect("generic.hertz"))
                .style(v < 5 ? ChatFormatting.DARK_GRAY : ChatFormatting.AQUA)
                .forGoggles(tooltip);

        return true;
    }

    private static boolean addToGoggleTooltip$ElectricGaugeBlockEntity(
            ElectricGaugeBlockEntity be, List<Component> tooltip, boolean isPlayerSneaking) {
        CreateLang.translate("gui.gauge.info_header").forGoggles(tooltip);

        Lang.builder(CreateElectroEnergetics.ID)
                .translate(be.voltmeter ? "generic.voltage" : "generic.current")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        double v = Math.abs(be.voltmeter ? be.voltage : be.voltage / 0.01);
        v *= be.scaling.getScale();
        if (v > 1) v = Math.round(v);
        Lang.builder(CreateElectroEnergetics.ID)
                .text(
                        TooltipHelper.makeProgressBar(
                                3,
                                be.dialState < 0.01
                                        ? 0
                                        : be.dialState < 0.33 ? 1 : be.dialState < 0.66 ? 2 : 3))
                .space()
                .add(CreateLang.number(v))
                .add(
                        Component.translatable(
                                be.voltmeter
                                        ? "electroenergetics.generic.volts"
                                        : "electroenergetics.generic.amps"))
                .style(
                        be.dialState < 0.01
                                ? ChatFormatting.DARK_GRAY
                                : be.dialState < 0.33f
                                        ? ChatFormatting.GREEN
                                        : be.dialState < 0.66f
                                                ? (be.voltmeter
                                                        ? ChatFormatting.AQUA
                                                        : ChatFormatting.GOLD)
                                                : (be.voltmeter
                                                        ? ChatFormatting.LIGHT_PURPLE
                                                        : ChatFormatting.RED))
                .forGoggles(tooltip);
        return true;
    }

    private static boolean addToGoggleTooltip$AlternatorBrushesBlockEntity(
            AlternatorBrushesBlockEntity be, List<Component> tooltip, boolean isPlayerSneaking) {
        float totalStress = 0;
        for (AlternatorRotorBlockEntity rotor : be.rotors)
            totalStress += rotor.magnets * Math.abs(rotor.getSpeed());

        CRotor rotorConfig = CEEConfigs.server().rotorValues;
        totalStress *= rotorConfig.rotorPowerMultiplier.getF();

        Lang.builder(CreateElectroEnergetics.ID)
                .translate("gui.goggles.electric_stats")
                .forGoggles(tooltip);

        Lang.builder(CreateElectroEnergetics.ID)
                .translate("gui.goggles.energy_generation")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        Lang.builder(CreateElectroEnergetics.ID)
                .text(LangNumberFormat.format(Math.round(Math.abs(totalStress))))
                .translate("generic.watts")
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);

        Lang.builder(CreateElectroEnergetics.ID)
                .translate("gui.goggles.voltage")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);

        Lang.builder(CreateElectroEnergetics.ID)
                .text(LangNumberFormat.format(Math.round(be.voltage)))
                .translate("generic.volts")
                .text(
                        " / "
                                + LangNumberFormat.format(
                                        Math.round(
                                                totalStress
                                                        / rotorConfig.rotorFullLoadCurrent.getF())))
                .translate("generic.volts")
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);
        return true;
    }

    private static boolean addToTooltip$SynchroscopeBlockEntity(
            SynchroscopeBlockEntity be, List<Component> tooltip, boolean isPlayerSneaking) {
        if (be.validConnection) return false;
        CEELang.translate("hint.synchroscope_misconfigured.title")
                .style(ChatFormatting.GOLD)
                .forGoggles(tooltip);
        Component hint = CEELang.translateDirect("hint.synchroscope_misconfigured");
        List<Component> cutComponent =
                TooltipHelper.cutTextComponent(hint, FontHelper.Palette.GRAY_AND_WHITE);
        for (Component component : cutComponent)
            CreateLang.builder().add(component).forGoggles(tooltip);
        return true;
    }

    private static boolean addToGoggleTooltip$TransformerBlockEntity(
            TransformerBlockEntity be, List<Component> tooltip, boolean isPlayerSneaking) {

        Lang.builder(CreateElectroEnergetics.ID)
                .translate("gui.goggles.electric_stats")
                .forGoggles(tooltip);
        Lang.builder(CreateElectroEnergetics.ID)
                .translate("gui.goggles.primary_voltage")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        Lang.builder(CreateElectroEnergetics.ID)
                .text(LangNumberFormat.format(Math.round(Math.abs(be.primaryVoltage))))
                .translate("generic.volts")
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);

        Lang.builder(CreateElectroEnergetics.ID)
                .translate("gui.goggles.secondary_voltage")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        Lang.builder(CreateElectroEnergetics.ID)
                .text(LangNumberFormat.format(Math.round(Math.abs(be.secondaryVoltage))))
                .translate("generic.volts")
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);

        Lang.builder(CreateElectroEnergetics.ID)
                .translate("gui.goggles.power")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        Lang.builder(CreateElectroEnergetics.ID)
                .text(LangNumberFormat.format(Math.round(be.power)))
                .translate("generic.watts")
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);
        return true;
    }

    private static boolean addToGoggleTooltip$TransformerCoreBlockEntity(
            TransformerCoreBlockEntity be, List<Component> tooltip, boolean isPlayerSneaking) {
        Direction facing = be.getBlockState().getValue(TransformerCoreBlock.FACING);
        BlockPos otherPos = be.getBlockPos().relative(facing);
        if (facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE) {
            if (be.getLevel().getBlockEntity(otherPos) instanceof TransformerCoreBlockEntity other)
                return addToGoggleTooltip$TransformerCoreBlockEntity(
                        other, tooltip, isPlayerSneaking);
            else return false;
        }

        Lang.builder(CreateElectroEnergetics.ID)
                .translate("gui.goggles.electric_stats")
                .forGoggles(tooltip);
        Lang.builder(CreateElectroEnergetics.ID)
                .translate("gui.goggles.primary_voltage")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        Lang.builder(CreateElectroEnergetics.ID)
                .text(LangNumberFormat.format(Math.round(Math.abs(be.primaryVoltage))))
                .translate("generic.volts")
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);

        Lang.builder(CreateElectroEnergetics.ID)
                .translate("gui.goggles.secondary_voltage")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        Lang.builder(CreateElectroEnergetics.ID)
                .text(LangNumberFormat.format(Math.round(Math.abs(be.secondaryVoltage))))
                .translate("generic.volts")
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);

        Lang.builder(CreateElectroEnergetics.ID)
                .translate("gui.goggles.power")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        Lang.builder(CreateElectroEnergetics.ID)
                .text(LangNumberFormat.format(Math.round(be.power)))
                .translate("generic.watts")
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);

        Lang.builder(CreateElectroEnergetics.ID)
                .translate("gui.goggles.max_heat_dissipation")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        Lang.builder(CreateElectroEnergetics.ID)
                .text(LangNumberFormat.format(Math.round(be.heatDissipationFactor)))
                .translate("generic.watts")
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);
        return true;
    }

    private static boolean addToGoggleTooltip$VoltageRegulatorBlockEntity(
            VoltageRegulatorBlockEntity be, List<Component> tooltip, boolean isPlayerSneaking) {

        if (!be.getBlockState().getValue(VoltageRegulatorBlock.BOTTOM)) {
            for (int i = 1; ; i++) {
                BlockPos pos = be.getBlockPos().below(i);
                if (!CEEBlocks.VOLTAGE_REGULATOR.has(be.getLevel().getBlockState(pos))) {
                    BlockPos bottomPos = be.getBlockPos().below(i - 1);
                    if (be.getLevel().getBlockEntity(bottomPos)
                            instanceof VoltageRegulatorBlockEntity other)
                        return addToGoggleTooltip$VoltageRegulatorBlockEntity(
                                other, tooltip, isPlayerSneaking);
                    return false;
                }
            }
        }

        Lang.builder(CreateElectroEnergetics.ID)
                .translate("gui.goggles.electric_stats")
                .forGoggles(tooltip);
        Lang.builder(CreateElectroEnergetics.ID)
                .translate("gui.goggles.input_voltage")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        Lang.builder(CreateElectroEnergetics.ID)
                .text(LangNumberFormat.format(Math.round(Math.abs(be.inputVoltage))))
                .translate("generic.volts")
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);

        Lang.builder(CreateElectroEnergetics.ID)
                .translate("gui.goggles.output_voltage")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        Lang.builder(CreateElectroEnergetics.ID)
                .text(LangNumberFormat.format(Math.round(Math.abs(be.outputVoltage))))
                .translate("generic.volts")
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);

        Lang.builder(CreateElectroEnergetics.ID)
                .translate("gui.goggles.power")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        Lang.builder(CreateElectroEnergetics.ID)
                .text(LangNumberFormat.format(Math.round(be.power)))
                .translate("generic.watts")
                .style(ChatFormatting.AQUA)
                .forGoggles(tooltip, 1);
        return true;
    }

    private static boolean baseKinetic(
            SmartBlockEntity be, List<Component> tooltip, boolean sneak) {
        return be instanceof KineticBlockEntity kinetic
                && new KineticTooltipBehaviour<>(kinetic).addToGoggleTooltip(tooltip, sneak);
    }

    private static boolean baseHover(SmartBlockEntity be, List<Component> tooltip, boolean sneak) {
        return be instanceof KineticBlockEntity kinetic
                && new KineticTooltipBehaviour<>(kinetic).addToTooltip(tooltip, sneak);
    }
}
