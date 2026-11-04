package com.george_vi.electroenergetics.client;

import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.foundation.scroll_value.*;
import com.google.common.collect.ImmutableList;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsBoard;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsFormatter;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollValueBehaviour;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

public class CEEClientValue extends ScrollValueBehaviour<SmartBlockEntity, CEEValueBehaviour> {
    private static final double[] scales = {1 / 1000d, 1 / 100d, 1 / 10d, 1, 10, 100, 1000};
    private static final int[] displayScales = {1000, 100, 10, 1, 10, 100, 1000};

    public CEEClientValue(SmartBlockEntity be, ValueBoxTransform slot) {
        super(
                ((CEEValueBehaviour) be.getBehaviour(ServerScrollValueBehaviour.TYPE)).label,
                be,
                slot);
    }

    @Override
    public String formatValue() {
        return behaviour.formatValue();
    }

    @Override
    public boolean isActive() {
        return behaviour != null && behaviour.isActive();
    }

    @Override
    public boolean onlyVisibleWithWrench() {
        return behaviour.needsWrench;
    }

    @Override
    public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
        if (behaviour instanceof CapacitanceScrollValueBehaviour) {
            return new ValueSettingsBoard(
                    behaviour.label,
                    190,
                    10,
                    ImmutableList.of(
                            CEELang.translate("capacitor.nano_capacitance_symbol").component(),
                            CEELang.translate("capacitor.micro_capacitance_symbol").component(),
                            CEELang.translate("capacitor.milli_capacitance_symbol").component()),
                    new ValueSettingsFormatter(
                            valueSettings ->
                                    CEELang.formatCapacitance(
                                                    indexToCapacitance(
                                                            valueSettings.value(),
                                                            valueSettings.row()))
                                            .component()));
        }
        if (behaviour instanceof InductanceScrollValueBehaviour) {
            return new ValueSettingsBoard(
                    behaviour.label,
                    190,
                    10,
                    ImmutableList.of(
                            CEELang.translate("inductor.nano_inductance_symbol").component(),
                            CEELang.translate("inductor.micro_inductance_symbol").component(),
                            CEELang.translate("inductor.milli_inductance_symbol").component()),
                    new ValueSettingsFormatter(
                            valueSettings ->
                                    CEELang.formatInductance(
                                                    indexToInductance(
                                                            valueSettings.value(),
                                                            valueSettings.row()))
                                            .component()));
        }
        if (behaviour instanceof KineticUnlockableScrollValueBehaviour) {
            ImmutableList<Component> rows =
                    ImmutableList.of(
                            Component.literal("⟳").withStyle(ChatFormatting.BOLD),
                            Component.literal("⟲").withStyle(ChatFormatting.BOLD),
                            Component.literal("🔓").withStyle(ChatFormatting.BOLD));
            ValueSettingsFormatter formatter =
                    new ValueSettingsFormatter(
                            ((KineticUnlockableScrollValueBehaviour) behaviour)::formatSettings);
            return new ValueSettingsBoard(behaviour.label, 256, 32, rows, formatter);
        }
        if (behaviour instanceof ResistanceScrollValueBehaviour) {
            return new ValueSettingsBoard(
                    behaviour.label,
                    190,
                    10,
                    ImmutableList.of(
                            CEELang.translate("resistor.milli_resistance_symbol").component(),
                            CEELang.translate("resistor.resistance_symbol").component(),
                            CEELang.translate("resistor.kilo_resistance_symbol").component()),
                    new ValueSettingsFormatter(
                            valueSettings ->
                                    CEELang.formatResistance(
                                                    indexToResistance(
                                                            valueSettings.value(),
                                                            valueSettings.row()))
                                            .component()));
        }
        if (behaviour instanceof ScalingScrollValueBehaviour) {
            return new ValueSettingsBoard(
                    behaviour.label,
                    6,
                    1,
                    ImmutableList.of(CEELang.translate("gauge.scaling_symbol").component()),
                    new ValueSettingsFormatter(
                            valueSettings ->
                                    CEELang.translateDirect(
                                                    valueSettings.value() > 2
                                                            ? "gauge.scaling_forward"
                                                            : "gauge.scaling_backward")
                                            .append(
                                                    String.valueOf(
                                                            displayScales[
                                                                    Mth.clamp(
                                                                            valueSettings.value(),
                                                                            0,
                                                                            scales.length - 1)]))));
        }
        if (behaviour instanceof VoltageScrollValueBehaviour) {
            return new ValueSettingsBoard(
                    behaviour.label,
                    190,
                    10,
                    ImmutableList.of(
                            CEELang.translate("creative_battery.voltage_symbol").component(),
                            CEELang.translate("creative_battery.kilo_voltage_symbol").component()),
                    new ValueSettingsFormatter(
                            valueSettings ->
                                    CEELang.formatVoltage(
                                                    indexToVoltage(
                                                            valueSettings.value(),
                                                            valueSettings.row()))
                                            .component()));
        }
        return new ValueSettingsBoard(
                behaviour.label,
                behaviour.max,
                Math.max(1, behaviour.max / 10),
                ImmutableList.of(Component.literal("Value")),
                new ValueSettingsFormatter(
                        v -> Component.literal(behaviour.formatter.apply(v.value()))));
    }

    private static double indexToCapacitance(int i, int row) {
        if (row == 0) {

            if (i < 100) return i * 1e-9d;
            else return (i - 90) * 1e-8d;
        }

        if (row == 1) {

            if (i < 100) return i == 0 ? 1e-6d : i * 1e-6d;
            else return (i - 90) * 1e-5d;
        }

        if (i < 100) return i == 0 ? 1e-3d : i * 1e-3d;
        else return (i - 90) * 1e-2d;
    }

    private static double indexToInductance(int i, int row) {
        if (row == 0) {

            if (i < 100) return i * 1e-9d;
            else return (i - 90) * 1e-8d;
        }

        if (row == 1) {

            if (i < 100) return i == 0 ? 1e-6d : i * 1e-6d;
            else return (i - 90) * 1e-5d;
        }

        if (i < 100) return i == 0 ? 1e-3d : i * 1e-3d;
        else return (i - 90) * 1e-2d;
    }

    private static double indexToResistance(int i, int row) {
        if (row == 0) {
            return (i / 20d) + 0.1;
        } else if (row == 1) {

            if (i < 100) return Math.max(1, i);
            else return (i - 90) * 10;
        }

        if (i < 100) return Math.max(1, i) * 1000;
        else return (i - 90) * 10000;
    }

    private static double indexToVoltage(int i, int row) {
        if (row == 0) {

            if (i < 100) return i;
            else return (i - 90) * 10;
        }

        if (i < 100) return i == 0 ? 1000 : i * 1000;
        else return (i - 90) * 10000;
    }
}
