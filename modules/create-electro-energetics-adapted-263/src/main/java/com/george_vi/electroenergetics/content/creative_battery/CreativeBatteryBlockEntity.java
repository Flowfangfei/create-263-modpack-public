package com.george_vi.electroenergetics.content.creative_battery;

import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.foundation.scroll_value.VoltageScrollValueBehaviour;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class CreativeBatteryBlockEntity extends SmartBlockEntity {

    public CreativeBatteryBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public VoltageScrollValueBehaviour voltage;

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        voltage = new VoltageScrollValueBehaviour(CEELang.translateDirect("creative_battery.voltage"),
                this);
        voltage.withCallback(i -> this.updateVoltage());

        behaviours.add(voltage);
    }

    private void updateVoltage() {
        if (!(level instanceof ServerLevel sl))
            return;
        CreativeBatteryDevice device = DevicesSavedData.load(sl).getDevice(getBlockPos(), CreativeBatteryDevice.class);

        if (device != null)
            device.voltage = voltage.getVoltage();
    }

}
