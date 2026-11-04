package com.george_vi.electroenergetics.content.electrical_panel.attachments;

import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelBlockEntity;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.simulation.electrical_properties.ElectricalProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ElectricFanAttachment extends PanelAttachment {

    public float temp;

    public ElectricFanAttachment(PanelAttachmentType type) {
        super(type);
    }

    @Override
    public void tickClient(ElectricalPanelBlockEntity be) {

    }

    @Override
    public float getNodeSize(Level level, BlockPos pos, BlockState state, int id) {
        return 2/16f;
    }

    @Override
    public void preTick(BridgeCollector bridges) {
        bridges.bridge(nodes[0], nodes[1], ElectricalProperties.resistor(CEEConfigs.server().resistanceValues.electricFanResistance.get()));
    }

    @Override
    public void postTick(SimulationResults results) {

    }

    @Override
    public void read(CompoundTag tag, boolean clientPacket, HolderLookup.Provider registries) {

    }

    @Override
    public void write(CompoundTag tag, boolean clientPacket, HolderLookup.Provider registries) {

    }
}
