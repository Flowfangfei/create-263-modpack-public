package com.george_vi.electroenergetics.events;

import com.george_vi.electroenergetics.*;
import com.george_vi.electroenergetics.client.ClientNodeData;
import com.george_vi.electroenergetics.client.ElectricPropertiesOverlay;
import com.george_vi.electroenergetics.client.WireEffects;
import com.george_vi.electroenergetics.client.WireRenderer;
import com.george_vi.electroenergetics.commands.CEECommands;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.accumulator.AccumulatorBlock;
import com.george_vi.electroenergetics.content.converter.ConverterBlockEntity;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelBlock;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelClientTicker;
import com.george_vi.electroenergetics.content.fuse.BlownFuseTracker;
import com.george_vi.electroenergetics.content.fuse.FuseBlockItem;
import com.george_vi.electroenergetics.content.linemans_stick.LinemansStickClientHandler;
import com.george_vi.electroenergetics.content.linemans_stick.LinemansStickItem;
import com.george_vi.electroenergetics.content.railway_electrification.gauges.ClientTrainGaugeData;
import com.george_vi.electroenergetics.content.railway_electrification.sound_effects.ElectricTrainSounds;
import com.george_vi.electroenergetics.content.wire.WireSync;
import com.george_vi.electroenergetics.content.wire.interaction.InteractDetachedNodePacket;
import com.george_vi.electroenergetics.content.wire.interaction.OutlinesOnWireRenderer;
import com.george_vi.electroenergetics.content.wire.interaction.WireInteractionBehaviour;
import com.george_vi.electroenergetics.content.wire.interaction.WireInteractionHandler;
import com.george_vi.electroenergetics.content.wire_spool.ChangeLengthWireInteractionBehaviour;
import com.george_vi.electroenergetics.content.wire_spool.WireApplyingBehaviour;
import com.george_vi.electroenergetics.content.wire_spool.WireSparkEffectTicker;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.foundation.CEEHoldInteractionHandler;
import com.george_vi.electroenergetics.foundation.CEELang;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.infrastructure.InWorldNodeData;
import com.george_vi.electroenergetics.simulation.infrastructure.InfrastructureSavedData;
import com.simibubi.create.AllSoundEvents;
import dev.engine_room.flywheel.api.event.ReloadLevelRendererEvent;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Objects;
import java.util.Optional;

@EventBusSubscriber(modid = CreateElectroEnergetics.ID, value = Dist.CLIENT)
public final class ClientGameEvents {

    @SubscribeEvent
    public static void tickClient(ClientTickEvent.Pre event) {
        if (Minecraft.getInstance().level == null || Minecraft.getInstance().player == null)
            return;
        WireApplyingBehaviour.tick();
        WireInteractionHandler.tick();
        OutlinesOnWireRenderer.OutlinerExt.tick();
        WireEffects.tick();
        CEEHoldInteractionHandler.tick();
        ElectricTrainSounds.tick();
        ClientTrainGaugeData.tick();
        LinemansStickClientHandler.tick();
        FuseBlockItem.tickClient();
        ElectricalPanelClientTicker.tick();

        ElectricPropertiesOverlay.INSTANCE.ticks++;

        // Safety?
        WireInteractionHandler.preventUseOnBlockPacket = false;

        for (ClientNodeData nodeData : WireRenderer.NODE_DATA.values()) {
            nodeData.tick();
        }
    }

    @SubscribeEvent
    public static void mouseScrolled(InputEvent.MouseScrollingEvent event) {
        double delta = event.getScrollDeltaY();
        event.setCanceled(FuseBlockItem.mouseScrolled(delta) || ChangeLengthWireInteractionBehaviour.mouseScrolled(delta));
    }

    @SubscribeEvent
    public static void extractBlockOutline(ExtractBlockOutlineRenderStateEvent event) {
        if (WireInteractionHandler.targetedPoint != null) {
            // The wire/node outliner already provides the current interaction target.
            event.setCanceled(true);
            return;
        }

        BlockState state = event.getBlockState();
        VoxelShape shape = null;
        if (CEEBlocks.ACCUMULATOR.has(state))
            shape = AccumulatorBlock.getHighlightShape(event.getHitResult(), state);
        else if (state.getBlock() instanceof ElectricalPanelBlock)
            shape = ElectricalPanelClientTicker.getHighlightShape(event.getLevel(), event.getHitResult(), state);

        if (shape == null)
            return;

        // Extraction owns level/hit queries. The vanilla submit pass consumes only this
        // selected shape and retains native high-contrast, line-width and terrain ordering.
        event.getLevelRenderState().blockOutlineRenderState = new BlockOutlineRenderState(
                event.getBlockPos().immutable(), event.isInTranslucentPass(), event.isHighContrast(),
                shape, event.getCustomRenderers());
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void extractLevel(net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent event) {
        var geometry=new CEEPartialModels.GeometryCollector();
        WireRenderer.render(Minecraft.getInstance().levelRenderer,new com.mojang.blaze3d.vertex.PoseStack(),event.getCamera(),geometry,event.getFrustum());
        event.getRenderState().setRenderData(WIRE_GEOMETRY,geometry);
    }

    @SubscribeEvent
    public static void submitLevel(net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent event) {
        var geometry=event.getLevelRenderState().getRenderData(WIRE_GEOMETRY);
        if(geometry!=null) geometry.submit(event.getSubmitNodeCollector());
    }

    @SubscribeEvent
    public static void reloadLevelRenderer(ReloadLevelRendererEvent event) {
        WireRenderer.recreateVisuals();
    }

    @SubscribeEvent
    public static void itemTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().is(CEETags.ELECTRICAL_PANEL_ATTACHMENT) &&
                CEEConfigs.client().displayPanelTooltip.get()) {
            event.getToolTip().add(CEELang.translateDirect("hint.placeable_in_electrical_panel"));
        }
    }

    @SubscribeEvent
    public static void playerInteractItem(PlayerInteractEvent.RightClickItem event) {
        if (!event.getLevel().isClientSide() || event.getHand() != InteractionHand.MAIN_HAND)
            return;

        ItemStack stack = event.getItemStack();

        if (CEEHoldInteractionHandler.isInteracting()) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
            WireInteractionHandler.preventUseOnBlockPacket = true;
            return;
        }

        // Detached Node Interactions:
        if (WireApplyingBehaviour.targetingDetachedNode != null) {
            net.createmod.catnip.api.client.network.ClientNetworkHelper.INSTANCE.sendToServer(new InteractDetachedNodePacket(WireApplyingBehaviour.targetingDetachedNode));

            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            WireInteractionHandler.preventUseOnBlockPacket = true;
        }

        // Wire interactions:
        WireInteractionBehaviour behaviour = CEERegistries.WIRE_INTERACTION_BEHAVIOUR.stream()
                .filter(h -> h.isActiveFor(stack, event.getEntity()))
                .findFirst().orElse(null);
        if (behaviour == null)
            return;

        if (behaviour.tryUseOnWire(event.getLevel(), event.getEntity(), event.getHand())) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    private static final net.minecraft.util.context.ContextKey<CEEPartialModels.GeometryCollector> WIRE_GEOMETRY=
        new net.minecraft.util.context.ContextKey<>(CreateElectroEnergetics.rl("wire_geometry"));

    public static void playerInteractOnBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        if (CEEHoldInteractionHandler.isInteracting()) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
            WireInteractionHandler.preventUseOnBlockPacket = true;
            return;
        }

        // Detached Node Interactions:
        if (WireApplyingBehaviour.targetingDetachedNode != null) {
            net.createmod.catnip.api.client.network.ClientNetworkHelper.INSTANCE.sendToServer(new InteractDetachedNodePacket(WireApplyingBehaviour.targetingDetachedNode));

            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            WireInteractionHandler.preventUseOnBlockPacket = true;
        }

        // Wire interactions:
        WireInteractionBehaviour behaviour = CEERegistries.WIRE_INTERACTION_BEHAVIOUR.stream()
                .filter(h -> h.isActiveFor(stack, event.getEntity()))
                .findFirst().orElse(null);
        if (behaviour == null)
            return;

        if (behaviour.tryUseOnWire(event.getLevel(), event.getEntity(), event.getHand())) {
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            WireInteractionHandler.preventUseOnBlockPacket = true;
        }
    }
}
