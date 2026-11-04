package com.george_vi.electroenergetics.content.electronic_components.capacitor;

import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.foundation.scroll_value.CapacitanceScrollValueBehaviour;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class CapacitorBlockEntity extends SmartBlockEntity {
    public CapacitorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public CapacitanceScrollValueBehaviour capacitance;

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        capacitance = new CapacitanceScrollValueBehaviour(CEELang.translate("capacitor.capacitance").component(), this);
        capacitance.withCallback(i -> this.updateCapacitance());
        behaviours.add(capacitance);
    }

    private void updateCapacitance() {
        if (!(level instanceof ServerLevel sl))
            return;
        CapacitorDevice device = DevicesSavedData.load(sl).getDevice(worldPosition, CapacitorDevice.class);

        if (device != null)
            device.capacitance = capacitance.getCapacitance();
    }

}
