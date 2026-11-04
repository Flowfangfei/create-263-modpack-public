package com.george_vi.electroenergetics.content.rotor;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.client.render.ElectricalKineticRenderer;
import com.george_vi.electroenergetics.client.render.ModelBuffers;
import com.george_vi.electroenergetics.client.render.GeometryBuffer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class AlternatorBrushesRenderer extends ElectricalKineticRenderer<AlternatorBrushesBlockEntity> {
    public AlternatorBrushesRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected GeometryBuffer getRotatedModel(AlternatorBrushesBlockEntity be, BlockState state) {
        return ModelBuffers.partialFacing(CEEPartialModels.ALTERNATOR_BRUSHES_SHAFT, state);
    }
}
