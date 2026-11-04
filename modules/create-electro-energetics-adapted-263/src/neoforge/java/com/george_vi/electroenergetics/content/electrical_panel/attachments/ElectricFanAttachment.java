package com.george_vi.electroenergetics.content.electrical_panel.attachments;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelBlockEntity;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelClientTicker;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.george_vi.electroenergetics.simulation.electrical_properties.ElectricalProperties;
import com.mojang.blaze3d.vertex.PoseStack;
import com.george_vi.electroenergetics.CEEPartialModels.Bufferer;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
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
    public void render(ElectricalPanelBlockEntity be, float partialTicks, PoseStack ms,
                       MultiBufferSource buffer, int light, int overlay) {
        transformPose(ms, be);

        Bufferer.partial(CEEPartialModels.PANEL_ATTACHMENT_FAN_CONNECTORS,
                        be.getBlockState())
                .light(light)
                .renderInto(ms, buffer.getBuffer(net.minecraft.client.renderer.rendertype.RenderTypes.solidMovingBlock()));

        float coverAlpha = Mth.lerp(partialTicks, ElectricalPanelClientTicker.prevCoverAlpha, ElectricalPanelClientTicker.coverAlpha);
        coverAlpha = Mth.lerp(coverAlpha, 0.5f, 1f);

        if (coverAlpha != 0) {
        Bufferer.partial(CEEPartialModels.PANEL_ATTACHMENT_FAN_BODY,
                        be.getBlockState())
                .light(light)
                .color(255, 255, 255, (int) (coverAlpha * 255))
                .renderInto(ms, buffer.getBuffer(coverAlpha == 1 ? net.minecraft.client.renderer.rendertype.RenderTypes.solidMovingBlock() : net.minecraft.client.renderer.rendertype.RenderTypes.translucentMovingBlock()));
        }
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
