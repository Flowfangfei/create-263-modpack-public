/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2025 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.content.switches;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.ListBuilder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.railwayteam.railways.content.switches.TrackSwitchBlock.SwitchState;
import com.railwayteam.railways.mixin_interfaces.ISwitchDisabledEdge;
import com.railwayteam.railways.registry.CREdgePointTypes;
import com.simibubi.create.Create;
import net.createmod.catnip.api.data.Couple;
import com.simibubi.create.content.trains.graph.DimensionPalette;
import com.simibubi.create.content.trains.graph.EdgePointType;
import com.simibubi.create.content.trains.graph.TrackEdge;
import com.simibubi.create.content.trains.graph.TrackGraph;
import com.simibubi.create.content.trains.graph.TrackNode;
import com.simibubi.create.content.trains.graph.TrackNodeLocation;
import com.simibubi.create.content.trains.signal.SignalPropagator;
import com.simibubi.create.content.trains.signal.SingleBlockEntityEdgePoint;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("DuplicatedCode")
public class TrackSwitch extends SingleBlockEntityEdgePoint {
	private static int selectionPriorityTicker;

	private TrackNodeLocation switchPoint;
	private final List<TrackNodeLocation> exits = new ArrayList<>();
	private @Nullable TrackNodeLocation straightExit;
	private @Nullable TrackNodeLocation leftExit;
	private @Nullable TrackNodeLocation rightExit;

	private @NotNull SwitchState switchState = SwitchState.NORMAL;
	private boolean automatic;
	private boolean locked;
	private boolean autoTrainsSwitch;
	private int ticks;
	private boolean forceTickClient;

	@ApiStatus.Internal
	public static int getSelectionPriority() {
		return ++selectionPriorityTicker;
	}

	public EdgePointType<?> getType() {
		return CREdgePointTypes.SWITCH;
	}

	public boolean canCoexistWith(EdgePointType<?> otherType, boolean front) {
		return otherType == EdgePointType.SIGNAL;
	}

	public void blockEntityAdded(BlockEntity tile, boolean front) {
		super.blockEntityAdded(tile, front);
		if (tile instanceof TrackSwitchBlockEntity te) {
			te.calculateExits(this);
			automatic = te.isAutomatic();
			locked = te.isLocked();
		}
		notifyTrains(tile.getLevel());
	}

	public void onRemoved(MinecraftServer server, TrackGraph graph) {
		exits.clear();
		sortExits();
		removeFromAllGraphs();
	}

	private void notifyTrains(Level level) {
		if (level == null || edgeLocation == null)
			return;
		TrackGraph graph = Create.RAILWAYS.sided(level).getGraph(level, edgeLocation.getFirst());
		if (graph == null)
			return;
		TrackEdge edge = graph.getConnection(edgeLocation.map(graph::locateNode));
		if (edge == null)
			return;
		SignalPropagator.notifyTrains(graph, edge);
	}

	public boolean shouldAutoTrainsSwitch() {
		return autoTrainsSwitch;
	}

	void setAutoTrainsSwitch(boolean autoTrainsSwitch) {
		this.autoTrainsSwitch = autoTrainsSwitch;
	}

	public boolean isAutomatic() {
		return automatic;
	}

	public boolean isLocked() {
		return locked;
	}

	void setLocked(boolean locked) {
		this.locked = locked;
	}

	private @Nullable TrackNodeLocation getExit(SwitchState state) {
		return switch (state) {
			case NORMAL -> straightExit;
			case REVERSE_RIGHT -> rightExit;
			case REVERSE_LEFT -> leftExit;
		};
	}

	private Map<SwitchState, Optional<TrackNodeLocation>> getExistingExits() {
		return Map.of(
			SwitchState.NORMAL, Optional.ofNullable(straightExit),
			SwitchState.REVERSE_LEFT, Optional.ofNullable(leftExit),
			SwitchState.REVERSE_RIGHT, Optional.ofNullable(rightExit)
		);
	}

	@ApiStatus.Internal
	public @Nullable SwitchState getTargetState(TrackNodeLocation loc) {
		for (Map.Entry<SwitchState, Optional<TrackNodeLocation>> entry : getExistingExits().entrySet()) {
			if (entry.getValue().isPresent() && entry.getValue().get().equals(loc))
				return entry.getKey();
		}
		return null;
	}

	// 26.3: FriendlyByteBuf lost readList
	private static List<TrackNodeLocation> readExits(FriendlyByteBuf buffer, DimensionPalette dimensions) {
		int count = buffer.readVarInt();
		List<TrackNodeLocation> list = new ArrayList<>(count);
		for (int i = 0; i < count; i++)
			list.add(TrackNodeLocation.receive(buffer, dimensions));
		return list;
	}

	void updateExits(TrackNodeLocation switchPoint, Collection<TrackNodeLocation> newExits) {
		this.switchPoint = switchPoint;

		if (edgeLocation == null || switchPoint == null) {
			exits.clear();
			sortExits();
			return;
		}

		Vec3 forward = edgeLocation.getFirst().getLocation().vectorTo(edgeLocation.getSecond().getLocation());
		exits.clear();
		exits.addAll(newExits.stream()
			.filter(e -> forward.dot(switchPoint.getLocation().vectorTo(e.getLocation())) > 0)
			.toList());
		sortExits();
		ensureValidState();
	}

	private void sortExits() {
		if (edgeLocation == null || switchPoint == null) {
			leftExit = null;
			straightExit = null;
			rightExit = null;
			return;
		}

		Vec3 forward = edgeLocation.getFirst().getLocation().vectorTo(edgeLocation.getSecond().getLocation()).normalize();
		exits.sort(Comparator.comparing(e -> {
			Vec3 exitDir = switchPoint.getLocation().vectorTo(e.getLocation());
			return forward.x * exitDir.z - forward.z * exitDir.x;
		}));

		if (exits.size() == 1) {
			leftExit = null;
			straightExit = exits.get(0);
			rightExit = null;
		} else if (exits.size() == 2) {
			Vec3 firstExitDir = switchPoint.getLocation().vectorTo(exits.get(0).getLocation()).normalize();
			Vec3 secondExitDir = switchPoint.getLocation().vectorTo(exits.get(1).getLocation()).normalize();

			double firstExitRelativeDir = forward.x * firstExitDir.z - forward.z * firstExitDir.x;
			double secondExitRelativeDir = forward.x * secondExitDir.z - forward.z * secondExitDir.x;

			double cutoff = 0.2;
			if (firstExitRelativeDir < 0 && secondExitRelativeDir <= cutoff) {
				leftExit = exits.get(0);
				straightExit = exits.get(1);
				rightExit = null;
			} else if (firstExitRelativeDir >= -cutoff && secondExitRelativeDir > 0) {
				leftExit = null;
				straightExit = exits.get(0);
				rightExit = exits.get(1);
			} else {
				leftExit = exits.get(0);
				straightExit = null;
				rightExit = exits.get(1);
			}
		} else if (exits.size() == 3) {
			leftExit = exits.get(0);
			straightExit = exits.get(1);
			rightExit = exits.get(2);
		} else {
			leftExit = null;
			straightExit = null;
			rightExit = null;
		}
	}

	@Nullable TrackNodeLocation getSwitchPoint() {
		return switchPoint;
	}

	Collection<TrackNodeLocation> getExits() {
		return exits.stream().toList();
	}

	private boolean isStateValid(SwitchState state) {
		if (state == null)
			return false;
		return switch (state) {
			case NORMAL -> hasStraightExit();
			case REVERSE_RIGHT -> hasRightExit();
			case REVERSE_LEFT -> hasLeftExit();
		};
	}

	private SwitchState getValidSwitchState() {
		for (SwitchState state : SwitchState.values()) {
			if (isStateValid(state))
				return state;
		}
		return SwitchState.NORMAL;
	}

	void ensureValidState() {
		if (!isStateValid(switchState))
			switchState = getValidSwitchState();
	}

	public boolean trySetSwitchState(@NotNull SwitchState state) {
		if (isLocked())
			return false;
		return setSwitchState(state);
	}

	public boolean setSwitchState(@NotNull SwitchState state) {
		if (isStateValid(state) && switchState != state) {
			switchState = state;
			ticks = 10000;
			forceTickClient = true;
			return true;
		}
		return false;
	}

	public @NotNull SwitchState getSwitchState() {
		return switchState;
	}

	public @Nullable TrackNodeLocation getSwitchTarget() {
		return getExit(switchState);
	}

	public boolean hasStraightExit() {
		return straightExit != null;
	}

	public boolean hasLeftExit() {
		return leftExit != null;
	}

	public boolean hasRightExit() {
		return rightExit != null;
	}

	public void write(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries, DimensionPalette dimensions) {
        super.write(tag, registries, dimensions);
        if (switchPoint != null) tag.put("SwitchPoint", switchPoint.write(dimensions));
        var list = new net.minecraft.nbt.ListTag();
        for (TrackNodeLocation exit : exits) list.add(exit.write(dimensions));
        tag.put("Exits", list);
        tag.putString("SwitchState", switchState.getSerializedName());
        tag.putBoolean("Automatic", automatic);
        tag.putBoolean("Locked", locked);
        tag.putBoolean("AutoTrainsSwitch", autoTrainsSwitch);
    }


	public void write(FriendlyByteBuf buffer, DimensionPalette dimensions) {
		super.write(buffer, dimensions);
		buffer.writeInt(switchState.ordinal());
		buffer.writeBoolean(automatic);
		buffer.writeBoolean(locked);
		buffer.writeBoolean(autoTrainsSwitch);
		boolean hasPoint = switchPoint != null;
		buffer.writeBoolean(hasPoint);
		if (hasPoint) {
			switchPoint.send(buffer, dimensions);
			// 26.3: FriendlyByteBuf lost writeCollection/readList
			buffer.writeVarInt(exits.size());
			for (TrackNodeLocation e : exits)
				e.send(buffer, dimensions);
		}
	}

	public void read(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries, boolean migration, DimensionPalette dimensions) {
        super.read(tag, registries, migration, dimensions);
        try { switchState = SwitchState.valueOf(tag.getStringOr("SwitchState", "normal").toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException e) { switchState = getValidSwitchState(); }
        automatic = tag.getBooleanOr("Automatic", false);
        locked = tag.getBooleanOr("Locked", false);
        autoTrainsSwitch = tag.getBooleanOr("AutoTrainsSwitch", false);
        if (tag.contains("SwitchPoint")) {
            var decoded = tag.getListOrEmpty("Exits").stream()
                .filter(net.minecraft.nbt.CompoundTag.class::isInstance)
                .map(net.minecraft.nbt.CompoundTag.class::cast)
                .map(value -> TrackNodeLocation.read(value, dimensions)).toList();
            updateExits(TrackNodeLocation.read(tag.getCompoundOrEmpty("SwitchPoint"), dimensions), decoded);
        } else { exits.clear(); sortExits(); ensureValidState(); }
    }


	public void read(FriendlyByteBuf buffer, DimensionPalette dimensions) {
		super.read(buffer, dimensions);
		switchState = SwitchState.values()[buffer.readInt()];
		automatic = buffer.readBoolean();
		locked = buffer.readBoolean();
		autoTrainsSwitch = buffer.readBoolean();
		if (buffer.readBoolean()) {
			updateExits(
				TrackNodeLocation.receive(buffer, dimensions),
				readExits(buffer, dimensions)
			);
		} else {
			exits.clear();
			sortExits();
			ensureValidState();
		}
	}

	boolean doForceTickClient() {
		if (!forceTickClient)
			return false;
		forceTickClient = false;
		return true;
	}

	public void tick(TrackGraph graph, boolean preTrains) {
		super.tick(graph, preTrains);
		if (preTrains) {
			ticks++;
			if (ticks < 10)
				return;
			ticks = 0;
			updateEdges(graph);
			if (automatic)
				switchForEdges(graph);
		}
	}

	public void updateEdges(TrackGraph graph) {
		updateEdges(graph, false);
	}

	public void setEdgesActive(TrackGraph graph) {
		updateEdges(graph, true);
	}

	private void updateEdges(TrackGraph graph, boolean forceActive) {
		TrackNodeLocation from = switchPoint;
		if (from == null)
			return;
		for (TrackNodeLocation to : exits) {
			if (to == null)
				continue;

			TrackNode toNode = graph.locateNode(to);
			Map<TrackNode, TrackEdge> connections = graph.getConnectionsFrom(toNode);
			if (connections == null)
				continue;

			TrackNode closestFromNode = null;
			TrackEdge closestEdge = null;
			double closestDistance = Double.MAX_VALUE;
			for (Map.Entry<TrackNode, TrackEdge> otherEnd : connections.entrySet()) {
				double distance = otherEnd.getKey().getLocation().distSqr(from);
				if (distance < closestDistance) {
					closestDistance = distance;
					closestEdge = otherEnd.getValue();
					closestFromNode = otherEnd.getKey();
				}
			}

			boolean enabled = forceActive || getSwitchTarget() == to;
			boolean autoSelectable = !forceActive && automatic && !locked && autoTrainsSwitch;
			if (closestEdge != null)
				updateSwitchEdge(closestEdge, enabled, autoSelectable);

			if (closestFromNode != null) {
				TrackEdge reverseEdge = graph.getConnection(Couple.create(closestFromNode, toNode));
				if (reverseEdge != null)
					updateSwitchEdge(reverseEdge, enabled, autoSelectable);
			}
		}
	}

	private static void updateSwitchEdge(TrackEdge edge, boolean enabled, boolean autoSelectable) {
		ISwitchDisabledEdge switchEdge = (ISwitchDisabledEdge) edge.getEdgeData();
		switchEdge.setEnabled(enabled);
		switchEdge.setAutomatic(autoSelectable);
	}

	private void switchForEdges(TrackGraph graph) {
		TrackNodeLocation from = switchPoint;
		if (from == null)
			return;

		TrackNodeLocation highestPriorityExit = null;
		int highestPriority = -100;
		for (TrackNodeLocation to : exits) {
			if (to == null)
				continue;

			TrackNode toNode = graph.locateNode(to);
			Map<TrackNode, TrackEdge> connections = graph.getConnectionsFrom(toNode);
			if (connections == null)
				continue;

			TrackNode closestFromNode = null;
			TrackEdge closestEdge = null;
			double closestDistance = Double.MAX_VALUE;
			for (Map.Entry<TrackNode, TrackEdge> otherEnd : connections.entrySet()) {
				double distance = otherEnd.getKey().getLocation().distSqr(from);
				if (distance < closestDistance) {
					closestDistance = distance;
					closestEdge = otherEnd.getValue();
					closestFromNode = otherEnd.getKey();
				}
			}

			if (closestEdge != null) {
				ISwitchDisabledEdge switchEdge = (ISwitchDisabledEdge) closestEdge.getEdgeData();
				if (switchEdge.isAutomaticallySelected()) {
					if (switchEdge.getAutomaticallySelectedPriority() > highestPriority) {
						highestPriorityExit = to;
						highestPriority = switchEdge.getAutomaticallySelectedPriority();
					}
					switchEdge.ackAutomaticSelection();
				}
			}

			if (closestFromNode != null) {
				TrackEdge reverseEdge = graph.getConnection(Couple.create(closestFromNode, toNode));
				if (reverseEdge != null) {
					ISwitchDisabledEdge reverseSwitchEdge = (ISwitchDisabledEdge) reverseEdge.getEdgeData();
					if (reverseSwitchEdge.isAutomaticallySelected()) {
						if (reverseSwitchEdge.getAutomaticallySelectedPriority() > highestPriority) {
							highestPriorityExit = to;
							highestPriority = reverseSwitchEdge.getAutomaticallySelectedPriority();
						}
						reverseSwitchEdge.ackAutomaticSelection();
					}
				}
			}
		}

		if (highestPriorityExit != null) {
			for (SwitchState state : SwitchState.values()) {
				if (highestPriorityExit == getExit(state)) {
					setSwitchState(state);
					break;
				}
			}
		}
	}
}
