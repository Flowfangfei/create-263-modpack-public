package com.railwayteam.railways.fabric_mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.railwayteam.railways.util.MixinVariables;
import net.createmod.catnip.api.data.Couple;
import net.createmod.catnip.api.data.Pair;
import com.simibubi.create.content.trains.graph.EdgeData;
import com.simibubi.create.content.trains.graph.TrackGraph;
import com.simibubi.create.content.trains.graph.TrackNode;
import com.simibubi.create.content.trains.signal.SignalBoundary;
import com.simibubi.create.content.trains.signal.SignalPropagator;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;
import java.util.function.Predicate;

@Mixin(value = SignalPropagator.class, remap = false)
public class MixinSignalPropagator {
	@WrapMethod(
		method = "walkSignals(Lcom/simibubi/create/content/trains/graph/TrackGraph;Ljava/util/List;Ljava/util/function/Predicate;Ljava/util/function/Predicate;Z)V"
	)
	private static void railways$trackSignalPropagatorDepth(
		TrackGraph graph,
		List<Couple<TrackNode>> frontier,
		Predicate<Pair<TrackNode, SignalBoundary>> boundaryCallback,
		Predicate<EdgeData> nonBoundaryCallback,
		boolean forCollection,
		Operation<Void> original
	) {
		MixinVariables.signalPropagatorCallDepth++;
		try {
			original.call(graph, frontier, boundaryCallback, nonBoundaryCallback, forCollection);
		} finally {
			MixinVariables.signalPropagatorCallDepth--;
		}
	}
}
