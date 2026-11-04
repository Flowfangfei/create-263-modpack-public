/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2024 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.railwayteam.railways.content.coupling.coupler;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.railwayteam.railways.mixin_interfaces.IHandcarTrain;
import com.railwayteam.railways.registry.CREdgePointTypes;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.graph.DimensionPalette;
import com.simibubi.create.content.trains.graph.EdgePointType;
import com.simibubi.create.content.trains.graph.TrackGraph;
import com.simibubi.create.content.trains.signal.SingleBlockEntityEdgePoint;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;
import net.minecraft.server.MinecraftServer;

import java.util.UUID;

public class TrackCoupler extends SingleBlockEntityEdgePoint {
	private int activated;
	private UUID currentTrain;

	public EdgePointType<?> getType() {
		return CREdgePointTypes.COUPLER;
	}

	@Override
	public void tick(TrackGraph graph, boolean preTrains) {
		super.tick(graph, preTrains);
		if (activated > 0)
			activated--;
		if (activated <= 0)
			currentTrain = null;
	}

	public UUID getCurrentTrain() {
		return currentTrain;
	}

	public boolean isActivated() {
		return activated > 0;
	}

	public void keepAlive(Train train) {
		if (((IHandcarTrain) train).railways$isHandcar())
			return;
		activated = 8;
		currentTrain = train.id;
	}

	public void blockEntityAdded(BlockEntity tile, boolean front) {
		super.blockEntityAdded(tile, front);
	}

	@Override
	public void read(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries, boolean migration, DimensionPalette dimensions) {
        super.read(tag, registries, migration, dimensions);
        activated = tag.getIntOr("Activated", 0);
        currentTrain = tag.getString("TrainId").map(UUID::fromString).orElse(null);
    }


	public void read(FriendlyByteBuf buffer, DimensionPalette dimensions) {
		super.read(buffer, dimensions);
		if (buffer.readBoolean())
			blockEntityPos = buffer.readBlockPos();
	}

	@Override
	public void write(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries, DimensionPalette dimensions) {
        super.write(tag, registries, dimensions);
        tag.putInt("Activated", activated);
        if (currentTrain != null) tag.putString("TrainId", currentTrain.toString());
    }


	public void write(FriendlyByteBuf buffer, DimensionPalette dimensions) {
		super.write(buffer, dimensions);
		buffer.writeBoolean(blockEntityPos != null);
		if (blockEntityPos != null)
			buffer.writeBlockPos(blockEntityPos);
	}
}
