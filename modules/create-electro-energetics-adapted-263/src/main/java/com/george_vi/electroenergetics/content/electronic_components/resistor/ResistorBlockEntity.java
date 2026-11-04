package com.george_vi.electroenergetics.content.electronic_components.resistor;

import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.foundation.scroll_value.ResistanceScrollValueBehaviour;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.simulation.electrical_properties.ElectricalProperties;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class ResistorBlockEntity extends SmartBlockEntity {
    public ResistorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public ResistanceScrollValueBehaviour resistance;

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        resistance = new ResistanceScrollValueBehaviour(CEELang.translate("resistor.resistance").component(), this);
        resistance.withCallback(i -> this.updateResistance());
        behaviours.add(resistance);
    }

    public void setResistance(double res) {
        resistance.setValue(Mth.floor(res * 1000));
    }

    private void updateResistance() {
        if (!(level instanceof ServerLevel sl))
            return;

        ResistorDevice device = DevicesSavedData.load(sl).getDevice(worldPosition, ResistorDevice.class);
        if (device != null)
            device.properties = ElectricalProperties.resistor(resistance.getResistance());

    }

}
