package com.george_vi.electroenergetics.content.electric_motor;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.client.render.ElectricalKineticRenderer;
import com.george_vi.electroenergetics.client.render.ModelBuffers;
import com.george_vi.electroenergetics.client.render.GeometryBuffer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class ElectricMotorRenderer extends ElectricalKineticRenderer<ElectricMotorBlockEntity> {
    public ElectricMotorRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected GeometryBuffer getRotatedModel(ElectricMotorBlockEntity be, BlockState state) {
        return ModelBuffers.partialFacing(CEEPartialModels.ELECTRIC_MOTOR_SHAFT, state);
    }
}
