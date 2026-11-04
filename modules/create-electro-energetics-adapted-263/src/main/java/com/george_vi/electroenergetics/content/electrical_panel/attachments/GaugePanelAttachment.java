package com.george_vi.electroenergetics.content.electrical_panel.attachments;

import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelBlockEntity;
import com.george_vi.electroenergetics.foundation.RMSHolder;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.simulation.electrical_properties.ElectricalProperties;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

public class GaugePanelAttachment extends PanelAttachment {
    public static final double AMMETER_RESISTANCE = 0.01;
    public float dialTarget;
    public float dialState;
    public float prevDialState;
    public final boolean voltmeter;
    public RMSHolder rmsVoltages;
    public double value;

    private GaugePanelAttachment(PanelAttachmentType type, boolean voltmeter) {
        super(type);
        this.voltmeter = voltmeter;
    }

    public static PanelAttachment ammeter(PanelAttachmentType type) {
        return new GaugePanelAttachment(type, false);
    }

    public static PanelAttachment voltmeter(PanelAttachmentType type) {
        return new GaugePanelAttachment(type, true);
    }

    @Override
    public void tickClient(ElectricalPanelBlockEntity be) {

        prevDialState = dialState;
        dialState += (dialTarget - dialState) * .125f;
        if (dialState > 1 && level.getRandom().nextFloat() < 1 / 2f)
            dialState -= (dialState - 1) * level.getRandom().nextFloat();
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        bridges.bridge(nodes[0], nodes[1], ElectricalProperties.resistor(voltmeter ? 1_000_000 : AMMETER_RESISTANCE));
    }

    @Override
    public void postTick(SimulationResults results) {
        if (!level.isLoaded(pos))
            return;

        double vd = results.getVoltageAt(nodes[0], nodes[1]);
        this.rmsVoltages.add(vd);
        vd = this.rmsVoltages.get();
        value = voltmeter ? Math.abs(vd) : Math.abs(vd) / AMMETER_RESISTANCE;
        if (level.getBlockEntity(pos) instanceof ElectricalPanelBlockEntity be)
            be.sendData();
    }

    @Override
    public void read(CompoundTag tag, boolean clientPacket, HolderLookup.Provider registries) {
        value = tag.getDoubleOr("Value", 0d);
        if (clientPacket) {
            dialTarget = (float) Mth.clamp(voltmeter ? value / 1000 : value / 40, 0, 1);
            return;
        }

        this.rmsVoltages = new RMSHolder(2);
        this.rmsVoltages.read(tag, "Voltages");
    }

    @Override
    public void write(CompoundTag tag, boolean clientPacket, HolderLookup.Provider registries) {
        tag.putDouble("Value", value);
        if (clientPacket)
            return;

        this.rmsVoltages.write(tag, "Voltages");
    }

    public boolean miniature() {
        return slot.fullWidth == 14;
    }
}
