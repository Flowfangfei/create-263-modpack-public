package com.george_vi.electroenergetics.content.electronic_components.inductor;

import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.foundation.scroll_value.InductanceScrollValueBehaviour;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class InductorBlockEntity extends SmartBlockEntity {
    public InductorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public InductanceScrollValueBehaviour inductance;

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        inductance = new InductanceScrollValueBehaviour(CEELang.translate("inductor.inductance").component(), this);
        inductance.withCallback(i -> this.updateInductance());
        behaviours.add(inductance);
    }

    private void updateInductance() {
        if (!(level instanceof ServerLevel sl))
            return;
        InfrastructureSavedData sd = InfrastructureSavedData.load(sl);
        InductorDevice device = DevicesSavedData.load(sl).getDevice(worldPosition, InductorDevice.class);

        if (device != null)
            device.inductance = inductance.getInductance();

    }

}
