package com.george_vi.electroenergetics.content.electrical_panel.attachments;

import com.george_vi.electroenergetics.CEEPartialModels;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelBlockEntity;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.simulation.BridgeCollector;
import com.george_vi.electroenergetics.simulation.SimulationResults;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllItems;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.physics.config.dimension_physics.DimensionPhysicsData;
import dev.simulated_team.simulated.data.SimLang;
import dev.simulated_team.simulated.index.SimPartialModels;
import com.george_vi.electroenergetics.CEEPartialModels.Bufferer;
import com.george_vi.electroenergetics.CEEPartialModels.ModelBuffer;
import net.minecraft.ChatFormatting;
import net.createmod.catnip.impl.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;

import java.util.List;

public class AltitudeSensorPanelAttachment extends PanelAttachment {
    public boolean radial = false;
    public double visualHeight = 0;
    public double worldHeight = 0;
    public double prevVisualHeight = 0;

    public AltitudeSensorPanelAttachment(PanelAttachmentType type) {
        super(type);
    }

    @Override
    public void render(ElectricalPanelBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        double visualHeight = Mth.lerp(partialTicks, prevVisualHeight, this.visualHeight);
        transformPose(ms, be);

        Bufferer.partial(radial ?
                                CEEPartialModels.PANEL_ATTACHMENT_SIM_RADIAL_ALTITUDE_SENSOR :
                                CEEPartialModels.PANEL_ATTACHMENT_SIM_LINEAR_ALTITUDE_SENSOR,
                        be.getBlockState())
                .light(light)
                .renderInto(ms, buffer.getBuffer(net.minecraft.client.renderer.rendertype.RenderTypes.cutoutMovingBlock()));

        if (!ModList.get().isLoaded("simulated"))
            return;

        PartialModel dial = radial ?
                SimPartialModels.ALTITUDE_SENSOR_RADIAL_HAND :
                SimPartialModels.ALTITUDE_SENSOR_LINEAR_HAND;

        ModelBuffer dialBuffer = Bufferer.partial(dial, be.getBlockState());

        if (radial) {
            dialBuffer.rotateZCentered((float) (visualHeight * Math.PI / 2.0));
        } else {
            double value = (visualHeight - level.getMinY()) / (level.getMaxY() - level.getMinY());
            dialBuffer.translate(0, (value * 8f - 4f) / 16f, 0);
        }

        dialBuffer
                .translate(0, 0, 5/16f)
                .light(light)
                .renderInto(ms, buffer.getBuffer(RenderTypes.cutoutMovingBlock()));
    }

    @Override
    public InteractionResult onInteract(ItemStack stack, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(AllItems.WRENCH)) {
            radial ^= true;
            sendData();
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void tickClient(ElectricalPanelBlockEntity be) {
        super.tickClient(be);
        Vec3 center = SableCompanion.INSTANCE.projectOutOfSubLevel(level, (Position) Vec3.atCenterOf(pos));

        worldHeight = center.y;

        if (this.visualHeight == 0.0f)
            this.visualHeight = worldHeight;

        float step = 0.15f;
        prevVisualHeight = visualHeight;
        visualHeight = visualHeight * (1.0f - step) + worldHeight * step;
    }

    @Override
    public boolean addToGoggleTooltip(ElectricalPanelBlockEntity be, List<Component> tooltip, boolean isPlayerSneaking) {
        if (!ModList.get().isLoaded("simulated"))
            return false;

        float airPressure = (float) (100 * DimensionPhysicsData.getAirPressure(level, Sable.HELPER.projectOutOfSubLevel(level, JOMLConversion.atCenterOf(pos))));

        CEELang.builder()
                .add(label == null ? Component.translatable("block.simulated.altitude_sensor") : Component.literal(label))
                .forGoggles(tooltip, 1);

        SimLang.translate("altitude_sensor.height", SimLang.text(String.format("%.2f", worldHeight)).style(ChatFormatting.AQUA))
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);

        SimLang.translate("altitude_sensor.air_pressure", SimLang.text(String.format("%.2f%%", airPressure)).style(ChatFormatting.AQUA))
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 2);

        return true;
    }

    @Override
    public void preTick(BridgeCollector bridges) {

    }

    @Override
    public void postTick(SimulationResults results) {

    }
}
