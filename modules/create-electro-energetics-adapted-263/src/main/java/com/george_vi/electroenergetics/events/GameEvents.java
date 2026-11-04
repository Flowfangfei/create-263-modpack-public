package com.george_vi.electroenergetics.events;

import com.george_vi.electroenergetics.*;
import com.george_vi.electroenergetics.commands.CEECommands;
import com.george_vi.electroenergetics.config.CEEConfigs;
import com.george_vi.electroenergetics.content.electrical_panel.ElectricalPanelBlock;
import com.george_vi.electroenergetics.content.fuse.BlownFuseTracker;
import com.george_vi.electroenergetics.content.linemans_stick.LinemansStickItem;
import com.george_vi.electroenergetics.content.wire.WireSync;
import com.george_vi.electroenergetics.content.wire_spool.WireSparkEffectTicker;
import com.george_vi.electroenergetics.devices.device.DevicesSavedData;
import com.george_vi.electroenergetics.foundation.nodes.InWorldNode;
import com.george_vi.electroenergetics.simulation.infrastructure.*;
import com.george_vi.electroenergetics.simulation.simulator.SimulationTicker;
import com.george_vi.electroenergetics.events.network.ServerConfigPacket;
import com.zurrtum.create.AllSoundEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import java.util.Objects;
import java.util.Optional;

public final class GameEvents {
    public static void register() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> SimulationTicker.runServer());
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> SimulationTicker.stopServer());
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) LinemansStickItem.tickPlayerRange(player);
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (CEEConfigs.CHANGED.getAndSet(false)) {
                for (ServerLevel level : server.getAllLevels())
                    InfrastructureSavedData.load(level).wireSimulationState.onReloadConfigs();
                ServerConfigPacket packet = new ServerConfigPacket(CEEConfigs.server().snapshot().toString());
                for (ServerPlayer player : server.getPlayerList().getPlayers()) ServerPlayNetworking.send(player, packet);
            }
        });
        ServerTickEvents.END_LEVEL_TICK.register(GameEvents::tickLevel);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> synchronizePlayer(handler.getPlayer()));
        ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register((player, origin, destination) -> {
            InfrastructureSavedData.load(origin).wireSync.unloadForPlayer(player);
            synchronizePlayer(player);
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, registries, environment) -> CEECommands.register(dispatcher));
        UseBlockCallback.EVENT.register(GameEvents::renameNode);
        AddToElectricGraphEvent.EVENT.register(GameEvents::addToElectricGraph);
        FinishElectricSimulationEvent.EVENT.register(GameEvents::finishElectricSimulation);
    }

    private static void tickLevel(ServerLevel level) {
        InfrastructureSavedData sd = InfrastructureSavedData.load(level);
        sd.tick();
        for (ServerPlayer player : level.players()) {
            Vec3 position = player.position();
            sd.wireSync.handlePlayerEnterNewSection(player,
                ChunkPos.pack(Mth.floor(position.x) >> 4, Mth.floor(position.z) >> 4));
        }
        BlownFuseTracker.tick();
    }

    private static void synchronizePlayer(ServerPlayer player) {
        WireSync sync = InfrastructureSavedData.load(player.level()).wireSync;
        sync.unloadForPlayer(player);
        sync.handlePlayerEnterNewSection(player, ChunkPos.pack(player.blockPosition()));
    }

    private static InteractionResult renameNode(Player player, Level world, InteractionHand hand, BlockHitResult hit) {
        if (!(world instanceof ServerLevel level) || hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        ItemStack stack = player.getItemInHand(hand);
        BlockPos pos = hit.getBlockPos();
        if (!(player instanceof ServerPlayer sender) ||
            !com.george_vi.electroenergetics.events.network.CEENetwork.canModify(sender, pos,
                sender.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.BLOCK_INTERACTION_RANGE) + 1))
            return InteractionResult.PASS;
        if (!stack.is(CEETags.NODE_RENAME_ITEM) || !player.mayUseItemAt(pos, hit.getDirection(), stack))
            return InteractionResult.PASS;
        InWorldNode hoveredNode = InWorldNode.closestNode(level, hit.getLocation(), 1.5f, pos);
        BlockState hoveredBlockState = level.getBlockState(pos);

        if (stack.is(CEETags.PANEL_ATTACHMENT_RENAME_ITEM) && hoveredBlockState.getBlock() instanceof ElectricalPanelBlock)
            return InteractionResult.PASS;

        if (hoveredNode == null)
            hoveredNode = InWorldNode.closestNode(level, pos, hoveredBlockState, 1.5f, hit.getLocation());

        if (hoveredNode == null)
            return InteractionResult.PASS;

        InfrastructureSavedData sd = InfrastructureSavedData.load(level);

        InWorldNodeData nodeData = sd.getNodeData(hoveredNode);
        if (nodeData == null)
            return InteractionResult.PASS;

        String prevLabel = nodeData.label;
        nodeData.label = Optional.ofNullable(stack.get(DataComponents.CUSTOM_NAME))
                .map(Component::getString)
                .orElse(null);

        sd.wireSync.handleNodeLabelRename(nodeData);

        if (!Objects.equals(prevLabel, nodeData.label))
            if (nodeData.label == null)
                AllSoundEvents.CLIPBOARD_ERASE.playOnServer(level, hoveredNode.sourcePos());
            else
                AllSoundEvents.CLIPBOARD_CHECKMARK.playOnServer(level, hoveredNode.sourcePos());

        return InteractionResult.SUCCESS;
    }

    public static void addToElectricGraph(AddToElectricGraphEvent event) {
        InfrastructureSavedData sd = event.sd;
        if (sd.wireSimulationState.rebuild) sd.wireSimulationState.rebuild();
        sd.catenaryModule.buildCircuit(event.builder);
        sd.wireElectrocutionModule.buildCircuit(event.builder);
        WireSparkEffectTicker.preTick(event.level);
    }

    public static void finishElectricSimulation(FinishElectricSimulationEvent event) {
        InfrastructureSavedData sd = event.sd;
        sd.wireElectrocutionModule.finishSimulation(event.results);
        sd.catenaryModule.finishSimulation(event.results);
        WireSparkEffectTicker.postTick(event.level, event.results);
    }

    public static boolean preventsNaturalSpawn(ServerLevel level, BlockPos position) {
        return CEEConfigs.server().bulbsPreventMobSpawns.get()
            && DevicesSavedData.load(level).getDevices(CEESimulatedDeviceFeatureTypes.SPAWN_PREVENTING.get()).stream()
                .anyMatch(device -> device.pos.distSqr(position) <= 400);
    }
}
